package com.mammoth.podcast.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import com.mammoth.podcast.ui.home.PodcastScreen
import com.mammoth.podcast.ui.home.HomeViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 手动初始化依赖
//        val repository = PodcastRepository(podcastService, podcastDao, episodesDao)
        val viewModel = HomeViewModel()

        setContent {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = "Podcast App") },
                        actions = {
                            IconButton(onClick = { /* TODO: Handle search action */ }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                            IconButton(onClick = { /* TODO: Handle settings action */ }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                            }
                        }
                    )
                },
                content = { paddingValues ->
                    // Main content goes here
                    PodcastScreen(
                        Modifier.padding(paddingValues),
                        viewModel)
                }
            )
        }
    }
}