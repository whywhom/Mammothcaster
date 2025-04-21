package com.mammoth.media

import androidx.compose.ui.graphics.vector.ImageVector

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

sealed class Screen(val rout: String) {
    object Home: Screen("home_screen")
    object Podcast: Screen("podcast_screen")
    object Ebook: Screen("ebook_screen")
    object Setting: Screen("setting_screen")
}