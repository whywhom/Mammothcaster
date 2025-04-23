package com.mammoth.podcast.ui.podcast

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mammoth.podcast.core.data.repository.EpisodeStore
import com.mammoth.podcast.core.data.repository.PodcastStore
import com.mammoth.podcast.core.model.EpisodeInfo
import com.mammoth.podcast.core.model.PodcastInfo
import com.mammoth.podcast.core.model.asExternalModel
import com.mammoth.podcast.core.player.EpisodePlayer
import com.mammoth.podcast.core.player.model.PlayerEpisode
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface PodcastUiState {
    data object Loading : PodcastUiState
    data class Ready(
        val podcast: PodcastInfo,
        val episodes: List<EpisodeInfo>,
    ) : PodcastUiState
}

/**
 * ViewModel that handles the business logic and screen state of the Podcast details screen.
 */
@HiltViewModel(assistedFactory = PodcastDetailsViewModel.PodcastDetailsViewModelFactory::class)
class PodcastDetailsViewModel @AssistedInject constructor(
    private val episodeStore: EpisodeStore,
    private val episodePlayer: EpisodePlayer,
    private val podcastStore: PodcastStore,
    @Assisted val podcastUri: String,
) : ViewModel() {

    @AssistedFactory
    interface PodcastDetailsViewModelFactory {
        fun create(podcastUri: String): PodcastDetailsViewModel
    }

    private val decodedPodcastUri = Uri.decode(podcastUri)

    var state: StateFlow<PodcastUiState> =
        combine(
            podcastStore.podcastWithExtraInfo(decodedPodcastUri),
            episodeStore.episodesInPodcast(decodedPodcastUri)
        ) { podcast, episodeToPodcasts ->
            val episodes = episodeToPodcasts.map { it.episode.asExternalModel() }
            PodcastUiState.Ready(
                podcast = podcast.podcast.asExternalModel().copy(isSubscribed = podcast.isFollowed),
                episodes = episodes,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PodcastUiState.Loading
        )

    fun toggleSusbcribe(podcast: PodcastInfo) {
        viewModelScope.launch {
            podcastStore.togglePodcastFollowed(podcast.uri)
        }
    }

    fun onQueueEpisode(playerEpisode: PlayerEpisode) {
        episodePlayer.addToQueue(playerEpisode)
    }
}
