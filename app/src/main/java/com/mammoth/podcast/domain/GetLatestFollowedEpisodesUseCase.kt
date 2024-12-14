package com.mammoth.podcast.domain

import com.mammoth.podcast.MammothCastApp
import com.mammoth.podcast.data.database.model.EpisodeToPodcast
import com.mammoth.podcast.data.repository.EpisodeStore
import com.mammoth.podcast.data.repository.PodcastStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

/**
 * A use case which returns all the latest episodes from all the podcasts the user follows.
 */
class GetLatestFollowedEpisodesUseCase(
    private val episodeStore: EpisodeStore = MammothCastApp.episodeStore,
    private val podcastStore: PodcastStore = MammothCastApp.podcastStore,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<EpisodeToPodcast>> =
        podcastStore.followedPodcastsSortedByLastEpisode()
            .flatMapLatest { followedPodcasts ->
                episodeStore.episodesInPodcasts(
                    followedPodcasts.map { it.podcast.uri },
                    followedPodcasts.size * 5
                )
            }
}
