package com.mammoth.podcast.data

import com.mammoth.podcast.data.model.PodcastSearchResult
import com.mammoth.podcast.data.model.ResultItem

/**
 * Data repository for Podcasts.
 */
class SearchRepository(
    private val remoteDataSource: SearchRemoteDataSource = SearchRemoteDataSource(),
) {

    suspend fun search(url: String): List<ResultItem> {
        return remoteDataSource.search(url)
    }
    suspend fun getTop(url: String): List<PodcastSearchResult> {
        return remoteDataSource.getTop(url)
    }
}
