package com.mammoth.podcast.domain

import com.mammoth.podcast.data.SearchRepository
import com.mammoth.podcast.data.model.PodcastSearchResult
import com.mammoth.podcast.data.model.ResultItem

class SearchUseCase(
    private val searchRepository: SearchRepository = SearchRepository()
) {
    suspend fun search(url: String): List<ResultItem> {
        return searchRepository.search(url)
    }

    suspend fun getTop(url: String): List<PodcastSearchResult> {
        return searchRepository.getTop(url)
    }
}