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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import mammoth.mollie.caster.data.MollieStore
import mammoth.mollie.caster.model.Download
import mammoth.mollie.caster.model.DownloadState
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.model.EpisodeOrder
import mammoth.mollie.caster.model.Podcast
import mammoth.mollie.caster.playback.PodcastPlayer
import mammoth.mollie.caster.ui.components.PodcastArtwork
import mammoth.mollie.caster.ui.format.formatDate
import mammoth.mollie.caster.ui.format.formatDuration
import mammoth.mollie.caster.ui.theme.AetherTheme
import molliecaster.shared.generated.resources.Res
import molliecaster.shared.generated.resources.back
import molliecaster.shared.generated.resources.cancel
import molliecaster.shared.generated.resources.cancel_download
import molliecaster.shared.generated.resources.cancel_download_progress
import molliecaster.shared.generated.resources.cancel_queued_download
import molliecaster.shared.generated.resources.clear_failed_download
import molliecaster.shared.generated.resources.delete_downloaded_episode
import molliecaster.shared.generated.resources.download_anyway
import molliecaster.shared.generated.resources.download_episode
import molliecaster.shared.generated.resources.download_without_wifi
import molliecaster.shared.generated.resources.episode
import molliecaster.shared.generated.resources.episode_count
import molliecaster.shared.generated.resources.failed
import molliecaster.shared.generated.resources.loading_rss_preview
import molliecaster.shared.generated.resources.mobile_download_warning
import molliecaster.shared.generated.resources.newest
import molliecaster.shared.generated.resources.oldest
import molliecaster.shared.generated.resources.play
import molliecaster.shared.generated.resources.removing_downloaded_episode
import molliecaster.shared.generated.resources.subscribe
import molliecaster.shared.generated.resources.subscribed
import molliecaster.shared.generated.resources.sync_feed
import molliecaster.shared.generated.resources.syncing
import mammoth.mollie.caster.ui.localization.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastDetails(
    podcast: Podcast,
    state: LibraryState,
    store: MollieStore,
    player: PodcastPlayer,
    onPlay: (Episode) -> Unit,
    onSubscribe: (Podcast) -> Unit,
    onSync: (Podcast) -> Unit,
    onOpenEpisode: (Episode) -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onOpenPlayer: () -> Unit,
) {
    var order by remember { mutableStateOf(EpisodeOrder.Newest) }
    val preview = state.feedPreview?.takeIf { it.feedUrl == podcast.feedUrl }
    val displayedPodcast = preview?.podcast?.copy(isSubscribed = podcast.isSubscribed) ?: podcast
    val episodes = (preview?.episodes ?: state.episodes.filter { it.podcastId == podcast.id }).let {
        when (order) { EpisodeOrder.Newest -> it.sortedByDescending(Episode::publishedAtMillis); EpisodeOrder.Oldest -> it.sortedBy(Episode::publishedAtMillis) }
    }
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                ),
                title = { Text(displayedPodcast.title, maxLines = 1) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = { ThemeToggle(darkTheme, onToggleTheme) },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PodcastArtwork(displayedPodcast.artworkUrl, displayedPodcast.title, 132)
                    Spacer(Modifier.width(20.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(displayedPodcast.title, style = MaterialTheme.typography.headlineMedium)
                        Text(displayedPodcast.author, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(Res.string.episode_count, episodes.size), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        displayedPodcast.latestEpisodeAtMillis?.let { Text("Latest update ${formatDate(it)}", style = MaterialTheme.typography.bodySmall) }
                        Button(
                            enabled = !state.busy,
                            shape = RoundedCornerShape(24.dp),
                            onClick = {
                                if (podcast.isSubscribed) store.setSubscribed(podcast.id, false)
                                else onSubscribe(podcast)
                            },
                        ) {
                            Icon(if (podcast.isSubscribed) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, null)
                            Spacer(Modifier.width(8.dp)); Text(if (state.busy) stringResource(Res.string.syncing) else if (podcast.isSubscribed) stringResource(Res.string.subscribed) else stringResource(Res.string.subscribe))
                        }
                        if (podcast.isSubscribed) OutlinedButton(enabled = !state.busy, onClick = { onSync(podcast) }) { Text(stringResource(Res.string.sync_feed)) }
                    }
                }
            }
            item { Text(displayedPodcast.description) }
            if (state.feedPreviewUrl == podcast.feedUrl && state.feedPreviewLoading) item { Text(stringResource(Res.string.loading_rss_preview), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (state.feedPreviewUrl == podcast.feedUrl) state.feedPreviewError?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AetherFilterChip(selected = order == EpisodeOrder.Newest, onClick = { order = EpisodeOrder.Newest }, label = stringResource(Res.string.newest))
                    AetherFilterChip(selected = order == EpisodeOrder.Oldest, onClick = { order = EpisodeOrder.Oldest }, label = stringResource(Res.string.oldest))
                }
            }
            items(episodes) { episode -> EpisodeRow(episode, state, store, onOpen = onOpenEpisode, onPlay = { onPlay(it); if (player.capabilities.realPlayback) onOpenPlayer() }) }
        }
    }
}

@Composable
fun EpisodeRow(
    episode: Episode,
    state: LibraryState,
    store: MollieStore?,
    onOpen: (Episode) -> Unit,
    onPlay: (Episode) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpen(episode) },
        colors = CardDefaults.cardColors(containerColor = AetherTheme.colors.glass),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (AetherTheme.colors.isDark) 0.dp else 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(episode.title, style = MaterialTheme.typography.titleMedium)
            Text(
                listOfNotNull(formatDate(episode.publishedAtMillis), episode.durationMillis?.let(::formatDuration)).joinToString(" • ").uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(episode.summary, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onPlay(episode) }) { Icon(Icons.Default.PlayArrow, "Play ${episode.title}") }
                val isSubscribedPodcast = state.podcasts.any { it.id == episode.podcastId && it.isSubscribed }
                if (store != null && isSubscribedPodcast) {
                    val favorite = episode.id in state.favoriteIds
                    IconButton(onClick = { store.setFavorite(episode.id, !favorite) }) { Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, if (favorite) "Remove favorite" else "Favorite") }
                }
                if (store != null) {
                    val download = state.downloads.firstOrNull { it.episodeId == episode.id }
                    DownloadAction(
                        download = download,
                        supported = state.downloadsSupported,
                        warnBeforeCellularDownload = state.cellularDownloadControlSupported && state.cellularDownloadsAllowed,
                        onDownload = { store.enqueueDownload(episode) },
                        onRemove = { store.removeDownload(episode.id) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeDetails(
    episode: Episode,
    state: LibraryState,
    store: MollieStore,
    player: PodcastPlayer,
    playerState: mammoth.mollie.caster.playback.PlayerState,
    onPlay: (Episode) -> Unit,
    onOpenPlayer: () -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
) {
    val podcast = state.podcasts.firstOrNull { it.id == episode.podcastId }
    val favorite = episode.id in state.favoriteIds
    val download = state.downloads.firstOrNull { it.episodeId == episode.id }
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                ),
                title = { Text(stringResource(Res.string.episode), maxLines = 1) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back)) } },
                actions = { ThemeToggle(darkTheme, onToggleTheme) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            playerState.episode?.let { nowPlaying ->
                item { MiniPlayer(nowPlaying, playerState.isPlaying, player, onOpenPlayer) }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PodcastArtwork(episode.artworkUrl ?: podcast?.artworkUrl, episode.title, 132)
                    Spacer(Modifier.width(20.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(episode.title, style = MaterialTheme.typography.headlineMedium)
                        Text(podcast?.title ?: episode.author.ifBlank { "Podcast episode" }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(listOfNotNull(formatDate(episode.publishedAtMillis), episode.durationMillis?.let(::formatDuration)).joinToString(" • "))
                    }
                }
            }
            if (episode.subtitle.isNotBlank()) item { Text(episode.subtitle, style = MaterialTheme.typography.titleMedium) }
            if (episode.summary.isNotBlank()) item { Text(episode.summary) }
            if (episode.descriptionHtml.isNotBlank() && episode.descriptionHtml != episode.summary) item { Text(episode.descriptionHtml) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onPlay(episode) }, shape = RoundedCornerShape(24.dp)) { Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text(stringResource(Res.string.play)) }
                    if (podcast?.isSubscribed == true) {
                        IconButton(onClick = { store.setFavorite(episode.id, !favorite) }) {
                            Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, if (favorite) "Remove favorite" else "Favorite")
                        }
                    }
                    DownloadAction(
                        download = download,
                        supported = state.downloadsSupported,
                        warnBeforeCellularDownload = state.cellularDownloadControlSupported && state.cellularDownloadsAllowed,
                        onDownload = { store.enqueueDownload(episode) },
                        onRemove = { store.removeDownload(episode.id) },
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadAction(
    download: Download?,
    supported: Boolean,
    warnBeforeCellularDownload: Boolean,
    onDownload: () -> Unit,
    onRemove: () -> Unit,
) {
    var showCellularDownloadWarning by remember { mutableStateOf(false) }
    val state = download?.state
    val active = state == DownloadState.Queued || state == DownloadState.Downloading
    val progress = download?.takeIf { state == DownloadState.Downloading }?.downloadProgress()
    val description = when (state) {
        DownloadState.Queued -> stringResource(Res.string.cancel_queued_download)
        DownloadState.Downloading -> progress?.let { stringResource(Res.string.cancel_download_progress, (it * 100).toInt()) } ?: stringResource(Res.string.cancel_download)
        DownloadState.Completed -> stringResource(Res.string.delete_downloaded_episode)
        DownloadState.Failed -> stringResource(Res.string.clear_failed_download)
        DownloadState.Removing -> stringResource(Res.string.removing_downloaded_episode)
        null -> stringResource(Res.string.download_episode)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            enabled = state != DownloadState.Removing && (supported || download != null),
            onClick = {
                when {
                    download != null -> onRemove()
                    warnBeforeCellularDownload -> showCellularDownloadWarning = true
                    else -> onDownload()
                }
            },
        ) {
            Icon(
                imageVector = if (state == DownloadState.Completed) Icons.Default.DownloadDone else Icons.Default.Download,
                contentDescription = description,
                tint = if (state == DownloadState.Completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (active) {
            Spacer(Modifier.width(6.dp))
            if (progress == null) {
                LinearProgressIndicator(modifier = Modifier.width(64.dp))
            } else {
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.width(64.dp))
            }
        } else if (state == DownloadState.Failed) {
            Text(stringResource(Res.string.failed), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
        }
    }
    if (showCellularDownloadWarning) {
        AlertDialog(
            onDismissRequest = { showCellularDownloadWarning = false },
            title = { Text(stringResource(Res.string.download_without_wifi)) },
            text = {
                Text(
                    stringResource(Res.string.mobile_download_warning),
                )
            },
            dismissButton = {
                TextButton(onClick = { showCellularDownloadWarning = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
            confirmButton = {
                Button(onClick = {
                    showCellularDownloadWarning = false
                    onDownload()
                }) {
                    Text(stringResource(Res.string.download_anyway))
                }
            },
        )
    }
}

fun Download.downloadProgress(): Float? {
    val total = totalBytes?.takeIf { it > 0L } ?: return null
    return (receivedBytes.toDouble() / total.toDouble()).toFloat().coerceIn(0f, 1f)
}
