package com.mammoth.podcast.domain.model

data class LibraryInfo(
    val episodes: List<PodcastToEpisodeInfo> = emptyList()
) : List<PodcastToEpisodeInfo> by episodes
