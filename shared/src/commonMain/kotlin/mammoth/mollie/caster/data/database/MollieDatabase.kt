package mammoth.mollie.caster.data.database

import androidx.room3.ConstructedBy
import androidx.room3.AutoMigration
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

@Database(
    entities = [
        PodcastEntity::class,
        FeedAliasEntity::class,
        FeedSyncStateEntity::class,
        EpisodeEntity::class,
        EpisodeEnclosureEntity::class,
        CategoryEntity::class,
        PodcastCategoryEntity::class,
        SubscriptionEntity::class,
        FavoriteEntity::class,
        PlaybackHistoryEntity::class,
        DownloadEntity::class,
        LocalPlaylistEntity::class,
        LocalPlaylistItemEntity::class,
    ],
    version = 3,
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3)],
    exportSchema = true,
)
@ConstructedBy(MollieDatabaseConstructor::class)
abstract class MollieDatabase : RoomDatabase() {
    abstract fun podcastDao(): PodcastDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun userDataDao(): UserDataDao
    abstract fun downloadDao(): DownloadDao
    abstract fun syncDao(): SyncDao
    abstract fun feedStoreDao(): FeedStoreDao
    abstract fun categoryDao(): CategoryDao
    abstract fun localPlaylistDao(): LocalPlaylistDao
}

@Suppress("KotlinNoActualForExpect")
expect object MollieDatabaseConstructor : RoomDatabaseConstructor<MollieDatabase> {
    override fun initialize(): MollieDatabase
}

fun buildDatabase(builder: RoomDatabase.Builder<MollieDatabase>): MollieDatabase = builder.build()
