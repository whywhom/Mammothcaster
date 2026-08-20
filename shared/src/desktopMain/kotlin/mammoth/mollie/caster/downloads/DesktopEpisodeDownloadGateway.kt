package mammoth.mollie.caster.downloads

import java.net.URI
import java.net.ConnectException
import java.net.http.HttpClient
import java.net.http.HttpTimeoutException
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.Base64
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mammoth.mollie.caster.data.DownloadSnapshot
import mammoth.mollie.caster.data.EpisodeDownloadGateway
import mammoth.mollie.caster.model.Download
import mammoth.mollie.caster.model.DownloadState
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.model.EpisodeId
import mammoth.mollie.caster.data.cache.DownloadRetryPolicy
import mammoth.mollie.caster.data.cache.MediaCachePolicy
import mammoth.mollie.caster.data.cache.TransferFailure

/** Native desktop downloads plus a private cache populated while remote media plays. */
class DesktopEpisodeDownloadGateway : EpisodeDownloadGateway, AutoCloseable {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()
    private val active = ConcurrentHashMap.newKeySet<String>()
    private val generations = ConcurrentHashMap<String, Int>()
    private val retryPolicy = DownloadRetryPolicy()
    private val indexFile = applicationDataRoot().resolve("downloads.properties")
    private val cacheRoot = applicationCacheRoot()
    private val mutableDownloads = MutableStateFlow(DownloadSnapshot(true, loadIndex()))
    override val downloads: StateFlow<DownloadSnapshot> = mutableDownloads.asStateFlow()
    override val supported: Boolean = true
    override val cellularDownloadControlSupported: Boolean = false
    override val cellularDownloadsAllowed: StateFlow<Boolean> = MutableStateFlow(false)

    override fun setCellularDownloadsAllowed(allowed: Boolean) = Unit

    override fun download(episode: Episode, podcastTitle: String) {
        val enclosure = episode.enclosures.firstOrNull { it.mimeType?.startsWith("audio/") == true }
            ?: episode.enclosures.firstOrNull() ?: return
        if (!active.add(episode.id.value)) return
        val generation = generations.compute(episode.id.value) { _, value -> (value ?: 0) + 1 } ?: 1
        val podcastDirectory = safeMediaPathComponent(podcastTitle, "Podcast")
        val target = downloadsRoot().resolve(podcastDirectory).resolve(uniqueMediaFileName(episode.title, episode.id, enclosure))
        replace(Download(episode.id, enclosure.url, DownloadState.Queued))
        scope.launch {
            replace(Download(episode.id, enclosure.url, DownloadState.Downloading))
            runCatching { transferWithRetry(enclosure.url, target) }
                .onSuccess {
                    synchronized(generations) {
                        if (generations[episode.id.value] == generation) {
                            replace(Download(episode.id, enclosure.url, DownloadState.Completed, target.toUri().toString(), Files.size(target), Files.size(target)))
                            persistIndex()
                        } else Files.deleteIfExists(target)
                    }
                }
                .onFailure { error ->
                    synchronized(generations) {
                        if (generations[episode.id.value] == generation) replace(Download(episode.id, enclosure.url, DownloadState.Failed, failureMessage = error.message ?: "Download failed"))
                    }
                }
            active.remove(episode.id.value)
        }
    }

    override fun delete(episodeId: EpisodeId) {
        synchronized(generations) {
            generations.compute(episodeId.value) { _, value -> (value ?: 0) + 1 }
            mutableDownloads.value.items.firstOrNull { it.episodeId == episodeId }?.localReference?.let { reference ->
                runCatching { Files.deleteIfExists(Path.of(URI(reference))) }
            }
            mutableDownloads.update { it.copy(items = it.items.filterNot { item -> item.episodeId == episodeId }) }
            persistIndex()
        }
    }

    fun playbackSource(episode: Episode): String {
        val downloaded = mutableDownloads.value.items.firstOrNull {
            it.episodeId == episode.id && it.state == DownloadState.Completed && it.localReference != null
        }?.localReference?.takeIf(::fileReferenceExists)
        if (downloaded != null) return downloaded
        val enclosure = episode.enclosures.firstOrNull() ?: return ""
        val cached = cacheRoot.resolve(mediaStorageKey(episode.id, enclosure))
        return if (Files.isRegularFile(cached)) cached.toUri().toString() else enclosure.url
    }

    fun cacheForPlayback(episode: Episode) {
        val enclosure = episode.enclosures.firstOrNull() ?: return
        if (playbackSource(episode) != enclosure.url || !active.add("cache:${episode.id.value}")) return
        val target = cacheRoot.resolve(mediaStorageKey(episode.id, enclosure))
        scope.launch {
            runCatching { transfer(enclosure.url, target); trimCache() }
            active.remove("cache:${episode.id.value}")
        }
    }

    override fun close() = scope.cancel()

    private fun transfer(source: String, target: Path) {
        Files.createDirectories(target.parent)
        val partial = target.resolveSibling("${target.fileName}.part")
        Files.deleteIfExists(partial)
        val request = HttpRequest.newBuilder(URI(source)).header("User-Agent", "Molliecaster/0.1").GET().build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofFile(partial))
        if (response.statusCode() !in 200..299) throw HttpStatusException(response.statusCode())
        runCatching { Files.move(partial, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING) }
            .getOrElse { Files.move(partial, target, StandardCopyOption.REPLACE_EXISTING) }
    }

    private suspend fun transferWithRetry(source: String, target: Path) {
        var failures = 0
        while (true) {
            try {
                transfer(source, target)
                return
            } catch (error: Throwable) {
                val failure = when (error) {
                    is HttpStatusException -> DownloadRetryPolicy.classifyHttpStatus(error.status)
                    is HttpTimeoutException -> TransferFailure.Timeout
                    is ConnectException, is IOException -> TransferFailure.ConnectionLost
                    else -> TransferFailure.Other
                }
                val retryDelay = retryPolicy.delayBeforeRetry(failure, failures++) ?: throw error
                delay(retryDelay)
            }
        }
    }

    private fun replace(download: Download) {
        mutableDownloads.update { snapshot ->
            snapshot.copy(items = snapshot.items.filterNot { it.episodeId == download.episodeId } + download)
        }
    }

    private fun loadIndex(): List<Download> {
        if (!Files.isRegularFile(indexFile)) return emptyList()
        val properties = runCatching { Properties().apply { Files.newInputStream(indexFile).use(::load) } }
            .getOrElse { writeIndex(emptyList()); return emptyList() }
        val indexed = properties.stringPropertyNames().mapNotNull { encodedId ->
            runCatching {
                val parts = properties.getProperty(encodedId).split('|', limit = 2)
                require(parts.size == 2)
                val id = EpisodeId(decode(encodedId))
                val reference = decode(parts[0])
                val source = decode(parts[1])
                val size = runCatching { Files.size(Path.of(URI(reference))) }.getOrDefault(0)
                Download(id, source, DownloadState.Completed, reference, size, size)
            }.getOrNull()
        }
        val reconciled = retainExistingDownloads(indexed, ::fileReferenceExists)
        if (reconciled.size != properties.size) writeIndex(reconciled)
        return reconciled
    }

    private fun persistIndex() = writeIndex(mutableDownloads.value.items)

    private fun writeIndex(downloads: List<Download>) {
        Files.createDirectories(indexFile.parent)
        val properties = Properties()
        downloads.filter { it.state == DownloadState.Completed && it.localReference != null }.forEach {
            properties[encode(it.episodeId.value)] = "${encode(requireNotNull(it.localReference))}|${encode(it.sourceUrl)}"
        }
        val temporary = indexFile.resolveSibling("${indexFile.fileName}.tmp")
        Files.newOutputStream(temporary).use { properties.store(it, "Molliecaster episode downloads") }
        runCatching { Files.move(temporary, indexFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING) }
            .getOrElse { Files.move(temporary, indexFile, StandardCopyOption.REPLACE_EXISTING) }
    }

    private fun fileReferenceExists(reference: String): Boolean = runCatching { Files.isRegularFile(Path.of(URI(reference))) }.getOrDefault(false)
    private fun trimCache() {
        if (!Files.isDirectory(cacheRoot)) return
        val files = Files.list(cacheRoot).use { stream -> stream.filter { Files.isRegularFile(it) }.filter { !it.fileName.toString().endsWith(".part") }.toList() }
            .sortedBy { Files.getLastModifiedTime(it).toMillis() }
        var total = files.sumOf { Files.size(it) }
        for (file in files) {
            if (total <= MAX_CACHE_BYTES) break
            val size = Files.size(file)
            if (Files.deleteIfExists(file)) total -= size
        }
    }
    private fun encode(value: String): String = Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray())
    private fun decode(value: String): String = String(Base64.getUrlDecoder().decode(value))

    private fun downloadsRoot(): Path = Path.of(System.getProperty("user.home"), "Downloads", "Molliecaster")
    private fun applicationDataRoot(): Path = Path.of(System.getProperty("user.home"), ".molliecaster")
    private fun applicationCacheRoot(): Path = when {
        System.getProperty("os.name").startsWith("Mac", ignoreCase = true) -> Path.of(System.getProperty("user.home"), "Library", "Caches", "Molliecaster")
        System.getenv("LOCALAPPDATA") != null -> Path.of(System.getenv("LOCALAPPDATA"), "Molliecaster", "Cache")
        else -> Path.of(System.getenv("XDG_CACHE_HOME") ?: Path.of(System.getProperty("user.home"), ".cache").toString(), "molliecaster")
    }

    private companion object { const val MAX_CACHE_BYTES = MediaCachePolicy.DEFAULT_MAX_BYTES }
}

private class HttpStatusException(val status: Int) : IOException("Download returned HTTP $status")
