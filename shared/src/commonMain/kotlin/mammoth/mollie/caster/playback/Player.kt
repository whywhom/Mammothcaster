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

enum class PlayerStatus { Idle, Loading, Ready, Playing, Paused, Ended, Failed }

data class PlayerState(
    val episode: Episode? = null,
    val status: PlayerStatus = PlayerStatus.Idle,
    val isPlaying: Boolean = false,
    val positionMillis: Long = 0,
    val bufferedPositionMillis: Long = 0,
    val durationMillis: Long = 0,
    val speed: Float = 1f,
    val sleepTimerEndsAtMillis: Long? = null,
    val errorMessage: String? = null,
)

data class PlayerCapabilities(
    val realPlayback: Boolean,
    val backgroundPlayback: Boolean = false,
    val lockScreenControls: Boolean = false,
    val notificationControls: Boolean = false,
)

interface PodcastPlayer {
    val state: StateFlow<PlayerState>
    val capabilities: PlayerCapabilities
    fun play(episode: Episode)
    fun toggle()
    fun seekTo(positionMillis: Long)
    fun skipBy(deltaMillis: Long)
    fun setSpeed(speed: Float)
    fun setSleepTimer(minutes: Int?)
}

/** A portable fallback used by desktop, iOS preview and web until their native adapters are attached. */
class PreviewPodcastPlayer : PodcastPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutableState = MutableStateFlow(PlayerState())
    override val state = mutableState.asStateFlow()
    override val capabilities = PlayerCapabilities(realPlayback = false)

    init {
        scope.launch {
            while (isActive) {
                delay(500)
                val value = mutableState.value
                if (!value.isPlaying) continue
                val timerExpired = value.sleepTimerEndsAtMillis?.let { it <= currentTimeMillis() } == true
                val next = (value.positionMillis + (500 * value.speed).toLong()).coerceAtMost(value.durationMillis)
                mutableState.value = value.copy(
                    positionMillis = next,
                    isPlaying = !timerExpired && next < value.durationMillis,
                    sleepTimerEndsAtMillis = value.sleepTimerEndsAtMillis.takeUnless { timerExpired },
                )
            }
        }
    }

    override fun play(episode: Episode) {
        mutableState.value = PlayerState(
            episode = episode,
            status = PlayerStatus.Idle,
            isPlaying = false,
            positionMillis = episode.playbackPositionMillis,
            durationMillis = episode.durationMillis ?: 30 * 60 * 1000L,
            speed = mutableState.value.speed,
        )
    }

    override fun toggle() { mutableState.value = mutableState.value.let { it.copy(isPlaying = it.episode != null && !it.isPlaying) } }
    override fun seekTo(positionMillis: Long) { mutableState.value = mutableState.value.let { it.copy(positionMillis = positionMillis.coerceIn(0, it.durationMillis)) } }
    override fun skipBy(deltaMillis: Long) = seekTo(mutableState.value.positionMillis + deltaMillis)
    override fun setSpeed(speed: Float) { if (speed in SPEEDS) mutableState.value = mutableState.value.copy(speed = speed) }
    override fun setSleepTimer(minutes: Int?) {
        mutableState.value = mutableState.value.copy(sleepTimerEndsAtMillis = minutes?.let { currentTimeMillis() + it * 60_000L })
    }

    companion object { val SPEEDS = listOf(0.8f, 1f, 1.25f, 1.5f, 2f) }
}
