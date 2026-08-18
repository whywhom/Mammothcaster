package mammoth.mollie.caster.playback

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors

/**
 * Process-scoped Media3 download bridge. The download index and media bytes are
 * durable; [MolliePlaybackService] reads from the same cache before the network.
 */
@OptIn(UnstableApi::class)
class AndroidEpisodeDownloadManager private constructor(context: Context) {
    private val mutableDownloads = MutableStateFlow<List<EpisodeDownloadState>>(emptyList())
    private val mutableInitialized = MutableStateFlow(false)
    private val downloadExecutor = Executors.newFixedThreadPool(MAX_PARALLEL_DOWNLOADS)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val cache = AndroidPlaybackCache.get(context)
    private val progressTicker = object : Runnable {
        override fun run() {
            refresh()
        }
    }
    private val manager = DownloadManager(
        context.applicationContext,
        cache.databaseProvider,
        cache.cache,
        cache.upstreamFactory,
        downloadExecutor,
    ).apply {
        maxParallelDownloads = MAX_PARALLEL_DOWNLOADS
        addListener(object : DownloadManager.Listener {
            override fun onInitialized(downloadManager: DownloadManager) {
                mutableInitialized.value = true
                refresh()
            }

            override fun onDownloadChanged(
                downloadManager: DownloadManager,
                download: Download,
                finalException: Exception?,
            ) = refresh()

            override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) = refresh()

            override fun onIdle(downloadManager: DownloadManager) = refresh()
        })
        resumeDownloads()
    }

    val downloads: StateFlow<List<EpisodeDownloadState>> = mutableDownloads.asStateFlow()
    val initialized: StateFlow<Boolean> = mutableInitialized.asStateFlow()

    fun download(episodeId: String, mediaUrl: String, mimeType: String? = null) {
        require(episodeId.isNotBlank()) { "episodeId must not be blank" }
        require(mediaUrl.isNotBlank()) { "mediaUrl must not be blank" }
        val request = DownloadRequest.Builder(episodeId, Uri.parse(mediaUrl))
            .apply { mimeType?.let(::setMimeType) }
            .build()
        manager.addDownload(request)
        refresh()
    }

    /** Removes both the Media3 index row and cached media for this episode only. */
    fun delete(episodeId: String) {
        manager.removeDownload(episodeId)
        refresh()
    }

    /** Cancels an in-flight item and removes any bytes already written for it. */
    fun cancel(episodeId: String) = delete(episodeId)

    fun retry(episodeId: String) {
        manager.downloadIndex.getDownload(episodeId)?.let { failedDownload ->
            manager.addDownload(failedDownload.request, Download.STOP_REASON_NONE)
            manager.resumeDownloads()
        }
        refresh()
    }

    fun pauseAll() {
        manager.pauseDownloads()
        refresh()
    }

    fun resumeAll() {
        manager.resumeDownloads()
        refresh()
    }

    fun isDownloaded(episodeId: String): Boolean =
        downloads.value.any { it.episodeId == episodeId && it.status == EpisodeDownloadStatus.Completed }

    private fun refresh() {
        val snapshot = buildList {
            manager.downloadIndex.getDownloads().use { cursor ->
                while (cursor.moveToNext()) add(cursor.download.toState())
            }
        }.sortedBy { it.episodeId }
        mutableDownloads.value = snapshot
        mainHandler.removeCallbacks(progressTicker)
        if (snapshot.any { it.status == EpisodeDownloadStatus.Queued || it.status == EpisodeDownloadStatus.Downloading }) {
            mainHandler.postDelayed(progressTicker, PROGRESS_TICK_MILLIS)
        }
    }

    private fun Download.toState() = EpisodeDownloadState(
        episodeId = request.id,
        mediaUrl = request.uri.toString(),
        status = when (state) {
            Download.STATE_QUEUED -> EpisodeDownloadStatus.Queued
            Download.STATE_DOWNLOADING -> EpisodeDownloadStatus.Downloading
            Download.STATE_STOPPED -> EpisodeDownloadStatus.Paused
            Download.STATE_COMPLETED -> {
                val cacheKey = request.customCacheKey ?: request.uri.toString()
                if (contentLength > 0L && !cache.cache.isCached(cacheKey, 0L, contentLength)) {
                    EpisodeDownloadStatus.MissingFile
                } else {
                    EpisodeDownloadStatus.Completed
                }
            }
            Download.STATE_FAILED -> EpisodeDownloadStatus.Failed
            Download.STATE_REMOVING -> EpisodeDownloadStatus.Removing
            Download.STATE_RESTARTING -> EpisodeDownloadStatus.Restarting
            else -> EpisodeDownloadStatus.Failed
        },
        percentDownloaded = percentDownloaded.takeIf { it >= 0f } ?: 0f,
        bytesDownloaded = bytesDownloaded,
        failureReason = failureReason.takeUnless { it == Download.FAILURE_REASON_NONE },
    )

    companion object {
        private const val MAX_PARALLEL_DOWNLOADS = 3
        private const val PROGRESS_TICK_MILLIS = 1_000L

        @Volatile
        private var instance: AndroidEpisodeDownloadManager? = null

        fun get(context: Context): AndroidEpisodeDownloadManager = instance ?: synchronized(this) {
            instance ?: AndroidEpisodeDownloadManager(context.applicationContext).also { instance = it }
        }
    }
}
