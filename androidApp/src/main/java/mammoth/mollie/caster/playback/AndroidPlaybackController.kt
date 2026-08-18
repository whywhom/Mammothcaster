package mammoth.mollie.caster.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

/**
 * UI/application bridge to [MolliePlaybackService]. Keep one instance at the
 * application/navigation-root scope and call [close] when that scope ends.
 */
class AndroidPlaybackController(context: Context) : AutoCloseable {
    private val applicationContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val mutableState = MutableStateFlow(AndroidPlaybackState())
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private val pendingCommands = ArrayDeque<(MediaController) -> Unit>()
    private var sleepDeadlineElapsedRealtime: Long? = null
    private var closed = false

    val state: StateFlow<AndroidPlaybackState> = mutableState.asStateFlow()
    val capabilities = AndroidPlaybackCapabilities()

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = publishState(player)

        override fun onPlayerError(error: PlaybackException) {
            publishState(controller, error.message ?: "Playback failed")
        }
    }

    private val progressTicker = object : Runnable {
        override fun run() {
            publishState(controller)
            mainHandler.postDelayed(this, PROGRESS_TICK_MILLIS)
        }
    }

    private val sleepTimer = Runnable {
        sleepDeadlineElapsedRealtime = null
        controller?.pause()
        publishState(controller)
    }

    init {
        connect()
    }

    fun load(item: AndroidPlaybackItem, playWhenReady: Boolean = true) {
        withController { player ->
            val metadata = MediaMetadata.Builder()
                .setTitle(item.title)
                .setArtist(item.podcastTitle)
                .setAlbumTitle(item.podcastTitle)
                .apply { item.artworkUrl?.let { setArtworkUri(Uri.parse(it)) } }
                .build()
            val mediaItem = MediaItem.Builder()
                .setMediaId(item.episodeId)
                .setUri(item.mediaUrl)
                .setMediaMetadata(metadata)
                .build()

            val resumePosition = item.knownDurationMillis
                ?.takeIf { it > 0L }
                ?.let { item.resumePositionMillis.coerceIn(0L, it) }
                ?: max(0L, item.resumePositionMillis)
            player.setMediaItem(mediaItem, resumePosition)
            player.prepare()
            player.playWhenReady = playWhenReady
            publishState(player)
        }
    }

    fun play() = withController(Player::play)

    fun pause() = withController(Player::pause)

    fun seekTo(positionMillis: Long) = withController { player ->
        val upperBound = player.duration.takeIf { it > 0L } ?: Long.MAX_VALUE
        player.seekTo(positionMillis.coerceIn(0L, upperBound))
    }

    fun skipForward15Seconds() = withController(Player::seekForward)

    fun skipBack15Seconds() = withController(Player::seekBack)

    fun setPlaybackSpeed(speed: Float) {
        require(speed in SUPPORTED_SPEEDS) { "Unsupported playback speed: $speed" }
        withController { it.setPlaybackSpeed(speed) }
    }

    fun setSleepTimer(minutes: Int) {
        require(minutes in SUPPORTED_SLEEP_TIMER_MINUTES) { "Unsupported sleep timer: $minutes minutes" }
        mainHandler.removeCallbacks(sleepTimer)
        val durationMillis = minutes * 60_000L
        sleepDeadlineElapsedRealtime = SystemClock.elapsedRealtime() + durationMillis
        mainHandler.postDelayed(sleepTimer, durationMillis)
        publishState(controller)
    }

    fun cancelSleepTimer() {
        mainHandler.removeCallbacks(sleepTimer)
        sleepDeadlineElapsedRealtime = null
        publishState(controller)
    }

    override fun close() {
        closed = true
        mainHandler.removeCallbacks(progressTicker)
        mainHandler.removeCallbacks(sleepTimer)
        pendingCommands.clear()
        controller?.removeListener(playerListener)
        controller = null
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
    }

    private fun connect() {
        val token = SessionToken(
            applicationContext,
            ComponentName(applicationContext, MolliePlaybackService::class.java),
        )
        val future = MediaController.Builder(applicationContext, token).buildAsync()
        controllerFuture = future
        future.addListener(
            listener@{
                if (closed) return@listener
                runCatching { future.get() }
                    .onSuccess { mediaController ->
                        if (closed) {
                            MediaController.releaseFuture(future)
                            return@onSuccess
                        }
                        controller = mediaController
                        mediaController.addListener(playerListener)
                        while (pendingCommands.isNotEmpty()) {
                            pendingCommands.removeFirst()(mediaController)
                        }
                        publishState(mediaController)
                        mainHandler.removeCallbacks(progressTicker)
                        mainHandler.post(progressTicker)
                    }
                    .onFailure { error ->
                        mutableState.value = mutableState.value.copy(
                            status = AndroidPlaybackStatus.Failed,
                            errorMessage = error.message ?: "Unable to connect to playback service",
                        )
                    }
            },
            ContextCompat.getMainExecutor(applicationContext),
        )
    }

    private fun withController(block: (MediaController) -> Unit) {
        val mediaController = controller
        if (mediaController == null) {
            if (!closed) pendingCommands.addLast(block)
            mutableState.value = mutableState.value.copy(
                status = AndroidPlaybackStatus.Loading,
                errorMessage = null,
            )
        } else {
            block(mediaController)
            publishState(mediaController)
        }
    }

    private fun publishState(player: Player?, errorMessage: String? = null) {
        if (player == null) return
        val metadata = player.mediaMetadata
        val status = when {
            errorMessage != null || player.playerError != null -> AndroidPlaybackStatus.Failed
            player.mediaItemCount == 0 -> AndroidPlaybackStatus.Idle
            player.playbackState == Player.STATE_BUFFERING -> AndroidPlaybackStatus.Loading
            player.playbackState == Player.STATE_ENDED -> AndroidPlaybackStatus.Ended
            player.isPlaying -> AndroidPlaybackStatus.Playing
            player.playbackState == Player.STATE_READY && player.playWhenReady -> AndroidPlaybackStatus.Ready
            player.playbackState == Player.STATE_READY -> AndroidPlaybackStatus.Paused
            else -> AndroidPlaybackStatus.Loading
        }
        val sleepRemaining = sleepDeadlineElapsedRealtime?.let { deadline ->
            (deadline - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        }
        mutableState.value = AndroidPlaybackState(
            episodeId = player.currentMediaItem?.mediaId,
            title = metadata.title?.toString(),
            podcastTitle = (metadata.albumTitle ?: metadata.artist)?.toString(),
            artworkUrl = metadata.artworkUri?.toString(),
            mediaUrl = player.currentMediaItem?.localConfiguration?.uri?.toString(),
            status = status,
            positionMillis = player.currentPosition.coerceAtLeast(0L),
            bufferedPositionMillis = player.bufferedPosition.coerceAtLeast(0L),
            durationMillis = player.duration.takeIf { it > 0L } ?: 0L,
            speed = player.playbackParameters.speed,
            sleepTimerRemainingMillis = sleepRemaining,
            errorMessage = errorMessage ?: player.playerError?.message,
        )
    }

    companion object {
        const val SEEK_INCREMENT_MILLIS = 15_000L
        const val PROGRESS_TICK_MILLIS = 1_000L
        val SUPPORTED_SPEEDS = setOf(0.8f, 1f, 1.25f, 1.5f, 2f)
        val SUPPORTED_SLEEP_TIMER_MINUTES = setOf(15, 30, 60)
    }
}
