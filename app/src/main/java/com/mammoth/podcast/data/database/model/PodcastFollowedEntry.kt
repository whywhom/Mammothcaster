package com.mammoth.podcast.data.database.model

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mammoth.podcast.data.database.model.Podcast

@Entity(
    tableName = "podcast_followed_entries",
    foreignKeys = [
        ForeignKey(
            entity = Podcast::class,
            parentColumns = ["uri"],
            childColumns = ["podcast_uri"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("podcast_uri", unique = true)
    ]
)
@Immutable
data class PodcastFollowedEntry(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "podcast_uri") val podcastUri: String
)
