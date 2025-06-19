package com.mammoth.podcast.core.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.mammoth.podcast.core.data.database.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * [Room] DAO for [Category] related operations.
 */
@Dao
abstract class CategoriesDao : BaseDao<Category> {
    @Query(
        """
        SELECT categories.* FROM categories
        INNER JOIN (
            SELECT category_id, COUNT(podcast_uri) AS podcast_count FROM podcast_category_entries
            GROUP BY category_id
        ) ON category_id = categories.id
        ORDER BY podcast_count DESC
        LIMIT :limit
        """
    )
    abstract fun categoriesSortedByPodcastCount(
        limit: Int
    ): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE name = :name")
    abstract suspend fun getCategoryWithName(name: String): Category?

    @Query("SELECT * FROM categories WHERE name = :name")
    abstract fun observeCategory(name: String): Flow<Category?>
}
