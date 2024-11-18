package com.mammoth.podcast.domain.model

/**
 * Model holding a list of categories and a selected category in the collection
 */
data class FilterableCategoriesModel(
    val categories: List<CategoryInfo> = emptyList(),
    val selectedCategory: CategoryInfo? = null
) {
    val isEmpty = categories.isEmpty() || selectedCategory == null
}
