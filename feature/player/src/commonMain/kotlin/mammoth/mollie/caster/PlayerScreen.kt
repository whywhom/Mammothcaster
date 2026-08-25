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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.playback.PlayerStatus
import mammoth.mollie.caster.playback.PodcastPlayer
import mammoth.mollie.caster.playback.PreviewPodcastPlayer
import mammoth.mollie.caster.ui.components.PodcastArtwork
import mammoth.mollie.caster.ui.format.formatDuration
import mammoth.mollie.caster.ui.format.formatPlaybackSpeed
import mammoth.mollie.caster.ui.localization.stringResource
import mammoth.mollie.caster.ui.theme.AetherTheme
import molliecaster.shared.generated.resources.Res
import molliecaster.shared.generated.resources.now_playing
import molliecaster.shared.generated.resources.pause
import molliecaster.shared.generated.resources.play

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    player: PodcastPlayer,
    podcastTitle: String,
    darkTheme: Boolean,
    onBack: () -> Unit,
) {
    val state by player.state.collectAsState()
    val episode = state.episode ?: return
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AetherTheme.colors.glassStrong,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                        Icon(Icons.AutoMirrored.Filled.ViewList, "Chapters unavailable", tint = color)
                    }
                    PlayerUtilityControl(label = "Share", enabled = false, onClick = {}) { color ->
                        Icon(Icons.Default.Share, "Sharing unavailable", tint = color)
                    }
                }

                Spacer(Modifier.height(32.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(enabled = false, onClick = {}) { Icon(Icons.Default.Cast, "Audio routing unavailable") }
                    IconButton(enabled = false, onClick = {}) { Icon(Icons.AutoMirrored.Filled.QueueMusic, "Queue unavailable") }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun PlayerVinylArtwork(
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
fun PlayerUtilityControl(
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
fun MiniPlayer(episode: Episode, playing: Boolean, player: PodcastPlayer, expand: () -> Unit) {
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
