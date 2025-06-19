package com.mammoth.podcast.ui.podcast

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mammoth.podcast.R
import com.mammoth.podcast.core.data.network.PodcastRssResponse
import com.mammoth.podcast.core.model.EpisodeInfo
import com.mammoth.podcast.core.model.PodcastInfo
import com.mammoth.podcast.core.player.model.PlayerEpisode
import com.mammoth.podcast.ui.shared.EpisodeListItem
import kotlinx.coroutines.launch

@Composable
fun ItunePodcastDetails(
    title: String = "",
    podcastUri: String,
    navigateToPlayer: (EpisodeInfo) -> Unit,
    viewModel: ItunePodcastDetailsViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
    showBackButton: Boolean = true
) {
    val feedItems by viewModel.feedItems.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackBarText = stringResource(id = R.string.episode_added_to_your_queue)
    LaunchedEffect(podcastUri) {
        viewModel.fetchFeed(podcastUri)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (showBackButton) {
                PodcastDetailsTopAppBar(
                    title = title,
                    navigateBack = navigateBack,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { contentPadding ->
        ItunePodcastDetailsScreen(
            modifier = Modifier.padding(contentPadding),
            data = feedItems,
            navigateToPlayer = navigateToPlayer,
            onQueueEpisode = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(snackBarText)
                }
                (viewModel::onQueueEpisode)(it)
            },
        )
    }
}

@Composable
fun ItunePodcastDetailsScreen(
    modifier: Modifier = Modifier,
    data: PodcastRssResponse?,
    navigateToPlayer: (EpisodeInfo) -> Unit,
    onQueueEpisode: (PlayerEpisode) -> Unit,
) {

    if(data == null) {
        Text("Loading...")
    } else {
        val feeds = (data as PodcastRssResponse.Success)
        LazyColumn(modifier = modifier) {
            items(feeds.episodes.size) { index ->
                val item = feeds.episodes[index]
                val episodeItem = EpisodeInfo(
                    uri = item.uri,
                    title = item.title,
                    subTitle = "",
                    summary = item.summary ?: "",
                    author = item.author ?: "",
                    duration = item.duration,
                    enclosureUrl = item.enclosureUrl,
                    enclosureLength = item.enclosureLength,
                    enclosureType = item.enclosureType,
                    isDownloaded = item.isDownloaded,
                    filePath = item.filePath,
                    downloadTime = item.downloadTime
                )
                val podcastItem = PodcastInfo(
                    uri = feeds.podcast.uri,
                    title = feeds.podcast.title,
                    author = feeds.podcast.author ?: "",
                    imageUrl = feeds.podcast.imageUrl ?: "",
                    description = feeds.podcast.description ?: "",
                )
                EpisodeListItem(
                    episode = episodeItem,
                    podcast = podcastItem,
                    onClick = navigateToPlayer,
                    onQueueEpisode =  onQueueEpisode,
                    modifier = Modifier.fillMaxWidth(),
                    showPodcastImage = false,
                    showSummary = true
                )
            }
        }
    }
}
