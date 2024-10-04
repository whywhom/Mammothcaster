/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mammoth.caster.ui.library

/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mammoth.caster.core.data.database.model.PodcastWithExtraInfo
import com.mammoth.caster.core.data.repository.CategoryStore
import com.mammoth.caster.core.data.repository.EpisodeStore
import com.mammoth.caster.core.data.repository.PodcastStore
import com.mammoth.caster.core.data.repository.PodcastsRepository
import com.mammoth.caster.core.domain.PodcastCategoryFilterUseCase
import com.mammoth.caster.core.model.CategoryTechnology
import com.mammoth.caster.core.model.PodcastInfo
import com.mammoth.caster.core.model.asExternalModel
import com.mammoth.caster.core.player.EpisodePlayer
import com.mammoth.caster.core.player.model.PlayerEpisode
import com.mammoth.caster.core.player.model.toPlayerEpisode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val podcastsRepository: PodcastsRepository,
    private val episodeStore: EpisodeStore,
    private val podcastStore: PodcastStore,
    private val episodePlayer: EpisodePlayer,
    private val categoryStore: CategoryStore,
    private val podcastCategoryFilterUseCase: PodcastCategoryFilterUseCase
) : ViewModel() {

    private val defaultCategory = categoryStore.getCategory(CategoryTechnology)
    private val topPodcastsFlow = defaultCategory.flatMapLatest {
        podcastCategoryFilterUseCase(it?.asExternalModel())
    }

    private val followingPodcastListFlow = podcastStore.followedPodcastsSortedByLastEpisode()

    private val queue = episodePlayer.playerState.map {
        it.queue
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val latestEpisodeListFlow = podcastStore
        .followedPodcastsSortedByLastEpisode()
        .flatMapLatest { podcastList ->
            if (podcastList.isNotEmpty()) {
                combine(podcastList.map { episodeStore.episodesInPodcast(it.podcast.uri, 1) }) {
                    it.map { episodes ->
                        episodes.first()
                    }
                }
            } else {
                flowOf(emptyList())
            }
        }.map { list ->
            (list.map { it.toPlayerEpisode() })
        }

    val uiState =
        combine(
            topPodcastsFlow,
            followingPodcastListFlow,
            latestEpisodeListFlow,
            queue
        ) { topPodcasts, podcastList, episodeList, queue ->
            if (podcastList.isEmpty()) {
                LibraryScreenUiState.NoSubscribedPodcast(topPodcasts.topPodcasts)
            } else {
                LibraryScreenUiState.Ready(podcastList, episodeList, queue)
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LibraryScreenUiState.Loading
        )

    init {
        viewModelScope.launch {
            podcastsRepository.updatePodcasts(false)
        }
    }

    fun playEpisode(playerEpisode: PlayerEpisode) {
        episodePlayer.play(playerEpisode)
    }

    fun onTogglePodcastFollowed(podcastUri: String) {
        viewModelScope.launch {
            podcastStore.togglePodcastFollowed(podcastUri)
        }
    }
}

sealed interface LibraryScreenUiState {
    data object Loading : LibraryScreenUiState
    data class NoSubscribedPodcast(
        val topPodcasts: List<PodcastInfo>
    ) : LibraryScreenUiState
    data class Ready(
        val subscribedPodcastList: List<PodcastWithExtraInfo>,
        val latestEpisodeList: List<PlayerEpisode>,
        val queue: List<PlayerEpisode>
    ) : LibraryScreenUiState
}
