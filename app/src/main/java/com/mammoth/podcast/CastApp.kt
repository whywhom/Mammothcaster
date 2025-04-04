package com.mammoth.podcast

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.window.layout.DisplayFeature
import com.mammoth.podcast.Screen.Companion.ARG_EPISODE_URI
import com.mammoth.podcast.ui.home.HomeViewModel
import com.mammoth.podcast.ui.home.MainScreen
import com.mammoth.podcast.ui.player.PlayerScreen
import com.mammoth.podcast.ui.player.PlayerViewModel
import android.Manifest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun CastApp(
    displayFeatures: List<DisplayFeature>,
    appState: CastAppState = rememberMammothAppState()
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        RequestNotificationPermissions()
    }
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
                        appState.navigateToPlayer(episode, backStackEntry)
                    },
                    viewModel = HomeViewModel()
                )
            }
            composable(Screen.Player.route) { backStackEntry ->
                val arguments = backStackEntry.arguments
                val uri = arguments?.getString(ARG_EPISODE_URI)
                uri?.let {
                    val viewModel = PlayerViewModel(
                        episodeUri = it,
                        context = MammothCastApp.appContext
                    )
                    PlayerScreen(
                        windowSizeClass = adaptiveInfo.windowSizeClass,
                        displayFeatures = displayFeatures,
                        onBackPress = appState::navigateBack,
                        viewModel = viewModel
                    )
                }
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

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun RequestNotificationPermissions() {
    // State to track whether notification permission is granted
    var hasNotificationPermission by remember { mutableStateOf(false) }

    // Request notification permission and update state based on the result
    val permissionResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasNotificationPermission = it }
    )

    // Request notification permission when the component is launched
    LaunchedEffect(key1 = true) {
        permissionResult.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}