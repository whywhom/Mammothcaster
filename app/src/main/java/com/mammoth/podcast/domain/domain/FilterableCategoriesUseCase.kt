package com.mammoth.podcast.domain.domain

import com.mammoth.podcast.MammothCastApp
import com.mammoth.podcast.data.repository.CategoryStore
import com.mammoth.podcast.domain.model.CategoryInfo
import com.mammoth.podcast.domain.model.FilterableCategoriesModel
import com.mammoth.podcast.domain.model.asExternalModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case for categories that can be used to filter podcasts.
 */
class FilterableCategoriesUseCase(
    private val categoryStore: CategoryStore = MammothCastApp.categoryStore
) {
    /**
     * Created a [FilterableCategoriesModel] from the list of categories in [categoryStore].
     * @param selectedCategory the currently selected category. If null, the first category
     *        returned by the backing category list will be selected in the returned
     *        FilterableCategoriesModel
     */
    operator fun invoke(selectedCategory: CategoryInfo?): Flow<FilterableCategoriesModel> =
        categoryStore.categoriesSortedByPodcastCount()
            .map { categories ->
                FilterableCategoriesModel(
                    categories = categories.map { it.asExternalModel() },
                    selectedCategory = selectedCategory
                        ?: categories.firstOrNull()?.asExternalModel()
                )
            }
}
