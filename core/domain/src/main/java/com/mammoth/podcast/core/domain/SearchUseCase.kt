package com.mammoth.podcast.core.domain

import com.mammoth.podcast.core.data.model.PodcastSearchResult
import com.mammoth.podcast.core.data.model.ResultItem
import com.mammoth.podcast.core.data.repository.SearchRepository
import javax.inject.Inject


class SearchUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    suspend fun search(url: String): List<ResultItem> {
        return searchRepository.search(url)
    }

    suspend fun getTop(url: String): List<PodcastSearchResult> {
        return searchRepository.getTop(url)
    }

    suspend fun fetchFeed(feedLookupUrl: String): List<ResultItem> {
        return searchRepository.fetchFeed(feedLookupUrl)
    }
}