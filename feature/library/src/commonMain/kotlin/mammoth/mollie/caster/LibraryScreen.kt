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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mammoth.mollie.caster.data.LibraryState
import mammoth.mollie.caster.data.MollieStore
import mammoth.mollie.caster.model.DownloadState
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.model.LocalPlaylist
import mammoth.mollie.caster.model.Podcast
import mammoth.mollie.caster.ui.components.EmptyHint
import mammoth.mollie.caster.ui.components.PodcastArtwork
import mammoth.mollie.caster.ui.components.SectionTitle
import mammoth.mollie.caster.ui.localization.stringResource
import mammoth.mollie.caster.ui.theme.AetherTheme
import molliecaster.shared.generated.resources.*


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
    LibrarySection.LocalAudio -> stringResource(Res.string.your_audio)
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
    onSetLocalPlaylistPinned: (LocalPlaylist, Boolean) -> Unit = { _, _ -> },
    onMoveLocalPlaylistFile: (LocalPlaylist, Int, Int) -> Unit = { _, _, _ -> },
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
                onSetPinned = { pinned -> onSetLocalPlaylistPinned(playlist, pinned) },
                onMoveItem = { from, to -> onMoveLocalPlaylistFile(playlist, from, to) },
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
        onSetLocalPlaylistPinned = onSetLocalPlaylistPinned,
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
                EmptyHint(stringResource(Res.string.recent_updates_empty))
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
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(Res.string.open_named, title), tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
    onSetLocalPlaylistPinned: (LocalPlaylist, Boolean) -> Unit,
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
                if (subscriptions.isEmpty()) item { EmptyHint(stringResource(Res.string.subscriptions_empty)) }
                items(subscriptions) { PodcastSearchRow(it, onPodcast) }
            }
            LibrarySection.Channels -> {
                if (channels.isEmpty()) item { EmptyHint(stringResource(Res.string.channels_empty)) }
                items(channels) { (author, podcasts) ->
                    LibraryChannelRow(author, podcasts, onClick = { onChannel(author) })
                }
            }
            LibrarySection.Saved -> {
                if (favorites.isEmpty()) item { EmptyHint(stringResource(Res.string.favorites_empty)) }
                items(favorites) { EpisodeRow(it, state, store, onOpen = onEpisode, onPlay = onPlay) }
            }
            LibrarySection.Downloaded -> {
                if (downloaded.isEmpty()) item { EmptyHint(stringResource(Res.string.downloaded_empty)) }
                items(downloaded) { EpisodeRow(it, state, store, onOpen = onEpisode, onPlay = onPlay) }
            }
            LibrarySection.LatestEpisodes -> {
                if (latestEpisodes.isEmpty()) item { EmptyHint(stringResource(Res.string.latest_episodes_empty)) }
                items(latestEpisodes) { EpisodeRow(it, state, store, onOpen = onEpisode, onPlay = onPlay) }
            }
            LibrarySection.RecentlyPlayed -> {
                if (recentlyPlayed.isEmpty()) item { EmptyHint(stringResource(Res.string.history_empty)) }
                items(recentlyPlayed) { EpisodeRow(it, state, store, onOpen = onEpisode, onPlay = onPlay) }
            }
            LibrarySection.LocalAudio -> {
                item { LocalPlaylistIntro(localPlaylists, onAddLocalPlaylist) }
                if (localPlaylists.isEmpty()) item {
                    EmptyHint(stringResource(Res.string.local_playlist_empty))
                }
                item {
                    var query by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text(stringResource(Res.string.find_playlist)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    val filteredPlaylists = localPlaylists.filter { it.name.contains(query, ignoreCase = true) }
                    if (query.isNotBlank() && filteredPlaylists.isEmpty()) {
                        EmptyHint(stringResource(Res.string.no_playlist_matches, query))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 12.dp)) {
                        filteredPlaylists.forEach { playlist ->
                            LocalPlaylistRow(
                                playlist = playlist,
                                onOpen = { onOpenLocalPlaylist(playlist) },
                                onPlay = { onPlayLocalPlaylist(playlist, false) },
                                onShuffle = { onPlayLocalPlaylist(playlist, true) },
                                onSetPinned = { onSetLocalPlaylistPinned(playlist, !playlist.isPinned) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalPlaylistIntro(localPlaylists: List<LocalPlaylist>, onAddLocalPlaylist: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AetherTheme.colors.glass,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(Res.string.your_audio_queue), style = MaterialTheme.typography.headlineSmall)
            Text(
                stringResource(Res.string.playlist_count_summary, localPlaylists.size, localPlaylists.sumOf { it.files.size }),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(stringResource(Res.string.create_playlist_description), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onAddLocalPlaylist) { Text(stringResource(Res.string.add_local_audio)) }
        }
    }
}

@Composable
private fun LocalPlaylistRow(
    playlist: LocalPlaylist,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onSetPinned: () -> Unit,
) {
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
                    Text(stringResource(Res.string.tracks_on_device, playlist.files.size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (playlist.isPinned) {
                        Text(stringResource(Res.string.pinned), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                IconButton(onClick = onSetPinned) {
                    Icon(
                        if (playlist.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        if (playlist.isPinned) stringResource(Res.string.unpin_named, playlist.name) else stringResource(Res.string.pin_named, playlist.name),
                        tint = if (playlist.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.align(Alignment.End)) {
                TextButton(onClick = onPlay, enabled = playlist.files.isNotEmpty()) { Text(stringResource(Res.string.play)) }
                TextButton(onClick = onShuffle, enabled = playlist.files.isNotEmpty()) { Text(stringResource(Res.string.shuffle)) }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(Res.string.open_named, playlist.name), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LocalPlaylistNameDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    title: String? = null,
    initialName: String = "",
    confirmLabel: String? = null,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title ?: stringResource(Res.string.name_playlist)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(Res.string.playlist_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(onClick = { onCreate(name.trim()) }, enabled = name.isNotBlank()) { Text(confirmLabel ?: stringResource(Res.string.choose_audio)) }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) } },
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
    onSetPinned: (Boolean) -> Unit,
    onMoveItem: (Int, Int) -> Unit,
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Keep the playlist identity visible without returning to a large,
                    // redundant hero image.
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Icon(
                                Icons.Default.MusicNote,
                                null,
                                modifier = Modifier.padding(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(
                            playlist.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        TextButton(
                            onClick = { onSetPinned(!playlist.isPinned) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp),
                        ) { Text(if (playlist.isPinned) stringResource(Res.string.unpin) else stringResource(Res.string.pin), maxLines = 1) }
                        TextButton(
                            onClick = { renaming = true },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp),
                        ) { Text(stringResource(Res.string.rename), maxLines = 1) }
                        TextButton(
                            onClick = onAddAudio,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp),
                        ) { Text(stringResource(Res.string.add_audio), maxLines = 1) }
                        TextButton(
                            onClick = { confirmingDelete = true },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp),
                        ) { Text(stringResource(Res.string.delete), maxLines = 1) }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = onPlay,
                            enabled = playlist.files.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                        ) { Text(stringResource(Res.string.play)) }
                        OutlinedButton(
                            onClick = onShuffle,
                            enabled = playlist.files.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                        ) { Text(stringResource(Res.string.shuffle)) }
                    }
                }
            }
        }
        itemsIndexed(playlist.files, key = { index, file -> "$index:${file.source}" }) { index, file ->
            LocalPlaylistTrackRow(
                number = index + 1,
                title = file.displayName,
                onClick = { onPlayItem(index) },
                onMoveUp = { onMoveItem(index, index - 1) },
                onMoveDown = { onMoveItem(index, index + 1) },
                canMoveUp = index > 0,
                canMoveDown = index < playlist.files.lastIndex,
                onRemove = { onRemoveItem(index) },
            )
        }
    }
    if (renaming) {
        LocalPlaylistNameDialog(
            onDismiss = { renaming = false },
            onCreate = { name -> onRename(name); renaming = false },
            title = stringResource(Res.string.rename_playlist),
            initialName = playlist.name,
            confirmLabel = stringResource(Res.string.save),
        )
    }
    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text(stringResource(Res.string.delete_playlist_title)) },
            text = { Text(stringResource(Res.string.delete_playlist_description)) },
            confirmButton = { Button(onClick = { onDelete(); confirmingDelete = false }) { Text(stringResource(Res.string.delete)) } },
            dismissButton = { OutlinedButton(onClick = { confirmingDelete = false }) { Text(stringResource(Res.string.cancel)) } },
        )
    }
}

@Composable
private fun LocalPlaylistTrackRow(
    number: Int,
    title: String,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onRemove: () -> Unit,
) {
    val displayTitle = title.substringBeforeLast('.').ifBlank { stringResource(Res.string.local_audio) }
    val fileType = title.substringAfterLast('.', missingDelimiterValue = "").uppercase()
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = AetherTheme.colors.glass,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    number.toString(),
                    modifier = Modifier.width(30.dp).padding(top = 3.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(Icons.Default.MusicNote, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        displayTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        if (fileType.isBlank()) stringResource(Res.string.local_audio_file) else stringResource(Res.string.audio_file_type, fileType),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Icon(Icons.Default.ArrowUpward, stringResource(Res.string.move_up, title))
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Icon(Icons.Default.ArrowDownward, stringResource(Res.string.move_down, title))
                }
                TextButton(onClick = onRemove) { Text(stringResource(Res.string.remove), maxLines = 1) }
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
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(Res.string.open_named, author), tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
        if (podcasts.isEmpty()) item { EmptyHint(stringResource(Res.string.channel_empty)) }
        items(podcasts) { PodcastSearchRow(it, onPodcast) }
    }
}
