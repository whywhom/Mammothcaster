package com.mammoth.podcast.domain.model

import com.mammoth.podcast.data.database.model.EpisodeToPodcast

data class PodcastToEpisodeInfo(
    val episode: EpisodeInfo,
    val podcast: PodcastInfo,
)

fun EpisodeToPodcast.asPodcastToEpisodeInfo(): PodcastToEpisodeInfo =
    PodcastToEpisodeInfo(
        episode = episode.asExternalModel(),
        podcast = podcast.asExternalModel(),
    )
