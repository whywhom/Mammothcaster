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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import mammoth.mollie.caster.data.LibraryState
import mammoth.mollie.caster.data.MollieStore
import mammoth.mollie.caster.model.DownloadState
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.model.Podcast
import mammoth.mollie.caster.model.LocalPlaylist
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
    Shows, Channels, Saved, Downloaded, LatestEpisodes, RecentlyPlayed, LocalAudio,
}

@Composable
fun LibrarySection.title(): String = when (this) {
    LibrarySection.Shows -> stringResource(Res.string.shows)
    LibrarySection.Channels -> stringResource(Res.string.channels)
    LibrarySection.Saved -> stringResource(Res.string.saved)
    LibrarySection.Downloaded -> stringResource(Res.string.downloaded)
    LibrarySection.LatestEpisodes -> stringResource(Res.string.latest_episodes)
    LibrarySection.RecentlyPlayed -> stringResource(Res.string.recently_played)
    LibrarySection.LocalAudio -> "Local playlists"
}
@Composable
fun LibraryScreen(
    state: LibraryState,
    store: MollieStore,
    selectedSection: LibrarySection?,
    selectedChannel: String?,
    selectedLocalPlaylistId: String?,
    onSection: (LibrarySection) -> Unit,
    onChannel: (String) -> Unit,
    onPodcast: (Podcast) -> Unit,
    onEpisode: (Episode) -> Unit,
    onPlay: (Episode) -> Unit,
    localPlaylists: List<LocalPlaylist> = emptyList(),
    onAddLocalPlaylist: () -> Unit = {},
    creatingLocalPlaylist: Boolean = false,
    onDismissCreateLocalPlaylist: () -> Unit = {},
    onCreateLocalPlaylist: (String) -> Unit = {},
    onOpenLocalPlaylist: (LocalPlaylist) -> Unit = {},
    onPlayLocalPlaylist: (LocalPlaylist, Boolean) -> Unit = { _, _ -> },
    onPlayLocalPlaylistItem: (LocalPlaylist, Int) -> Unit = { _, _ -> },
    onRenameLocalPlaylist: (LocalPlaylist, String) -> Unit = { _, _ -> },
    onAddLocalPlaylistFiles: (LocalPlaylist) -> Unit = {},
    onRemoveLocalPlaylistFile: (LocalPlaylist, Int) -> Unit = { _, _ -> },
    onDeleteLocalPlaylist: (LocalPlaylist) -> Unit = {},
) {
    if (selectedSection == null) {
        LibraryOverview(state = state, onSection = onSection, onPodcast = onPodcast)
        return
    }
    if (selectedSection == LibrarySection.Channels && selectedChannel != null) {
        LibraryChannelDetails(
            channel = selectedChannel,
            podcasts = state.podcasts.filter { it.isSubscribed && it.author.trim() == selectedChannel },
            onPodcast = onPodcast,
        )
        return
    }
    if (selectedSection == LibrarySection.LocalAudio && selectedLocalPlaylistId != null) {
        localPlaylists.firstOrNull { it.id == selectedLocalPlaylistId }?.let { playlist ->
            LocalPlaylistDetails(
                playlist = playlist,
                onPlay = { onPlayLocalPlaylist(playlist, false) },
                onShuffle = { onPlayLocalPlaylist(playlist, true) },
                onPlayItem = { index -> onPlayLocalPlaylistItem(playlist, index) },
                onRename = { name -> onRenameLocalPlaylist(playlist, name) },
                onAddAudio = { onAddLocalPlaylistFiles(playlist) },
                onRemoveItem = { index -> onRemoveLocalPlaylistFile(playlist, index) },
                onDelete = { onDeleteLocalPlaylist(playlist) },
            )
            return
        }
    }
    LibrarySectionDetails(
        section = selectedSection,
        state = state,
        store = store,
        onChannel = onChannel,
        onPodcast = onPodcast,
        onEpisode = onEpisode,
        onPlay = onPlay,
        localPlaylists = localPlaylists,
        onAddLocalPlaylist = onAddLocalPlaylist,
        onOpenLocalPlaylist = onOpenLocalPlaylist,
        onPlayLocalPlaylist = onPlayLocalPlaylist,
    )
    if (creatingLocalPlaylist) {
        LocalPlaylistNameDialog(onDismiss = onDismissCreateLocalPlaylist, onCreate = onCreateLocalPlaylist)
    }
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
        Triple(LibrarySection.LocalAudio, Icons.AutoMirrored.Filled.QueueMusic, null),
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
    onChannel: (String) -> Unit,
    onPodcast: (Podcast) -> Unit,
    onEpisode: (Episode) -> Unit,
    onPlay: (Episode) -> Unit,
    localPlaylists: List<LocalPlaylist>,
    onAddLocalPlaylist: () -> Unit,
    onOpenLocalPlaylist: (LocalPlaylist) -> Unit,
    onPlayLocalPlaylist: (LocalPlaylist, Boolean) -> Unit,
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
            LibrarySection.LocalAudio -> {
                item {
                    LocalPlaylistIntro(onAddLocalPlaylist)
                }
                if (localPlaylists.isEmpty()) item {
                    EmptyHint("Add audio files from this device to make a playlist.")
                }
                items(localPlaylists, key = { it.id }) { playlist ->
                    LocalPlaylistRow(playlist, onOpen = { onOpenLocalPlaylist(playlist) })
                }
            }
        }
    }
}

@Composable
private fun LocalPlaylistIntro(onAddLocalPlaylist: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AetherTheme.colors.glass,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Play audio from this device", style = MaterialTheme.typography.titleMedium)
            Text("Select multiple files, then play them in order or shuffle.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onAddLocalPlaylist) { Text("Add local audio") }
        }
    }
}

@Composable
private fun LocalPlaylistRow(playlist: LocalPlaylist, onOpen: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        shape = RoundedCornerShape(16.dp),
        color = AetherTheme.colors.glass,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.QueueMusic, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(playlist.name, style = MaterialTheme.typography.titleMedium)
                    Text("${playlist.files.size} audio ${if (playlist.files.size == 1) "file" else "files"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Open ${playlist.name}", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.End))
        }
    }
}

@Composable
private fun LocalPlaylistNameDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    title: String = "Name your playlist",
    initialName: String = "",
    confirmLabel: String = "Choose audio",
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Playlist name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(onClick = { onCreate(name.trim()) }, enabled = name.isNotBlank()) { Text(confirmLabel) }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun LocalPlaylistDetails(
    playlist: LocalPlaylist,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onPlayItem: (Int) -> Unit,
    onRename: (String) -> Unit,
    onAddAudio: () -> Unit,
    onRemoveItem: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    var renaming by remember { mutableStateOf(false) }
    var confirmingDelete by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = AetherTheme.colors.glass,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
            ) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(modifier = Modifier.size(112.dp), shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Icon(Icons.Default.MusicNote, null, modifier = Modifier.padding(24.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Text("${playlist.files.size} audio ${if (playlist.files.size == 1) "file" else "files"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onPlay, enabled = playlist.files.isNotEmpty()) { Text("Play") }
                        OutlinedButton(onClick = onShuffle, enabled = playlist.files.isNotEmpty()) { Text("Shuffle") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { renaming = true }) { Text("Rename") }
                        TextButton(onClick = onAddAudio) { Text("Add audio") }
                        TextButton(onClick = { confirmingDelete = true }) { Text("Delete") }
                    }
                }
            }
        }
        itemsIndexed(playlist.files, key = { index, file -> "$index:${file.source}" }) { index, file ->
            LocalPlaylistTrackRow(number = index + 1, title = file.displayName, onClick = { onPlayItem(index) }, onRemove = { onRemoveItem(index) })
        }
    }
    if (renaming) {
        LocalPlaylistNameDialog(
            onDismiss = { renaming = false },
            onCreate = { name -> onRename(name); renaming = false },
            title = "Rename playlist",
            initialName = playlist.name,
            confirmLabel = "Save",
        )
    }
    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text("Delete playlist?") },
            text = { Text("This removes the playlist, but does not delete audio files from your device.") },
            confirmButton = { Button(onClick = { onDelete(); confirmingDelete = false }) { Text("Delete") } },
            dismissButton = { OutlinedButton(onClick = { confirmingDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun LocalPlaylistTrackRow(number: Int, title: String, onClick: () -> Unit, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(number.toString(), modifier = Modifier.width(30.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Icon(Icons.Default.MusicNote, null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(14.dp))
        Text(title.substringBeforeLast('.').ifBlank { "Local audio" }, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        TextButton(onClick = onRemove) { Text("Remove") }
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
    onPodcast: (Podcast) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (podcasts.isEmpty()) item { EmptyHint("No subscribed shows are available for this channel.") }
        items(podcasts) { PodcastSearchRow(it, onPodcast) }
    }
}
