package mammoth.mollie.caster

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mammoth.mollie.caster.data.LibraryState
import mammoth.mollie.caster.data.discovery.recommendFromDiscovery
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.model.LocalPlaylist
import mammoth.mollie.caster.model.Podcast
import mammoth.mollie.caster.model.PodcastCategories
import mammoth.mollie.caster.platform.currentTimeMillis
import mammoth.mollie.caster.ui.components.EmptyHint
import mammoth.mollie.caster.ui.components.PodcastArtwork
import mammoth.mollie.caster.ui.components.SectionTitle
import mammoth.mollie.caster.ui.format.formatDate
import mammoth.mollie.caster.ui.format.formatDuration
import mammoth.mollie.caster.ui.localization.localizedCategoryName
import mammoth.mollie.caster.ui.localization.stringResource
import mammoth.mollie.caster.ui.theme.AetherTheme
import molliecaster.shared.generated.resources.Res
import molliecaster.shared.generated.resources.browse_categories
import molliecaster.shared.generated.resources.latest_episodes
import molliecaster.shared.generated.resources.latest_podcasts
import molliecaster.shared.generated.resources.loading_popular_podcasts
import molliecaster.shared.generated.resources.popular_podcasts
import molliecaster.shared.generated.resources.recommended_for_you
import molliecaster.shared.generated.resources.todays_resonance

@Composable
fun HomeScreen(
    state: LibraryState,
    onPodcast: (Podcast) -> Unit,
    onEpisode: (Episode) -> Unit,
    onPlay: (Episode) -> Unit,
    onSearch: () -> Unit,
    localPlaylists: List<LocalPlaylist> = emptyList(),
    onOpenLocalPlaylist: (LocalPlaylist) -> Unit = {},
    onPlayLocalPlaylist: (LocalPlaylist, Boolean) -> Unit = { _, _ -> },
) {
    val subscriptions = state.podcasts.filter(Podcast::isSubscribed)
    if (subscriptions.isEmpty()) {
        EmptyLibraryHome(state, onPodcast, onSearch, localPlaylists, onOpenLocalPlaylist, onPlayLocalPlaylist)
    } else {
        PopulatedLibraryHome(state, subscriptions, onPodcast, onEpisode, onPlay, localPlaylists, onOpenLocalPlaylist, onPlayLocalPlaylist)
    }
}

@Composable
private fun EmptyLibraryHome(
    state: LibraryState,
    onPodcast: (Podcast) -> Unit,
    onSearch: () -> Unit,
    localPlaylists: List<LocalPlaylist>,
    onOpenLocalPlaylist: (LocalPlaylist) -> Unit,
    onPlayLocalPlaylist: (LocalPlaylist, Boolean) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        item { PopularPodcastSearch(onClick = onSearch) }
        if (localPlaylists.isNotEmpty()) item { LocalPlaylistShelf(localPlaylists, onOpenLocalPlaylist, onPlayLocalPlaylist) }
        if (state.discoveryLoading && state.popularPodcasts.isEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    Text(stringResource(Res.string.loading_popular_podcasts), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (state.popularPodcasts.isNotEmpty()) item { PodcastShelf(stringResource(Res.string.popular_podcasts), state.popularPodcasts, onPodcast) }
        if (!state.discoveryLoading && state.popularPodcasts.isEmpty()) item { EmptyHint("Popular podcasts are unavailable right now. Try again shortly.") }
    }
}

@Composable
private fun PopulatedLibraryHome(
    state: LibraryState,
    subscriptions: List<Podcast>,
    onPodcast: (Podcast) -> Unit,
    onEpisode: (Episode) -> Unit,
    onPlay: (Episode) -> Unit,
    localPlaylists: List<LocalPlaylist>,
    onOpenLocalPlaylist: (LocalPlaylist) -> Unit,
    onPlayLocalPlaylist: (LocalPlaylist, Boolean) -> Unit,
) {
    val recommended = recommendFromDiscovery(
        candidates = state.popularPodcasts,
        libraryPodcasts = state.podcasts,
        episodes = state.episodes,
        favoriteEpisodeIds = state.favoriteIds.mapTo(mutableSetOf()) { it.value },
        historyEpisodeIds = state.history.mapTo(mutableSetOf()) { it.episodeId.value },
    )
    val categories = PodcastCategories.fromSubscriptions(state.podcasts)
    var selectedCategory by remember(categories) { mutableStateOf(categories.firstOrNull()?.key) }
    val podcastsById = state.podcasts.associateBy { it.id }
    val now = currentTimeMillis()
    val latestCutoff = now - SEVEN_DAYS_MILLIS
    val latestPodcasts = subscriptions
        .filter { it.latestEpisodeAtMillis in latestCutoff..now }
        .sortedByDescending(Podcast::latestEpisodeAtMillis)
    val latestEpisodes = state.episodes
        .filter { it.publishedAtMillis in latestCutoff..now }
        .sortedByDescending { it.publishedAtMillis }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(36.dp),
    ) {
        item { HeroCard(recommended.firstOrNull() ?: subscriptions.firstOrNull(), onPodcast) }
        if (localPlaylists.isNotEmpty()) item { LocalPlaylistShelf(localPlaylists, onOpenLocalPlaylist, onPlayLocalPlaylist) }
        if (state.popularPodcasts.isNotEmpty()) {
            item { PodcastShelf(stringResource(Res.string.popular_podcasts), state.popularPodcasts, onPodcast) }
        }
        if (recommended.isNotEmpty()) {
            item { PodcastShelf(stringResource(Res.string.recommended_for_you), recommended, onPodcast) }
        }
        if (latestPodcasts.isNotEmpty()) item {
            PodcastShelf(
                stringResource(Res.string.latest_podcasts),
                latestPodcasts,
                onPodcast,
            )
        }
        if (categories.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(stringResource(Res.string.browse_categories))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { category ->
                            AetherFilterChip(
                                selected = selectedCategory == category.key,
                                onClick = { selectedCategory = category.key },
                                label = localizedCategoryName(category),
                            )
                        }
                    }
                    selectedCategory?.let { key ->
                        categories.firstOrNull { it.key == key }?.let { category ->
                            PodcastShelf(
                                localizedCategoryName(category),
                                subscriptions.filter { podcast -> podcast.categories.any { it.key == key } },
                                onPodcast,
                            )
                        }
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(stringResource(Res.string.latest_episodes))
                if (latestEpisodes.isEmpty()) EmptyHint("No episodes were published in the past 7 days.")
            }
        }
        items(latestEpisodes, key = { it.id.value }) { episode ->
            val podcast = podcastsById[episode.podcastId]
            LatestEpisodeRow(
                episode = episode,
                podcastTitle = podcast?.title.orEmpty(),
                artworkUrl = episode.artworkUrl ?: podcast?.artworkUrl,
                onOpen = { onEpisode(episode) },
                onPlay = { onPlay(episode) },
            )
        }
    }
}

@Composable
private fun LocalPlaylistShelf(
    playlists: List<LocalPlaylist>,
    onOpen: (LocalPlaylist) -> Unit,
    onPlay: (LocalPlaylist, Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Your local audio")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(playlists.take(8), key = { it.id }) { playlist ->
                Surface(
                    modifier = Modifier.width(220.dp).clickable { onOpen(playlist) },
                    shape = RoundedCornerShape(20.dp),
                    color = AetherTheme.colors.glass,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Text(playlist.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${playlist.files.size} ${if (playlist.files.size == 1) "track" else "tracks"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Play", modifier = Modifier.clickable(enabled = playlist.files.isNotEmpty()) { onPlay(playlist, false) }.padding(6.dp), color = MaterialTheme.colorScheme.primary)
                            Text("Shuffle", modifier = Modifier.clickable(enabled = playlist.files.isNotEmpty()) { onPlay(playlist, true) }.padding(6.dp), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PopularPodcastSearch(
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = AetherTheme.colors.glass,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Search podcasts", style = MaterialTheme.typography.titleMedium)
                Text("Find a show from Apple Podcasts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LatestEpisodeRow(
    episode: Episode,
    podcastTitle: String,
    artworkUrl: String?,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        shape = RoundedCornerShape(20.dp),
        color = AetherTheme.colors.glass,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PodcastArtwork(artworkUrl, episode.title, 84)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(episode.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                podcastTitle.takeIf(String::isNotBlank)?.let {
                    Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                listOfNotNull(formatDate(episode.publishedAtMillis), episode.durationMillis?.let(::formatDuration))
                    .joinToString("  •  ")
                    .takeIf(String::isNotBlank)
                    ?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary) }
                episode.summary.takeIf(String::isNotBlank)?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            IconButton(onClick = onPlay) {
                Icon(Icons.Default.PlayArrow, "Play ${episode.title}", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private const val SEVEN_DAYS_MILLIS = 7L * 24 * 60 * 60 * 1_000

@Composable
fun HeroCard(podcast: Podcast?, onPodcast: (Podcast) -> Unit) {
    if (podcast == null) return
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onPodcast(podcast) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
    ) {
        Row(
            Modifier.background(AetherTheme.colors.actionGradient).padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PodcastArtwork(podcast.artworkUrl, podcast.title, 116)
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.todays_resonance), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary)
                Text(podcast.title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimary)
                Text(podcast.description, color = MaterialTheme.colorScheme.onPrimary, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Text(podcast.author.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun PodcastShelf(title: String, podcasts: List<Podcast>, onPodcast: (Podcast) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(title)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(podcasts) { podcast ->
                Surface(
                    modifier = Modifier.width(164.dp).clickable { onPodcast(podcast) },
                    shape = RoundedCornerShape(16.dp),
                    color = AetherTheme.colors.glass,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
                    shadowElevation = if (AetherTheme.colors.isDark) 0.dp else 3.dp,
                ) {
                    Column(Modifier.padding(8.dp)) {
                        PodcastArtwork(podcast.artworkUrl, podcast.title, 148)
                        Spacer(Modifier.height(10.dp))
                        Text(podcast.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(podcast.author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }
            }
        }
    }
}
