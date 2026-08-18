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
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.platform.currentTimeMillis
import org.w3c.dom.Audio

/** Browser HTMLAudio adapter. Playback must be initiated from a user gesture. */
class WebPodcastPlayer : PodcastPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val audio = Audio("")
    private val mutableState = MutableStateFlow(PlayerState())
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
                delay(500)
                val deadline = mutableState.value.sleepTimerEndsAtMillis ?: continue
                if (currentTimeMillis() >= deadline) {
                    audio.pause()
                    mutableState.value = mutableState.value.copy(sleepTimerEndsAtMillis = null)
                }
            }
        }
    }

    override fun play(episode: Episode) {
        val source = episode.enclosures.firstOrNull()?.url ?: run {
            mutableState.value = PlayerState(episode = episode, status = PlayerStatus.Failed, errorMessage = "This episode has no playable audio URL")
            return
        }
        mutableState.value = PlayerState(episode = episode, status = PlayerStatus.Loading, positionMillis = episode.playbackPositionMillis, speed = state.value.speed)
        audio.src = source
        audio.playbackRate = state.value.speed.toDouble()
        audio.currentTime = episode.playbackPositionMillis / 1000.0
        audio.play()
    }

    override fun toggle() = if (audio.paused) audio.play() else audio.pause()
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
