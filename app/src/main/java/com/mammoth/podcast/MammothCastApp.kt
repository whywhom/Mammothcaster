package com.mammoth.podcast

import android.app.Application
import coil.ImageLoader
import com.mammoth.podcast.data.repository.CategoryStore
import com.mammoth.podcast.data.repository.EpisodeStore
import com.mammoth.podcast.data.repository.LocalCategoryStore
import com.mammoth.podcast.data.repository.LocalEpisodeStore
import com.mammoth.podcast.data.repository.LocalPodcastStore
import com.mammoth.podcast.data.repository.PodcastStore
import com.mammoth.podcast.data.database.JetcasterDatabase
import com.mammoth.podcast.data.database.dao.CategoriesDao
import com.mammoth.podcast.data.database.dao.EpisodesDao
import com.mammoth.podcast.data.database.dao.PodcastCategoryEntryDao
import com.mammoth.podcast.data.database.dao.PodcastFollowedEntryDao
import com.mammoth.podcast.data.database.dao.PodcastsDao
import com.mammoth.podcast.data.database.dao.TransactionRunner
import com.rometools.rome.io.SyndFeedInput
import okhttp3.OkHttpClient

/**
 * Application which sets up our dependency [Graph] with a context.
 */
class MammothCastApp : Application() {
    companion object {
        lateinit var database: JetcasterDatabase
        lateinit var categoriesDao: CategoriesDao
        lateinit var podcastCategoryEntryDao: PodcastCategoryEntryDao
        lateinit var podcastsDao: PodcastsDao
        lateinit var episodesDao: EpisodesDao
        lateinit var podcastFollowedEntryDao: PodcastFollowedEntryDao
        lateinit var transactionRunner: TransactionRunner
        lateinit var syndFeedInput: SyndFeedInput
        lateinit var episodeStore: EpisodeStore
        lateinit var podcastStore: PodcastStore
        lateinit var categoryStore: CategoryStore
        lateinit var imageLoader: ImageLoader
        val okHttpClient = OkHttpClient()
    }
    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        database = JetcasterDatabase.getInstance(this)
        categoriesDao = database.categoriesDao()
        podcastCategoryEntryDao = database.podcastCategoryEntryDao()
        podcastsDao = database.podcastsDao()
        episodesDao = database.episodesDao()
        podcastFollowedEntryDao = database.podcastFollowedEntryDao()
        transactionRunner = database.transactionRunnerDao()
        syndFeedInput = SyndFeedInput()
        episodeStore = LocalEpisodeStore(episodesDao)
        podcastStore = LocalPodcastStore(
            podcastDao = podcastsDao,
            podcastFollowedEntryDao = podcastFollowedEntryDao,
            transactionRunner = transactionRunner
        )
        categoryStore = LocalCategoryStore(
            episodesDao = episodesDao,
            podcastsDao = podcastsDao,
            categoriesDao = categoriesDao,
            categoryEntryDao = podcastCategoryEntryDao,
        )
        imageLoader = ImageLoader.Builder(this)
            // Disable `Cache-Control` header support as some podcast images disable disk caching.
            .respectCacheHeaders(false)
            .build()
    }
}
