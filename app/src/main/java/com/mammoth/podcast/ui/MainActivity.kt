package com.mammoth.podcast.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import com.google.accompanist.adaptive.calculateDisplayFeatures
import com.mammoth.podcast.CastApp
import com.mammoth.podcast.ui.theme.MammothTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        val viewModel = HomeViewModel()

//        setContent {
//            Scaffold(
//                topBar = {
//                    TopAppBar(
//                        title = { Text(text = "Podcast App") },
//                        actions = {
//                            IconButton(onClick = { /* TODO: Handle search action */ }) {
//                                Icon(Icons.Default.Search, contentDescription = "Search")
//                            }
//                            IconButton(onClick = { /* TODO: Handle settings action */ }) {
//                                Icon(Icons.Default.Settings, contentDescription = "Settings")
//                            }
//                        }
//                    )
//                },
//                content = { paddingValues ->
//                    // Main content goes here
//                    PodcastScreen(
//                        Modifier.padding(paddingValues),
//                        viewModel)
//                }
//            )
//        }
        enableEdgeToEdge()

        setContent {
            val displayFeatures = calculateDisplayFeatures(this)
            MammothTheme {
                CastApp(
                    displayFeatures
                )
            }
        }
    }
}