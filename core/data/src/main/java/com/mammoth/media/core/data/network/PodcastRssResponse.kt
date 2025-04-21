package com.mammoth.media.core.data.network

sealed class PodcastRssResponse {
    data class Error(
        val throwable: Throwable?,
    ) : PodcastRssResponse()

    data class Success(
        val podcast: Podcast,
        val episodes: List<Episode>,
        val categories: Set<Category>
    ) : PodcastRssResponse()
}