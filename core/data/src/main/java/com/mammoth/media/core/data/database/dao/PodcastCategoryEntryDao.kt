package com.mammoth.media.core.data.database.dao

import androidx.room.Dao
import com.mammoth.media.core.data.database.model.PodcastCategoryEntry

/**
 * [Room] DAO for [PodcastCategoryEntry] related operations.
 */
@Dao
abstract class PodcastCategoryEntryDao : BaseDao<PodcastCategoryEntry>
