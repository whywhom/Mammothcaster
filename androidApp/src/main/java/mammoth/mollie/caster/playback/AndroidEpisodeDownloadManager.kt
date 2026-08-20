package mammoth.mollie.caster.playback

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.scheduler.Requirements
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import mammoth.mollie.caster.data.cache.MediaCachePolicy
import mammoth.mollie.caster.data.cache.validateRemoteMedia
import mammoth.mollie.caster.downloads.safeMediaPathComponent
import mammoth.mollie.caster.downloads.uniqueMediaFileName
import mammoth.mollie.caster.model.Enclosure
import mammoth.mollie.caster.model.EpisodeId
import androidx.core.content.edit
import androidx.core.net.toUri

/** Media3 owns transfer/index state; completed bytes are exported to public Downloads. */
@UnstableApi
@OptIn(UnstableApi::class)
class AndroidEpisodeDownloadManager private constructor(private val context: Context) {
    private val mutableDownloads = MutableStateFlow<List<EpisodeDownloadState>>(emptyList())
    private val mutableInitialized = MutableStateFlow(false)
    private val downloadExecutor = Executors.newFixedThreadPool(MAX_PARALLEL_DOWNLOADS)
    private val exportExecutor = Executors.newSingleThreadExecutor()
    @RequiresApi(Build.VERSION_CODES.N)
    private val exporting = ConcurrentHashMap.newKeySet<String>()
    private val exportFailures = ConcurrentHashMap<String, Int>()
    private val generations = ConcurrentHashMap<String, Int>()
    @RequiresApi(Build.VERSION_CODES.N)
    private val startupDiscardIds = ConcurrentHashMap.newKeySet<String>()
    private val orphanedPublicReferences = ConcurrentHashMap<String, String>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val cache = AndroidPlaybackCache.get(context)
    private val publicStore = AndroidPublicDownloadStore(context)
    private val preferences = context.getSharedPreferences("public_episode_downloads", Context.MODE_PRIVATE)
    private val mutableCellularDownloadsAllowed = MutableStateFlow(preferences.getBoolean(CELLULAR_DOWNLOADS_ALLOWED_KEY, false))
    private val progressTicker = object : Runnable { @RequiresApi(Build.VERSION_CODES.N)
    override fun run() = refresh() }
    private val manager = DownloadManager(context, cache.databaseProvider, cache.downloadCache, cache.upstreamFactory, downloadExecutor).apply {
        maxParallelDownloads = MAX_PARALLEL_DOWNLOADS
        setRequirements(downloadRequirements(mutableCellularDownloadsAllowed.value))
        addListener(object : DownloadManager.Listener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onInitialized(downloadManager: DownloadManager) {
                reconcileMissingPublicFiles(downloadManager)
                refresh()
                mutableInitialized.value = true
            }
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onDownloadChanged(downloadManager: DownloadManager, download: Download, finalException: Exception?) { refresh() }
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
                startupDiscardIds.remove(download.request.id)
                refresh()
            }
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onIdle(downloadManager: DownloadManager) = refresh()
        })
        resumeDownloads()
    }

    val downloads: StateFlow<List<EpisodeDownloadState>> = mutableDownloads.asStateFlow()
    val initialized: StateFlow<Boolean> = mutableInitialized.asStateFlow()
    val cellularDownloadsAllowed: StateFlow<Boolean> = mutableCellularDownloadsAllowed.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.N)
    fun setCellularDownloadsAllowed(allowed: Boolean) {
        if (mutableCellularDownloadsAllowed.value == allowed) return
        mutableCellularDownloadsAllowed.value = allowed
        preferences.edit { putBoolean(CELLULAR_DOWNLOADS_ALLOWED_KEY, allowed) }
        manager.setRequirements(downloadRequirements(allowed))
        refresh()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun download(episodeId: String, mediaUrl: String, mimeType: String?, podcastDirectory: String, fileName: String) {
        val validationError = validateRemoteMedia(episodeId, mediaUrl)
        require(validationError == null) { validationError ?: "Invalid media request" }
        generations.compute(episodeId) { _, value -> (value ?: 0) + 1 }
        exportFailures.remove(episodeId)
        orphanedPublicReferences.remove(episodeId)
        val metadata = JSONObject().put("podcastDirectory", podcastDirectory).put("fileName", fileName).toString().encodeToByteArray()
        val request = DownloadRequest.Builder(episodeId, mediaUrl.toUri()).setData(metadata).apply { mimeType?.let(::setMimeType) }.build()
        manager.addDownload(request)
        refresh()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun delete(episodeId: String) {
        generations.compute(episodeId) { _, value -> (value ?: 0) + 1 }
        exportFailures.remove(episodeId)
        orphanedPublicReferences.remove(episodeId)
        publicStore.delete(preferences.getString(episodeId, null))
        preferences.edit { remove(episodeId) }
        manager.removeDownload(episodeId)
        refresh()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun cancel(episodeId: String) = delete(episodeId)
    @RequiresApi(Build.VERSION_CODES.N)
    fun retry(episodeId: String) { manager.downloadIndex.getDownload(episodeId)?.let { manager.addDownload(it.request, Download.STOP_REASON_NONE) }; manager.resumeDownloads(); refresh() }
    @RequiresApi(Build.VERSION_CODES.N)
    fun pauseAll() { manager.pauseDownloads(); refresh() }
    @RequiresApi(Build.VERSION_CODES.N)
    fun resumeAll() { manager.resumeDownloads(); refresh() }
    fun isDownloaded(episodeId: String): Boolean = localReference(episodeId) != null
    fun localReference(episodeId: String): String? = storedReference(episodeId)?.takeIf(publicStore::exists)

    @RequiresApi(Build.VERSION_CODES.N)
    private fun refresh() {
        val media3Downloads = buildList { manager.downloadIndex.getDownloads().use { cursor -> while (cursor.moveToNext()) add(cursor.download) } }
            .filterNot { it.request.id in startupDiscardIds }
        media3Downloads.filter {
            it.state == Download.STATE_COMPLETED && localReference(it.request.id) == null && !exportFailures.containsKey(it.request.id)
        }.forEach(::export)
        val publicOnly = orphanedPublicReferences.map { (episodeId, reference) ->
            EpisodeDownloadState(
                episodeId = episodeId,
                mediaUrl = reference,
                status = EpisodeDownloadStatus.Completed,
                percentDownloaded = 100f,
                bytesDownloaded = 0,
                localReference = reference,
            )
        }
        mutableDownloads.value = (media3Downloads.map(::toState) + publicOnly).sortedBy(EpisodeDownloadState::episodeId)
        mainHandler.removeCallbacks(progressTicker)
        if (mutableDownloads.value.any { it.status == EpisodeDownloadStatus.Queued || it.status == EpisodeDownloadStatus.Downloading }) {
            mainHandler.postDelayed(progressTicker, PROGRESS_TICK_MILLIS)
        }
    }

    private fun reconcileMissingPublicFiles(downloadManager: DownloadManager) {
        val indexed = buildList {
            downloadManager.downloadIndex.getDownloads().use { cursor -> while (cursor.moveToNext()) add(cursor.download) }
        }.associateBy { it.request.id }
        preferences.edit(commit = true) {
            preferences.all.forEach { (episodeId, rawReference) ->
                val reference = rawReference as? String
                val indexedDownload = indexed[episodeId]
                when {
                    reference == null -> remove(episodeId)
                    publicStore.exists(reference) -> if (indexedDownload == null) orphanedPublicReferences[episodeId] =
                        reference

                    else -> {
                        remove(episodeId)
                        orphanedPublicReferences.remove(episodeId)
                        if (indexedDownload?.state == Download.STATE_COMPLETED) {
                            startupDiscardIds += episodeId
                            generations.compute(episodeId) { _, value -> (value ?: 0) + 1 }
                            exportFailures.remove(episodeId)
                            downloadManager.removeDownload(episodeId)
                        }
                    }
                }
            }
        }
    }

    private fun storedReference(episodeId: String): String? = preferences.getString(episodeId, null)

    private fun export(download: Download) {
        if (!exporting.add(download.request.id)) return
        val generation = generations[download.request.id] ?: 0
        exportExecutor.execute {
            runCatching {
                val metadata = runCatching { JSONObject(download.request.data.decodeToString()) }.getOrNull()
                val fallbackEnclosure = Enclosure(download.request.uri.toString(), download.request.mimeType)
                val podcastDirectory = metadata?.optString("podcastDirectory").orEmpty()
                    .ifBlank { "Recovered downloads" }
                val fileName = metadata?.optString("fileName").orEmpty().ifBlank {
                    uniqueMediaFileName(
                        title = safeMediaPathComponent(download.request.uri.lastPathSegment.orEmpty().substringBeforeLast('.'), "Episode"),
                        episodeId = EpisodeId(download.request.id),
                        enclosure = fallbackEnclosure,
                    )
                }
                val reference = publicStore.export(
                    download.request.uri.toString(), download.request.mimeType,
                    podcastDirectory, fileName,
                )
                if ((generations[download.request.id] ?: 0) == generation) {
                    preferences.edit().putString(download.request.id, reference).apply()
                    exportFailures.remove(download.request.id)
                } else {
                    publicStore.delete(reference)
                }
            }.onFailure {
                if ((generations[download.request.id] ?: 0) == generation) exportFailures[download.request.id] = EXPORT_ERROR
            }
            exporting.remove(download.request.id)
            mainHandler.post(::refresh)
        }
    }

    private fun toState(download: Download): EpisodeDownloadState {
        val reference = localReference(download.request.id)
        val status = when (download.state) {
            Download.STATE_QUEUED -> EpisodeDownloadStatus.Queued
            Download.STATE_DOWNLOADING -> EpisodeDownloadStatus.Downloading
            Download.STATE_STOPPED -> EpisodeDownloadStatus.Paused
            Download.STATE_COMPLETED -> when {
                reference != null -> EpisodeDownloadStatus.Completed
                exportFailures.containsKey(download.request.id) -> EpisodeDownloadStatus.Failed
                else -> EpisodeDownloadStatus.Downloading
            }
            Download.STATE_FAILED -> EpisodeDownloadStatus.Failed
            Download.STATE_REMOVING -> EpisodeDownloadStatus.Removing
            Download.STATE_RESTARTING -> EpisodeDownloadStatus.Restarting
            else -> EpisodeDownloadStatus.Failed
        }
        return EpisodeDownloadState(
            episodeId = download.request.id,
            mediaUrl = download.request.uri.toString(),
            status = status,
            percentDownloaded = download.percentDownloaded.takeIf { it >= 0 } ?: 0f,
            bytesDownloaded = download.bytesDownloaded,
            totalBytes = download.contentLength.takeIf { it > 0 },
            localReference = reference,
            failureReason = exportFailures[download.request.id] ?: download.failureReason.takeUnless { it == Download.FAILURE_REASON_NONE },
        )
    }

    private fun downloadRequirements(allowsCellular: Boolean): Requirements = Requirements(
        if (allowsCellular) Requirements.NETWORK else Requirements.NETWORK_UNMETERED,
    )

    companion object {
        private const val MAX_PARALLEL_DOWNLOADS = MediaCachePolicy.DEFAULT_MAX_CONCURRENT_DOWNLOADS
        private const val PROGRESS_TICK_MILLIS = 1_000L
        private const val EXPORT_ERROR = -1
        private const val CELLULAR_DOWNLOADS_ALLOWED_KEY = "cellular_downloads_allowed"
        @Volatile private var instance: AndroidEpisodeDownloadManager? = null
        fun get(context: Context): AndroidEpisodeDownloadManager = instance ?: synchronized(this) {
            instance ?: AndroidEpisodeDownloadManager(context.applicationContext).also { instance = it }
        }
    }
}
