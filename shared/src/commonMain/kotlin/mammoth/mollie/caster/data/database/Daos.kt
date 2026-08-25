package mammoth.mollie.caster.data.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PodcastDao {
    @Query("SELECT * FROM podcasts ORDER BY title")
    suspend fun allPodcasts(): List<PodcastEntity>

    @Query("SELECT * FROM podcasts WHERE podcast_id = :id")
    fun observePodcast(id: String): Flow<PodcastEntity?>

    @Query("SELECT * FROM podcasts ORDER BY latest_episode_at IS NULL, latest_episode_at DESC, podcast_id ASC LIMIT :limit")
    fun observeLatest(limit: Int): Flow<List<PodcastEntity>>

    @Query("SELECT p.* FROM podcasts p INNER JOIN subscriptions s ON s.podcast_id = p.podcast_id ORDER BY s.subscribed_at DESC")
    fun observeSubscribed(): Flow<List<PodcastEntity>>

    @Query("""
        SELECT * FROM podcasts
        WHERE :query != '' AND (search_title LIKE :contains ESCAPE '\' OR search_author LIKE :contains ESCAPE '\')
        ORDER BY CASE
            WHEN search_title LIKE :prefix ESCAPE '\' THEN 0
            WHEN search_title LIKE :contains ESCAPE '\' THEN 1
            ELSE 2
        END, latest_episode_at IS NULL, latest_episode_at DESC, podcast_id ASC
        LIMIT :limit
    """)
    fun search(query: String, prefix: String, contains: String, limit: Int): Flow<List<PodcastEntity>>

    @Query("SELECT podcast_id FROM feed_aliases WHERE alias_url = :url LIMIT 1")
    suspend fun resolveByFeedAlias(url: String): String?

    @Upsert
    suspend fun upsert(podcast: PodcastEntity)

    @Upsert
    suspend fun upsertAlias(alias: FeedAliasEntity)

    @Query("UPDATE podcasts SET last_refresh_at = :validatedAt WHERE podcast_id = :podcastId")
    suspend fun updateLastRefreshAt(podcastId: String, validatedAt: Long)
}

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes ORDER BY published_at IS NULL, published_at DESC")
    suspend fun allEpisodes(): List<EpisodeEntity>

    @Query("SELECT * FROM episode_enclosures ORDER BY episode_id, position")
    suspend fun allEnclosures(): List<EpisodeEnclosureEntity>

    /** Startup must not materialize an unbounded episode catalog on constrained devices. */
    @Query("SELECT * FROM episodes ORDER BY published_at IS NULL, published_at DESC LIMIT :limit")
    suspend fun latestForRestore(limit: Int): List<EpisodeEntity>

    @Query("SELECT * FROM episode_enclosures WHERE episode_id IN (:episodeIds) ORDER BY episode_id, position")
    suspend fun enclosuresFor(episodeIds: List<String>): List<EpisodeEnclosureEntity>

    @Query("SELECT * FROM episodes WHERE episode_id = :id")
    fun observeEpisode(id: String): Flow<EpisodeEntity?>

    @Query("SELECT * FROM episodes WHERE podcast_id = :podcastId ORDER BY published_at IS NULL, published_at DESC, episode_id ASC")
    fun observeNewest(podcastId: String): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE podcast_id = :podcastId ORDER BY published_at IS NULL, published_at ASC, episode_id ASC")
    fun observeOldest(podcastId: String): Flow<List<EpisodeEntity>>

    @Query("SELECT e.* FROM episodes e INNER JOIN subscriptions s ON s.podcast_id = e.podcast_id ORDER BY e.published_at IS NULL, e.published_at DESC LIMIT :limit")
    fun observeLatestForSubscribed(limit: Int): Flow<List<EpisodeEntity>>

    @Upsert
    suspend fun upsertAll(episodes: List<EpisodeEntity>)

    @Query("DELETE FROM episode_enclosures WHERE episode_id IN (:episodeIds)")
    suspend fun deleteEnclosures(episodeIds: List<String>)

    @Upsert
    suspend fun insertEnclosures(enclosures: List<EpisodeEnclosureEntity>)
}

@Dao
interface UserDataDao {
    @Query("SELECT * FROM favorites")
    suspend fun allFavorites(): List<FavoriteEntity>

    @Query("SELECT * FROM subscriptions")
    suspend fun allSubscriptions(): List<SubscriptionEntity>

    @Query("SELECT * FROM playback_history ORDER BY last_played_at DESC")
    suspend fun allHistory(): List<PlaybackHistoryEntity>

    @Query("SELECT * FROM playback_history ORDER BY last_played_at DESC LIMIT :limit")
    suspend fun recentHistoryForRestore(limit: Int): List<PlaybackHistoryEntity>

    @Query("SELECT * FROM favorites ORDER BY created_at DESC")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(value: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE episode_id = :episodeId")
    suspend fun removeFavorite(episodeId: String)

    @Query("SELECT * FROM subscriptions ORDER BY subscribed_at DESC")
    fun observeSubscriptions(): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSubscription(value: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE podcast_id = :podcastId")
    suspend fun removeSubscription(podcastId: String)

    @Query("SELECT * FROM playback_history ORDER BY last_played_at DESC LIMIT :limit")
    fun observeHistory(limit: Int): Flow<List<PlaybackHistoryEntity>>

    @Query("""
        INSERT INTO playback_history(episode_id,last_played_at,position_ms,duration_snapshot_ms,total_played_ms,completed,completed_at,updated_at)
        VALUES(:episodeId,:lastPlayedAt,:positionMs,:durationMs,:totalPlayedMs,:completed,:completedAt,:updatedAt)
        ON CONFLICT(episode_id) DO UPDATE SET
          last_played_at=excluded.last_played_at,
          position_ms=excluded.position_ms,
          duration_snapshot_ms=excluded.duration_snapshot_ms,
          total_played_ms=excluded.total_played_ms,
          completed=excluded.completed,
          completed_at=excluded.completed_at,
          updated_at=excluded.updated_at
        WHERE excluded.updated_at >= playback_history.updated_at
    """)
    suspend fun recordPlayback(
        episodeId: String,
        lastPlayedAt: Long,
        positionMs: Long,
        durationMs: Long?,
        totalPlayedMs: Long,
        completed: Boolean,
        completedAt: Long?,
        updatedAt: Long,
    )
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY requested_at DESC")
    suspend fun allDownloads(): List<DownloadEntity>

    @Query("SELECT * FROM downloads ORDER BY requested_at DESC")
    fun observeAll(): Flow<List<DownloadEntity>>

    @Upsert
    suspend fun upsert(download: DownloadEntity)

    @Query("UPDATE downloads SET state=:newState, updated_at=:updatedAt WHERE episode_id=:episodeId AND state=:expectedState")
    suspend fun compareAndSetState(episodeId: String, expectedState: String, newState: String, updatedAt: Long): Int

    @Query("DELETE FROM downloads WHERE episode_id = :episodeId")
    suspend fun delete(episodeId: String)

    @Query("DELETE FROM downloads")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(downloads: List<DownloadEntity>) {
        clearAll()
        downloads.forEach { upsert(it) }
    }
}

@Dao
interface LocalPlaylistDao {
    @Query("SELECT * FROM local_playlists ORDER BY is_pinned DESC, name COLLATE NOCASE ASC, playlist_id ASC")
    suspend fun allPlaylists(): List<LocalPlaylistEntity>

    @Query("SELECT * FROM local_playlist_items ORDER BY playlist_id ASC, position ASC")
    suspend fun allItems(): List<LocalPlaylistItemEntity>

    @Upsert
    suspend fun upsert(playlist: LocalPlaylistEntity)

    @Upsert
    suspend fun upsertItems(items: List<LocalPlaylistItemEntity>)

    @Query("UPDATE local_playlists SET name = :name WHERE playlist_id = :playlistId")
    suspend fun rename(playlistId: String, name: String)

    @Query("UPDATE local_playlists SET is_pinned = :pinned WHERE playlist_id = :playlistId")
    suspend fun setPinned(playlistId: String, pinned: Boolean)

    @Query("DELETE FROM local_playlist_items WHERE playlist_id = :playlistId")
    suspend fun deleteItems(playlistId: String)

    @Query("DELETE FROM local_playlists WHERE playlist_id = :playlistId")
    suspend fun delete(playlistId: String)

    @Transaction
    suspend fun add(playlist: LocalPlaylistEntity, items: List<LocalPlaylistItemEntity>) {
        upsert(playlist)
        if (items.isNotEmpty()) upsertItems(items)
    }

    @Transaction
    suspend fun replace(playlistId: String, name: String, items: List<LocalPlaylistItemEntity>) {
        rename(playlistId, name)
        deleteItems(playlistId)
        if (items.isNotEmpty()) upsertItems(items)
    }
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    suspend fun allCategories(): List<CategoryEntity>

    @Query("SELECT * FROM podcast_categories")
    suspend fun allMappings(): List<PodcastCategoryEntity>
}

@Dao
interface SyncDao {
    @Upsert
    suspend fun upsertState(state: FeedSyncStateEntity)

    @Query("SELECT * FROM feed_sync_state WHERE podcast_id=:podcastId")
    suspend fun state(podcastId: String): FeedSyncStateEntity?
}

@Dao
interface FeedStoreDao {
    @Upsert
    suspend fun upsertPodcast(podcast: PodcastEntity)

    @Upsert
    suspend fun upsertAliases(aliases: List<FeedAliasEntity>)

    @Upsert
    suspend fun upsertEpisodes(episodes: List<EpisodeEntity>)

    @Query("DELETE FROM episode_enclosures WHERE episode_id IN (:episodeIds)")
    suspend fun deleteEnclosures(episodeIds: List<String>)

    @Upsert
    suspend fun upsertEnclosures(enclosures: List<EpisodeEnclosureEntity>)

    @Upsert
    suspend fun upsertCategories(categories: List<CategoryEntity>)

    @Query("DELETE FROM podcast_categories WHERE podcast_id = :podcastId")
    suspend fun deleteCategoryMappings(podcastId: String)

    @Upsert
    suspend fun upsertCategoryMappings(mappings: List<PodcastCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun subscribe(subscription: SubscriptionEntity)

    @Transaction
    suspend fun replaceFeed(
        podcast: PodcastEntity,
        aliases: List<FeedAliasEntity>,
        episodes: List<EpisodeEntity>,
        enclosures: List<EpisodeEnclosureEntity>,
        categories: List<CategoryEntity>,
        categoryMappings: List<PodcastCategoryEntity>,
        subscription: SubscriptionEntity,
    ) {
        upsertPodcast(podcast)
        upsertAliases(aliases)
        upsertEpisodes(episodes)
        if (episodes.isNotEmpty()) deleteEnclosures(episodes.map { it.episodeId })
        if (enclosures.isNotEmpty()) upsertEnclosures(enclosures)
        if (categories.isNotEmpty()) upsertCategories(categories)
        deleteCategoryMappings(podcast.podcastId)
        if (categoryMappings.isNotEmpty()) upsertCategoryMappings(categoryMappings)
        subscribe(subscription)
    }
}
