package com.mammoth.podcast

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mammoth.podcast.domain.model.EpisodeInfo

sealed class Screen(val route: String) {

    data object Home : Screen(NAVIGATOR_HOME)

    data object Player : Screen("player/{$ARG_EPISODE_URI}") {
        fun createRoute(episodeUri: String) :String {
            val path = "player/$episodeUri"
            return path
        }
    }

    data object PodcastDetails : Screen("podcast/{$ARG_PODCAST_URI}") {
        fun createRoute(podcastUri: String) = "podcast/$podcastUri"
    }

    data object Search : Screen("search/{$ARG_SEARCH_QUERY}") {
        fun createRoute(query: String?) :String {
            val path = "search/$query"
            return path
        }
    }

    companion object {
        val ARG_PODCAST_URI = "podcastUri"
        val ARG_EPISODE_URI = "episodeUri"
        val ARG_SEARCH_QUERY = "searchContent"

        const val NAVIGATOR_HOME = "home"
        const val NAVIGATOR_SEARCH = "search"
    }
}

@Composable
fun rememberMammothAppState(
    navController: NavHostController = rememberNavController(),
    context: Context = LocalContext.current
) = remember(navController, context) {
    CastAppState(navController, context)
}

class CastAppState(
    val navController: NavHostController,
    private val context: Context
) {
    var isOnline by mutableStateOf(checkIfOnline())
        private set

    fun refreshOnline() {
        isOnline = checkIfOnline()
    }

    fun navigateToPlayer(episode: EpisodeInfo, from: NavBackStackEntry) {
        // In order to discard duplicated navigation events, we check the Lifecycle
        if (from.lifecycleIsResumed()) {
            val encodedUri = Uri.encode(episode.uri)
            navController.navigate(Screen.Player.createRoute(encodedUri))
        }
    }

    fun navigateToSearch(query: String?, from: NavBackStackEntry) {
        if (from.lifecycleIsResumed()) {
            navController.navigate(Screen.Search.createRoute(""))
        }
    }

    fun navigateToPodcastDetails(podcastUri: String, from: NavBackStackEntry) {
        if (from.lifecycleIsResumed()) {
            val encodedUri = Uri.encode(podcastUri)
            navController.navigate(Screen.PodcastDetails.createRoute(encodedUri))
        }
    }

    fun navigateBack() {
        navController.popBackStack()
    }

    private fun checkIfOnline(): Boolean {
        val cm = getSystemService(context, ConnectivityManager::class.java)
        val capabilities = cm?.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

    }
}

/**
 * If the lifecycle is not resumed it means this NavBackStackEntry already processed a nav event.
 *
 * This is used to de-duplicate navigation events.
 */
private fun NavBackStackEntry.lifecycleIsResumed() =
    this.lifecycle.currentState == Lifecycle.State.RESUMED
