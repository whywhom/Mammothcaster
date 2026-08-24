package mammoth.mollie.caster

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import mammoth.mollie.caster.data.LibraryState
import mammoth.mollie.caster.data.MollieStore
import mammoth.mollie.caster.model.DownloadState
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.model.Podcast
import mammoth.mollie.caster.ui.components.EmptyHint
import mammoth.mollie.caster.ui.components.PodcastArtwork
import mammoth.mollie.caster.ui.components.SectionTitle
import mammoth.mollie.caster.ui.localization.stringResource
import mammoth.mollie.caster.ui.theme.AetherTheme
import molliecaster.shared.generated.resources.Res
import molliecaster.shared.generated.resources.back_to_library
import molliecaster.shared.generated.resources.channels
import molliecaster.shared.generated.resources.downloaded
import molliecaster.shared.generated.resources.latest_episodes
import molliecaster.shared.generated.resources.recently_played
import molliecaster.shared.generated.resources.recently_updated
import molliecaster.shared.generated.resources.saved
import molliecaster.shared.generated.resources.shows


enum class LibrarySection {
    Shows, Channels, Saved, Downloaded, LatestEpisodes, RecentlyPlayed,
}

@Composable
private fun LibrarySection.title(): String = when (this) {
    LibrarySection.Shows -> stringResource(Res.string.shows)
    LibrarySection.Channels -> stringResource(Res.string.channels)
    LibrarySection.Saved -> stringResource(Res.string.saved)
    LibrarySection.Downloaded -> stringResource(Res.string.downloaded)
    LibrarySection.LatestEpisodes -> stringResource(Res.string.latest_episodes)
    LibrarySection.RecentlyPlayed -> stringResource(Res.string.recently_played)
}
@Composable
fun LibraryScreen(
    state: LibraryState,
    store: MollieStore,
    selectedSection: LibrarySection?,
    selectedChannel: String?,
    onSection: (LibrarySection) -> Unit,
    onChannel: (String) -> Unit,
    onBack: () -> Unit,
    onPodcast: (Podcast) -> Unit,
    onEpisode: (Episode) -> Unit,
    onPlay: (Episode) -> Unit,
) {
    if (selectedSection == null) {
        LibraryOverview(state = state, onSection = onSection, onPodcast = onPodcast)
        return
    }
    if (selectedSection == LibrarySection.Channels && selectedChannel != null) {
        LibraryChannelDetails(
            channel = selectedChannel,
            podcasts = state.podcasts.filter { it.isSubscribed && it.author.trim() == selectedChannel },
            onBack = onBack,
            onPodcast = onPodcast,
        )
        return
    }
    LibrarySectionDetails(
        section = selectedSection,
        state = state,
        store = store,
        onBack = onBack,
        onChannel = onChannel,
        onPodcast = onPodcast,
        onEpisode = onEpisode,
        onPlay = onPlay,
    )
}

@Composable
fun LibraryOverview(
    state: LibraryState,
    onSection: (LibrarySection) -> Unit,
    onPodcast: (Podcast) -> Unit,
) {
    val subscriptions = state.podcasts.filter(Podcast::isSubscribed)
    val recentlyUpdated = subscriptions.sortedByDescending { it.lastRefreshAtMillis ?: it.latestEpisodeAtMillis }
    val menuItems = listOf(
        Triple(LibrarySection.Shows, Icons.Default.LibraryMusic, null),
        Triple(LibrarySection.Channels, Icons.Default.Cast, null),
        Triple(LibrarySection.Saved, Icons.Default.Bookmark, null),
        Triple(LibrarySection.Downloaded, Icons.Default.CloudDownload, null),
        Triple(LibrarySection.LatestEpisodes, Icons.AutoMirrored.Filled.QueueMusic, null),
        Triple(LibrarySection.RecentlyPlayed, Icons.Default.History, state.history.size.takeIf { it > 0 }),
    )
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = AetherTheme.colors.glass,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
            ) {
                Column {
                    menuItems.forEachIndexed { index, (section, icon, count) ->
                        LibraryMenuRow(
                            title = section.title(),
                            icon = icon,
                            count = count,
                            onClick = { onSection(section) },
                        )
                        if (index != menuItems.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                    }
                }
            }
        }
        if (recentlyUpdated.isEmpty()) {
            item {
                SectionTitle(stringResource(Res.string.recently_updated))
                EmptyHint("Subscribe to a podcast to see recent updates.")
            }
        } else {
            item { PodcastShelf(stringResource(Res.string.recently_updated), recentlyUpdated, onPodcast) }
        }
    }
}

@Composable
fun LibraryMenuRow(
    title: String,
    icon: ImageVector,
    count: Int?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        count?.let {
            Text(it.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(6.dp))
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Open $title", tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LibrarySectionDetails(
    section: LibrarySection,
    state: LibraryState,
    store: MollieStore,
    onBack: () -> Unit,
    onChannel: (String) -> Unit,
    onPodcast: (Podcast) -> Unit,
    onEpisode: (Episode) -> Unit,
    onPlay: (Episode) -> Unit,
) {
    val subscriptions = state.podcasts.filter(Podcast::isSubscribed)
    val subscribedIds = subscriptions.mapTo(mutableSetOf(), Podcast::id)
    val favorites = state.episodes.filter { it.id in state.favoriteIds }
    val downloaded = state.downloads.filter { it.state == DownloadState.Completed }.mapNotNull { download ->
        state.episodes.firstOrNull { it.id == download.episodeId }
    }
    val latestEpisodes = state.episodes.filter { it.podcastId in subscribedIds }.sortedByDescending(Episode::publishedAtMillis)
    val recentlyPlayed = state.history.mapNotNull { record ->
        state.episodes.firstOrNull { it.id == record.episodeId }
            ?.copy(playbackPositionMillis = record.positionMillis)
    }
    val channels = subscriptions
        .filter { it.author.isNotBlank() }
        .groupBy { it.author.trim() }
        .toList()
        .sortedBy { it.first.lowercase() }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { LibraryDetailHeader(section.title(), stringResource(Res.string.back_to_library), onBack) }
        when (section) {
            LibrarySection.Shows -> {
                if (subscriptions.isEmpty()) item { EmptyHint("Your subscribed podcasts will appear here.") }
                items(subscriptions) { PodcastSearchRow(it, onPodcast) }
            }
            LibrarySection.Channels -> {
                if (channels.isEmpty()) item { EmptyHint("Publisher channels appear when subscribed feeds include an author.") }
                items(channels) { (author, podcasts) ->
                    LibraryChannelRow(author, podcasts, onClick = { onChannel(author) })
                }
            }
            LibrarySection.Saved -> {
                if (favorites.isEmpty()) item { EmptyHint("Favorite an episode to keep it here.") }
                items(favorites) { EpisodeRow(it, state, store, onOpen = onEpisode, onPlay = onPlay) }
            }
            LibrarySection.Downloaded -> {
                if (downloaded.isEmpty()) item { EmptyHint("Downloaded episodes are available offline.") }
                items(downloaded) { EpisodeRow(it, state, store, onOpen = onEpisode, onPlay = onPlay) }
            }
            LibrarySection.LatestEpisodes -> {
                if (latestEpisodes.isEmpty()) item { EmptyHint("New episodes from subscribed podcasts will appear here.") }
                items(latestEpisodes) { EpisodeRow(it, state, store, onOpen = onEpisode, onPlay = onPlay) }
            }
            LibrarySection.RecentlyPlayed -> {
                if (recentlyPlayed.isEmpty()) item { EmptyHint("Your listening history will appear here.") }
                items(recentlyPlayed) { EpisodeRow(it, state, store, onOpen = onEpisode, onPlay = onPlay) }
            }
        }
    }
}

@Composable
fun LibraryChannelRow(author: String, podcasts: List<Podcast>, onClick: () -> Unit) {
    val artwork = podcasts.firstOrNull()
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = AetherTheme.colors.glass,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PodcastArtwork(artwork?.artworkUrl, author, 58)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(author, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${podcasts.size} ${if (podcasts.size == 1) "show" else "shows"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Open $author", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LibraryChannelDetails(
    channel: String,
    podcasts: List<Podcast>,
    onBack: () -> Unit,
    onPodcast: (Podcast) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { LibraryDetailHeader(channel, "Back to Channels", onBack) }
        if (podcasts.isEmpty()) item { EmptyHint("No subscribed shows are available for this channel.") }
        items(podcasts) { PodcastSearchRow(it, onPodcast) }
    }
}
