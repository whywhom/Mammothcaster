package com.mammoth.podcast.data.database.dao

import androidx.room.Dao
import com.mammoth.podcast.data.database.model.PodcastCategoryEntry

/**
 * [Room] DAO for [PodcastCategoryEntry] related operations.
 */
@Dao
abstract class PodcastCategoryEntryDao : BaseDao<PodcastCategoryEntry>
