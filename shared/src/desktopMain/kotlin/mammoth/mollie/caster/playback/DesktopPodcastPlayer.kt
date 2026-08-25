package mammoth.mollie.caster.playback

import javafx.application.Platform
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.downloads.DesktopEpisodeDownloadGateway
import mammoth.mollie.caster.data.cache.validatePlayableMedia
import mammoth.mollie.caster.platform.currentTimeMillis
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds

/** JavaFX Media adapter for JVM desktop. JavaFX supplies MP3/AAC streaming and seek support. */
class DesktopPodcastPlayer(private val mediaFiles: DesktopEpisodeDownloadGateway) : PodcastPlayer, AutoCloseable {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutableState = MutableStateFlow(PlayerState())
    private var player: MediaPlayer? = null
    override val state: StateFlow<PlayerState> = mutableState.asStateFlow()
    override val capabilities = PlayerCapabilities(realPlayback = true)

    init {
        scope.launch {
            while (isActive) {
                delay(500.milliseconds)
                onFx {
                    val deadline = state.value.sleepTimerEndsAtMillis
                    if (deadline != null && currentTimeMillis() >= deadline) {
                        player?.pause()
                        mutableState.value = mutableState.value.copy(sleepTimerEndsAtMillis = null)
                    }
                    publish()
                }
            }
        }
    }

    override fun play(episode: Episode) = load(episode, autoPlay = true)

    override fun prepare(episode: Episode) = load(episode, autoPlay = false)

    private fun load(episode: Episode, autoPlay: Boolean) {
        episode.enclosures.firstOrNull()?.let { enclosure ->
            validatePlayableMedia(episode.id.value, enclosure.url)?.let { return fail(episode, it) }
        }
        val source = mediaFiles.playbackSource(episode).ifBlank { return fail(episode, "This episode has no playable audio URL") }
        onFx {
            player?.dispose()
            mutableState.value = PlayerState(episode = episode, status = PlayerStatus.Loading, positionMillis = episode.playbackPositionMillis, speed = state.value.speed)
            runCatching {
                MediaPlayer(Media(source)).also { mediaPlayer ->
                    player = mediaPlayer
                    mediaPlayer.rate = state.value.speed.toDouble()
                    mediaPlayer.setOnReady {
                        val position = episode.playbackPositionMillis.toDouble()
                        if (position > 0) mediaPlayer.seek(javafx.util.Duration.millis(position))
                        if (autoPlay) {
                            mediaPlayer.play()
                            publish()
                        } else {
                            val duration = mediaPlayer.totalDuration.toMillis()
                                .takeIf { it.isFinite() && it > 0 }
                                ?.toLong()
                                ?: 0L
                            // JavaFX applies seek asynchronously. Keep the requested resume
                            // position visible until its next engine callback confirms it.
                            mutableState.value = mutableState.value.copy(
                                status = PlayerStatus.Ready,
                                isPlaying = false,
                                positionMillis = episode.playbackPositionMillis,
                                bufferedPositionMillis = duration,
                                durationMillis = duration,
                            )
                        }
                    }
                    mediaPlayer.setOnPlaying { publish() }
                    mediaPlayer.setOnPaused { publish() }
                    mediaPlayer.setOnEndOfMedia { publish(PlayerStatus.Ended) }
                    mediaPlayer.setOnError { fail(episode, mediaPlayer.error?.message ?: "Desktop media engine could not play this stream") }
                }
            }.onFailure { fail(episode, it.message ?: "Desktop media engine could not load this stream") }
        }
    }

    override fun toggle() = onFx { player?.let { if (it.status == MediaPlayer.Status.PLAYING) it.pause() else it.play() } }
    override fun seekTo(positionMillis: Long) = onFx { player?.seek(javafx.util.Duration.millis(positionMillis.coerceAtLeast(0).toDouble())); publish() }
    override fun skipBy(deltaMillis: Long) = seekTo(state.value.positionMillis + deltaMillis)
    override fun setSpeed(speed: Float) = onFx { if (speed in PreviewPodcastPlayer.SPEEDS) { player?.rate = speed.toDouble(); mutableState.value = mutableState.value.copy(speed = speed) } }
    override fun setSleepTimer(minutes: Int?) { mutableState.value = mutableState.value.copy(sleepTimerEndsAtMillis = minutes?.let { currentTimeMillis() + it * 60_000L }) }

    override fun close() {
        scope.cancel()
        onFx { player?.dispose(); player = null }
        // JavaFX Media owns a separate runtime. Stop it explicitly so it cannot
        // keep the macOS application process alive after Compose exits.
        runCatching { Platform.exit() }
    }

    private fun publish(endedStatus: PlayerStatus? = null) {
        val mediaPlayer = player ?: return
        val status = endedStatus ?: when (mediaPlayer.status) {
            MediaPlayer.Status.READY -> PlayerStatus.Ready
            MediaPlayer.Status.PLAYING -> PlayerStatus.Playing
            MediaPlayer.Status.PAUSED, MediaPlayer.Status.STOPPED -> PlayerStatus.Paused
            MediaPlayer.Status.STALLED -> PlayerStatus.Loading
            MediaPlayer.Status.HALTED -> PlayerStatus.Failed
            MediaPlayer.Status.DISPOSED, MediaPlayer.Status.UNKNOWN -> PlayerStatus.Idle
        }
        val duration = mediaPlayer.totalDuration.toMillis().takeIf { it.isFinite() && it > 0 }?.toLong() ?: 0L
        mutableState.value = mutableState.value.copy(
            status = status,
            isPlaying = status == PlayerStatus.Playing,
            positionMillis = mediaPlayer.currentTime.toMillis().toLong().coerceAtLeast(0),
            bufferedPositionMillis = duration,
            durationMillis = duration,
            errorMessage = mediaPlayer.error?.message,
        )
    }

    private fun fail(episode: Episode, message: String) {
        mutableState.value = PlayerState(episode = episode, status = PlayerStatus.Failed, errorMessage = message)
    }

    private fun onFx(block: () -> Unit) = JavaFxRuntime.run(block)
}

private object JavaFxRuntime {
    private val started = AtomicBoolean(false)

    fun run(block: () -> Unit) {
        if (Platform.isFxApplicationThread()) block()
        else if (started.compareAndSet(false, true)) Platform.startup(block)
        else Platform.runLater(block)
    }
}
