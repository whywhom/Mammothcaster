package com.mammoth.podcast.core.domain

import com.mammoth.podcast.core.data.database.model.EpisodeToPodcast
import com.mammoth.podcast.core.data.repository.EpisodeStore
import com.mammoth.podcast.core.data.repository.PodcastStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

/**
 * A use case which returns all the latest episodes from all the podcasts the user follows.
 */
class GetLatestFollowedEpisodesUseCase(
    private val episodeStore: EpisodeStore,
    private val podcastStore: PodcastStore,
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
