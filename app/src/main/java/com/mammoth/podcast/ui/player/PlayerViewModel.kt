package com.mammoth.podcast.ui.player

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mammoth.podcast.MammothCastApp
import com.mammoth.podcast.data.repository.EpisodeStore
import com.mammoth.podcast.ui.player.model.toPlayerEpisode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Duration

data class PlayerUiState(
    val episodePlayerState: EpisodePlayerState = EpisodePlayerState()
)

/**
 * ViewModel that handles the business logic and screen state of the Player screen
 */
@OptIn(ExperimentalCoroutinesApi::class)
@SuppressLint("StaticFieldLeak")
class PlayerViewModel(
    val context: Context,
    episodeStore: EpisodeStore = MammothCastApp.episodeStore,
    private val episodePlayer: EpisodePlayer = MammothCastApp.episodePlayer,
    episodeUri: String
) : ViewModel() {

    var uiState by mutableStateOf(PlayerUiState())
        private set

    init {
        viewModelScope.launch {
            episodeStore.episodeAndPodcastWithUri(episodeUri).flatMapConcat {
                episodePlayer.currentEpisode = it.toPlayerEpisode()
                episodePlayer.playerState
            }.map {
                PlayerUiState(episodePlayerState = it)
            }.collect {
                uiState = it
            }
        }
    }

    fun onPlay() {
        episodePlayer.play()
    }

    fun onPause() {
        episodePlayer.pause()
    }

    fun onStop() {
        episodePlayer.stop()
    }

    fun onPrevious() {
        episodePlayer.previous()
    }

    fun onNext() {
        episodePlayer.next()
    }

    fun onAdvanceBy(duration: Duration) {
        episodePlayer.advanceBy(duration)
    }

    fun onRewindBy(duration: Duration) {
        episodePlayer.rewindBy(duration)
    }

    fun onSeekingStarted() {
        episodePlayer.onSeekingStarted()
    }

    fun onSeekingFinished(duration: Duration) {
        episodePlayer.onSeekingFinished(duration)
    }

    fun onAddToQueue() {
        uiState.episodePlayerState.currentEpisode?.let {
            episodePlayer.addToQueue(it)
        }
    }
}
