package com.mammoth.podcast.core.model

/**
 * A model holding top podcasts and matching episodes when filtering based on a category.
 */
data class PodcastCategoryFilterResult(
    val topPodcasts: List<PodcastInfo> = emptyList(),
    val episodes: List<PodcastToEpisodeInfo> = emptyList()
)
