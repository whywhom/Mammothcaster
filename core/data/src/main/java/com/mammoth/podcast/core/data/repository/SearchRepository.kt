package com.mammoth.podcast.core.data.repository

import com.mammoth.podcast.core.data.Dispatcher
import com.mammoth.podcast.core.data.MammothDispatchers
import com.mammoth.podcast.core.data.model.PodcastSearchResult
import com.mammoth.podcast.core.data.model.ResultItem
import com.mammoth.podcast.core.data.network.SearchRemoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

/**
 * Data repository for Podcasts.
 */
class SearchRepository @Inject constructor(
    @Dispatcher(MammothDispatchers.Main) mainDispatcher: CoroutineDispatcher,
    private val searchRemoteDataSource: SearchRemoteDataSource,
) {

    fun search(url: String): List<ResultItem> {
        return searchRemoteDataSource.search(url)
    }
    fun getTop(url: String): List<PodcastSearchResult> {
        return searchRemoteDataSource.getTop(url)
    }

    suspend fun fetchFeed(feedLookupUrl: String): List<ResultItem> {
        return searchRemoteDataSource.fetchFeed(feedLookupUrl)
    }
}
