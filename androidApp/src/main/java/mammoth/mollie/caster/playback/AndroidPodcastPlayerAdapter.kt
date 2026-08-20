package mammoth.mollie.caster.playback

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.model.EpisodeId
import mammoth.mollie.caster.model.PodcastId
import mammoth.mollie.caster.model.Enclosure
import mammoth.mollie.caster.data.cache.validateRemoteMedia
import mammoth.mollie.caster.playback.PlayerState
import mammoth.mollie.caster.playback.PlayerCapabilities
import mammoth.mollie.caster.playback.PlayerStatus
import mammoth.mollie.caster.playback.PodcastPlayer
import mammoth.mollie.caster.platform.currentTimeMillis

class AndroidPodcastPlayerAdapter(
    context: Context,
    private val downloads: AndroidDownloadGateway,
) : PodcastPlayer, AutoCloseable {
    private val controller = AndroidPlaybackController(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutableState = MutableStateFlow(PlayerState())
    private var episode: Episode? = null
    override val state: StateFlow<PlayerState> = mutableState.asStateFlow()
    override val capabilities = PlayerCapabilities(
        realPlayback = true,
        backgroundPlayback = true,
        lockScreenControls = true,
        notificationControls = true,
        transparentStreamCache = true,
        partialOfflinePlayback = true,
    )

    init {
        scope.launch {
            controller.state.collect { android ->
                if (episode == null && android.episodeId != null && android.mediaUrl != null) {
                    episode = Episode(
                        id = EpisodeId(android.episodeId),
                        podcastId = PodcastId("restored-media-session"),
                        title = android.title ?: "Podcast episode",
                        author = android.podcastTitle.orEmpty(),
                        artworkUrl = android.artworkUrl,
                        durationMillis = android.durationMillis.takeIf { it > 0 },
                        enclosures = listOf(Enclosure(android.mediaUrl, "audio/*")),
                    )
                }
                mutableState.value = PlayerState(
                    episode = episode,
                    status = when (android.status) {
                        AndroidPlaybackStatus.Idle -> PlayerStatus.Idle
                        AndroidPlaybackStatus.Loading -> PlayerStatus.Loading
                        AndroidPlaybackStatus.Ready -> PlayerStatus.Ready
                        AndroidPlaybackStatus.Playing -> PlayerStatus.Playing
                        AndroidPlaybackStatus.Paused -> PlayerStatus.Paused
                        AndroidPlaybackStatus.Ended -> PlayerStatus.Ended
                        AndroidPlaybackStatus.Failed -> PlayerStatus.Failed
                    },
                    isPlaying = android.status == AndroidPlaybackStatus.Playing,
                    positionMillis = android.positionMillis,
                    bufferedPositionMillis = android.bufferedPositionMillis,
                    durationMillis = android.durationMillis,
                    speed = android.speed,
                    sleepTimerEndsAtMillis = android.sleepTimerRemainingMillis?.let { currentTimeMillis() + it },
                    errorMessage = android.errorMessage,
                )
            }
        }
    }

    override fun play(episode: Episode) {
        val enclosure = episode.enclosures.firstOrNull { it.mimeType?.startsWith("audio/") == true }
            ?: episode.enclosures.firstOrNull()
            ?: return
        validateRemoteMedia(episode.id.value, enclosure.url)?.let { message ->
            mutableState.value = PlayerState(episode = episode, status = PlayerStatus.Failed, errorMessage = message)
            return
        }
        this.episode = episode
        controller.load(
            AndroidPlaybackItem(
                episodeId = episode.id.value,
                mediaUrl = downloads.localReference(episode.id) ?: enclosure.url,
                title = episode.title,
                podcastTitle = episode.author.ifBlank { "Molliecaster" },
                artworkUrl = episode.artworkUrl,
                resumePositionMillis = episode.playbackPositionMillis,
                knownDurationMillis = episode.durationMillis,
            ),
        )
    }

    override fun toggle() {
        if (state.value.isPlaying) controller.pause() else controller.play()
    }

    override fun seekTo(positionMillis: Long) = controller.seekTo(positionMillis)
    override fun skipBy(deltaMillis: Long) = if (deltaMillis < 0) controller.skipBack15Seconds() else controller.skipForward15Seconds()
    override fun setSpeed(speed: Float) = controller.setPlaybackSpeed(speed)
    override fun setSleepTimer(minutes: Int?) = if (minutes == null) controller.cancelSleepTimer() else controller.setSleepTimer(minutes)

    override fun close() {
        scope.cancel()
        controller.close()
    }
}
