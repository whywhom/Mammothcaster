package com.mammoth.podcast.ui.shared

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mammoth.podcast.R
import com.mammoth.podcast.core.data.util.DownloadState
import com.mammoth.podcast.core.designsystem.component.HtmlTextContainer
import com.mammoth.podcast.core.designsystem.component.PodcastImage
import com.mammoth.podcast.core.model.EpisodeInfo
import com.mammoth.podcast.core.model.PodcastInfo
import com.mammoth.podcast.core.player.model.PlayerEpisode
import com.mammoth.podcast.ui.theme.MammothTheme
import com.mammoth.podcast.util.enqueueEpisodeDownloads
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun EpisodeListItem(
    episode: EpisodeInfo,
    podcast: PodcastInfo,
    onClick: (EpisodeInfo) -> Unit,
    onQueueEpisode: (PlayerEpisode) -> Unit,
    modifier: Modifier = Modifier,
    showPodcastImage: Boolean = true,
    showSummary: Boolean = false,
) {
    Box(modifier = modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer,
            onClick = { onClick(episode) }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Top Part
                EpisodeListItemHeader(
                    episode = episode,
                    podcast = podcast,
                    showPodcastImage = showPodcastImage,
                    showSummary = showSummary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Bottom Part
                EpisodeListItemFooter(
                    episode = episode,
                    podcast = podcast,
                    onQueueEpisode = onQueueEpisode,
                )
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
private fun EpisodeListItemFooter(
    episode: EpisodeInfo,
    podcast: PodcastInfo,
    onQueueEpisode: (PlayerEpisode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Image(
            imageVector = Icons.Rounded.PlayCircleFilled,
            contentDescription = stringResource(R.string.cd_play),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = 24.dp)
                ) { /* TODO */ }
                .size(48.dp)
                .padding(6.dp)
                .semantics { role = Role.Button }
        )

        val duration = episode.duration
        Text(
            text = when {
                duration != null -> {
                    // If we have the duration, we combine the date/duration via a
                    // formatted string
                    stringResource(
                        R.string.episode_date_duration,
                        MediumDateFormatter.format(episode.published),
                        duration.toMinutes().toInt()
                    )
                }
                // Otherwise we just use the date
                else -> MediumDateFormatter.format(episode.published)
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .weight(1f)
        )

        IconButton(
            onClick = {
                onQueueEpisode(
                    PlayerEpisode(
                        podcastInfo = podcast,
                        episodeInfo = episode
                    )
                )
            },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                contentDescription = stringResource(R.string.cd_add),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = {
                if (episode.isDownloaded == DownloadState.NOT_DOWNLOAD.value) {
                    enqueueEpisodeDownloads(arrayListOf(episode), context)
                }
            },
        ) {
            when (episode.isDownloaded) {
                DownloadState.DOWNLOADED.value -> Icon(
                    imageVector = Icons.Default.DownloadDone,
                    contentDescription = stringResource(R.string.cd_download),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DownloadState.NOT_DOWNLOAD.value -> Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = stringResource(R.string.cd_download),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DownloadState.DOWNLOADING.value -> CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            }
        }

        IconButton(
            onClick = { /* TODO */ },
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.cd_more),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EpisodeListItemHeader(
    episode: EpisodeInfo,
    podcast: PodcastInfo,
    showPodcastImage: Boolean,
    showSummary: Boolean,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Column(
            modifier =
            Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                text = episode.title,
                maxLines = 2,
                minLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            if (showSummary) {
                HtmlTextContainer(text = episode.summary) {
                    Text(
                        text = it,
                        maxLines = 2,
                        minLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            } else {
                Text(
                    text = podcast.title,
                    maxLines = 2,
                    minLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
        if (showPodcastImage) {
            EpisodeListItemImage(
                podcast = podcast,
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
        }
    }
}

@Composable
private fun EpisodeListItemImage(
    podcast: PodcastInfo,
    modifier: Modifier = Modifier
) {
    PodcastImage(
        podcastImageUrl = podcast.imageUrl,
        contentDescription = null,
        modifier = modifier,
    )
}

@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun EpisodeListItemPreview() {
    MammothTheme {
        EpisodeListItem(
            episode = PreviewEpisodes[0],
            podcast = PreviewPodcasts[0],
            onClick = {},
            onQueueEpisode = {},
            showSummary = true
        )
    }
}

private val MediumDateFormatter by lazy {
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
}

val PreviewEpisodes = listOf(
    EpisodeInfo(
        uri = "fakeUri://episode/1",
        title = "Episode 140: Lorem ipsum dolor",
        summary = "In this episode, Romain, Chet and Tor talked with Mady Melor and Artur " +
                "Tsurkan from the System UI team about... Bubbles!",
        published = OffsetDateTime.of(
            2020, 6, 2, 9,
            27, 0, 0, ZoneOffset.of("-0800")
        ),
        enclosureUrl = null,
        enclosureLength = 0,
        enclosureType = null
    )
)

val PreviewPodcasts = listOf(
    PodcastInfo(
        uri = "fakeUri://podcast/1",
        title = "Android Developers Backstage",
        author = "Android Developers",
        isSubscribed = true,
        lastEpisodeDate = OffsetDateTime.now()
    ),
    PodcastInfo(
        uri = "fakeUri://podcast/2",
        title = "Google Developers podcast",
        author = "Google Developers",
        lastEpisodeDate = OffsetDateTime.now()
    )
)
