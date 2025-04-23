package com.mammoth.podcast.ui.home.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mammoth.podcast.core.designsystem.component.PodcastImage
import com.mammoth.podcast.core.designsystem.theme.Keyline1
import com.mammoth.podcast.core.model.EpisodeInfo
import com.mammoth.podcast.core.model.PodcastCategoryFilterResult
import com.mammoth.podcast.core.model.PodcastInfo
import com.mammoth.podcast.core.player.model.PlayerEpisode
import com.mammoth.podcast.ui.shared.EpisodeListItem
import com.mammoth.podcast.ui.shared.PreviewEpisodes
import com.mammoth.podcast.ui.shared.PreviewPodcasts
import com.mammoth.podcast.ui.theme.MammothTheme
import com.mammoth.podcast.util.ToggleFollowPodcastIconButton
import com.mammoth.podcast.util.fullWidthItem

fun LazyListScope.podcastCategory(
    podcastCategoryFilterResult: PodcastCategoryFilterResult,
    navigateToPodcastDetails: (PodcastInfo) -> Unit,
    navigateToPlayer: (EpisodeInfo) -> Unit,
    onQueueEpisode: (PlayerEpisode) -> Unit,
    onTogglePodcastFollowed: (PodcastInfo) -> Unit,
) {
    item {
        CategoryPodcasts(
            topPodcasts = podcastCategoryFilterResult.topPodcasts,
            navigateToPodcastDetails = navigateToPodcastDetails,
            onTogglePodcastFollowed = onTogglePodcastFollowed
        )
    }

    val episodes = podcastCategoryFilterResult.episodes
    items(episodes, key = { it.episode.uri }) { item ->
        EpisodeListItem(
            episode = item.episode,
            podcast = item.podcast,
            onClick = navigateToPlayer,
            onQueueEpisode = onQueueEpisode,
            modifier = Modifier.fillParentMaxWidth()
        )
    }
}

fun LazyGridScope.podcastCategory(
    podcastCategoryFilterResult: PodcastCategoryFilterResult,
    navigateToPodcastDetails: (PodcastInfo) -> Unit,
    navigateToPlayer: (EpisodeInfo) -> Unit,
    onQueueEpisode: (PlayerEpisode) -> Unit,
    onTogglePodcastFollowed: (PodcastInfo) -> Unit,
) {
    fullWidthItem {
        CategoryPodcasts(
            topPodcasts = podcastCategoryFilterResult.topPodcasts,
            navigateToPodcastDetails = navigateToPodcastDetails,
            onTogglePodcastFollowed = onTogglePodcastFollowed
        )
    }

    val episodes = podcastCategoryFilterResult.episodes
    items(episodes, key = { it.episode.uri }) { item ->
        EpisodeListItem(
            episode = item.episode,
            podcast = item.podcast,
            onClick = navigateToPlayer,
            onQueueEpisode = onQueueEpisode,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CategoryPodcasts(
    topPodcasts: List<PodcastInfo>,
    navigateToPodcastDetails: (PodcastInfo) -> Unit,
    onTogglePodcastFollowed: (PodcastInfo) -> Unit
) {
    CategoryPodcastRow(
        podcasts = topPodcasts,
        onTogglePodcastFollowed = onTogglePodcastFollowed,
        navigateToPodcastDetails = navigateToPodcastDetails,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CategoryPodcastRow(
    podcasts: List<PodcastInfo>,
    onTogglePodcastFollowed: (PodcastInfo) -> Unit,
    navigateToPodcastDetails: (PodcastInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = Keyline1,
            top = 8.dp,
            end = Keyline1,
            bottom = 24.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(
            items = podcasts,
            key = { it.uri }
        ) { podcast ->
            TopPodcastRowItem(
                podcastTitle = podcast.title,
                podcastImageUrl = podcast.imageUrl,
                isFollowed = podcast.isSubscribed ?: false,
                onToggleFollowClicked = { onTogglePodcastFollowed(podcast) },
                modifier = Modifier
                    .width(128.dp)
                    .clickable {
                        navigateToPodcastDetails(podcast)
                    }
            )
        }
    }
}

@Composable
private fun TopPodcastRowItem(
    podcastTitle: String,
    podcastImageUrl: String,
    isFollowed: Boolean,
    modifier: Modifier = Modifier,
    onToggleFollowClicked: () -> Unit,
) {
    Column(
        modifier.semantics(mergeDescendants = true) {}
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .align(Alignment.CenterHorizontally)
        ) {
            PodcastImage(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium),
                podcastImageUrl = podcastImageUrl,
                contentDescription = podcastTitle
            )

            ToggleFollowPodcastIconButton(
                onClick = onToggleFollowClicked,
                isFollowed = isFollowed,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }

        Text(
            text = podcastTitle,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        )
    }
}

@Preview
@Composable
fun PreviewEpisodeListItem() {
    MammothTheme {
        EpisodeListItem(
            episode = PreviewEpisodes[0],
            podcast = PreviewPodcasts[0],
            onClick = { },
            onQueueEpisode = { },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
