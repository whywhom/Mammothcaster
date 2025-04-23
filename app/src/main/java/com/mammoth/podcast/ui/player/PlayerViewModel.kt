package com.mammoth.podcast.ui.player

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mammoth.podcast.core.data.repository.EpisodeStore
import com.mammoth.podcast.core.player.EpisodePlayer
import com.mammoth.podcast.core.player.EpisodePlayerState
import com.mammoth.podcast.core.player.model.toPlayerEpisode
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
@HiltViewModel(assistedFactory = PlayerViewModel.PlayerViewModelFactory::class)
class PlayerViewModel @AssistedInject constructor(
    private val episodeStore: EpisodeStore,
    private val episodePlayer: EpisodePlayer,
    @Assisted episodeUri: String
) : ViewModel() {

    @AssistedFactory
    interface PlayerViewModelFactory {
        fun create(episodeUri: String): PlayerViewModel
    }

    private var _uiState = MutableStateFlow(PlayerUiState())
    var uiState: StateFlow<PlayerUiState> = _uiState
    init {
        viewModelScope.launch {
            episodeStore.episodeAndPodcastWithUri(episodeUri).flatMapConcat {
                episodePlayer.currentEpisode = it.toPlayerEpisode()
                episodePlayer.playerState
            }.map {
                PlayerUiState(episodePlayerState = it)
            }.collect {
                _uiState.value = it
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
        uiState.value.episodePlayerState.currentEpisode?.let {
            episodePlayer.addToQueue(it)
        }
    }
}
