package com.mammoth.podcast.data.repository

import com.mammoth.podcast.MammothCastApp
import com.mammoth.podcast.data.database.dao.TransactionRunner
import com.mammoth.podcast.data.network.PodcastRssResponse
import com.mammoth.podcast.data.network.PodcastsFetcher
import com.mammoth.podcast.data.network.MammothFeeds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Data repository for Podcasts.
 */
class PodcastsRepository(
    private val podcastsFetcher: PodcastsFetcher = PodcastsFetcher(),
    private val podcastStore: PodcastStore = MammothCastApp.podcastStore,
    private val episodeStore: EpisodeStore = MammothCastApp.episodeStore,
    private val categoryStore: CategoryStore = MammothCastApp.categoryStore,
    private val transactionRunner: TransactionRunner = MammothCastApp.transactionRunner,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
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
}
