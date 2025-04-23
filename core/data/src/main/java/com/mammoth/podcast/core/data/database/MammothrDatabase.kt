package com.mammoth.podcast.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mammoth.podcast.core.data.database.dao.CategoriesDao
import com.mammoth.podcast.core.data.database.dao.EpisodesDao
import com.mammoth.podcast.core.data.database.dao.PodcastCategoryEntryDao
import com.mammoth.podcast.core.data.database.dao.PodcastFollowedEntryDao
import com.mammoth.podcast.core.data.database.dao.PodcastsDao
import com.mammoth.podcast.core.data.database.dao.TransactionRunnerDao
import com.mammoth.podcast.core.data.database.model.Category
import com.mammoth.podcast.core.data.database.model.Episode
import com.mammoth.podcast.core.data.database.model.Podcast
import com.mammoth.podcast.core.data.database.model.PodcastCategoryEntry
import com.mammoth.podcast.core.data.database.model.PodcastFollowedEntry

const val MammothDataBase = "data.db"
/**
 * The [RoomDatabase] we use in this app.
 */
@Database(
    entities = [
        Podcast::class,
        Episode::class,
        PodcastCategoryEntry::class,
        Category::class,
        PodcastFollowedEntry::class
    ],
    version = 1,
    exportSchema = false
)

@TypeConverters(DateTimeTypeConverters::class)
abstract class MammothDatabase : RoomDatabase() {
    abstract fun podcastsDao(): PodcastsDao
    abstract fun episodesDao(): EpisodesDao
    abstract fun categoriesDao(): CategoriesDao
    abstract fun podcastCategoryEntryDao(): PodcastCategoryEntryDao
    abstract fun transactionRunnerDao(): TransactionRunnerDao
    abstract fun podcastFollowedEntryDao(): PodcastFollowedEntryDao
}
