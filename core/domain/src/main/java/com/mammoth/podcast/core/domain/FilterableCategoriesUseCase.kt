package com.mammoth.podcast.core.domain

import com.mammoth.podcast.core.data.repository.CategoryStore
import com.mammoth.podcast.core.model.CategoryInfo
import com.mammoth.podcast.core.model.FilterableCategoriesModel
import com.mammoth.podcast.core.model.asExternalModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for categories that can be used to filter podcasts.
 */
class FilterableCategoriesUseCase @Inject constructor(
    private val categoryStore: CategoryStore
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
