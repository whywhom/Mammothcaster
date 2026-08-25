package mammoth.mollie.caster.data.database

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

@Entity(
    tableName = "podcasts",
    indices = [Index("feed_url", unique = true), Index("latest_episode_at"), Index("search_title"), Index("search_author")],
    primaryKeys = ["podcast_id"],
)
data class PodcastEntity(
    @ColumnInfo(name = "podcast_id") val podcastId: String,
    @ColumnInfo(name = "feed_url") val feedUrl: String,
    @ColumnInfo(name = "canonical_feed_url") val canonicalFeedUrl: String,
    @ColumnInfo(name = "website_url") val websiteUrl: String?,
    val title: String,
    val author: String,
    val description: String,
    @ColumnInfo(name = "artwork_url") val artworkUrl: String?,
    val language: String?,
    val copyright: String?,
    @ColumnInfo(name = "is_explicit") val isExplicit: Boolean,
    @ColumnInfo(name = "search_title") val searchTitle: String,
    @ColumnInfo(name = "search_author") val searchAuthor: String,
    @ColumnInfo(name = "episode_count") val episodeCount: Int,
    @ColumnInfo(name = "latest_episode_at") val latestEpisodeAt: Long?,
    @ColumnInfo(name = "last_refresh_at") val lastRefreshAt: Long?,
)

@Entity(
    tableName = "feed_aliases",
    foreignKeys = [ForeignKey(
        entity = PodcastEntity::class,
        parentColumns = ["podcast_id"],
        childColumns = ["podcast_id"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("podcast_id")],
    primaryKeys = ["alias_url"],
)
data class FeedAliasEntity(
    @ColumnInfo(name = "alias_url") val aliasUrl: String,
    @ColumnInfo(name = "podcast_id") val podcastId: String,
)

@Entity(
    tableName = "feed_sync_state",
    foreignKeys = [ForeignKey(
        entity = PodcastEntity::class,
        parentColumns = ["podcast_id"],
        childColumns = ["podcast_id"],
        onDelete = ForeignKey.CASCADE,
    )],
    primaryKeys = ["podcast_id"],
)
data class FeedSyncStateEntity(
    @ColumnInfo(name = "podcast_id") val podcastId: String,
    val etag: String?,
    @ColumnInfo(name = "last_modified") val lastModified: String?,
    @ColumnInfo(name = "last_attempt_at") val lastAttemptAt: Long,
    @ColumnInfo(name = "last_success_at") val lastSuccessAt: Long?,
    @ColumnInfo(name = "last_http_status") val lastHttpStatus: Int?,
    @ColumnInfo(name = "failure_code") val failureCode: String?,
    @ColumnInfo(name = "failure_message") val failureMessage: String?,
)

@Entity(
    tableName = "episodes",
    foreignKeys = [ForeignKey(
        entity = PodcastEntity::class,
        parentColumns = ["podcast_id"],
        childColumns = ["podcast_id"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("podcast_id", "published_at", "episode_id"), Index("guid")],
    primaryKeys = ["episode_id"],
)
data class EpisodeEntity(
    @ColumnInfo(name = "episode_id") val episodeId: String,
    @ColumnInfo(name = "podcast_id") val podcastId: String,
    val guid: String?,
    @ColumnInfo(name = "permalink_url") val permalinkUrl: String?,
    val title: String,
    val subtitle: String,
    @ColumnInfo(name = "summary_text") val summaryText: String,
    @ColumnInfo(name = "description_html") val descriptionHtml: String,
    val author: String,
    @ColumnInfo(name = "published_at") val publishedAt: Long?,
    @ColumnInfo(name = "duration_ms") val durationMs: Long?,
    @ColumnInfo(name = "artwork_url") val artworkUrl: String?,
    @ColumnInfo(name = "season_number") val seasonNumber: Int?,
    @ColumnInfo(name = "episode_number") val episodeNumber: Int?,
    @ColumnInfo(name = "episode_type") val episodeType: String?,
    @ColumnInfo(name = "is_explicit") val isExplicit: Boolean,
    @ColumnInfo(name = "last_seen_sync_id") val lastSeenSyncId: String?,
)

@Entity(
    tableName = "episode_enclosures",
    foreignKeys = [ForeignKey(
        entity = EpisodeEntity::class,
        parentColumns = ["episode_id"],
        childColumns = ["episode_id"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("episode_id")],
    primaryKeys = ["episode_id", "position"],
)
data class EpisodeEnclosureEntity(
    @ColumnInfo(name = "episode_id") val episodeId: String,
    val position: Int,
    val url: String,
    @ColumnInfo(name = "mime_type") val mimeType: String?,
    @ColumnInfo(name = "length_bytes") val lengthBytes: Long?,
)

@Entity(tableName = "categories", primaryKeys = ["category_key"])
data class CategoryEntity(
    @ColumnInfo(name = "category_key") val categoryKey: String,
    @ColumnInfo(name = "display_name") val displayName: String,
)

@Entity(
    tableName = "podcast_categories",
    foreignKeys = [ForeignKey(
        entity = PodcastEntity::class,
        parentColumns = ["podcast_id"],
        childColumns = ["podcast_id"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("podcast_id"), Index("canonical_category_key")],
    primaryKeys = ["podcast_id", "source_category_key"],
)
data class PodcastCategoryEntity(
    @ColumnInfo(name = "podcast_id") val podcastId: String,
    @ColumnInfo(name = "source_category_key") val sourceCategoryKey: String,
    @ColumnInfo(name = "source_name") val sourceName: String,
    @ColumnInfo(name = "canonical_category_key") val canonicalCategoryKey: String,
)

@Entity(
    tableName = "subscriptions",
    foreignKeys = [ForeignKey(entity = PodcastEntity::class, parentColumns = ["podcast_id"], childColumns = ["podcast_id"], onDelete = ForeignKey.CASCADE)],
    primaryKeys = ["podcast_id"],
)
data class SubscriptionEntity(
    @ColumnInfo(name = "podcast_id") val podcastId: String,
    @ColumnInfo(name = "subscribed_at") val subscribedAt: Long,
)

@Entity(
    tableName = "favorites",
    foreignKeys = [ForeignKey(entity = EpisodeEntity::class, parentColumns = ["episode_id"], childColumns = ["episode_id"], onDelete = ForeignKey.CASCADE)],
    primaryKeys = ["episode_id"],
)
data class FavoriteEntity(
    @ColumnInfo(name = "episode_id") val episodeId: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Entity(
    tableName = "playback_history",
    foreignKeys = [ForeignKey(entity = EpisodeEntity::class, parentColumns = ["episode_id"], childColumns = ["episode_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("last_played_at")],
    primaryKeys = ["episode_id"],
)
data class PlaybackHistoryEntity(
    @ColumnInfo(name = "episode_id") val episodeId: String,
    @ColumnInfo(name = "last_played_at") val lastPlayedAt: Long,
    @ColumnInfo(name = "position_ms") val positionMs: Long,
    @ColumnInfo(name = "duration_snapshot_ms") val durationSnapshotMs: Long?,
    @ColumnInfo(name = "total_played_ms") val totalPlayedMs: Long,
    val completed: Boolean,
    @ColumnInfo(name = "completed_at") val completedAt: Long?,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

@Entity(
    tableName = "downloads",
    foreignKeys = [ForeignKey(entity = EpisodeEntity::class, parentColumns = ["episode_id"], childColumns = ["episode_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("state")],
    primaryKeys = ["episode_id"],
)
data class DownloadEntity(
    @ColumnInfo(name = "episode_id") val episodeId: String,
    @ColumnInfo(name = "source_url") val sourceUrl: String,
    val state: String,
    @ColumnInfo(name = "local_reference") val localReference: String?,
    @ColumnInfo(name = "received_bytes") val receivedBytes: Long,
    @ColumnInfo(name = "total_bytes") val totalBytes: Long?,
    @ColumnInfo(name = "failure_code") val failureCode: String?,
    @ColumnInfo(name = "failure_message") val failureMessage: String?,
    @ColumnInfo(name = "requested_at") val requestedAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "completed_at") val completedAt: Long?,
)

@Entity(tableName = "local_playlists", primaryKeys = ["playlist_id"])
data class LocalPlaylistEntity(
    @ColumnInfo(name = "playlist_id") val playlistId: String,
    val name: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Entity(
    tableName = "local_playlist_items",
    foreignKeys = [ForeignKey(
        entity = LocalPlaylistEntity::class,
        parentColumns = ["playlist_id"],
        childColumns = ["playlist_id"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("playlist_id")],
    primaryKeys = ["playlist_id", "position"],
)
data class LocalPlaylistItemEntity(
    @ColumnInfo(name = "playlist_id") val playlistId: String,
    val position: Int,
    val source: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "mime_type") val mimeType: String?,
)
