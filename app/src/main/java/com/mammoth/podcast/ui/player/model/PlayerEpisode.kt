package com.mammoth.podcast.ui.player.model

import androidx.room.ColumnInfo
import com.mammoth.podcast.data.database.model.EpisodeToPodcast
import com.mammoth.podcast.domain.model.EpisodeInfo
import com.mammoth.podcast.domain.model.PodcastInfo
import com.mammoth.podcast.util.DownloadState
import java.time.Duration
import java.time.OffsetDateTime

/**
 * Episode data with necessary information to be used within a player.
 */
data class PlayerEpisode(
    val uri: String = "",
    val title: String = "",
    val subTitle: String = "",
    val published: OffsetDateTime = OffsetDateTime.MIN,
    val duration: Duration? = null,
    val podcastName: String = "",
    val author: String = "",
    val summary: String = "",
    val podcastImageUrl: String = "",
    val enclosureUrl: String? = null,
    val enclosureLength: Long = 0,
    val enclosureType: String? = null,
    val isDownloaded: Int = DownloadState.NOT_DOWNLOAD.value,
    val filePath: String? = null,
    val downloadTime: Long = 0
) {
    constructor(podcastInfo: PodcastInfo, episodeInfo: EpisodeInfo) : this(
        title = episodeInfo.title,
        subTitle = episodeInfo.subTitle,
        published = episodeInfo.published,
        duration = episodeInfo.duration,
        podcastName = podcastInfo.title,
        author = episodeInfo.author,
        summary = episodeInfo.summary,
        podcastImageUrl = podcastInfo.imageUrl,
        uri = episodeInfo.uri,
        enclosureUrl = episodeInfo.enclosureUrl,
        enclosureLength = episodeInfo.enclosureLength,
        enclosureType = episodeInfo.enclosureType,
        isDownloaded= episodeInfo.isDownloaded,
        filePath = episodeInfo.filePath,
        downloadTime = episodeInfo.downloadTime
    )
}

fun EpisodeToPodcast.toPlayerEpisode(): PlayerEpisode =
    PlayerEpisode(
        uri = episode.uri,
        title = episode.title,
        subTitle = episode.subtitle ?: "",
        published = episode.published,
        duration = episode.duration,
        podcastName = podcast.title,
        author = episode.author ?: podcast.author ?: "",
        summary = episode.summary ?: "",
        podcastImageUrl = podcast.imageUrl ?: "",
        enclosureUrl = episode.enclosureUrl,
        enclosureLength = episode.enclosureLength,
        enclosureType = episode.enclosureType,
        isDownloaded= episode.isDownloaded,
        filePath = episode.filePath,
        downloadTime = episode.downloadTime
    )
