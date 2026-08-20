package mammoth.mollie.caster

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import molliecaster.shared.generated.resources.*
import mammoth.mollie.caster.data.LibraryState
import mammoth.mollie.caster.data.MollieStore
import mammoth.mollie.caster.data.discovery.recommendFromDiscovery
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.model.EpisodeOrder
import mammoth.mollie.caster.model.Download
import mammoth.mollie.caster.model.DownloadState
import mammoth.mollie.caster.model.Podcast
import mammoth.mollie.caster.model.PodcastCategories
import mammoth.mollie.caster.model.PodcastCategory
import mammoth.mollie.caster.playback.PodcastPlayer
import mammoth.mollie.caster.playback.PlayerStatus
import mammoth.mollie.caster.playback.PreviewPodcastPlayer
import mammoth.mollie.caster.ui.theme.AetherTheme
import mammoth.mollie.caster.ui.theme.MolliecasterTheme
import mammoth.mollie.caster.ui.components.EmptyHint
import mammoth.mollie.caster.ui.components.PodcastArtwork
import mammoth.mollie.caster.ui.components.SectionTitle
import mammoth.mollie.caster.ui.dialogs.AddFeedDialog
import mammoth.mollie.caster.ui.dialogs.OpmlDialog
import mammoth.mollie.caster.ui.format.formatDate
import mammoth.mollie.caster.ui.format.formatDuration
import mammoth.mollie.caster.ui.format.formatPlaybackSpeed
import molliecaster.shared.generated.resources.Res
import org.jetbrains.compose.resources.stringResource

private enum class Destination { Home, Search, Library, Settings }

private enum class LibrarySection(val title: String) {
    Shows("Shows"),
    Channels("Channels"),
    Saved("Saved"),
    Downloaded("Downloaded"),
    LatestEpisodes("Latest Episodes"),
    RecentlyPlayed("Recently Played"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MolliecasterApp(
    store: MollieStore = remember { MollieStore() },
    player: PodcastPlayer = remember { PreviewPodcastPlayer() },
) {
    val systemDarkTheme = isSystemInDarkTheme()
    val library by store.state.collectAsState()
    val playerState by player.state.collectAsState()
    var darkTheme by remember(systemDarkTheme) { mutableStateOf(systemDarkTheme) }
    var destination by remember { mutableStateOf(Destination.Home) }
    var selectedPodcast by remember { mutableStateOf<Podcast?>(null) }
    var selectedEpisode by remember { mutableStateOf<Episode?>(null) }
    var selectedSearchCategory by remember { mutableStateOf<PodcastCategory?>(null) }
    var visibleSearchCategoryCount by remember { mutableStateOf(CATEGORY_PAGE_SIZE) }
    var selectedLibrarySection by remember { mutableStateOf<LibrarySection?>(null) }
    var selectedLibraryChannel by remember { mutableStateOf<String?>(null) }
    var playerExpanded by remember { mutableStateOf(false) }
    var addFeed by remember { mutableStateOf(false) }
    var opml by remember { mutableStateOf(false) }
    var settingsExpanded by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val audioPlaybackUnavailable = stringResource(Res.string.audio_playback_unavailable)
    val startPlayback: (Episode) -> Unit = { episode ->
        if (player.capabilities.realPlayback) {
            store.markPlayed(episode)
            player.play(episode)
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
    LaunchedEffect(playerState.episode?.id, playerState.positionMillis / 5_000L, playerState.status) {
        playerState.episode?.takeIf { playerState.positionMillis > 0 }?.let { episode ->
            store.recordPlayback(episode, playerState.positionMillis, playerState.durationMillis)
        }
    }

    MolliecasterTheme(darkTheme = darkTheme) {
        Box(Modifier.fillMaxSize().background(AetherTheme.colors.ambientGradient)) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ) {
                when {
                playerExpanded && playerState.episode != null -> PlayerScreen(
                    player = player,
                    podcastTitle = library.podcasts
                        .firstOrNull { it.id == playerState.episode?.podcastId }
                        ?.title
                        ?: playerState.episode?.author.orEmpty(),
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme },
                    onBack = { playerExpanded = false },
                )
                selectedEpisode != null -> EpisodeDetails(
                    episode = library.episodes.firstOrNull { it.id == selectedEpisode!!.id } ?: selectedEpisode!!,
                    state = library,
                    store = store,
                    player = player,
                    playerState = playerState,
                    onPlay = {
                        startPlayback(it)
                        if (player.capabilities.realPlayback) playerExpanded = true
                    },
                    onOpenPlayer = { playerExpanded = true },
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme },
                    onBack = { selectedEpisode = null },
                )
                selectedPodcast != null -> PodcastDetails(
                    podcast = library.podcasts.firstOrNull { it.id == selectedPodcast!!.id } ?: selectedPodcast!!,
                    state = library,
                    store = store,
                    player = player,
                    onPlay = startPlayback,
                    onSubscribe = { podcast -> scope.launch { store.subscribeFeed(podcast.feedUrl) } },
                    onSync = { podcast -> scope.launch { store.subscribeFeed(podcast.feedUrl) } },
                    onOpenEpisode = { selectedEpisode = it },
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme },
                    onBack = { selectedPodcast = null },
                    onOpenPlayer = { playerExpanded = true },
                )
                else -> Scaffold(
                    containerColor = Color.Transparent,
                    snackbarHost = { SnackbarHost(snackbar) },
                    topBar = {
                        CenterAlignedTopAppBar(
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                            title = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.headlineSmall)
                                    Text(stringResource(Res.string.app_tagline), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                }
                            },
                            actions = {
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
                                                opml = true
                                            },
                                        )
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
                                        }, item.name) },
                                        label = { Text(item.name) },
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
                                    if (player.capabilities.realPlayback) playerExpanded = true
                                },
                            )
                            Destination.Search -> {
                                val category = selectedSearchCategory
                                if (category == null) {
                                    SearchScreen(
                                        state = library,
                                        onPodcast = { selectedPodcast = it },
                                        onSearch = { query -> scope.launch { store.searchApplePodcasts(query) } },
                                        onCategory = {
                                            selectedSearchCategory = it
                                            visibleSearchCategoryCount = CATEGORY_PAGE_SIZE
                                            scope.launch { store.searchAppleCategory(it) }
                                        },
                                        onPreview = { podcast ->
                                            selectedPodcast = podcast
                                            scope.launch { store.previewFeed(podcast.feedUrl) }
                                        },
                                    )
                                } else {
                                    CategorySearchResultsScreen(
                                        state = library,
                                        category = category,
                                        visibleCount = visibleSearchCategoryCount,
                                        onVisibleCountChange = { visibleSearchCategoryCount = it },
                                        onBack = { selectedSearchCategory = null },
                                        onRetry = { scope.launch { store.searchAppleCategory(category) } },
                                        onPodcast = { selectedPodcast = it },
                                        onPreview = { podcast ->
                                            selectedPodcast = podcast
                                            scope.launch { store.previewFeed(podcast.feedUrl) }
                                        },
                                    )
                                }
                            }
                            Destination.Library -> LibraryScreen(
                                state = library,
                                store = store,
                                selectedSection = selectedLibrarySection,
                                selectedChannel = selectedLibraryChannel,
                                onSection = {
                                    selectedLibrarySection = it
                                    selectedLibraryChannel = null
                                },
                                onChannel = { selectedLibraryChannel = it },
                                onBack = {
                                    if (selectedLibraryChannel != null) selectedLibraryChannel = null
                                    else selectedLibrarySection = null
                                },
                                onPodcast = { selectedPodcast = it },
                                onEpisode = { selectedEpisode = it },
                                onPlay = {
                                    startPlayback(it)
                                    if (player.capabilities.realPlayback) playerExpanded = true
                                },
                            )
                            Destination.Settings -> SettingsScreen(
                                darkTheme = darkTheme,
                                player = player,
                                playerState = playerState,
                                downloadsSupported = library.downloadsSupported,
                                cellularDownloadControlSupported = library.cellularDownloadControlSupported,
                                cellularDownloadsAllowed = library.cellularDownloadsAllowed,
                                refreshing = library.busy,
                                onToggleTheme = { darkTheme = !darkTheme },
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
                                onOpml = { opml = true },
                            )
                        }
                    }
                }
                }
            }

            if (addFeed) AddFeedDialog(library.busy, onDismiss = { addFeed = false }) { url ->
                scope.launch { store.subscribeFeed(url); addFeed = false }
            }
            if (opml) OpmlDialog(store, onDismiss = { opml = false })
        }
    }
}

@Composable
private fun HomeScreen(
    state: LibraryState,
    onPodcast: (Podcast) -> Unit,
    onEpisode: (Episode) -> Unit,
    onPlay: (Episode) -> Unit,
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val recommended = recommendFromDiscovery(
        candidates = state.popularPodcasts,
        libraryPodcasts = state.podcasts,
        episodes = state.episodes,
        favoriteEpisodeIds = state.favoriteIds.mapTo(mutableSetOf()) { it.value },
        historyEpisodeIds = state.history.mapTo(mutableSetOf()) { it.episodeId.value },
    )
    val subscribedPodcasts = state.podcasts.filter(Podcast::isSubscribed)
    val subscriptionCategories = PodcastCategories.fromSubscriptions(state.podcasts)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp),
    ) {
        item { HeroCard(recommended.firstOrNull() ?: state.podcasts.firstOrNull(), onPodcast) }
        if (state.discoveryLoading && state.popularPodcasts.isEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    Text(stringResource(Res.string.loading_popular_podcasts), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (state.popularPodcasts.isNotEmpty()) item { PodcastShelf(stringResource(Res.string.popular_podcasts), state.popularPodcasts, onPodcast) }
        if (recommended.isNotEmpty()) item { PodcastShelf(stringResource(Res.string.recommended_for_you), recommended, onPodcast) }
        if (!state.discoveryLoading && state.discoveryWarnings.isNotEmpty()) {
            item {
                Text(
                    "Some discovery sources are unavailable: ${state.discoveryWarnings.joinToString(" • ")}",
                    color = if (state.popularPodcasts.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        item { PodcastShelf(stringResource(Res.string.latest_podcasts), state.podcasts.sortedByDescending { it.latestEpisodeAtMillis }, onPodcast) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(stringResource(Res.string.browse_categories))
                if (subscriptionCategories.isEmpty()) {
                    EmptyHint("Subscribe to podcasts to browse their categories here.")
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(subscriptionCategories) { category ->
                            AetherFilterChip(
                                selected = selectedCategory == category.key,
                                onClick = { selectedCategory = category.key.takeUnless { it == selectedCategory } },
                                label = category.displayName,
                            )
                        }
                    }
                }
                selectedCategory?.let { key ->
                    subscriptionCategories.firstOrNull { it.key == key }?.let { category ->
                        PodcastShelf(
                            category.displayName,
                            subscribedPodcasts.filter { podcast -> podcast.categories.any { it.key == key } },
                            onPodcast,
                        )
                    }
                }
            }
        }
        item { SectionTitle(stringResource(Res.string.latest_episodes)) }
        items(state.episodes.sortedByDescending { it.publishedAtMillis }) {
            EpisodeRow(it, state, null, onOpen = onEpisode, onPlay = onPlay)
        }
    }
}

@Composable
private fun HeroCard(podcast: Podcast?, onPodcast: (Podcast) -> Unit) {
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
private fun PodcastShelf(title: String, podcasts: List<Podcast>, onPodcast: (Podcast) -> Unit) {
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

@Composable
private fun SearchScreen(
    state: LibraryState,
    onPodcast: (Podcast) -> Unit,
    onSearch: (String) -> Unit,
    onCategory: (PodcastCategory) -> Unit,
    onPreview: (Podcast) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val normalized = query.trim().lowercase()
    val submitSearch = {
        if (normalized.isNotBlank() && !state.appleSearchLoading) onSearch(query)
    }
    val localResults = if (normalized.isBlank()) emptyList() else state.podcasts.filter {
        normalized in it.title.lowercase() || normalized in it.author.lowercase()
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(Res.string.find_next_frequency), style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(Res.string.search_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().onPreviewKeyEvent { event ->
                        if (event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
                            submitSearch()
                            true
                        } else false
                    },
                    singleLine = true,
                    label = { Text(stringResource(Res.string.search_apple_podcasts)) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.78f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { submitSearch() }),
                )
                Button(
                    onClick = submitSearch,
                    enabled = normalized.isNotBlank() && !state.appleSearchLoading,
                    shape = RoundedCornerShape(24.dp),
                ) { Text(if (state.appleSearchLoading) stringResource(Res.string.searching) else stringResource(Res.string.search_apple_podcasts)) }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(stringResource(Res.string.browse_all_categories))
                Text(
                    "Apple Podcasts top-level categories, plus AI",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CategoryGrid(
                    categories = PodcastCategories.all,
                    selectedKey = null,
                    onSelect = onCategory,
                )
            }
        }
        if (normalized.isBlank()) item {
            Text(stringResource(Res.string.search_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        val appleResultsCurrent = state.appleSearchQuery.equals(query.trim(), ignoreCase = true)
        if (appleResultsCurrent) state.appleSearchError?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
        if (appleResultsCurrent && normalized.isNotBlank() && !state.appleSearchLoading && state.appleSearchError == null && state.appleSearchResults.isEmpty()) item { Text(stringResource(Res.string.no_apple_podcasts)) }
        if (localResults.isNotEmpty()) {
            item { Text(stringResource(Res.string.in_your_library), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(localResults) { PodcastSearchRow(it, onPodcast) }
        }
        if (appleResultsCurrent && state.appleSearchResults.isNotEmpty()) item { Text(stringResource(Res.string.apple_podcasts), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        if (appleResultsCurrent) items(state.appleSearchResults) { PodcastSearchRow(it, onPreview) }
    }
}

@Composable
private fun CategorySearchResultsScreen(
    state: LibraryState,
    category: PodcastCategory,
    visibleCount: Int,
    onVisibleCountChange: (Int) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onPodcast: (Podcast) -> Unit,
    onPreview: (Podcast) -> Unit,
) {
    val resultsCurrent = state.appleCategoryKey == category.key
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            LibraryDetailHeader(category.displayName, stringResource(Res.string.back_to_search), onBack)
            Text(
                stringResource(Res.string.category_results),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        when {
            !resultsCurrent || state.appleCategoryLoading -> item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    Text(stringResource(Res.string.searching_apple_podcasts), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            state.appleCategoryError != null -> item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(state.appleCategoryError, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = onRetry) { Text(stringResource(Res.string.retry)) }
                }
            }
            state.appleCategoryResults.isEmpty() -> item {
                EmptyHint(stringResource(Res.string.no_category_podcasts))
            }
            else -> {
                item {
                    Text(
                        stringResource(Res.string.showing_results, minOf(visibleCount, state.appleCategoryResults.size), state.appleCategoryResults.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                items(state.appleCategoryResults.take(visibleCount)) { podcast ->
                    PodcastSearchRow(
                        podcast,
                        if (state.podcasts.any { it.id == podcast.id }) onPodcast else onPreview,
                    )
                }
                if (visibleCount < state.appleCategoryResults.size) {
                    item {
                        val remaining = state.appleCategoryResults.size - visibleCount
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onVisibleCountChange(nextCategoryVisibleCount(visibleCount, state.appleCategoryResults.size))
                            },
                            shape = RoundedCornerShape(24.dp),
                        ) {
                            Text(stringResource(Res.string.show_more, minOf(CATEGORY_PAGE_SIZE, remaining)))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryGrid(
    categories: List<PodcastCategory>,
    selectedKey: String?,
    onSelect: (PodcastCategory) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columnCount = if (maxWidth < 600.dp) 2 else 4
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            categories.chunked(columnCount).forEach { rowCategories ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowCategories.forEach { category ->
                        val selected = selectedKey == category.key
                        Surface(
                            modifier = Modifier.weight(1f).clickable { onSelect(category) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else AetherTheme.colors.glass,
                            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            border = BorderStroke(
                                1.dp,
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            ),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 18.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Text(
                                    category.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    repeat(columnCount - rowCategories.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun LibraryScreen(
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
private fun LibraryOverview(
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
        Triple(LibrarySection.LatestEpisodes, Icons.Default.QueueMusic, null),
        Triple(LibrarySection.RecentlyPlayed, Icons.Default.History, state.history.size.takeIf { it > 0 }),
    )
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text(stringResource(Res.string.library), style = MaterialTheme.typography.headlineLarge)
        }
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
                            title = section.title,
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
private fun LibraryMenuRow(
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
        Icon(Icons.Default.KeyboardArrowRight, "Open $title", tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LibrarySectionDetails(
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
        item { LibraryDetailHeader(section.title, "Back to Library", onBack) }
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
private fun LibraryDetailHeader(title: String, backDescription: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, backDescription) }
        Spacer(Modifier.width(4.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LibraryChannelRow(author: String, podcasts: List<Podcast>, onClick: () -> Unit) {
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
            Icon(Icons.Default.KeyboardArrowRight, "Open $author", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LibraryChannelDetails(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PodcastDetails(
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
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
private fun EpisodeRow(
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
                if (store != null) {
                    val favorite = episode.id in state.favoriteIds
                    IconButton(onClick = { store.setFavorite(episode.id, !favorite) }) { Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, if (favorite) "Remove favorite" else "Favorite") }
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
private fun EpisodeDetails(
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
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
                    IconButton(onClick = { store.setFavorite(episode.id, !favorite) }) {
                        Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, if (favorite) "Remove favorite" else "Favorite")
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
private fun DownloadAction(
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

private fun Download.downloadProgress(): Float? {
    val total = totalBytes?.takeIf { it > 0L } ?: return null
    return (receivedBytes.toDouble() / total.toDouble()).toFloat().coerceIn(0f, 1f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerScreen(
    player: PodcastPlayer,
    podcastTitle: String,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
) {
    val state by player.state.collectAsState()
    val episode = state.episode ?: return
    var optionsExpanded by remember { mutableStateOf(false) }
    var speedExpanded by remember { mutableStateOf(false) }
    var sleepExpanded by remember { mutableStateOf(false) }
    val playerMotion = rememberInfiniteTransition(label = "player motion")
    val aetherColors = AetherTheme.colors
    val auraScale by playerMotion.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(2_000), RepeatMode.Reverse),
        label = "artwork aura",
    )
    val artworkRotation by playerMotion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20_000, easing = LinearEasing)),
        label = "artwork rotation",
    )
    val playbackEnabled = player.capabilities.realPlayback
    val playbackLoading = state.status == PlayerStatus.Loading
    val durationMillis = state.durationMillis.coerceAtLeast(1L)
    val positionMillis = state.positionMillis.coerceIn(0L, durationMillis)
    val remainingMillis = (state.durationMillis - state.positionMillis).coerceAtLeast(0L)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AetherTheme.colors.glassStrong,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "NOW PLAYING",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        Text(
                            podcastTitle.ifBlank { episode.author.ifBlank { "Podcast episode" } },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.KeyboardArrowDown, "Collapse player", Modifier.size(32.dp))
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { optionsExpanded = true }) {
                            Icon(Icons.Default.MoreVert, "Player options")
                        }
                        DropdownMenu(expanded = optionsExpanded, onDismissRequest = { optionsExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text(if (darkTheme) "Use light theme" else "Use dark theme") },
                                leadingIcon = { Icon(if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) },
                                onClick = {
                                    optionsExpanded = false
                                    onToggleTheme()
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(
            Modifier.padding(padding).fillMaxSize().drawBehind {
                drawCircle(
                    color = aetherColors.purple.copy(alpha = if (darkTheme) 0.12f else 0.08f),
                    radius = size.maxDimension * 0.42f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.82f, size.height * 0.18f),
                )
                drawCircle(
                    color = aetherColors.teal.copy(alpha = if (darkTheme) 0.10f else 0.07f),
                    radius = size.maxDimension * 0.38f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.12f, size.height * 0.82f),
                )
            },
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().widthIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier.width(48.dp).height(5.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f), CircleShape),
                )
                Spacer(Modifier.height(28.dp))

                PlayerVinylArtwork(
                    episode = episode,
                    isPlaying = state.isPlaying,
                    auraScale = auraScale,
                    artworkRotation = artworkRotation,
                )

                Spacer(Modifier.height(32.dp))
                Text(
                    episode.title,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    episode.author.ifBlank { podcastTitle.ifBlank { "Podcast episode" } },
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (state.errorMessage != null || !playbackEnabled) {
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.82f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Text(
                            state.errorMessage ?: "Audio playback is not available on this platform yet.",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (state.errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))
                Slider(
                    value = positionMillis.toFloat(),
                    onValueChange = { player.seekTo(it.toLong()) },
                    enabled = playbackEnabled && !playbackLoading,
                    valueRange = 0f..durationMillis.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onSurface,
                        activeTrackColor = AetherTheme.colors.teal,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        disabledThumbColor = MaterialTheme.colorScheme.outline,
                        disabledActiveTrackColor = AetherTheme.colors.teal.copy(alpha = 0.45f),
                    ),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatDuration(positionMillis), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    Text("-${formatDuration(remainingMillis)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                }

                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    IconButton(
                        modifier = Modifier.size(58.dp),
                        enabled = playbackEnabled && !playbackLoading,
                        onClick = { player.skipBy(-15_000) },
                    ) { Icon(Icons.Default.FastRewind, "Back 15 seconds", Modifier.size(36.dp)) }
                    Box(
                        modifier = Modifier.size(84.dp).graphicsLayer {
                            if (state.isPlaying) {
                                scaleX = auraScale
                                scaleY = auraScale
                            }
                        }.background(AetherTheme.colors.actionGradient, CircleShape).clickable(
                            enabled = playbackEnabled && !playbackLoading,
                            onClick = player::toggle,
                        ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (playbackLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(34.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 3.dp,
                            )
                        } else {
                            Icon(
                                if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                if (state.isPlaying) "Pause" else "Play",
                                Modifier.size(46.dp),
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                    IconButton(
                        modifier = Modifier.size(58.dp),
                        enabled = playbackEnabled && !playbackLoading,
                        onClick = { player.skipBy(15_000) },
                    ) { Icon(Icons.Default.FastForward, "Forward 15 seconds", Modifier.size(36.dp)) }
                }

                Spacer(Modifier.height(28.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Box {
                        PlayerUtilityControl(
                            label = "Speed",
                            enabled = playbackEnabled && !playbackLoading,
                            onClick = { speedExpanded = true },
                        ) { color ->
                            Surface(color = Color.Transparent, shape = RoundedCornerShape(5.dp), border = BorderStroke(1.dp, color)) {
                                Text("${formatPlaybackSpeed(state.speed)}x", Modifier.padding(horizontal = 5.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = color)
                            }
                        }
                        DropdownMenu(expanded = speedExpanded, onDismissRequest = { speedExpanded = false }) {
                            PreviewPodcastPlayer.SPEEDS.forEach { speed ->
                                DropdownMenuItem(
                                    text = { Text("${formatPlaybackSpeed(speed)}x") },
                                    onClick = {
                                        player.setSpeed(speed)
                                        speedExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    Box {
                        PlayerUtilityControl(
                            label = "Sleep",
                            enabled = playbackEnabled && !playbackLoading,
                            active = state.sleepTimerEndsAtMillis != null,
                            onClick = { sleepExpanded = true },
                        ) { color -> Icon(Icons.Default.Timer, null, tint = color) }
                        DropdownMenu(expanded = sleepExpanded, onDismissRequest = { sleepExpanded = false }) {
                            listOf(15, 30, 60).forEach { minutes ->
                                DropdownMenuItem(
                                    text = { Text("$minutes minutes") },
                                    onClick = {
                                        player.setSleepTimer(minutes)
                                        sleepExpanded = false
                                    },
                                )
                            }
                            if (state.sleepTimerEndsAtMillis != null) {
                                DropdownMenuItem(
                                    text = { Text("Turn off sleep timer") },
                                    onClick = {
                                        player.setSleepTimer(null)
                                        sleepExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    PlayerUtilityControl(label = "Chapters", enabled = false, onClick = {}) { color ->
                        Icon(Icons.Default.ViewList, "Chapters unavailable", tint = color)
                    }
                    PlayerUtilityControl(label = "Share", enabled = false, onClick = {}) { color ->
                        Icon(Icons.Default.Share, "Sharing unavailable", tint = color)
                    }
                }

                Spacer(Modifier.height(32.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(enabled = false, onClick = {}) { Icon(Icons.Default.Cast, "Audio routing unavailable") }
                    IconButton(enabled = false, onClick = {}) { Icon(Icons.Default.QueueMusic, "Queue unavailable") }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun PlayerVinylArtwork(
    episode: Episode,
    isPlaying: Boolean,
    auraScale: Float,
    artworkRotation: Float,
) {
    Box(Modifier.size(268.dp), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.size(268.dp).graphicsLayer {
                scaleX = auraScale
                scaleY = auraScale
                alpha = if (isPlaying) 0.42f else 0.24f
            },
            shape = CircleShape,
            color = AetherTheme.colors.glow,
            shadowElevation = if (AetherTheme.colors.isDark) 22.dp else 8.dp,
        ) {}
        Box(
            modifier = Modifier.size(248.dp).graphicsLayer { rotationZ = if (isPlaying) artworkRotation else 0f }
                .clip(CircleShape).background(AetherTheme.colors.actionGradient),
            contentAlignment = Alignment.Center,
        ) {
            if (episode.artworkUrl.isNullOrBlank()) {
                Icon(Icons.Default.LibraryMusic, null, Modifier.size(92.dp), tint = MaterialTheme.colorScheme.onPrimary)
            } else {
                AsyncImage(
                    model = episode.artworkUrl,
                    contentDescription = "${episode.title} cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)),
            shadowElevation = 2.dp,
        ) {}
    }
}

@Composable
private fun PlayerUtilityControl(
    label: String,
    enabled: Boolean,
    active: Boolean = false,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
) {
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        active -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = Modifier.width(64.dp).clickable(enabled = enabled, onClick = onClick).padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.height(26.dp), contentAlignment = Alignment.Center) { icon(contentColor) }
        Text(label, style = MaterialTheme.typography.labelSmall, color = contentColor)
    }
}

@Composable
private fun MiniPlayer(episode: Episode, playing: Boolean, player: PodcastPlayer, expand: () -> Unit) {
    Surface(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        color = AetherTheme.colors.glassStrong,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp,
            if (AetherTheme.colors.isDark) AetherTheme.colors.glow else MaterialTheme.colorScheme.outlineVariant,
        ),
        shadowElevation = if (AetherTheme.colors.isDark) 14.dp else 6.dp,
    ) {
        Row(Modifier.fillMaxWidth().clickable(onClick = expand).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            PodcastArtwork(episode.artworkUrl, episode.title, 48)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(episode.title, maxLines = 1, style = MaterialTheme.typography.titleMedium)
                Text(stringResource(Res.string.now_playing), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
            Box(
                Modifier.size(44.dp).background(AetherTheme.colors.actionGradient, CircleShape).clickable(onClick = player::toggle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, if (playing) stringResource(Res.string.pause) else stringResource(Res.string.play), tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun PodcastSearchRow(podcast: Podcast, onPodcast: (Podcast) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onPodcast(podcast) },
        color = AetherTheme.colors.glass,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PodcastArtwork(podcast.artworkUrl, podcast.title, 64); Spacer(Modifier.width(14.dp))
            Column {
                Text(podcast.title, style = MaterialTheme.typography.titleMedium)
                Text(podcast.author, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ThemeToggle(darkTheme: Boolean, onToggle: () -> Unit) {
    IconButton(modifier = Modifier.size(40.dp), onClick = onToggle) {
        Icon(
            if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
            if (darkTheme) stringResource(Res.string.use_light_theme) else stringResource(Res.string.use_dark_theme),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    darkTheme: Boolean,
    player: PodcastPlayer,
    playerState: mammoth.mollie.caster.playback.PlayerState,
    downloadsSupported: Boolean,
    cellularDownloadControlSupported: Boolean,
    cellularDownloadsAllowed: Boolean,
    refreshing: Boolean,
    onToggleTheme: () -> Unit,
    onRefresh: () -> Unit,
    onManageDownloads: () -> Unit,
    onCellularDownloadsAllowedChange: (Boolean) -> Unit,
    onOpml: () -> Unit,
) {
    var speedExpanded by remember { mutableStateOf(false) }
    var sleepExpanded by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(Res.string.settings), style = MaterialTheme.typography.headlineLarge)
                    Text(
                        "Control appearance, listening, downloads, and your library data.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                SettingsGroup(stringResource(Res.string.appearance)) {
                    SettingsSwitchRow(
                        title = stringResource(Res.string.dark_theme),
                        summary = if (darkTheme) "Using the dark Aether theme" else "Using the light Aether theme",
                        checked = darkTheme,
                        onCheckedChange = { onToggleTheme() },
                    )
                }
            }
            item {
                SettingsGroup(stringResource(Res.string.playback)) {
                    Box {
                        SettingsActionRow(
                            title = stringResource(Res.string.playback_speed),
                            summary = "${formatPlaybackSpeed(playerState.speed)}x",
                            onClick = { speedExpanded = true },
                        )
                        DropdownMenu(expanded = speedExpanded, onDismissRequest = { speedExpanded = false }) {
                            PreviewPodcastPlayer.SPEEDS.forEach { speed ->
                                DropdownMenuItem(
                                    text = { Text("${formatPlaybackSpeed(speed)}x") },
                                    onClick = {
                                        player.setSpeed(speed)
                                        speedExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Box {
                        SettingsActionRow(
                            title = stringResource(Res.string.sleep_timer),
                            summary = playerState.sleepTimerEndsAtMillis?.let { "Active" } ?: "Off",
                            onClick = { sleepExpanded = true },
                        )
                        DropdownMenu(expanded = sleepExpanded, onDismissRequest = { sleepExpanded = false }) {
                            listOf(15, 30, 60).forEach { minutes ->
                                DropdownMenuItem(
                                    text = { Text("$minutes minutes") },
                                    onClick = {
                                        player.setSleepTimer(minutes)
                                        sleepExpanded = false
                                    },
                                )
                            }
                            if (playerState.sleepTimerEndsAtMillis != null) {
                                DropdownMenuItem(
                                    text = { Text("Turn off sleep timer") },
                                    onClick = {
                                        player.setSleepTimer(null)
                                        sleepExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingsActionRow(
                        title = "Skip interval",
                        summary = "15 seconds backward and forward",
                        enabled = false,
                        onClick = {},
                    )
                }
            }
            item {
                SettingsGroup(stringResource(Res.string.downloads)) {
                    if (cellularDownloadControlSupported) {
                        SettingsSwitchRow(
                            title = stringResource(Res.string.download_over_mobile_data),
                            summary = if (cellularDownloadsAllowed) "Downloads can use Wi-Fi or mobile data" else "Downloads use Wi-Fi only",
                            checked = cellularDownloadsAllowed,
                            onCheckedChange = onCellularDownloadsAllowedChange,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                    SettingsActionRow(
                        title = stringResource(Res.string.manage_downloads),
                        summary = if (downloadsSupported) "View downloaded episodes" else "Downloads are not available on this platform",
                        enabled = downloadsSupported,
                        onClick = onManageDownloads,
                    )
                }
            }
            item {
                SettingsGroup(stringResource(Res.string.library_and_data)) {
                    SettingsActionRow(
                        title = if (refreshing) stringResource(Res.string.refreshing_subscriptions) else stringResource(Res.string.refresh_subscriptions),
                        summary = "Check your followed podcasts for new episodes",
                        enabled = !refreshing,
                        onClick = onRefresh,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingsActionRow(
                        title = "Import or export OPML",
                        summary = "Move subscriptions between podcast apps",
                        onClick = onOpml,
                    )
                }
            }
            item {
                Text(
                    stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
        Surface(
            color = AetherTheme.colors.glass,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun SettingsActionRow(title: String, summary: String, enabled: Boolean = true, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick).padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsSwitchRow(title: String, summary: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun AetherFilterChip(selected: Boolean, onClick: () -> Unit, label: String) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.78f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = Color.Transparent,
        ),
    )
}
