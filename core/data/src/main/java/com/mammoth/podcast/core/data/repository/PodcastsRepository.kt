package com.mammoth.podcast.core.data.repository

import com.mammoth.podcast.core.data.Dispatcher
import com.mammoth.podcast.core.data.MammothDispatchers
import com.mammoth.podcast.core.data.database.dao.TransactionRunner
import com.mammoth.podcast.core.data.network.MammothFeeds
import com.mammoth.podcast.core.data.network.PodcastRssResponse
import com.mammoth.podcast.core.data.network.PodcastsFetcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data repository for Podcasts.
 */
class PodcastsRepository @Inject constructor(
    private val podcastsFetcher: PodcastsFetcher,
    private val podcastStore: PodcastStore,
    private val episodeStore: EpisodeStore,
    private val categoryStore: CategoryStore,
    private val transactionRunner: TransactionRunner,
    @Dispatcher(MammothDispatchers.Main) mainDispatcher: CoroutineDispatcher
) {
    private var refreshingJob: Job? = null

    private val scope = CoroutineScope(mainDispatcher)

    suspend fun updatePodcasts(force: Boolean) {
        if (refreshingJob?.isActive == true) {
            refreshingJob?.join()
        } else if (force || podcastStore.isEmpty()) {

            refreshingJob = scope.launch {
                // Now fetch the podcasts, and add each to each store
                podcastsFetcher(MammothFeeds)
                    .filter { it is PodcastRssResponse.Success }
                    .map { it as PodcastRssResponse.Success }
                    .collect { (podcast, episodes, categories) ->
                        transactionRunner {
                            podcastStore.addPodcast(podcast)
                            episodeStore.addEpisodes(episodes)

                            categories.forEach { category ->
                                // First insert the category
                                val categoryId = categoryStore.addCategory(category)
                                // Now we can add the podcast to the category
                                categoryStore.addPodcastToCategory(
                                    podcastUri = podcast.uri,
                                    categoryId = categoryId
                                )
                            }
                        }
                    }
            }
        }
    }

    fun fetchFeed(feedUrl: String): Flow<PodcastRssResponse> {
        val list = ArrayList<String>().apply { add(feedUrl) }
        return podcastsFetcher.invoke(list)
    }
}
