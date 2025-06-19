package com.mammoth.podcast.core.model

data class LibraryInfo(
    val episodes: List<PodcastToEpisodeInfo> = emptyList()
) : List<PodcastToEpisodeInfo> by episodes
