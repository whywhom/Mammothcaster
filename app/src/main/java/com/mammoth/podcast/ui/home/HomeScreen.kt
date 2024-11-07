package com.mammoth.podcast.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mammoth.podcast.R
import com.mammoth.podcast.domain.model.PodcastInfo

@Composable
fun PodcastScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel
) {
    val homeScreenUiState by viewModel.state.collectAsStateWithLifecycle()

    when (val uiState = homeScreenUiState) {
        is HomeScreenUiState.Loading -> HomeScreenLoading()
        is HomeScreenUiState.Error -> HomeScreenError(onRetry = viewModel::refresh)
        is HomeScreenUiState.Ready -> {
            HomeScreenReady(
                modifier = modifier,
                uiState = uiState,
                viewModel = viewModel,
            )
        }
    }
}

@Composable
private fun HomeScreenReady(
    modifier: Modifier = Modifier,
    uiState: HomeScreenUiState.Ready,
    viewModel: HomeViewModel
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        items(uiState.podcastCategoryFilterResult.topPodcasts) {
            PodcastItem(podcast = it)
        }
    }
}

@Composable
fun HomeScreenError(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = stringResource(id = R.string.an_error_has_occurred),
            modifier = Modifier.padding(16.dp)
        )
        Button(onClick = onRetry) {
            Text(text = stringResource(id = R.string.retry_label))
        }
    }
}

@Composable
fun HomeScreenLoading() {
    Box {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun EmptyScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add",
            modifier = Modifier
                .align(Alignment.Center)
                .size(48.dp)
                .clickable {
                    viewModel.refresh()
                },
            tint = Color.Gray
        )
    }
}

@Composable
fun PodcastItem(podcast: PodcastInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = podcast.title, style = MaterialTheme.typography.headlineMedium)
        Text(text = podcast.description, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
    }
}