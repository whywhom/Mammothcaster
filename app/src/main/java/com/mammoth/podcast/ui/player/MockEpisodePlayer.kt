package com.mammoth.podcast.ui.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.mammoth.podcast.MediaPlayerService
import com.mammoth.podcast.ui.player.model.PlayerEpisode
import java.time.Duration
import kotlin.reflect.KProperty
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MockEpisodePlayer(
    context: Context,
    private val mainDispatcher: CoroutineDispatcher
) : EpisodePlayer {

    private var player: MediaController? = null
    private val _playerState = MutableStateFlow(EpisodePlayerState())
    private val _currentEpisode = MutableStateFlow<PlayerEpisode?>(null)
    private val queue = MutableStateFlow<List<PlayerEpisode>>(emptyList())
    private val isPlaying = MutableStateFlow(false)
    private val timeElapsed = MutableStateFlow(Duration.ZERO)
    private val _playerSpeed = MutableStateFlow(DefaultPlaybackSpeed)
    private val coroutineScope = CoroutineScope(mainDispatcher)

    private var timerJob: Job? = null

    init {
        coroutineScope.launch {
            // Combine streams here
            combine(
                _currentEpisode,
                queue,
                isPlaying,
                timeElapsed,
                _playerSpeed
            ) { currentEpisode, queue, isPlaying, timeElapsed, playerSpeed ->
                EpisodePlayerState(
                    currentEpisode = currentEpisode,
                    queue = queue,
                    isPlaying = isPlaying,
                    timeElapsed = timeElapsed,
                    playbackSpeed = playerSpeed
                )
            }.catch {
                // TODO handle error state
                throw it
            }.collect {
                _playerState.value = it
            }
        }

        /* Creating session token (links our UI with service and starts it) */
        val sessionToken =
            SessionToken(context, ComponentName(context, MediaPlayerService::class.java))
        /* Instantiating our MediaController and linking it to the service using the session token */
        val mediacontrollerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        mediacontrollerFuture.addListener({
            player = mediacontrollerFuture.get()
        }, MoreExecutors.directExecutor())
    }

    override var playerSpeed: Duration = _playerSpeed.value

    override val playerState: StateFlow<EpisodePlayerState> = _playerState.asStateFlow()

    override var currentEpisode: PlayerEpisode? by _currentEpisode

    override fun addToQueue(episode: PlayerEpisode) {
        queue.update {
            it + episode
        }
    }

    override fun removeAllFromQueue() {
        queue.value = emptyList()
    }

    override fun play() {
        // Do nothing if already playing
        if (isPlaying.value) {
            return
        }

        val episode = _currentEpisode.value ?: return

        isPlaying.value = true
        timerJob = coroutineScope.launch {
            // Increment timer by a second
            while (isActive && timeElapsed.value < episode.duration) {
                delay(playerSpeed.toMillis())
                timeElapsed.update { it + playerSpeed }
            }

            // Once done playing, see if
            isPlaying.value = false
            timeElapsed.value = Duration.ZERO

            if (hasNext()) {
                next()
            }
        }

        episode.enclosureUrl?.let { url ->
            // Build the media item.
            val mediaItem = MediaItem.fromUri(url)
            // Set the media item to be played.
            player?.setMediaItem(mediaItem)
            // Prepare the player.
            player?.prepare()
            // Start the playback.
            player?.play()
        }

    }

    override fun play(playerEpisode: PlayerEpisode) {
        play(listOf(playerEpisode))
    }

    override fun play(playerEpisodes: List<PlayerEpisode>) {
        if (isPlaying.value) {
            pause()
        }

        // Keep the currently playing episode in the queue
        val playingEpisode = _currentEpisode.value
        var previousList: List<PlayerEpisode> = emptyList()
        queue.update { queue ->
            playerEpisodes.map { episode ->
                if (queue.contains(episode)) {
                    val mutableList = queue.toMutableList()
                    mutableList.remove(episode)
                    previousList = mutableList
                } else {
                    previousList = queue
                }
            }
            if (playingEpisode != null) {
                playerEpisodes + listOf(playingEpisode) + previousList
            } else {
                playerEpisodes + previousList
            }
        }

        next()
    }

    override fun pause() {
        isPlaying.value = false

        timerJob?.cancel()
        timerJob = null
    }

    override fun stop() {
        isPlaying.value = false
        timeElapsed.value = Duration.ZERO

        timerJob?.cancel()
        timerJob = null
    }

    override fun advanceBy(duration: Duration) {
        val currentEpisodeDuration = _currentEpisode.value?.duration ?: return
        timeElapsed.update {
            (it + duration).coerceAtMost(currentEpisodeDuration)
        }
    }

    override fun rewindBy(duration: Duration) {
        timeElapsed.update {
            (it - duration).coerceAtLeast(Duration.ZERO)
        }
    }

    override fun onSeekingStarted() {
        // Need to pause the player so that it doesn't compete with timeline progression.
        pause()
    }

    override fun onSeekingFinished(duration: Duration) {
        val currentEpisodeDuration = _currentEpisode.value?.duration ?: return
        timeElapsed.update { duration.coerceIn(Duration.ZERO, currentEpisodeDuration) }
        play()
    }

    override fun increaseSpeed(speed: Duration) {
        _playerSpeed.value += speed
    }

    override fun decreaseSpeed(speed: Duration) {
        _playerSpeed.value -= speed
    }

    override fun next() {
        val q = queue.value
        if (q.isEmpty()) {
            return
        }

        timeElapsed.value = Duration.ZERO
        val nextEpisode = q[0]
        currentEpisode = nextEpisode
        queue.value = q - nextEpisode
        play()
    }

    override fun previous() {
        timeElapsed.value = Duration.ZERO
        isPlaying.value = false
        timerJob?.cancel()
        timerJob = null
    }

    private fun hasNext(): Boolean {
        return queue.value.isNotEmpty()
    }
}

// Used to enable property delegation
private operator fun <T> MutableStateFlow<T>.setValue(
    thisObj: Any?,
    property: KProperty<*>,
    value: T
) {
    this.value = value
}

private operator fun <T> MutableStateFlow<T>.getValue(thisObj: Any?, property: KProperty<*>): T =
    this.value
