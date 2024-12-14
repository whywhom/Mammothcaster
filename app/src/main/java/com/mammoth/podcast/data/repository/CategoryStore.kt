package com.mammoth.podcast.data.repository

import com.mammoth.podcast.MammothCastApp
import com.mammoth.podcast.data.database.dao.CategoriesDao
import com.mammoth.podcast.data.database.dao.EpisodesDao
import com.mammoth.podcast.data.database.dao.PodcastCategoryEntryDao
import com.mammoth.podcast.data.database.dao.PodcastsDao
import com.mammoth.podcast.data.database.model.Category
import com.mammoth.podcast.data.database.model.EpisodeToPodcast
import com.mammoth.podcast.data.database.model.PodcastCategoryEntry
import com.mammoth.podcast.data.database.model.PodcastWithExtraInfo
import kotlinx.coroutines.flow.Flow

interface CategoryStore {
    /**
     * Returns a flow containing a list of categories which is sorted by the number
     * of podcasts in each category.
     */
    fun categoriesSortedByPodcastCount(
        limit: Int = Integer.MAX_VALUE
    ): Flow<List<Category>>

    /**
     * Returns a flow containing a list of podcasts in the category with the given [categoryId],
     * sorted by the their last episode date.
     */
    fun podcastsInCategorySortedByPodcastCount(
        categoryId: Long,
        limit: Int = Int.MAX_VALUE
    ): Flow<List<PodcastWithExtraInfo>>

    /**
     * Returns a flow containing a list of episodes from podcasts in the category with the
     * given [categoryId], sorted by the their last episode date.
     */
    fun episodesFromPodcastsInCategory(
        categoryId: Long,
        limit: Int = Integer.MAX_VALUE
    ): Flow<List<EpisodeToPodcast>>

    /**
     * Adds the category to the database if it doesn't already exist.
     *
     * @return the id of the newly inserted/existing category
     */
    suspend fun addCategory(category: Category): Long

    suspend fun addPodcastToCategory(podcastUri: String, categoryId: Long)

    /**
     * @return gets the category with [name], if it exists, otherwise, null
     */
    fun getCategory(name: String): Flow<Category?>
}

/**
 * A data repository for [Category] instances.
 */
class LocalCategoryStore constructor(
    private val categoriesDao: CategoriesDao = MammothCastApp.categoriesDao,
    private val categoryEntryDao: PodcastCategoryEntryDao = MammothCastApp.podcastCategoryEntryDao,
    private val episodesDao: EpisodesDao = MammothCastApp.episodesDao,
    private val podcastsDao: PodcastsDao = MammothCastApp.podcastsDao
) : CategoryStore {
    /**
     * Returns a flow containing a list of categories which is sorted by the number
     * of podcasts in each category.
     */
    override fun categoriesSortedByPodcastCount(limit: Int): Flow<List<Category>> {
        return categoriesDao.categoriesSortedByPodcastCount(limit)
    }

    /**
     * Returns a flow containing a list of podcasts in the category with the given [categoryId],
     * sorted by the their last episode date.
     */
    override fun podcastsInCategorySortedByPodcastCount(
        categoryId: Long,
        limit: Int
    ): Flow<List<PodcastWithExtraInfo>> {
        return podcastsDao.podcastsInCategorySortedByLastEpisode(categoryId, limit)
    }

    /**
     * Returns a flow containing a list of episodes from podcasts in the category with the
     * given [categoryId], sorted by the their last episode date.
     */
    override fun episodesFromPodcastsInCategory(
        categoryId: Long,
        limit: Int
    ): Flow<List<EpisodeToPodcast>> {
        return episodesDao.episodesFromPodcastsInCategory(categoryId, limit)
    }

    /**
     * Adds the category to the database if it doesn't already exist.
     *
     * @return the id of the newly inserted/existing category
     */
    override suspend fun addCategory(category: Category): Long {
        return when (val local = categoriesDao.getCategoryWithName(category.name)) {
            null -> categoriesDao.insert(category)
            else -> local.id
        }
    }

    override suspend fun addPodcastToCategory(podcastUri: String, categoryId: Long) {
        categoryEntryDao.insert(
            PodcastCategoryEntry(podcastUri = podcastUri, categoryId = categoryId)
        )
    }

    override fun getCategory(name: String): Flow<Category?> =
        categoriesDao.observeCategory(name)
}
