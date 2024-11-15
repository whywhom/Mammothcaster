package com.mammoth.podcast.domain.domain

import com.mammoth.podcast.MammothCastApp
import com.mammoth.podcast.data.database.model.Category
import com.mammoth.podcast.data.repository.CategoryStore
import com.mammoth.podcast.domain.model.CategoryInfo
import com.mammoth.podcast.domain.model.PodcastCategoryFilterResult
import com.mammoth.podcast.domain.model.asExternalModel
import com.mammoth.podcast.domain.model.asPodcastToEpisodeInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

/**
 *  A use case which returns top podcasts and matching episodes in a given [Category].
 */
class PodcastCategoryFilterUseCase(
    private val categoryStore: CategoryStore = MammothCastApp.categoryStore
) {
    operator fun invoke(category: CategoryInfo?): Flow<PodcastCategoryFilterResult> {
        if (category == null) {
            return flowOf(PodcastCategoryFilterResult())
        }

        val recentPodcastsFlow = categoryStore.podcastsInCategorySortedByPodcastCount(
            category.id,
            limit = 10
        )

        val episodesFlow = categoryStore.episodesFromPodcastsInCategory(
            category.id,
            limit = 20
        )

        // Combine our flows and collect them into the view state StateFlow
        return combine(recentPodcastsFlow, episodesFlow) { topPodcasts, episodes ->
            PodcastCategoryFilterResult(
                topPodcasts = topPodcasts.map { it.asExternalModel() },
                episodes = episodes.map { it.asPodcastToEpisodeInfo() }
            )
        }
    }
}
