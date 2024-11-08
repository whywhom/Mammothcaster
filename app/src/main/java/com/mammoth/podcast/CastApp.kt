package com.mammoth.podcast

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.window.layout.DisplayFeature
import com.mammoth.podcast.ui.home.HomeViewModel
import com.mammoth.podcast.ui.home.MainScreen
import com.mammoth.podcast.ui.player.PlayerScreen
import com.mammoth.podcast.ui.player.PlayerViewModel

@Composable
fun CastApp(
    displayFeatures: List<DisplayFeature>,
    appState: CastAppState = rememberJetcasterAppState()
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    if (appState.isOnline) {
        NavHost(
            navController = appState.navController,
            startDestination = Screen.Home.route
        ) {
            composable(Screen.Home.route) { backStackEntry ->
                MainScreen(
                    windowSizeClass = adaptiveInfo.windowSizeClass,
                    navigateToPlayer = { episode ->
                        appState.navigateToPlayer(episode.uri, backStackEntry)
                    },
                    viewModel = HomeViewModel()
                )
            }
            composable(Screen.Player.route) { backStackEntry ->
                PlayerScreen(
                    windowSizeClass = adaptiveInfo.windowSizeClass,
                    displayFeatures = displayFeatures,
                    onBackPress = appState::navigateBack,
                    PlayerViewModel( savedStateHandle = backStackEntry.savedStateHandle)
                )
            }
        }
    } else {
        OfflineDialog { appState.refreshOnline() }
    }
}

@Composable
fun OfflineDialog(onRetry: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(text = stringResource(R.string.connection_error_title)) },
        text = { Text(text = stringResource(R.string.connection_error_message)) },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry_label))
            }
        }
    )
}
