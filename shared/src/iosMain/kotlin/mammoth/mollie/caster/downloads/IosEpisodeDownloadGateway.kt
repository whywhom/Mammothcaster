package mammoth.mollie.caster.downloads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.cinterop.ExperimentalForeignApi
import mammoth.mollie.caster.data.DownloadSnapshot
import mammoth.mollie.caster.data.EpisodeDownloadGateway
import mammoth.mollie.caster.model.Download
import mammoth.mollie.caster.model.DownloadState
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.model.EpisodeId
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSLock
import platform.Foundation.NSError
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.NSURLSessionDownloadDelegateProtocol
import platform.Foundation.NSURLSessionDownloadTask
import platform.Foundation.NSURLSessionTask
import platform.darwin.NSObject

/** NSURLSession-backed iOS downloads stored in Application Support and excluded from public-path assumptions. */
@OptIn(ExperimentalForeignApi::class)
class IosEpisodeDownloadGateway : EpisodeDownloadGateway {
    private val fileManager = NSFileManager.defaultManager
    private val active = mutableSetOf<String>()
    private val activeLock = NSLock()
    private val generations = mutableMapOf<String, Int>()
    private val pendingTransfers = mutableMapOf<ULong, PendingTransfer>()
    private val sessionDelegate = DownloadDelegate(::downloadFinished, ::downloadFailed)
    private val mutableCellularDownloadsAllowed = MutableStateFlow(
        NSUserDefaults.standardUserDefaults.boolForKey(CELLULAR_DOWNLOADS_ALLOWED_KEY),
    )
    private var session = newSession(mutableCellularDownloadsAllowed.value)
    private val mutableDownloads = MutableStateFlow(DownloadSnapshot(true, restoreIndex()))
    override val downloads: StateFlow<DownloadSnapshot> = mutableDownloads.asStateFlow()
    override val supported: Boolean = true
    override val cellularDownloadControlSupported: Boolean = true
    override val cellularDownloadsAllowed: StateFlow<Boolean> = mutableCellularDownloadsAllowed.asStateFlow()

    override fun setCellularDownloadsAllowed(allowed: Boolean) {
        if (mutableCellularDownloadsAllowed.value == allowed) return
        mutableCellularDownloadsAllowed.value = allowed
        NSUserDefaults.standardUserDefaults.setBool(allowed, CELLULAR_DOWNLOADS_ALLOWED_KEY)
        session = newSession(allowed)
    }

    override fun download(episode: Episode, podcastTitle: String) {
        val enclosure = episode.enclosures.firstOrNull { it.mimeType?.startsWith("audio/") == true }
            ?: episode.enclosures.firstOrNull() ?: return
        val generation = begin(episode.id.value) ?: return
        val podcastDirectory = safeMediaPathComponent(podcastTitle, "Podcast")
        val target = "$downloadsRoot/$podcastDirectory/${uniqueMediaFileName(episode.title, episode.id, enclosure)}"
        replace(Download(episode.id, enclosure.url, DownloadState.Queued))
        transfer(episode.id, enclosure.url, target, isUserDownload = true, generation = generation)
    }

    override fun delete(episodeId: EpisodeId) {
        invalidate(episodeId.value)
        mutableDownloads.value.items.firstOrNull { it.episodeId == episodeId }?.localReference?.let { reference ->
            NSURL(string = reference)?.path?.let { fileManager.removeItemAtPath(it, error = null) }
        }
        mutableDownloads.update { it.copy(items = it.items.filterNot { item -> item.episodeId == episodeId }) }
        persistIndex()
    }

    fun playbackSource(episode: Episode): String {
        val downloaded = mutableDownloads.value.items.firstOrNull {
            it.episodeId == episode.id && it.state == DownloadState.Completed
        }?.localReference?.takeIf(::referenceExists)
        if (downloaded != null) return downloaded
        val enclosure = episode.enclosures.firstOrNull() ?: return ""
        val cachedPath = "$cacheRoot/${mediaStorageKey(episode.id, enclosure)}"
        return if (fileManager.fileExistsAtPath(cachedPath)) NSURL.fileURLWithPath(cachedPath).absoluteString ?: enclosure.url else enclosure.url
    }

    fun cacheForPlayback(episode: Episode) {
        val enclosure = episode.enclosures.firstOrNull() ?: return
        val key = "cache:${episode.id.value}"
        if (playbackSource(episode) != enclosure.url || begin(key) == null) return
        transfer(episode.id, enclosure.url, "$cacheRoot/${mediaStorageKey(episode.id, enclosure)}", isUserDownload = false, activeKey = key)
    }

    private fun transfer(episodeId: EpisodeId, source: String, targetPath: String, isUserDownload: Boolean, activeKey: String = episodeId.value, generation: Int? = null) {
        val sourceUrl = NSURL(string = source) ?: run {
            if (isUserDownload) replace(Download(episodeId, source, DownloadState.Failed, failureMessage = "Invalid media URL"))
            finish(activeKey)
            return
        }
        targetPath.substringBeforeLast('/').let { fileManager.createDirectoryAtPath(it, true, null, null) }
        if (isUserDownload) replace(Download(episodeId, source, DownloadState.Downloading))
        val task = session.downloadTaskWithURL(sourceUrl)
        activeLock.lock()
        try {
            pendingTransfers[task.taskIdentifier] = PendingTransfer(episodeId, source, targetPath, isUserDownload, activeKey, generation)
        } finally { activeLock.unlock() }
        task.resume()
    }

    private fun downloadFinished(task: NSURLSessionDownloadTask, temporaryUrl: NSURL) {
        val pending = takePending(task.taskIdentifier) ?: return
        if (pending.isUserDownload && pending.generation != currentGeneration(pending.episodeId.value)) {
            fileManager.removeItemAtURL(temporaryUrl, error = null)
            finish(pending.activeKey)
            return
        }
        fileManager.removeItemAtPath(pending.targetPath, error = null)
        val targetUrl = NSURL.fileURLWithPath(pending.targetPath)
        val moved = fileManager.moveItemAtURL(temporaryUrl, targetUrl, error = null)
        if (moved && pending.isUserDownload) {
            targetUrl.setResourceValue(true, forKey = NSURLIsExcludedFromBackupKey, error = null)
            if (pending.generation == currentGeneration(pending.episodeId.value)) {
                replace(Download(pending.episodeId, pending.source, DownloadState.Completed, targetUrl.absoluteString))
                persistIndex()
            } else fileManager.removeItemAtPath(pending.targetPath, error = null)
        } else if (!moved && pending.isUserDownload) {
            replace(Download(pending.episodeId, pending.source, DownloadState.Failed, failureMessage = "Could not save downloaded file"))
        }
        finish(pending.activeKey)
    }

    private fun downloadFailed(task: NSURLSessionTask, error: NSError?) {
        if (error == null) return
        val pending = takePending(task.taskIdentifier) ?: return
        if (pending.isUserDownload) replace(Download(pending.episodeId, pending.source, DownloadState.Failed, failureMessage = "Download failed"))
        finish(pending.activeKey)
    }

    private fun takePending(taskId: ULong): PendingTransfer? {
        activeLock.lock()
        return try { pendingTransfers.remove(taskId) } finally { activeLock.unlock() }
    }

    private fun replace(download: Download) {
        mutableDownloads.update { snapshot -> snapshot.copy(items = snapshot.items.filterNot { it.episodeId == download.episodeId } + download) }
    }

    private fun restoreIndex(): List<Download> {
        val dictionary = NSUserDefaults.standardUserDefaults.dictionaryForKey(INDEX_KEY) ?: return emptyList()
        val indexed = dictionary.mapNotNull { (rawId, rawValue) ->
            val id = rawId as? String ?: return@mapNotNull null
            val value = rawValue as? String ?: return@mapNotNull null
            val parts = value.split('\n', limit = 2)
            if (parts.size != 2) return@mapNotNull null
            val reference = parts[1]
            Download(EpisodeId(id), parts[0], DownloadState.Completed, reference)
        }
        val reconciled = retainExistingDownloads(indexed, ::referenceExists)
        if (reconciled.size != dictionary.size) writeIndex(reconciled)
        return reconciled
    }

    private fun persistIndex() = writeIndex(mutableDownloads.value.items)

    private fun writeIndex(downloads: List<Download>) {
        val dictionary = downloads.filter { it.state == DownloadState.Completed && it.localReference != null }
            .associate { it.episodeId.value to "${it.sourceUrl}\n${it.localReference}" }
        NSUserDefaults.standardUserDefaults.setObject(dictionary, forKey = INDEX_KEY)
    }

    private fun referenceExists(reference: String): Boolean = NSURL(string = reference)?.path?.let(fileManager::fileExistsAtPath) == true
    private fun begin(key: String): Int? { activeLock.lock(); return try { if (!active.add(key)) null else ((generations[key] ?: 0) + 1).also { generations[key] = it } } finally { activeLock.unlock() } }
    private fun invalidate(key: String) { activeLock.lock(); try { generations[key] = (generations[key] ?: 0) + 1 } finally { activeLock.unlock() } }
    private fun currentGeneration(key: String): Int { activeLock.lock(); return try { generations[key] ?: 0 } finally { activeLock.unlock() } }
    private fun finish(key: String) { activeLock.lock(); try { active.remove(key) } finally { activeLock.unlock() } }
    private fun newSession(allowsCellularAccess: Boolean): NSURLSession {
        val configuration = NSURLSessionConfiguration.defaultSessionConfiguration()
        configuration.allowsCellularAccess = allowsCellularAccess
        return NSURLSession.sessionWithConfiguration(configuration, sessionDelegate, null)
    }
    private val downloadsRoot: String by lazy {
        (fileManager.URLForDirectory(NSApplicationSupportDirectory, NSUserDomainMask, null, true, null)?.path ?: ".") + "/Molliecaster/Downloads"
    }
    private val cacheRoot: String by lazy {
        (fileManager.URLForDirectory(NSCachesDirectory, NSUserDomainMask, null, true, null)?.path ?: ".") + "/Molliecaster/Media"
    }

    private companion object {
        const val INDEX_KEY = "MolliecasterEpisodeDownloads"
        const val CELLULAR_DOWNLOADS_ALLOWED_KEY = "MolliecasterCellularDownloadsAllowed"
    }
}

private data class PendingTransfer(
    val episodeId: EpisodeId,
    val source: String,
    val targetPath: String,
    val isUserDownload: Boolean,
    val activeKey: String,
    val generation: Int?,
)

@OptIn(ExperimentalForeignApi::class)
private class DownloadDelegate(
    private val onFinished: (NSURLSessionDownloadTask, NSURL) -> Unit,
    private val onFailed: (NSURLSessionTask, NSError?) -> Unit,
) : NSObject(), NSURLSessionDownloadDelegateProtocol {
    override fun URLSession(session: NSURLSession, downloadTask: NSURLSessionDownloadTask, didFinishDownloadingToURL: NSURL) {
        onFinished(downloadTask, didFinishDownloadingToURL)
    }

    override fun URLSession(session: NSURLSession, task: NSURLSessionTask, didCompleteWithError: NSError?) {
        onFailed(task, didCompleteWithError)
    }
}
