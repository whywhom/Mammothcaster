package com.mammoth.podcast.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mammoth.podcast.data.database.dao.CategoriesDao
import com.mammoth.podcast.data.database.dao.EpisodesDao
import com.mammoth.podcast.data.database.dao.PodcastCategoryEntryDao
import com.mammoth.podcast.data.database.dao.PodcastFollowedEntryDao
import com.mammoth.podcast.data.database.dao.PodcastsDao
import com.mammoth.podcast.data.database.dao.TransactionRunnerDao
import com.mammoth.podcast.data.database.model.Category
import com.mammoth.podcast.data.database.model.Episode
import com.mammoth.podcast.data.database.model.Podcast
import com.mammoth.podcast.data.database.model.PodcastCategoryEntry
import com.mammoth.podcast.data.database.model.PodcastFollowedEntry

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
abstract class JetcasterDatabase : RoomDatabase() {
    abstract fun podcastsDao(): PodcastsDao
    abstract fun episodesDao(): EpisodesDao
    abstract fun categoriesDao(): CategoriesDao
    abstract fun podcastCategoryEntryDao(): PodcastCategoryEntryDao
    abstract fun transactionRunnerDao(): TransactionRunnerDao
    abstract fun podcastFollowedEntryDao(): PodcastFollowedEntryDao
    companion object {
        fun getInstance(context: Context): JetcasterDatabase {
            return Room.databaseBuilder(context, JetcasterDatabase::class.java, MammothDataBase)
                // This is not recommended for normal apps, but the goal of this sample isn't to
                // showcase all of Room.
                .fallbackToDestructiveMigration()
                .build()
        }

    }
}
