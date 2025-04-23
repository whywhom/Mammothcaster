package com.mammoth.podcast.ui.podcast

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mammoth.podcast.R
import com.mammoth.podcast.core.designsystem.component.PodcastImage
import com.mammoth.podcast.core.designsystem.theme.Keyline1
import com.mammoth.podcast.core.model.EpisodeInfo
import com.mammoth.podcast.core.model.PodcastInfo
import com.mammoth.podcast.core.player.model.PlayerEpisode
import com.mammoth.podcast.ui.shared.EpisodeListItem
import com.mammoth.podcast.ui.shared.Loading
import com.mammoth.podcast.ui.shared.PreviewEpisodes
import com.mammoth.podcast.ui.shared.PreviewPodcasts
import com.mammoth.podcast.util.fullWidthItem
import kotlinx.coroutines.launch

@Composable
fun PodcastDetailsScreen(
    navigateToPlayer: (EpisodeInfo) -> Unit,
    navigateBack: () -> Unit,
    showBackButton: Boolean,
    modifier: Modifier = Modifier,
    podcastUri: String
) {

    val viewModel = hiltViewModel<PodcastDetailsViewModel, PodcastDetailsViewModel.PodcastDetailsViewModelFactory> { factory ->
        factory.create(podcastUri)
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val s = state) {
        is PodcastUiState.Loading -> {
            PodcastDetailsLoadingScreen(
                modifier = Modifier.fillMaxSize()
            )
        }
        is PodcastUiState.Ready -> {
            PodcastDetailsScreen(
                podcast = s.podcast,
                episodes = s.episodes,
                toggleSubscribe = viewModel::toggleSusbcribe,
                onQueueEpisode = viewModel::onQueueEpisode,
                navigateToPlayer = navigateToPlayer,
                navigateBack = navigateBack,
                showBackButton = showBackButton,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun PodcastDetailsLoadingScreen(
    modifier: Modifier = Modifier
) {
    Loading(modifier = modifier)
}

@Composable
fun PodcastDetailsScreen(
    podcast: PodcastInfo,
    episodes: List<EpisodeInfo>,
    toggleSubscribe: (PodcastInfo) -> Unit,
    onQueueEpisode: (PlayerEpisode) -> Unit,
    navigateToPlayer: (EpisodeInfo) -> Unit,
    navigateBack: () -> Unit,
    showBackButton: Boolean,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackBarText = stringResource(id = R.string.episode_added_to_your_queue)
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (showBackButton) {
                PodcastDetailsTopAppBar(
                    navigateBack = navigateBack,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { contentPadding ->
        PodcastDetailsContent(
            podcast = podcast,
            episodes = episodes,
            toggleSubscribe = toggleSubscribe,
            onQueueEpisode = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(snackBarText)
                }
                onQueueEpisode(it)
            },
            navigateToPlayer = navigateToPlayer,
            modifier = Modifier.padding(contentPadding)
        )
    }
}

@Composable
fun PodcastDetailsContent(
    podcast: PodcastInfo,
    episodes: List<EpisodeInfo>,
    toggleSubscribe: (PodcastInfo) -> Unit,
    onQueueEpisode: (PlayerEpisode) -> Unit,
    navigateToPlayer: (EpisodeInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(362.dp),
        modifier.fillMaxSize()
    ) {
        fullWidthItem {
            PodcastDetailsHeaderItem(
                podcast = podcast,
                toggleSubscribe = toggleSubscribe,
                modifier = Modifier.fillMaxWidth()
            )
        }
        items(episodes, key = { it.uri }) { episode ->
            EpisodeListItem(
                episode = episode,
                podcast = podcast,
                onClick = navigateToPlayer,
                onQueueEpisode = onQueueEpisode,
                modifier = Modifier.fillMaxWidth(),
                showPodcastImage = false,
                showSummary = true
            )
        }
    }
}

@Composable
fun PodcastDetailsHeaderItem(
    podcast: PodcastInfo,
    toggleSubscribe: (PodcastInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.padding(Keyline1)
    ) {
        val maxImageSize = this.maxWidth / 2
        val imageSize = min(maxImageSize, 148.dp)
        Column {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                PodcastImage(
                    modifier = Modifier
                        .size(imageSize)
                        .clip(MaterialTheme.shapes.large),
                    podcastImageUrl = podcast.imageUrl,
                    contentDescription = podcast.title
                )
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = podcast.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    PodcastDetailsHeaderItemButtons(
                        isSubscribed = podcast.isSubscribed ?: false,
                        onClick = {
                            toggleSubscribe(podcast)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            PodcastDetailsDescription(
                podcast = podcast,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
fun PodcastDetailsDescription(
    podcast: PodcastInfo,
    modifier: Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showSeeMore by remember { mutableStateOf(false) }
    Box(
        modifier = modifier.clickable { isExpanded = !isExpanded }
    ) {
        Text(
            text = podcast.description,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (isExpanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                showSeeMore = result.hasVisualOverflow
            },
            modifier = Modifier.animateContentSize(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = EaseOutExpo
                )
            )
        )
        if (showSeeMore) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // TODO: Add gradient effect
                Text(
                    text = stringResource(id = R.string.see_more),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
fun PodcastDetailsHeaderItemButtons(
    isSubscribed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.padding(top = 16.dp)) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSubscribed)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier.semantics(mergeDescendants = true) { }
        ) {
            Icon(
                imageVector = if (isSubscribed)
                    Icons.Default.Check
                else
                    Icons.Default.Add,
                contentDescription = null
            )
            Text(
                text = if (isSubscribed)
                    stringResource(id = R.string.subscribed)
                else
                    stringResource(id = R.string.subscribe),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = { /* TODO */ },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.cd_more)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastDetailsTopAppBar(
    title: String = "",
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.cd_back)
                )
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
fun PodcastDetailsHeaderItemPreview() {
    PodcastDetailsHeaderItem(
        podcast = PreviewPodcasts[0],
        toggleSubscribe = { },
    )
}

@Composable
fun PodcastDetailsScreenPreview() {
    PodcastDetailsScreen(
        podcast = PreviewPodcasts[0],
        episodes = PreviewEpisodes,
        toggleSubscribe = { },
        onQueueEpisode = { },
        navigateToPlayer = { },
        navigateBack = { },
        showBackButton = true,
    )
}
