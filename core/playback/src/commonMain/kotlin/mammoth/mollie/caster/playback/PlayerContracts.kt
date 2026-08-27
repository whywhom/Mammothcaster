package mammoth.mollie.caster.playback
import kotlinx.coroutines.flow.StateFlow
import mammoth.mollie.caster.model.Episode
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
    val transparentStreamCache: Boolean = false,
    val partialOfflinePlayback: Boolean = false,
)

interface PodcastPlayer {
    val state: StateFlow<PlayerState>
    val capabilities: PlayerCapabilities
    fun play(episode: Episode)
    /** Loads an episode without starting it, for restoring the mini-player after relaunch. */
    fun prepare(episode: Episode) = play(episode)
    fun toggle()
    fun seekTo(positionMillis: Long)
    fun skipBy(deltaMillis: Long)
    fun setSpeed(speed: Float)
    fun setSleepTimer(minutes: Int?)
}
