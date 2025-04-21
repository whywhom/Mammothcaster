package com.mammoth.media.core.data.network

import com.rometools.rome.io.SyndFeedInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import javax.inject.Inject

class RemoteRepository @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val syndFeedInput: SyndFeedInput,
) {
    suspend fun fetchFeeds(feedUrls: List<String>): Flow<PodcastRssResponse> {
        // We use flatMapMerge here to achieve concurrent fetching/parsing of the feeds.
        return feedUrls.asFlow()
            .flatMapMerge { feedUrl ->
                flow {
                    emit(fetchPodcast(feedUrl))
                }.catch { e ->
                    // If an exception was caught while fetching the podcast, wrap it in
                    // an Error instance.
                    emit(PodcastRssResponse.Error(e))
                }
            }
    }
}