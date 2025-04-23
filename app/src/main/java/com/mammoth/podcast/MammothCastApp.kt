package com.mammoth.podcast

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

/**
 * Application which sets up our dependency with a context.
 */
@HiltAndroidApp
class MammothCastApp : Application() {

    companion object {
        var shouldRefresh: Boolean = false
        lateinit var appContext: Context
    }
    override fun onCreate() {
        super.onCreate()
        appContext = this@MammothCastApp.applicationContext
//        init()
    }

//    private fun init() {
//        shouldRefresh = AppTimeChecker(this).checkIfOverTime()
//        database = MammothDatabase.getInstance(this)
//        categoriesDao = database.categoriesDao()
//        podcastCategoryEntryDao = database.podcastCategoryEntryDao()
//        podcastsDao = database.podcastsDao()
//        episodesDao = database.episodesDao()
//        podcastFollowedEntryDao = database.podcastFollowedEntryDao()
//        transactionRunner = database.transactionRunnerDao()
//        syndFeedInput = SyndFeedInput()
//        episodeStore = LocalEpisodeStore(episodesDao)
//        podcastStore = LocalPodcastStore(
//            podcastDao = podcastsDao,
//            podcastFollowedEntryDao = podcastFollowedEntryDao,
//            transactionRunner = transactionRunner
//        )
//        categoryStore = LocalCategoryStore(
//            episodesDao = episodesDao,
//            podcastsDao = podcastsDao,
//            categoriesDao = categoriesDao,
//            categoryEntryDao = podcastCategoryEntryDao,
//        )
//        imageLoader = ImageLoader.Builder(this)
//            // Disable `Cache-Control` header support as some podcast images disable disk caching.
//            .respectCacheHeaders(false)
//            .build()
//        episodePlayer = MammothEpisodePlayer(this@MammothCastApp.applicationContext, Dispatchers.Main)
//    }
}
