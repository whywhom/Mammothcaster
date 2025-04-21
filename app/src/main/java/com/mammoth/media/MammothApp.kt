package com.mammoth.media

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.mammoth.media.ui.EbookScreen
import com.mammoth.media.ui.HomeScreen
import com.mammoth.media.ui.podcast.PodcastScreen
import com.mammoth.media.ui.SettingScreen

@Composable
fun MammothApp() {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomNavigationBar(navController)}
    ) { innerPadding ->
        val graph =
            navController.createGraph(startDestination = Screen.Podcast.rout) {
                composable(route = Screen.Home.rout) {
                    HomeScreen()
                }
                composable(route = Screen.Ebook.rout) {
                    EbookScreen()
                }
                composable(route = Screen.Setting.rout) {
                    SettingScreen()
                }
                composable(route = Screen.Podcast.rout) {
                    PodcastScreen()
                }
            }
        NavHost(
            navController = navController,
            graph = graph,
            modifier = Modifier.padding(innerPadding),
            popExitTransition = { scaleOut(targetScale = 0.9f) },
            popEnterTransition = { EnterTransition.None }
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationItems = listOf(
//        NavigationItem(
//            title = "Home",
//            icon = Icons.Default.Home,
//            route = Screen.Home.rout
//        ),
        NavigationItem(
            title = "Podcast",
            icon = Icons.Default.PlayArrow,
            route = Screen.Podcast.rout
        ),
        NavigationItem(
            title = "Ebook",
            icon = Icons.Default.Favorite,
            route = Screen.Ebook.rout
        ),
//        NavigationItem(
//            title = "Setting",
//            icon = Icons.Default.Settings,
//            route = Screen.Setting.rout
//        ),
    )

    NavigationBar(
        containerColor = Color.White
    ) {
        navigationItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = currentRoute == item.route, //selectedNavigationIndex.intValue == index,
                onClick = {
                    navController.navigate(item.route) {
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Pop up to the start destination of the graph to clear intermediate destinations
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Restore state if reselecting a previously selected item
                        restoreState = true
                    }
                },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.title)
                },
                label = {
                    Text(
                        item.title,
                        color = if (currentRoute == item.route)
                            Color.Black
                        else Color.Gray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.surface,
                    indicatorColor = MaterialTheme.colorScheme.primary
                )

            )
        }
    }
}
