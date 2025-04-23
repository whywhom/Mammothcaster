package com.mammoth.podcast.ui.podcast

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mammoth.podcast.core.data.network.PodcastRssResponse
import com.mammoth.podcast.core.data.repository.PodcastsRepository
import com.mammoth.podcast.core.player.EpisodePlayer
import com.mammoth.podcast.core.player.model.PlayerEpisode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedItem(
    val title: String,
    val description: String?,
    val link: String
)

/**
 * ViewModel that handles the business logic and screen state of the Podcast details screen.
 */
@HiltViewModel
class ItunePodcastDetailsViewModel @Inject constructor(
    private val podcastsRepository: PodcastsRepository,
    private val episodePlayer: EpisodePlayer,
): ViewModel() {
    private val _feedItems = MutableStateFlow<PodcastRssResponse?>(null)
    val feedItems: StateFlow<PodcastRssResponse?> = _feedItems

    fun fetchFeed(feedUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val decodedPodcastUri = Uri.decode(feedUrl)
                podcastsRepository.fetchFeed(decodedPodcastUri).collect{ feedItems ->
                    _feedItems.value = feedItems
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onQueueEpisode(playerEpisode: PlayerEpisode) {
        episodePlayer.addToQueue(playerEpisode)
    }
}
