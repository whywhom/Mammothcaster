package com.mammoth.podcast.ui.home.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mammoth.podcast.data.model.PodcastSearchResult
import com.mammoth.podcast.data.model.ResultItem
import com.mammoth.podcast.domain.SearchUseCase
import com.mammoth.podcast.util.BSAE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class SearchViewModel(
    private val searchUseCase: SearchUseCase = SearchUseCase()
) : ViewModel() {
    private var _searchResults = MutableStateFlow<List<ResultItem>>(ArrayList())
    val searchResults: StateFlow<List<ResultItem>> = _searchResults

    private var _podcastTopResult = MutableStateFlow<List<PodcastSearchResult>>(ArrayList())
    val podcastTopResult: StateFlow<List<PodcastSearchResult>> = _podcastTopResult

    private val _navigationState = MutableStateFlow<List<ResultItem>>(listOf())
    val navigationState: StateFlow<List<ResultItem>> = _navigationState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun search(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (query.isNotEmpty()) {
                _isLoading.value = true
                try {
                    val language = Locale.getDefault().language
                        val url = BSAE_URL + "search?term=$query&lang=$language&limit=20&media=podcast"
                        val results = searchUseCase.search(url)
                        _searchResults.value = results
                        _errorMessage.value = null
                } catch (e: Exception) {
                    _errorMessage.value = e.message ?: "Unknown error"
                } finally {
                    _isLoading.value = false
                }
            }
            else {
                _searchResults.value = ArrayList()
            }
        }
    }

    fun getTop(number: Int = 20) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val country = Locale.getDefault().country
                val url = BSAE_URL+"$country/rss/toppodcasts/limit=$number/explicit=true/json"
                val results = searchUseCase.getTop(url)
                _podcastTopResult.value = results
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun lookUpFeedById(feedLookupUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val feedList = searchUseCase.fetchFeed(feedLookupUrl)
                if (feedList.isNotEmpty()) {
                _navigationState.value = feedList
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearNavigationState() {
        _navigationState.value = listOf()
    }
}