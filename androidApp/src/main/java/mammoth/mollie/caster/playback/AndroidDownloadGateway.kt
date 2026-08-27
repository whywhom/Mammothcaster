package mammoth.mollie.caster.playback

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mammoth.mollie.caster.data.EpisodeDownloadGateway
import mammoth.mollie.caster.data.DownloadSnapshot
import mammoth.mollie.caster.model.Download
import mammoth.mollie.caster.model.DownloadState
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.model.EpisodeId
import mammoth.mollie.caster.downloads.uniqueMediaFileName
import mammoth.mollie.caster.downloads.safeMediaPathComponent

class AndroidDownloadGateway(context: Context) : EpisodeDownloadGateway {
    private val manager = AndroidEpisodeDownloadManager.get(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutableDownloads = MutableStateFlow(DownloadSnapshot())
    override val downloads: StateFlow<DownloadSnapshot> = mutableDownloads.asStateFlow()
    override val supported: Boolean = true
    override val cellularDownloadControlSupported: Boolean = true
    override val cellularDownloadsAllowed: StateFlow<Boolean> = manager.cellularDownloadsAllowed

    init {
        scope.launch {
            manager.downloads.collect { values ->
                if (!manager.initialized.value) return@collect
                mutableDownloads.value = DownloadSnapshot(initialized = true, items = values.map(::toDownload))
            }
        }
        scope.launch {
            manager.initialized.collect { initialized ->
                if (initialized) mutableDownloads.value = DownloadSnapshot(true, manager.downloads.value.map(::toDownload))
            }
        }
    }

    private fun toDownload(value: EpisodeDownloadState) = Download(
        episodeId = EpisodeId(value.episodeId),
        sourceUrl = value.mediaUrl,
        state = when (value.status) {
            EpisodeDownloadStatus.Queued, EpisodeDownloadStatus.Paused, EpisodeDownloadStatus.Restarting -> DownloadState.Queued
            EpisodeDownloadStatus.Downloading -> DownloadState.Downloading
            EpisodeDownloadStatus.Completed -> DownloadState.Completed
            EpisodeDownloadStatus.Removing -> DownloadState.Removing
            EpisodeDownloadStatus.Failed -> DownloadState.Failed
        },
        receivedBytes = value.bytesDownloaded,
        totalBytes = value.totalBytes,
        localReference = value.localReference,
        failureMessage = value.failureReason?.let { "Android download error $it" },
    )

    override fun download(episode: Episode, podcastTitle: String) {
        val enclosure = episode.enclosures.firstOrNull { it.mimeType?.startsWith("audio/") == true }
            ?: episode.enclosures.firstOrNull()
            ?: return
        manager.download(
            episode.id.value,
            enclosure.url,
            enclosure.mimeType,
            safeMediaPathComponent(podcastTitle, "Podcast"),
            uniqueMediaFileName(episode.title, episode.id, enclosure),
        )
    }

    override fun delete(episodeId: EpisodeId) = manager.delete(episodeId.value)
    override fun setCellularDownloadsAllowed(allowed: Boolean) = manager.setCellularDownloadsAllowed(allowed)

    fun localReference(episodeId: EpisodeId): String? = manager.localReference(episodeId.value)
}
