package mammoth.mollie.caster

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mammoth.mollie.caster.data.MollieStore
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.model.Podcast
import mammoth.mollie.caster.model.PodcastCategory
import mammoth.mollie.caster.playback.PodcastPlayer
import mammoth.mollie.caster.playback.PreviewPodcastPlayer
import mammoth.mollie.caster.model.LocalPlaylist
import mammoth.mollie.caster.playback.QueuedPodcastPlayer
import mammoth.mollie.caster.ui.localaudio.rememberLocalAudioFilePicker
import mammoth.mollie.caster.ui.dialogs.AddFeedDialog
import mammoth.mollie.caster.ui.localization.LocalAppLanguage
import mammoth.mollie.caster.ui.localization.localizedCategoryName
import mammoth.mollie.caster.ui.localization.rememberAppLanguagePreference
import mammoth.mollie.caster.ui.localization.stringResource
import mammoth.mollie.caster.ui.theme.AetherTheme
import mammoth.mollie.caster.ui.theme.MolliecasterTheme
import molliecaster.shared.generated.resources.Res
import molliecaster.shared.generated.resources.add_rss_feed
import molliecaster.shared.generated.resources.app_name
import molliecaster.shared.generated.resources.audio_playback_unavailable
import molliecaster.shared.generated.resources.back
import molliecaster.shared.generated.resources.discovery_sources_unavailable
import molliecaster.shared.generated.resources.home
import molliecaster.shared.generated.resources.library
import molliecaster.shared.generated.resources.more_actions
import molliecaster.shared.generated.resources.opml_import_export
import molliecaster.shared.generated.resources.refresh
import molliecaster.shared.generated.resources.search
import molliecaster.shared.generated.resources.search_results
import molliecaster.shared.generated.resources.settings

private enum class Destination { Home, Search, Library, Settings }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MolliecasterApp(
    store: MollieStore = remember { MollieStore() },
    player: PodcastPlayer = remember { PreviewPodcastPlayer() },
) {
    val queuedPlayer = remember(player) { QueuedPodcastPlayer(player) }
    val localAudioPicker = rememberLocalAudioFilePicker()
    val systemDarkTheme = isSystemInDarkTheme()
    val library by store.state.collectAsState()
    val playerState by player.state.collectAsState()
    var darkTheme by remember(systemDarkTheme) { mutableStateOf(systemDarkTheme) }
    var destination by remember { mutableStateOf(Destination.Home) }
    var selectedPodcast by remember { mutableStateOf<Podcast?>(null) }
    var selectedEpisode by remember { mutableStateOf<Episode?>(null) }
    var selectedSearchCategory by remember { mutableStateOf<PodcastCategory?>(null) }
    var visibleSearchCategoryCount by remember { mutableStateOf(CATEGORY_PAGE_SIZE) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResultsVisible by remember { mutableStateOf(false) }
    var selectedLibrarySection by remember { mutableStateOf<LibrarySection?>(null) }
    var selectedLibraryChannel by remember { mutableStateOf<String?>(null) }
    var selectedLocalPlaylistId by remember { mutableStateOf<String?>(null) }
    var localPlaylistNameDialog by remember { mutableStateOf(false) }
    var playerExpanded by remember { mutableStateOf(false) }
    var addFeed by remember { mutableStateOf(false) }
    var librarySync by remember { mutableStateOf(false) }
    var settingsExpanded by remember { mutableStateOf(false) }
    var playbackRestoreAttempted by remember { mutableStateOf(false) }
    val languagePreference = rememberAppLanguagePreference()
    var language by remember(languagePreference) { mutableStateOf(languagePreference.initialLanguage) }
    var initialHomeEntryHandled by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val audioPlaybackUnavailable = stringResource(Res.string.audio_playback_unavailable)
    val discoverySourcesUnavailable = stringResource(Res.string.discovery_sources_unavailable)
    val toolbarBack: (() -> Unit)? = when (destination) {
        Destination.Search -> when {
            selectedSearchCategory != null -> ({ selectedSearchCategory = null })
            searchResultsVisible -> ({ searchResultsVisible = false })
            else -> null
        }
        Destination.Library -> selectedLibrarySection?.let {
            {
                if (selectedLocalPlaylistId != null) selectedLocalPlaylistId = null
                else if (selectedLibraryChannel != null) selectedLibraryChannel = null
                else selectedLibrarySection = null
            }
        }
        else -> null
    }
    val toolbarTitle = when (destination) {
        Destination.Search -> when {
            selectedSearchCategory != null -> localizedCategoryName(selectedSearchCategory!!)
            searchResultsVisible -> stringResource(Res.string.search_results)
            else -> destinationLabel(destination)
        }
        Destination.Library -> when {
            selectedLocalPlaylistId != null -> library.localPlaylists
                .firstOrNull { it.id == selectedLocalPlaylistId }
                ?.name
                ?: selectedLibrarySection?.title().orEmpty()
            selectedLibraryChannel != null -> selectedLibraryChannel!!
            selectedLibrarySection != null -> selectedLibrarySection!!.title()
            else -> destinationLabel(destination)
        }
        Destination.Home -> stringResource(Res.string.app_name)
        Destination.Settings -> destinationLabel(destination)
    }
    val startPlayback: (Episode) -> Unit = { episode ->
        if (queuedPlayer.capabilities.realPlayback) {
            store.markPlayed(episode)
            queuedPlayer.play(episode)
        } else {
            scope.launch { snackbar.showSnackbar(audioPlaybackUnavailable) }
        }
    }

    LaunchedEffect(library.message) {
        library.message?.let {
            snackbar.showSnackbar(it)
            store.clearMessage()
        }
    }
    LaunchedEffect(library.discoveryWarnings) {
        if (library.discoveryWarnings.isNotEmpty()) {
            snackbar.showSnackbar(discoverySourcesUnavailable)
        }
    }
    LaunchedEffect(destination) {
        if (destination == Destination.Home) {
            if (initialHomeEntryHandled) store.refreshDiscovery(force = false)
            initialHomeEntryHandled = true
        }
    }
    LaunchedEffect(playerState.episode?.id, playerState.positionMillis / 5_000L, playerState.status) {
        playerState.episode?.takeIf { playerState.positionMillis > 0 }?.let { episode ->
            store.recordPlayback(episode, playerState.positionMillis, playerState.durationMillis)
        }
    }
    // Restore the most recent unfinished episode only after the durable library has loaded.
    // It is prepared, rather than played, so reopening the app never starts audio unexpectedly.
    LaunchedEffect(library.restored, playbackRestoreAttempted, playerState.episode?.id) {
        if (!playbackRestoreAttempted && library.restored && playerState.episode == null) {
            playbackRestoreAttempted = true
            val history = library.history.firstOrNull { !it.completed && it.positionMillis > 0 }
            val episode = history?.let { record ->
                library.episodes.firstOrNull { it.id == record.episodeId }
            }
            if (episode != null) queuedPlayer.prepare(episode)
        }
    }

    CompositionLocalProvider(LocalAppLanguage provides language) {
    MolliecasterTheme(darkTheme = darkTheme) {
        Box(Modifier.fillMaxSize().background(AetherTheme.colors.ambientGradient)) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ) {
                when {
                playerExpanded && playerState.episode != null -> PlayerScreen(
                    player = queuedPlayer,
                    podcastTitle = library.podcasts
                        .firstOrNull { it.id == playerState.episode?.podcastId }
                        ?.title
                        ?: playerState.episode?.author.orEmpty(),
                    darkTheme = darkTheme,
                    onBack = { playerExpanded = false },
                )
                selectedEpisode != null -> EpisodeDetails(
                    episode = library.episodes.firstOrNull { it.id == selectedEpisode!!.id } ?: selectedEpisode!!,
                    state = library,
                    store = store,
                    player = queuedPlayer,
                    playerState = playerState,
                    onPlay = {
                        startPlayback(it)
                        if (queuedPlayer.capabilities.realPlayback) playerExpanded = true
                    },
                    onOpenPlayer = { playerExpanded = true },
                    onBack = { selectedEpisode = null },
                )
                selectedPodcast != null -> PodcastDetails(
                    podcast = library.podcasts.firstOrNull { it.id == selectedPodcast!!.id } ?: selectedPodcast!!,
                    state = library,
                    store = store,
                    player = queuedPlayer,
                    onPlay = startPlayback,
                    onSubscribe = { podcast -> scope.launch { store.subscribeFeed(podcast.feedUrl) } },
                    onSync = { podcast -> scope.launch { store.subscribeFeed(podcast.feedUrl) } },
                    onOpenEpisode = { selectedEpisode = it },
                    onBack = { selectedPodcast = null },
                    onOpenPlayer = { playerExpanded = true },
                )
                librarySync -> LibrarySyncScreen(
                    onBack = { librarySync = false },
                    onImportOpml = store::importOpml,
                    exportDocument = store::exportOpml,
                )
                else -> Scaffold(
                    containerColor = Color.Transparent,
                    snackbarHost = { SnackbarHost(snackbar) },
                    topBar = {
                        CenterAlignedTopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                scrolledContainerColor = Color.Unspecified,
                                navigationIconContentColor = Color.Unspecified,
                                titleContentColor = Color.Unspecified,
                                actionIconContentColor = Color.Unspecified
                            ),
                            title = { Text(toolbarTitle, style = MaterialTheme.typography.headlineSmall, maxLines = 1) },
                            navigationIcon = {
                                toolbarBack?.let { onBack ->
                                    IconButton(onClick = onBack) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back))
                                    }
                                }
                            },
                            actions = {
                                // Global toolbar actions belong only to the four root destinations.
                                // Detail routes use their own contextual toolbar and back navigation.
                                if (toolbarBack == null) {
                                    ThemeToggle(darkTheme = darkTheme, onToggle = { darkTheme = !darkTheme })
                                    Box {
                                        IconButton(
                                            modifier = Modifier.size(40.dp),
                                            onClick = { settingsExpanded = true },
                                        ) { Icon(Icons.Default.MoreVert, stringResource(Res.string.more_actions)) }
                                        DropdownMenu(
                                            expanded = settingsExpanded,
                                            onDismissRequest = { settingsExpanded = false },
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(Res.string.refresh)) },
                                                leadingIcon = { Icon(Icons.Default.Refresh, null) },
                                                onClick = {
                                                    settingsExpanded = false
                                                    scope.launch {
                                                        store.refreshSubscriptions(force = true)
                                                        store.refreshDiscovery(force = true)
                                                    }
                                                },
                                            )
                                            DropdownMenuItem(
                                                text = { Text(stringResource(Res.string.add_rss_feed)) },
                                                leadingIcon = { Icon(Icons.Default.Add, null) },
                                                onClick = {
                                                    settingsExpanded = false
                                                    addFeed = true
                                                },
                                            )
                                            DropdownMenuItem(
                                                text = { Text(stringResource(Res.string.opml_import_export)) },
                                                leadingIcon = { Icon(Icons.Default.Share, null) },
                                                onClick = {
                                                    settingsExpanded = false
                                                    librarySync = true
                                                },
                                            )
                                        }
                                    }
                                }
                            },
                        )
                    },
                    bottomBar = {
                        Column {
                            playerState.episode?.let { episode ->
                                MiniPlayer(episode, playerState.isPlaying, player, { playerExpanded = true })
                            }
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.94f),
                                tonalElevation = NavigationBarDefaults.Elevation,
                            ) {
                                Destination.entries.forEach { item ->
                                    NavigationBarItem(
                                        selected = destination == item,
                                        onClick = { destination = item },
                                        icon = { Icon(when (item) {
                                            Destination.Home -> Icons.Default.Home
                                            Destination.Search -> Icons.Default.Search
                                            Destination.Library -> Icons.Default.LibraryMusic
                                            Destination.Settings -> Icons.Default.Settings
                                        }, destinationLabel(item)) },
                                        label = { Text(destinationLabel(item)) },
                                    )
                                }
                            }
                        }
                    },
                ) { padding ->
                    Box(Modifier.padding(padding).fillMaxSize()) {
                        when (destination) {
                            Destination.Home -> HomeScreen(
                                library,
                                onPodcast = { podcast ->
                                    selectedPodcast = podcast
                                    if (library.podcasts.none { it.id == podcast.id }) {
                                        scope.launch { store.previewFeed(podcast.feedUrl) }
                                    }
                                },
                                onEpisode = { selectedEpisode = it },
                                onPlay = {
                                    startPlayback(it)
                                    if (queuedPlayer.capabilities.realPlayback) playerExpanded = true
                                },
                                onSearch = {
                                    selectedSearchCategory = null
                                    searchResultsVisible = false
                                    destination = Destination.Search
                                },
                                localPlaylists = library.localPlaylists,
                                onOpenLocalPlaylist = { playlist ->
                                    destination = Destination.Library
                                    selectedLibrarySection = LibrarySection.LocalAudio
                                    selectedLocalPlaylistId = playlist.id
                                },
                                onPlayLocalPlaylist = { playlist, shuffle ->
                                    queuedPlayer.playPlaylist(playlist, shuffle)
                                    playerExpanded = true
                                },
                            )
                            Destination.Search -> {
                                val category = selectedSearchCategory
                                if (category != null) {
                                    CategorySearchResultsScreen(
                                        state = library,
                                        category = category,
                                        visibleCount = visibleSearchCategoryCount,
                                        onVisibleCountChange = { visibleSearchCategoryCount = it },
                                        onRetry = { scope.launch { store.searchAppleCategory(category) } },
                                        onPodcast = { selectedPodcast = it },
                                        onPreview = { podcast ->
                                            selectedPodcast = podcast
                                            scope.launch { store.previewFeed(podcast.feedUrl) }
                                        },
                                    )
                                } else if (searchResultsVisible) {
                                    SearchResultsScreen(
                                        state = library,
                                        query = searchQuery,
                                        onRetry = { scope.launch { store.searchApplePodcasts(searchQuery) } },
                                        onPodcast = { selectedPodcast = it },
                                        onPreview = { podcast ->
                                            selectedPodcast = podcast
                                            scope.launch { store.previewFeed(podcast.feedUrl) }
                                        },
                                    )
                                } else {
                                    SearchScreen(
                                        state = library,
                                        query = searchQuery,
                                        onQueryChange = {
                                            searchQuery = it
                                            searchResultsVisible = false
                                        },
                                        onSearch = { query ->
                                            searchQuery = query
                                            searchResultsVisible = true
                                            scope.launch { store.searchApplePodcasts(query) }
                                        },
                                        onCategory = {
                                            selectedSearchCategory = it
                                            visibleSearchCategoryCount = CATEGORY_PAGE_SIZE
                                            scope.launch { store.searchAppleCategory(it) }
                                        },
                                    )
                                }
                            }
                            Destination.Library -> LibraryScreen(
                                state = library,
                                store = store,
                                selectedSection = selectedLibrarySection,
                                selectedChannel = selectedLibraryChannel,
                                selectedLocalPlaylistId = selectedLocalPlaylistId,
                                onSection = {
                                    selectedLibrarySection = it
                                    selectedLibraryChannel = null
                                    selectedLocalPlaylistId = null
                                },
                                onChannel = { selectedLibraryChannel = it },
                                onPodcast = { selectedPodcast = it },
                                onEpisode = { selectedEpisode = it },
                                onPlay = {
                                    startPlayback(it)
                                    if (queuedPlayer.capabilities.realPlayback) playerExpanded = true
                                },
                                localPlaylists = library.localPlaylists,
                                onAddLocalPlaylist = { localPlaylistNameDialog = true },
                                onCreateLocalPlaylist = { name ->
                                    localPlaylistNameDialog = false
                                    localAudioPicker.pickMultiple(
                                        onFiles = { files ->
                                            store.addLocalPlaylist(LocalPlaylist(
                                                id = "local-${library.localPlaylists.size + 1}-${files.hashCode()}",
                                                name = name,
                                                files = files,
                                            ))
                                        },
                                        onFailure = { message -> scope.launch { snackbar.showSnackbar(message) } },
                                    )
                                },
                                creatingLocalPlaylist = localPlaylistNameDialog,
                                onDismissCreateLocalPlaylist = { localPlaylistNameDialog = false },
                                onOpenLocalPlaylist = { selectedLocalPlaylistId = it.id },
                                onPlayLocalPlaylist = { playlist, shuffle ->
                                    queuedPlayer.playPlaylist(playlist, shuffle)
                                    playerExpanded = true
                                },
                                onPlayLocalPlaylistItem = { playlist, index ->
                                    queuedPlayer.playPlaylist(playlist, shuffle = false, startIndex = index)
                                    playerExpanded = true
                                },
                                onRenameLocalPlaylist = { playlist, name ->
                                    store.renameLocalPlaylist(playlist.id, name)
                                },
                                onAddLocalPlaylistFiles = { playlist ->
                                    localAudioPicker.pickMultiple(
                                        onFiles = { files -> store.addLocalPlaylistFiles(playlist.id, files) },
                                        onFailure = { message -> scope.launch { snackbar.showSnackbar(message) } },
                                    )
                                },
                                onRemoveLocalPlaylistFile = { playlist, index ->
                                    store.removeLocalPlaylistFile(playlist.id, index)
                                },
                                onDeleteLocalPlaylist = { playlist ->
                                    store.deleteLocalPlaylist(playlist.id)
                                    selectedLocalPlaylistId = null
                                },
                                onSetLocalPlaylistPinned = { playlist, pinned ->
                                    store.setLocalPlaylistPinned(playlist.id, pinned)
                                },
                                onMoveLocalPlaylistFile = { playlist, from, to ->
                                    store.moveLocalPlaylistFile(playlist.id, from, to)
                                },
                            )
                            Destination.Settings -> SettingsScreen(
                                darkTheme = darkTheme,
                                player = queuedPlayer,
                                playerState = playerState,
                                downloadsSupported = library.downloadsSupported,
                                cellularDownloadControlSupported = library.cellularDownloadControlSupported,
                                cellularDownloadsAllowed = library.cellularDownloadsAllowed,
                                refreshing = library.busy,
                                language = language,
                                onToggleTheme = { darkTheme = !darkTheme },
                                onLanguageChange = {
                                    language = it
                                    languagePreference.save(it)
                                },
                                onRefresh = {
                                    scope.launch {
                                        store.refreshSubscriptions(force = true)
                                        store.refreshDiscovery(force = true)
                                    }
                                },
                                onManageDownloads = {
                                    destination = Destination.Library
                                    selectedLibrarySection = LibrarySection.Downloaded
                                },
                                onCellularDownloadsAllowedChange = store::setCellularDownloadsAllowed,
                                onOpml = { librarySync = true },
                            )
                        }
                    }
                }
                }
            }

            if (addFeed) AddFeedDialog(library.busy, onDismiss = { addFeed = false }) { url ->
                scope.launch { store.subscribeFeed(url); addFeed = false }
            }
        }
    }
    }
}

@Composable
private fun destinationLabel(destination: Destination): String = when (destination) {
    Destination.Home -> stringResource(Res.string.home)
    Destination.Search -> stringResource(Res.string.search)
    Destination.Library -> stringResource(Res.string.library)
    Destination.Settings -> stringResource(Res.string.settings)
}
