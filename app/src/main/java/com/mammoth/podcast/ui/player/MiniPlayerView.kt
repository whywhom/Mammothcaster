package com.mammoth.podcast.ui.player

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import com.mammoth.podcast.R
import java.time.Duration

/**
 * Stateful version of the Podcast player
 */
@Composable
private fun MiniPlayerView(
    uiState: PlayerUiState,
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
    Log.d("andy_test", "PlayerButtons isPlaying = ${uiState.episodePlayerState.isPlaying}")
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
        if (uiState.episodePlayerState.isPlaying) {
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
            if (uiState.episodePlayerState.exoPlayState == ExoPlayer.STATE_BUFFERING) {
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
