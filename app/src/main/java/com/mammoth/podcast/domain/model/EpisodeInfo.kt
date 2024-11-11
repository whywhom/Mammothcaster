package com.mammoth.podcast.domain.model

import androidx.room.ColumnInfo
import com.mammoth.podcast.data.database.model.Episode
import java.time.Duration
import java.time.OffsetDateTime

/**
 * External data layer representation of an episode.
 */
data class EpisodeInfo(
    val uri: String = "",
    val title: String = "",
    val subTitle: String = "",
    val summary: String = "",
    val author: String = "",
    val published: OffsetDateTime = OffsetDateTime.MIN,
    val duration: Duration? = null,
    val enclosureUrl: String?,
    val enclosureLength: Long = 0,
    val enclosureType: String?
)

fun Episode.asExternalModel(): EpisodeInfo =
    EpisodeInfo(
        uri = uri,
        title = title,
        subTitle = subtitle ?: "",
        summary = summary ?: "",
        author = author ?: "",
        published = published,
        duration = duration,
        enclosureUrl = enclosureUrl,
        enclosureLength = enclosureLength,
        enclosureType = enclosureType
    )
