package com.mammoth.podcast.core.data.repository

import android.util.Log
import com.mammoth.podcast.core.data.database.dao.PodcastFollowedEntryDao
import com.mammoth.podcast.core.data.database.dao.PodcastsDao
import com.mammoth.podcast.core.data.database.dao.TransactionRunner
import com.mammoth.podcast.core.data.database.model.Category
import com.mammoth.podcast.core.data.database.model.Podcast
import com.mammoth.podcast.core.data.database.model.PodcastFollowedEntry
import com.mammoth.podcast.core.data.database.model.PodcastWithExtraInfo
import kotlinx.coroutines.flow.Flow

interface PodcastStore {
    /**
     * Return a flow containing the [Podcast] with the given [uri].
     */
    fun podcastWithUri(uri: String): Flow<Podcast>

    /**
     * Return a flow containing the [PodcastWithExtraInfo] with the given [podcastUri].
     */
    fun podcastWithExtraInfo(podcastUri: String): Flow<PodcastWithExtraInfo>

    /**
     * Returns a flow containing the entire collection of podcasts, sorted by the last episode
     * publish date for each podcast.
     */
    fun podcastsSortedByLastEpisode(
        limit: Int = Int.MAX_VALUE
    ): Flow<List<PodcastWithExtraInfo>>

    /**
     * Returns a flow containing a list of all followed podcasts, sorted by the their last
     * episode date.
     */
    fun followedPodcastsSortedByLastEpisode(
        limit: Int = Int.MAX_VALUE
    ): Flow<List<PodcastWithExtraInfo>>

    /**
     * Returns a flow containing a list of podcasts such that its name partially matches
     * with the specified keyword
     */
    fun searchPodcastByTitle(
        keyword: String,
        limit: Int = Int.MAX_VALUE
    ): Flow<List<PodcastWithExtraInfo>>

    /**
     * Return a flow containing a list of podcast such that it belongs to the any of categories
     * specified with categories parameter and its name partially matches with the specified
     * keyword.
     */
    fun searchPodcastByTitleAndCategories(
        keyword: String,
        categories: List<Category>,
        limit: Int = Int.MAX_VALUE
    ): Flow<List<PodcastWithExtraInfo>>

    suspend fun togglePodcastFollowed(podcastUri: String)

    suspend fun followPodcast(podcastUri: String)

    suspend fun unfollowPodcast(podcastUri: String)

    /**
     * Add a new [Podcast] to this store.
     *
     * This automatically switches to the main thread to maintain thread consistency.
     */
    suspend fun addPodcast(podcast: Podcast)

    suspend fun isEmpty(): Boolean
}

/**
 * A com.mammoth.podcast.core.data repository for [Podcast] instances.
 */
class LocalPodcastStore(
    private val podcastDao: PodcastsDao,
    private val podcastFollowedEntryDao: PodcastFollowedEntryDao,
    private val transactionRunner: TransactionRunner
) : PodcastStore {
    /**
     * Return a flow containing the [Podcast] with the given [uri].
     */
    override fun podcastWithUri(uri: String): Flow<Podcast> {
        return podcastDao.podcastWithUri(uri)
    }

    /**
     * Return a flow containing the [PodcastWithExtraInfo] with the given [podcastUri].
     */
    override fun podcastWithExtraInfo(podcastUri: String): Flow<PodcastWithExtraInfo> =
        podcastDao.podcastWithExtraInfo(podcastUri)

    /**
     * Returns a flow containing the entire collection of podcasts, sorted by the last episode
     * publish date for each podcast.
     */
    override fun podcastsSortedByLastEpisode(
        limit: Int
    ): Flow<List<PodcastWithExtraInfo>> {
        return podcastDao.podcastsSortedByLastEpisode(limit)
    }

    /**
     * Returns a flow containing a list of all followed podcasts, sorted by the their last
     * episode date.
     */
    override fun followedPodcastsSortedByLastEpisode(
        limit: Int
    ): Flow<List<PodcastWithExtraInfo>> {
        return podcastDao.followedPodcastsSortedByLastEpisode(limit)
    }

    override fun searchPodcastByTitle(
        keyword: String,
        limit: Int
    ): Flow<List<PodcastWithExtraInfo>> {
        return podcastDao.searchPodcastByTitle(keyword, limit)
    }

    override fun searchPodcastByTitleAndCategories(
        keyword: String,
        categories: List<Category>,
        limit: Int
    ): Flow<List<PodcastWithExtraInfo>> {
        val categoryIdList = categories.map { it.id }
        return podcastDao.searchPodcastByTitleAndCategory(keyword, categoryIdList, limit)
    }

    override suspend fun followPodcast(podcastUri: String) {
        podcastFollowedEntryDao.insert(PodcastFollowedEntry(podcastUri = podcastUri))
    }

    override suspend fun togglePodcastFollowed(podcastUri: String) = transactionRunner {
        if (podcastFollowedEntryDao.isPodcastFollowed(podcastUri)) {
            unfollowPodcast(podcastUri)
        } else {
            followPodcast(podcastUri)
        }
    }

    override suspend fun unfollowPodcast(podcastUri: String) {
        podcastFollowedEntryDao.deleteWithPodcastUri(podcastUri)
    }

    /**
     * Add a new [Podcast] to this store.
     *
     * This automatically switches to the main thread to maintain thread consistency.
     */
    override suspend fun addPodcast(podcast: Podcast) {
        podcastDao.insert(podcast)
    }

    override suspend fun isEmpty(): Boolean {
        Log.d(this::class.simpleName, "podcastDao.count() = ${podcastDao.count()}")
        return podcastDao.count() == 0
    }
}
