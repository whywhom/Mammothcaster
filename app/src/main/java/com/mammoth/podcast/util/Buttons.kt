package com.mammoth.podcast.util

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mammoth.podcast.R

@Composable
fun ToggleFollowPodcastIconButton(
    isFollowed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clickLabel = stringResource(if (isFollowed) R.string.cd_unfollow else R.string.cd_follow)
    IconButton(
        onClick = onClick,
        modifier = modifier.semantics {
            onClick(label = clickLabel, action = null)
        }
    ) {
        Icon(
            // TODO: think about animating these icons
            imageVector = when {
                isFollowed -> Icons.Default.Check
                else -> Icons.Default.Add
            },
            contentDescription = when {
                isFollowed -> stringResource(R.string.cd_following)
                else -> stringResource(R.string.cd_not_following)
            },
            tint = animateColorAsState(
                when {
                    isFollowed -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.primary
                }
            ).value,
            modifier = Modifier
                .shadow(
                    elevation = animateDpAsState(if (isFollowed) 0.dp else 1.dp).value,
                    shape = MaterialTheme.shapes.small
                )
                .background(
                    color = animateColorAsState(
                        when {
                            isFollowed -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceContainerHighest
                        }
                    ).value,
                    shape = CircleShape
                )
                .padding(4.dp)
        )
    }
}
