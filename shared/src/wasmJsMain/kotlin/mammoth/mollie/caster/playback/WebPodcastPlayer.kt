package mammoth.mollie.caster.playback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mammoth.mollie.caster.data.cache.validatePlayableMedia
import mammoth.mollie.caster.downloads.WebEpisodeDownloadGateway
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.platform.currentTimeMillis
import org.w3c.dom.Audio
import kotlin.time.Duration.Companion.milliseconds

/** Browser HTMLAudio adapter. Playback must be initiated from a user gesture. */
class WebPodcastPlayer(private val mediaFiles: WebEpisodeDownloadGateway) : PodcastPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val audio = Audio("")
    private val mutableState = MutableStateFlow(PlayerState())
    private var loadGeneration = 0
    override val state: StateFlow<PlayerState> = mutableState.asStateFlow()
    override val capabilities = PlayerCapabilities(realPlayback = true, backgroundPlayback = true)

    init {
        audio.preload = "metadata"
        audio.onloadedmetadata = { publish(PlayerStatus.Ready) }
        audio.onplaying = { publish(PlayerStatus.Playing) }
        audio.onpause = { publish(if (audio.ended) PlayerStatus.Ended else PlayerStatus.Paused) }
        audio.onended = { publish(PlayerStatus.Ended) }
        audio.ontimeupdate = { publish(if (audio.paused) PlayerStatus.Paused else PlayerStatus.Playing) }
        audio.onerror = { _, _, _, _, _ ->
            mutableState.value = mutableState.value.copy(status = PlayerStatus.Failed, isPlaying = false, errorMessage = "Browser could not play this audio stream")
            null
        }
        scope.launch {
            while (isActive) {
                delay(500.milliseconds)
                val deadline = mutableState.value.sleepTimerEndsAtMillis ?: continue
                if (currentTimeMillis() >= deadline) {
                    audio.pause()
                    mutableState.value = mutableState.value.copy(sleepTimerEndsAtMillis = null)
                }
            }
        }
    }

    override fun play(episode: Episode) = load(episode, autoPlay = true)

    override fun prepare(episode: Episode) = load(episode, autoPlay = false)

    private fun load(episode: Episode, autoPlay: Boolean) {
        episode.enclosures.firstOrNull()?.let { enclosure ->
            validatePlayableMedia(episode.id.value, enclosure.url)?.let {
                mutableState.value = PlayerState(episode = episode, status = PlayerStatus.Failed, errorMessage = it)
                return
            }
        }
        val generation = ++loadGeneration
        val previousSource = audio.src
        mutableState.value = PlayerState(episode = episode, status = PlayerStatus.Loading, positionMillis = episode.playbackPositionMillis, speed = state.value.speed)
        val start: (String) -> Unit = { source ->
            if (generation == loadGeneration) {
                mediaFiles.revoke(previousSource)
                audio.src = source
                audio.playbackRate = state.value.speed.toDouble()
                audio.currentTime = episode.playbackPositionMillis / 1000.0
                if (autoPlay) audio.play() else publish(PlayerStatus.Paused)
            } else mediaFiles.revoke(source)
        }
        if (!mediaFiles.isCached(episode)) {
            val remote = episode.enclosures.firstOrNull()?.url ?: return
            start(remote)
            return
        }
        mediaFiles.resolveForPlayback(
            episode,
            onReady = start,
            onError = { message -> if (generation == loadGeneration) mutableState.value = PlayerState(episode = episode, status = PlayerStatus.Failed, errorMessage = message) },
        )
    }

    override fun toggle() {
        if (audio.paused) audio.play() else audio.pause()
    }
    override fun seekTo(positionMillis: Long) { audio.currentTime = positionMillis.coerceAtLeast(0) / 1000.0; publish(if (audio.paused) PlayerStatus.Paused else PlayerStatus.Playing) }
    override fun skipBy(deltaMillis: Long) = seekTo(state.value.positionMillis + deltaMillis)
    override fun setSpeed(speed: Float) { if (speed in PreviewPodcastPlayer.SPEEDS) { audio.playbackRate = speed.toDouble(); mutableState.value = mutableState.value.copy(speed = speed) } }
    override fun setSleepTimer(minutes: Int?) { mutableState.value = mutableState.value.copy(sleepTimerEndsAtMillis = minutes?.let { currentTimeMillis() + it * 60_000L }) }

    private fun publish(status: PlayerStatus) {
        val value = mutableState.value
        val duration = audio.duration.takeIf { it.isFinite() && it > 0 }?.times(1000)?.toLong() ?: value.durationMillis
        mutableState.value = value.copy(
            status = status,
            isPlaying = !audio.paused && !audio.ended,
            positionMillis = (audio.currentTime * 1000).toLong().coerceAtLeast(0),
            bufferedPositionMillis = duration,
            durationMillis = duration,
            errorMessage = null,
        )
    }
}
