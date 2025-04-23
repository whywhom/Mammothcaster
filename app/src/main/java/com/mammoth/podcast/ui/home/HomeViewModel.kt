package com.mammoth.podcast.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mammoth.podcast.MammothCastApp
import com.mammoth.podcast.core.data.database.model.EpisodeToPodcast
import com.mammoth.podcast.core.data.repository.EpisodeStore
import com.mammoth.podcast.core.data.repository.PodcastStore
import com.mammoth.podcast.core.data.repository.PodcastsRepository
import com.mammoth.podcast.core.domain.FilterableCategoriesUseCase
import com.mammoth.podcast.core.domain.PodcastCategoryFilterUseCase
import com.mammoth.podcast.core.model.CategoryInfo
import com.mammoth.podcast.core.model.FilterableCategoriesModel
import com.mammoth.podcast.core.model.LibraryInfo
import com.mammoth.podcast.core.model.PodcastCategoryFilterResult
import com.mammoth.podcast.core.model.PodcastInfo
import com.mammoth.podcast.core.model.asExternalModel
import com.mammoth.podcast.core.model.asPodcastToEpisodeInfo
import com.mammoth.podcast.core.player.EpisodePlayer
import com.mammoth.podcast.core.player.model.PlayerEpisode
import com.mammoth.podcast.util.combine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val podcastsRepository: PodcastsRepository,
    private val podcastStore: PodcastStore,
    private val episodeStore: EpisodeStore,
    private val podcastCategoryFilterUseCase: PodcastCategoryFilterUseCase,
    private val filterableCategoriesUseCase: FilterableCategoriesUseCase,
    private val episodePlayer: EpisodePlayer,
) : ViewModel() {
    // Holds the view state if the UI is refreshing for new com.mammoth.podcast.core.data
    private val refreshing = MutableStateFlow(false)
    // Holds our currently selected podcast in the library
    private val selectedLibraryPodcast = MutableStateFlow<PodcastInfo?>(null)
    // Holds our currently selected home category
    private val selectedHomeCategory = MutableStateFlow(HomeCategory.Discover)
    // Holds the currently available home categories
    private val homeCategories = MutableStateFlow(HomeCategory.entries)
    // Holds our currently selected category
    private val _selectedCategory = MutableStateFlow<CategoryInfo?>(null)
    private val subscribedPodcasts = podcastStore.followedPodcastsSortedByLastEpisode(limit = 10)
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed())
    // Holds our view state which the UI collects via [state]
    private val _state = MutableStateFlow<HomeScreenUiState>(HomeScreenUiState.Loading)

    val state: StateFlow<HomeScreenUiState>
        get() = _state

    init {
        viewModelScope.launch {
            // Combines the latest value from each of the flows, allowing us to generate a
            // view state instance which only contains the latest values.
            combine(
                homeCategories,
                selectedHomeCategory,
                subscribedPodcasts,
                refreshing,
                _selectedCategory.flatMapLatest { selectedCategory ->
                    filterableCategoriesUseCase(selectedCategory)
                },
                _selectedCategory.flatMapLatest {
                    podcastCategoryFilterUseCase(it)
                },
                subscribedPodcasts.flatMapLatest { podcasts ->
                    episodeStore.episodesInPodcasts(
                        podcastUris = podcasts.map { it.podcast.uri },
                        limit = 20
                    )
                }
            ) { homeCategories,
                homeCategory,
                podcasts,
                refreshing,
                filterableCategories,
                podcastCategoryFilterResult,
                libraryEpisodes ->

                if (refreshing) {
                    Log.d("Jetcaster", "refreshing: $refreshing, podcasts $podcasts")
                    return@combine HomeScreenUiState.Loading
                }

                _selectedCategory.value = filterableCategories.selectedCategory

                // Override selected home category to show 'DISCOVER' if there are no
                // featured podcasts
                selectedHomeCategory.value =
                    if (podcasts.isEmpty()) HomeCategory.Discover else homeCategory

                HomeScreenUiState.Ready(
                    homeCategories = homeCategories,
                    selectedHomeCategory = homeCategory,
                    featuredPodcasts = podcasts.map { it.asExternalModel() }.toPersistentList(),
                    filterableCategoriesModel = filterableCategories,
                    podcastCategoryFilterResult = podcastCategoryFilterResult,
                    library = libraryEpisodes.asLibrary()
                )
            }.catch { throwable ->
                _state.value = HomeScreenUiState.Error(throwable.message)
            }.collect {
                _state.value = it
            }
        }

        refresh(force = checkIfOverTime())
    }

    private fun checkIfOverTime(): Boolean {
        Log.d(HomeViewModel::class.java.simpleName, "checkIfOverTime = ${MammothCastApp.shouldRefresh}")
        return MammothCastApp.shouldRefresh
    }

    fun refresh(force: Boolean = true) {
        viewModelScope.launch {
            runCatching {
                refreshing.value = true
                podcastsRepository.updatePodcasts(force)
            }
            // TODO: look at result of runCatching and show any errors
            refreshing.value = false
        }
    }

    fun onCategorySelected(category: CategoryInfo) {
        _selectedCategory.value = category
    }

    fun onHomeCategorySelected(category: HomeCategory) {
        selectedHomeCategory.value = category
    }

    fun onPodcastUnfollowed(podcast: PodcastInfo) {
        viewModelScope.launch {
            podcastStore.unfollowPodcast(podcast.uri)
        }
    }

    fun onTogglePodcastFollowed(podcast: PodcastInfo) {
        viewModelScope.launch {
            podcastStore.togglePodcastFollowed(podcast.uri)
        }
    }

    fun onLibraryPodcastSelected(podcast: PodcastInfo?) {
        selectedLibraryPodcast.value = podcast
    }

    fun onQueueEpisode(episode: PlayerEpisode) {
        episodePlayer.addToQueue(episode)
    }
}

private fun List<EpisodeToPodcast>.asLibrary(): LibraryInfo =
    LibraryInfo(
        episodes = this.map { it.asPodcastToEpisodeInfo() }
    )

enum class HomeCategory {
    Library, Discover
}

sealed interface HomeScreenUiState {
    data object Loading : HomeScreenUiState

    data class Error(
        val errorMessage: String? = null
    ) : HomeScreenUiState

    data class Ready(
        val featuredPodcasts: PersistentList<PodcastInfo> = persistentListOf(),
        val selectedHomeCategory: HomeCategory = HomeCategory.Discover,
        val homeCategories: List<HomeCategory> = emptyList(),
        val filterableCategoriesModel: FilterableCategoriesModel =
            FilterableCategoriesModel(),
        val podcastCategoryFilterResult: PodcastCategoryFilterResult =
            PodcastCategoryFilterResult(),
        val library: LibraryInfo = LibraryInfo(),
    ) : HomeScreenUiState
}