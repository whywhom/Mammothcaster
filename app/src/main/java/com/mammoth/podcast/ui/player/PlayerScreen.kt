package com.mammoth.podcast.ui.player

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.window.layout.DisplayFeature
import androidx.window.layout.FoldingFeature
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import com.google.accompanist.adaptive.VerticalTwoPaneStrategy
import com.mammoth.podcast.R
import com.mammoth.podcast.component.HtmlTextContainer
import com.mammoth.podcast.component.ImageBackgroundColorScrim
import com.mammoth.podcast.component.PodcastImage
import com.mammoth.podcast.ui.player.model.PlayerEpisode
import com.mammoth.podcast.ui.theme.MammothTheme
import com.mammoth.podcast.util.isBookPosture
import com.mammoth.podcast.util.isSeparatingPosture
import com.mammoth.podcast.util.isTableTopPosture
import com.mammoth.podcast.util.verticalGradientScrim
import kotlinx.coroutines.launch
import java.time.Duration

/**
 * Stateful version of the Podcast player
 */
@Composable
fun PlayerScreen(
    windowSizeClass: WindowSizeClass,
    displayFeatures: List<DisplayFeature>,
    onBackPress: () -> Unit,
    viewModel: PlayerViewModel
) {
    val uiState = viewModel.uiState.collectAsState()
    PlayerScreen(
        viewModel = viewModel,
        uiState = uiState.value,
        windowSizeClass = windowSizeClass,
        displayFeatures = displayFeatures,
        onBackPress = onBackPress,
        onAddToQueue = viewModel::onAddToQueue,
        onStop = viewModel::onStop,
        playerControlActions = PlayerControlActions(
            onPlayPress = viewModel::onPlay,
            onPausePress = viewModel::onPause,
            onAdvanceBy = viewModel::onAdvanceBy,
            onRewindBy = viewModel::onRewindBy,
            onSeekingStarted = viewModel::onSeekingStarted,
            onSeekingFinished = viewModel::onSeekingFinished,
            onNext = viewModel::onNext,
            onPrevious = viewModel::onPrevious,
        ),
    )
}

/**
 * Stateless version of the Player screen
 */
@Composable
private fun PlayerScreen(
    viewModel: PlayerViewModel,
    uiState: PlayerUiState,
    windowSizeClass: WindowSizeClass,
    displayFeatures: List<DisplayFeature>,
    onBackPress: () -> Unit,
    onAddToQueue: () -> Unit,
    onStop: () -> Unit,
    playerControlActions: PlayerControlActions,
    modifier: Modifier = Modifier
) {
    DisposableEffect(Unit) {
        onDispose {
            onStop()
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val snackBarText = stringResource(id = R.string.episode_added_to_your_queue)
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier
    ) { contentPadding ->
        if (uiState.episodePlayerState.currentEpisode != null) {
            PlayerContentWithBackground(
                viewModel = viewModel,
                uiState = uiState,
                windowSizeClass = windowSizeClass,
                displayFeatures = displayFeatures,
                onBackPress = onBackPress,
                onAddToQueue = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(snackBarText)
                    }
                    onAddToQueue()
                },
                playerControlActions = playerControlActions,
                contentPadding = contentPadding,
            )
        } else {
            FullScreenLoading()
        }
    }
}

@Composable
private fun PlayerBackground(
    episode: PlayerEpisode?,
    modifier: Modifier,
) {
    ImageBackgroundColorScrim(
        url = episode?.podcastImageUrl,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        modifier = modifier,
    )
}

@Composable
fun PlayerContentWithBackground(
    viewModel: PlayerViewModel,
    uiState: PlayerUiState,
    windowSizeClass: WindowSizeClass,
    displayFeatures: List<DisplayFeature>,
    onBackPress: () -> Unit,
    onAddToQueue: () -> Unit,
    playerControlActions: PlayerControlActions,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        PlayerBackground(
            episode = uiState.episodePlayerState.currentEpisode,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        )
        PlayerContent(
            viewModel = viewModel,
            uiState = uiState,
            windowSizeClass = windowSizeClass,
            displayFeatures = displayFeatures,
            onBackPress = onBackPress,
            onAddToQueue = onAddToQueue,
            playerControlActions = playerControlActions,
        )
    }
}

/**
 * Wrapper around all actions for the player controls.
 */
data class PlayerControlActions(
    val onPlayPress: () -> Unit,
    val onPausePress: () -> Unit,
    val onAdvanceBy: (Duration) -> Unit,
    val onRewindBy: (Duration) -> Unit,
    val onNext: () -> Unit,
    val onPrevious: () -> Unit,
    val onSeekingStarted: () -> Unit,
    val onSeekingFinished: (newElapsed: Duration) -> Unit,
)

@Composable
fun PlayerContent(
    viewModel: PlayerViewModel,
    uiState: PlayerUiState,
    windowSizeClass: WindowSizeClass,
    displayFeatures: List<DisplayFeature>,
    onBackPress: () -> Unit,
    onAddToQueue: () -> Unit,
    playerControlActions: PlayerControlActions,
    modifier: Modifier = Modifier
) {
    val foldingFeature = displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()

    // Use a two pane layout if there is a fold impacting layout (meaning it is separating
    // or non-flat) or if we have a large enough width to show both.
    if (
        windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED ||
        isBookPosture(foldingFeature) ||
        isTableTopPosture(foldingFeature) ||
        isSeparatingPosture(foldingFeature)
    ) {
        // Determine if we are going to be using a vertical strategy (as if laying out
        // both sides in a column). We want to do so if we are in a tabletop posture,
        // or we have an impactful horizontal fold. Otherwise, we'll use a horizontal strategy.
        val usingVerticalStrategy =
            isTableTopPosture(foldingFeature) ||
                (
                    isSeparatingPosture(foldingFeature) &&
                        foldingFeature.orientation == FoldingFeature.Orientation.HORIZONTAL
                    )

        if (usingVerticalStrategy) {
            TwoPane(
                first = {
                    PlayerContentTableTopTop(
                        uiState = uiState,
                    )
                },
                second = {
                    PlayerContentTableTopBottom(
                        viewModel = viewModel,
                        uiState = uiState,
                        onBackPress = onBackPress,
                        onAddToQueue = onAddToQueue,
                        playerControlActions = playerControlActions,
                    )
                },
                strategy = VerticalTwoPaneStrategy(splitFraction = 0.5f),
                displayFeatures = displayFeatures,
                modifier = modifier,
            )
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalGradientScrim(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.50f),
                        startYPercentage = 1f,
                        endYPercentage = 0f
                    )
                    .systemBarsPadding()
                    .padding(horizontal = 8.dp)
            ) {
                TopAppBar(
                    onBackPress = onBackPress,
                    onAddToQueue = onAddToQueue,
                )
                TwoPane(
                    first = {
                        PlayerContentBookStart(uiState = uiState)
                    },
                    second = {
                        PlayerContentBookEnd(
                            viewModel = viewModel,
                            uiState = uiState,
                            playerControlActions = playerControlActions,
                        )
                    },
                    strategy = HorizontalTwoPaneStrategy(splitFraction = 0.5f),
                    displayFeatures = displayFeatures
                )
            }
        }
    } else {
        PlayerContentRegular(
            viewModel = viewModel,
            uiState = uiState,
            onBackPress = onBackPress,
            onAddToQueue = onAddToQueue,
            playerControlActions = playerControlActions,
            modifier = modifier,
        )
    }
}

/**
 * The UI for the top pane of a tabletop layout.
 */
@Composable
private fun PlayerContentRegular(
    viewModel: PlayerViewModel,
    uiState: PlayerUiState,
    onBackPress: () -> Unit,
    onAddToQueue: () -> Unit,
    playerControlActions: PlayerControlActions,
    modifier: Modifier = Modifier
) {
    val playerEpisode = uiState.episodePlayerState
    val currentEpisode = playerEpisode.currentEpisode ?: return
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalGradientScrim(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.50f),
                startYPercentage = 1f,
                endYPercentage = 0f
            )
            .systemBarsPadding()
            .padding(horizontal = 8.dp)
    ) {
        TopAppBar(
            onBackPress = onBackPress,
            onAddToQueue = onAddToQueue,
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            PlayerImage(
                podcastImageUrl = currentEpisode.podcastImageUrl,
                modifier = Modifier.weight(10f)
            )
            Spacer(modifier = Modifier.height(32.dp))
            PodcastDescription(currentEpisode.title, currentEpisode.podcastName)
            Spacer(modifier = Modifier.height(32.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(10f)
            ) {
                PlayerSlider(
                    viewModel = viewModel,
                    timeElapsed = playerEpisode.timeElapsed,
                    episodeDuration = currentEpisode.duration,
                    onSeekingStarted = playerControlActions.onSeekingStarted,
                    onSeekingFinished = playerControlActions.onSeekingFinished
                )
                PlayerButtons(
                    viewModel = viewModel,
                    hasNext = playerEpisode.queue.isNotEmpty(),
                    isPlaying = playerEpisode.isPlaying,
                    onPlayPress = playerControlActions.onPlayPress,
                    onPausePress = playerControlActions.onPausePress,
                    onAdvanceBy = playerControlActions.onAdvanceBy,
                    onRewindBy = playerControlActions.onRewindBy,
                    onNext = playerControlActions.onNext,
                    onPrevious = playerControlActions.onPrevious,
                    Modifier.padding(vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * The UI for the top pane of a tabletop layout.
 */
@Composable
private fun PlayerContentTableTopTop(
    uiState: PlayerUiState,
    modifier: Modifier = Modifier
) {
    // Content for the top part of the screen
    val episode = uiState.episodePlayerState.currentEpisode ?: return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalGradientScrim(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.50f),
                startYPercentage = 1f,
                endYPercentage = 0f
            )
            .windowInsetsPadding(
                WindowInsets.systemBars.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Top
                )
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PlayerImage(episode.podcastImageUrl)
    }
}

/**
 * The UI for the bottom pane of a tabletop layout.
 */
@Composable
private fun PlayerContentTableTopBottom(
    viewModel: PlayerViewModel,
    uiState: PlayerUiState,
    onBackPress: () -> Unit,
    onAddToQueue: () -> Unit,
    playerControlActions: PlayerControlActions,
    modifier: Modifier = Modifier
) {
    val episodePlayerState = uiState.episodePlayerState
    val episode = uiState.episodePlayerState.currentEpisode ?: return
    // Content for the table part of the screen
    Column(
        modifier = modifier
            .windowInsetsPadding(
                WindowInsets.systemBars.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
            )
            .padding(horizontal = 32.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            onBackPress = onBackPress,
            onAddToQueue = onAddToQueue,
        )
        PodcastDescription(
            title = episode.title,
            podcastName = episode.podcastName,
            titleTextStyle = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.weight(0.5f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(10f)
        ) {
            PlayerButtons(
                viewModel = viewModel,
                hasNext = episodePlayerState.queue.isNotEmpty(),
                isPlaying = episodePlayerState.isPlaying,
                onPlayPress = playerControlActions.onPlayPress,
                onPausePress = playerControlActions.onPausePress,
                playerButtonSize = 92.dp,
                onAdvanceBy = playerControlActions.onAdvanceBy,
                onRewindBy = playerControlActions.onRewindBy,
                onNext = playerControlActions.onNext,
                onPrevious = playerControlActions.onPrevious,
                modifier = Modifier.padding(top = 8.dp)
            )
            PlayerSlider(
                viewModel = viewModel,
                timeElapsed = episodePlayerState.timeElapsed,
                episodeDuration = episode.duration,
                onSeekingStarted = playerControlActions.onSeekingStarted,
                onSeekingFinished = playerControlActions.onSeekingFinished
            )
        }
    }
}

/**
 * The UI for the start pane of a book layout.
 */
@Composable
private fun PlayerContentBookStart(
    uiState: PlayerUiState,
    modifier: Modifier = Modifier
) {
    val episode = uiState.episodePlayerState.currentEpisode ?: return
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                vertical = 40.dp,
                horizontal = 16.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PodcastInformation(
            title = episode.title,
            name = episode.podcastName,
            summary = episode.summary,
        )
    }
}

/**
 * The UI for the end pane of a book layout.
 */
@Composable
private fun PlayerContentBookEnd(
    viewModel: PlayerViewModel,
    uiState: PlayerUiState,
    playerControlActions: PlayerControlActions,
    modifier: Modifier = Modifier
) {
    val episodePlayerState = uiState.episodePlayerState
    val episode = episodePlayerState.currentEpisode ?: return
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround,
    ) {
        PlayerImage(
            podcastImageUrl = episode.podcastImageUrl,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .weight(1f)
        )
        PlayerSlider(
            viewModel = viewModel,
            timeElapsed = episodePlayerState.timeElapsed,
            episodeDuration = episode.duration,
            onSeekingStarted = playerControlActions.onSeekingStarted,
            onSeekingFinished = playerControlActions.onSeekingFinished,
        )
        PlayerButtons(
            viewModel = viewModel,
            hasNext = episodePlayerState.queue.isNotEmpty(),
            isPlaying = episodePlayerState.isPlaying,
            onPlayPress = playerControlActions.onPlayPress,
            onPausePress = playerControlActions.onPausePress,
            onAdvanceBy = playerControlActions.onAdvanceBy,
            onRewindBy = playerControlActions.onRewindBy,
            onNext = playerControlActions.onNext,
            onPrevious = playerControlActions.onPrevious,
            Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun TopAppBar(
    onBackPress: () -> Unit,
    onAddToQueue: () -> Unit,
) {
    Row(Modifier.fillMaxWidth()) {
        IconButton(onClick = onBackPress) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back)
            )
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onAddToQueue) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                contentDescription = stringResource(R.string.cd_add)
            )
        }
        IconButton(onClick = { /* TODO */ }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.cd_more)
            )
        }
    }
}

@Composable
private fun PlayerImage(
    podcastImageUrl: String,
    modifier: Modifier = Modifier
) {
    PodcastImage(
        podcastImageUrl = podcastImageUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .sizeIn(maxWidth = 500.dp, maxHeight = 500.dp)
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.medium)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PodcastDescription(
    title: String,
    podcastName: String,
    titleTextStyle: TextStyle = MaterialTheme.typography.headlineSmall
) {
    Text(
        text = title,
        style = titleTextStyle,
        maxLines = 1,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.basicMarquee()
    )
    Text(
        text = podcastName,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1
    )
}

@Composable
private fun PodcastInformation(
    title: String,
    name: String,
    summary: String,
    modifier: Modifier = Modifier,
    titleTextStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    nameTextStyle: TextStyle = MaterialTheme.typography.displaySmall,
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = name,
            style = nameTextStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = title,
            style = titleTextStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        HtmlTextContainer(text = summary) {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = LocalContentColor.current
            )
        }
    }
}

fun Duration.formatString(): String {
    val minutes = this.toMinutes().toString().padStart(2, '0')
    val secondsLeft = (this.toSeconds() % 60).toString().padStart(2, '0')
    return "$minutes:$secondsLeft"
}

@Composable
private fun PlayerSlider(
    viewModel: PlayerViewModel,
    timeElapsed: Duration,
    episodeDuration: Duration?,
    onSeekingStarted: () -> Unit,
    onSeekingFinished: (newElapsed: Duration) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        var sliderValue by remember(timeElapsed) { mutableStateOf(timeElapsed) }
        val maxRange = (episodeDuration?.toSeconds() ?: 0).toFloat()

        Row(Modifier.fillMaxWidth()) {
            Text(
                text = "${sliderValue.formatString()} • ${episodeDuration?.formatString()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Slider(
            value = sliderValue.seconds.toFloat(),
            valueRange = 0f..maxRange,
            onValueChange = {
                onSeekingStarted()
                sliderValue = Duration.ofSeconds(it.toLong())
            },
            onValueChangeFinished = { onSeekingFinished(sliderValue) }
        )
    }
}

@Composable
private fun PlayerButtons(
    viewModel: PlayerViewModel,
    hasNext: Boolean,
    isPlaying: Boolean,
    onPlayPress: () -> Unit,
    onPausePress: () -> Unit,
    onAdvanceBy: (Duration) -> Unit,
    onRewindBy: (Duration) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
    playerButtonSize: Dp = 72.dp,
    sideButtonSize: Dp = 48.dp,
) {
    val uiState = viewModel.uiState.collectAsState()
    Log.d("andy_test", "PlayerButtons isPlaying = ${uiState.value.episodePlayerState.isPlaying}")
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val sideButtonsModifier = Modifier
            .size(sideButtonSize)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = CircleShape
            )
            .semantics { role = Role.Button }

        val primaryButtonModifier = Modifier
            .size(playerButtonSize)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            )
            .semantics { role = Role.Button }

        Image(
            imageVector = Icons.Filled.SkipPrevious,
            contentDescription = stringResource(R.string.cd_skip_previous),
            contentScale = ContentScale.Inside,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
            modifier = sideButtonsModifier
                .clickable(enabled = isPlaying, onClick = onPrevious)
                .alpha(if (isPlaying) 1f else 0.25f)
        )
        Image(
            imageVector = Icons.Filled.Replay10,
            contentDescription = stringResource(R.string.cd_replay10),
            contentScale = ContentScale.Inside,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            modifier = sideButtonsModifier
                .clickable {
                    onRewindBy(Duration.ofSeconds(10))
                }
        )
        if (uiState.value.episodePlayerState.isPlaying) {
            Image(
                imageVector = Icons.Outlined.Pause,
                contentDescription = stringResource(R.string.cd_pause),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                modifier = primaryButtonModifier
                    .padding(8.dp)
                    .clickable {
                        onPausePress()
                    }
            )
        } else {
            if (uiState.value.episodePlayerState.exoPlayState == ExoPlayer.STATE_BUFFERING) {
                CircularProgressIndicator(
                    modifier = primaryButtonModifier
                        .padding(16.dp)
                        .clickable {},
                    color = Color.Black,
                    strokeWidth = 4.dp
                )
            } else {
                Image(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = stringResource(R.string.cd_play),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                    modifier = primaryButtonModifier
                        .padding(8.dp)
                        .clickable {
                            onPlayPress()
                        }
                )
            }
        }
        Image(
            imageVector = Icons.Filled.Forward10,
            contentDescription = stringResource(R.string.cd_forward10),
            contentScale = ContentScale.Inside,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            modifier = sideButtonsModifier
                .clickable {
                    onAdvanceBy(Duration.ofSeconds(10))
                }
        )
        Image(
            imageVector = Icons.Filled.SkipNext,
            contentDescription = stringResource(R.string.cd_skip_next),
            contentScale = ContentScale.Inside,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
            modifier = sideButtonsModifier
                .clickable(enabled = hasNext, onClick = onNext)
                .alpha(if (hasNext) 1f else 0.25f)
        )
    }
}

/**
 * Full screen circular progress indicator
 */
@Composable
private fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        CircularProgressIndicator()
    }
}

@Preview
@Composable
fun TopAppBarPreview() {
    MammothTheme {
        TopAppBar(
            onBackPress = {},
            onAddToQueue = {},
        )
    }
}

@Preview
@Composable
fun PlayerButtonsPreview() {
    MammothTheme {
        PlayerButtons(
            viewModel = PlayerViewModel(context = LocalContext.current, episodeUri = ""),
            hasNext = false,
            isPlaying = true,
            onPlayPress = {},
            onPausePress = {},
            onAdvanceBy = {},
            onRewindBy = {},
            onNext = {},
            onPrevious = {},
        )
    }
}

@Composable
fun PlayerScreenPreview() {
    MammothTheme {
        BoxWithConstraints {
            PlayerScreen(
                viewModel = PlayerViewModel(context = LocalContext.current, episodeUri = ""),
                PlayerUiState(
                    episodePlayerState = EpisodePlayerState(
                        currentEpisode = PlayerEpisode(
                            title = "Title",
                            duration = Duration.ofHours(2),
                            podcastName = "Podcast",
                        ),
                        isPlaying = false,
                        queue = listOf(
                            PlayerEpisode(),
                            PlayerEpisode(),
                            PlayerEpisode(),
                        )
                    ),
                ),
                displayFeatures = emptyList(),
                windowSizeClass = WindowSizeClass.compute(maxWidth.value, maxHeight.value),
                onBackPress = { },
                onAddToQueue = {},
                onStop = {},
                playerControlActions = PlayerControlActions(
                    onPlayPress = {},
                    onPausePress = {},
                    onAdvanceBy = {},
                    onRewindBy = {},
                    onSeekingStarted = {},
                    onSeekingFinished = {},
                    onNext = {},
                    onPrevious = {},
                )
            )
        }
    }
}
