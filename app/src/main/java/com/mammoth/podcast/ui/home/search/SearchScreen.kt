package com.mammoth.podcast.ui.home.search

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.mammoth.podcast.Screen
import com.mammoth.podcast.data.model.PodcastSearchResult
import com.mammoth.podcast.data.model.ResultItem
import com.mammoth.podcast.util.isCompact

@Composable
fun SearchScreen(
    query: String?,
    navController: NavHostController,
    viewModel: SearchViewModel = viewModel()
) {
    var showMore by remember { mutableStateOf(false) }
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val podcastTopResult by viewModel.podcastTopResult.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val navigationState by viewModel.navigationState.collectAsStateWithLifecycle()
    // Observe navigation state
    LaunchedEffect(navigationState) {
        navigationState.let { feed ->
            if (feed.isNotEmpty()) {
                viewModel.clearNavigationState()
                navController.navigate(
                    Screen.ItunePodcastDetails.createRoute(
                        ituneUri = Uri.encode(feed[0].feedUrl),
                        title = feed[0].artistName?:""
                    )
                )
            }
        }
    }
    LaunchedEffect(query) {
        viewModel.getTop()
    }
    Scaffold(
        topBar = {
            SearchAppBar(
                isExpanded = currentWindowAdaptiveInfo().windowSizeClass.isCompact,
                modifier = Modifier.fillMaxWidth(),
                search = { viewModel.search(it) },
                onBackPress = { navController.popBackStack() }
            )
        }
    ) { padding ->
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        } else if (errorMessage != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
            ) {
                Text(
                    text = errorMessage ?: "An error occurred",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        } else {
            if (searchResults.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding)
                ) {
                    Text(
                        text = "Top 20 by Apple Podcasts",
                        modifier = Modifier.padding(8.dp)
                    )
                    if (!showMore) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4), // 4-column grid
                            modifier = Modifier.fillMaxWidth(), // Fill available space
                            contentPadding = PaddingValues(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            items(podcastTopResult.take(12)) { podcast -> // Show top 9 items
                                SearchTopResultItemGrid(
                                    navController = navController,
                                    viewModel = viewModel,
                                    result = podcast)
                            }
                        }

                        TextButton(
                            onClick = { showMore = !showMore },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End // Aligns content to the end of the Row
                            ) {
                                Text(text = "Discover more >>")
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            LazyColumn(
                                modifier = Modifier.weight(1f), // Fill available space
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                items(podcastTopResult) { result ->
                                    SearchTopResultItemList(
                                        navController = navController,
                                        viewModel = viewModel,
                                        result = result
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                showMore = false
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding)
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f), // Fill available space
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(searchResults) { result ->
                            SearchResultItem(result, viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    result: ResultItem,
    viewModel: SearchViewModel
) {
    Row(
        modifier = Modifier
                .clickable {
                    result.trackId?.let {
                        val url = "https://itunes.apple.com/lookup?id=${result.trackId}"
                        viewModel.lookUpFeedById(url)
                    }
        }
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(result.artworkUrl100),
            contentDescription = result.trackName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .padding(end = 8.dp)
        )
        Column {
            Text(result.trackName?:"", style = MaterialTheme.typography.bodyLarge)
            Text(result.artistName?:"", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun SearchTopResultItemGrid(
    navController: NavHostController,
    viewModel: SearchViewModel,
    result: PodcastSearchResult
) {
    Box(
        modifier = Modifier
            .clickable {
                result.feedUrl?.let {
                    viewModel.lookUpFeedById(result.feedUrl)
                }
            }
            .fillMaxSize()
    ) {
        Image(
            painter = rememberAsyncImagePainter(result.imageUrl),
            contentDescription = result.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(96.dp)
                .padding(2.dp)
        )
    }
}

@Composable
fun SearchTopResultItemList(
    navController: NavHostController,
    viewModel: SearchViewModel,
    result: PodcastSearchResult
) {
    Row(
        modifier = Modifier
            .clickable {
                result.feedUrl?.let {
                    viewModel.lookUpFeedById(result.feedUrl)
                }
            }
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(result.imageUrl),
            contentDescription = result.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .padding(end = 8.dp)
        )
        Column {
            result.title?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
            result.author?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}