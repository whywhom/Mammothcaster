package com.mammoth.podcast.core.model

import com.mammoth.podcast.core.data.database.model.EpisodeToPodcast

data class PodcastToEpisodeInfo(
    val episode: EpisodeInfo,
    val podcast: PodcastInfo,
)

fun EpisodeToPodcast.asPodcastToEpisodeInfo(): PodcastToEpisodeInfo =
    PodcastToEpisodeInfo(
        episode = episode.asExternalModel(),
        podcast = podcast.asExternalModel(),
    )
