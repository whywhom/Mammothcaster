package com.mammoth.podcast.ui.home.library

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mammoth.podcast.R
import com.mammoth.podcast.core.designsystem.theme.Keyline1
import com.mammoth.podcast.core.model.EpisodeInfo
import com.mammoth.podcast.core.model.LibraryInfo
import com.mammoth.podcast.core.player.model.PlayerEpisode
import com.mammoth.podcast.ui.shared.EpisodeListItem
import com.mammoth.podcast.util.fullWidthItem

fun LazyListScope.libraryItems(
    library: LibraryInfo,
    navigateToPlayer: (EpisodeInfo) -> Unit,
    onQueueEpisode: (PlayerEpisode) -> Unit
) {
    item {
        Text(
            text = stringResource(id = R.string.latest_episodes),
            modifier = Modifier.padding(
                start = Keyline1,
                top = 16.dp,
            ),
            style = MaterialTheme.typography.titleLarge,
        )
    }

    items(
        library,
        key = { it.episode.uri }
    ) { item ->
        EpisodeListItem(
            episode = item.episode,
            podcast = item.podcast,
            onClick = navigateToPlayer,
            onQueueEpisode = onQueueEpisode,
            modifier = Modifier.fillParentMaxWidth(),
        )
    }
}

fun LazyGridScope.libraryItems(
    library: LibraryInfo,
    navigateToPlayer: (EpisodeInfo) -> Unit,
    onQueueEpisode: (PlayerEpisode) -> Unit
) {
    fullWidthItem {
        Text(
            text = stringResource(id = R.string.latest_episodes),
            modifier = Modifier.padding(
                start = Keyline1,
                top = 16.dp,
            ),
            style = MaterialTheme.typography.headlineLarge,
        )
    }

    items(
        library,
        key = { it.episode.uri }
    ) { item ->
        EpisodeListItem(
            episode = item.episode,
            podcast = item.podcast,
            onClick = navigateToPlayer,
            onQueueEpisode = onQueueEpisode,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
