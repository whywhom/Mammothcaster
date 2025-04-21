package com.mammoth.media.core.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.mammoth.media.core.data.database.model.Episode
import com.mammoth.media.core.data.database.model.EpisodeToPodcast
import kotlinx.coroutines.flow.Flow

/**
 * [Room] DAO for [Episode] related operations.
 */
@Dao
abstract class EpisodesDao : BaseDao<Episode> {

    @Query(
        """
        SELECT * FROM episodes WHERE uri = :uri
        """
    )
    abstract fun episode(uri: String): Flow<Episode>

    @Transaction
    @Query(
        """
        SELECT episodes.* FROM episodes
        INNER JOIN podcasts ON episodes.podcast_uri = podcasts.uri
        WHERE episodes.uri = :episodeUri
        """
    )
    abstract fun episodeAndPodcast(episodeUri: String): Flow<EpisodeToPodcast>

    @Transaction
    @Query(
        """
        SELECT * FROM episodes WHERE podcast_uri = :podcastUri
        ORDER BY datetime(published) DESC
        LIMIT :limit
        """
    )
    abstract fun episodesForPodcastUri(
        podcastUri: String,
        limit: Int
    ): Flow<List<EpisodeToPodcast>>

    @Transaction
    @Query(
        """
        SELECT episodes.* FROM episodes
        INNER JOIN podcast_category_entries ON episodes.podcast_uri = podcast_category_entries.podcast_uri
        WHERE category_id = :categoryId
        ORDER BY datetime(published) DESC
        LIMIT :limit
        """
    )
    abstract fun episodesFromPodcastsInCategory(
        categoryId: Long,
        limit: Int
    ): Flow<List<EpisodeToPodcast>>

    @Query("SELECT COUNT(*) FROM episodes")
    abstract suspend fun count(): Int

    @Transaction
    @Query(
        """
        SELECT * FROM episodes WHERE podcast_uri IN (:podcastUris)
        ORDER BY datetime(published) DESC
        LIMIT :limit
        """
    )
    abstract fun episodesForPodcasts(
        podcastUris: List<String>,
        limit: Int
    ): Flow<List<EpisodeToPodcast>>
}
