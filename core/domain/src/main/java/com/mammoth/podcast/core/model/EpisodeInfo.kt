package com.mammoth.podcast.core.model

import com.mammoth.podcast.core.data.database.model.Episode
import com.mammoth.podcast.core.data.util.DownloadState
import java.time.Duration
import java.time.OffsetDateTime

/**
 * External com.mammoth.podcast.core.data layer representation of an episode.
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
    val enclosureType: String?,
    val isDownloaded: Int = DownloadState.NOT_DOWNLOAD.value,
    val filePath: String? = null,
    val downloadTime: Long = 0
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
        enclosureType = enclosureType,
        isDownloaded = isDownloaded,
        filePath = filePath,
        downloadTime = downloadTime
    )
