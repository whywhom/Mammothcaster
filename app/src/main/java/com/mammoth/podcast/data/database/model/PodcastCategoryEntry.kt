package com.mammoth.podcast.data.database.model

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mammoth.podcast.data.database.model.Category
import com.mammoth.podcast.data.database.model.Podcast

@Entity(
    tableName = "podcast_category_entries",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Podcast::class,
            parentColumns = ["uri"],
            childColumns = ["podcast_uri"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("podcast_uri", "category_id", unique = true),
        Index("category_id"),
        Index("podcast_uri")
    ]
)
@Immutable
data class PodcastCategoryEntry(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "podcast_uri") val podcastUri: String,
    @ColumnInfo(name = "category_id") val categoryId: Long
)
