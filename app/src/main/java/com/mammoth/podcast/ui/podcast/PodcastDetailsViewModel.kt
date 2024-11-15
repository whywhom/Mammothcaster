package com.example.jetcaster.ui.podcast

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mammoth.podcast.MammothCastApp
import com.mammoth.podcast.data.repository.EpisodeStore
import com.mammoth.podcast.data.repository.PodcastStore
import com.mammoth.podcast.domain.model.EpisodeInfo
import com.mammoth.podcast.domain.model.PodcastInfo
import com.mammoth.podcast.domain.model.asExternalModel
import com.mammoth.podcast.ui.player.EpisodePlayer
import com.mammoth.podcast.ui.player.model.PlayerEpisode
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
class PodcastDetailsViewModel(
    private val episodeStore: EpisodeStore = MammothCastApp.episodeStore,
    private val episodePlayer: EpisodePlayer = MammothCastApp.episodePlayer,
    private val podcastStore: PodcastStore = MammothCastApp.podcastStore,
    private val podcastUri: String,
) : ViewModel() {

    private val decodedPodcastUri = Uri.decode(podcastUri)

    val state: StateFlow<PodcastUiState> =
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
