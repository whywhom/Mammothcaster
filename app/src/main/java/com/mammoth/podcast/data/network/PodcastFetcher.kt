package com.mammoth.podcast.data.network

import coil.network.HttpException
import com.mammoth.podcast.MammothCastApp
import com.mammoth.podcast.data.database.model.Category
import com.mammoth.podcast.data.database.model.Episode
import com.mammoth.podcast.data.database.model.Podcast
import com.mammoth.podcast.util.DownloadState
import com.rometools.modules.itunes.EntryInformation
import com.rometools.modules.itunes.FeedInformation
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * A class which fetches some selected podcast RSS feeds.
 *
 * @param okHttpClient [OkHttpClient] to use for network requests
 * @param syndFeedInput [SyndFeedInput] to use for parsing RSS feeds.
 * @param ioDispatcher [CoroutineDispatcher] to use for running fetch requests.
 */
class PodcastsFetcher(
    private val okHttpClient: OkHttpClient = MammothCastApp.okHttpClient,
    private val syndFeedInput: SyndFeedInput = MammothCastApp.syndFeedInput,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * It seems that most podcast hosts do not implement HTTP caching appropriately.
     * Instead of fetching data on every app open, we instead allow the use of 'stale'
     * network responses (up to 8 hours).
     */
    private val cacheControl by lazy {
        CacheControl.Builder().maxStale(8, TimeUnit.HOURS).build()
    }

    /**
     * Returns a [Flow] which fetches each podcast feed and emits it in turn.
     *
     * The feeds are fetched concurrently, meaning that the resulting emission order may not
     * match the order of [feedUrls].
     */
    operator fun invoke(feedUrls: List<String>): Flow<PodcastRssResponse> {
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

    private suspend fun fetchPodcast(url: String): PodcastRssResponse {
        return withContext(ioDispatcher) {
            val request = Request.Builder()
                .url(url)
                .cacheControl(cacheControl)
                .build()

            val response = okHttpClient.newCall(request).execute()

            // If the network request wasn't successful, throw an exception
            if (!response.isSuccessful) throw HttpException(response)

            // Otherwise we can parse the response using a Rome SyndFeedInput, then map it
            // to a Podcast instance. We run this on the IO dispatcher since the parser is reading
            // from a stream.
            response.body!!.use { body ->
                syndFeedInput.build(body.charStream()).toPodcastResponse(url)
            }
        }
    }
}

sealed class PodcastRssResponse {
    data class Error(
        val throwable: Throwable?,
    ) : PodcastRssResponse()

    data class Success(
        val podcast: Podcast,
        val episodes: List<Episode>,
        val categories: Set<Category>
    ) : PodcastRssResponse()
}

/**
 * Map a Rome [SyndFeed] instance to our own [Podcast] data class.
 */
private fun SyndFeed.toPodcastResponse(feedUrl: String): PodcastRssResponse {
    val podcastUri = uri ?: feedUrl
    val episodes = entries.map { it.toEpisode(podcastUri) }

    val feedInfo = getModule(PodcastModuleDtd) as? FeedInformation
    val podcast = Podcast(
        uri = podcastUri,
        title = title,
        description = feedInfo?.summary ?: description,
        author = author,
        copyright = copyright,
        imageUrl = feedInfo?.imageUri?.toString()
    )

    val categories = feedInfo?.categories
        ?.map { Category(name = it.name) }
        ?.toSet() ?: emptySet()

    return PodcastRssResponse.Success(podcast, episodes, categories)
}

/**
 * Map a Rome [SyndEntry] instance to our own [Episode] data class.
 */
private fun SyndEntry.toEpisode(podcastUri: String): Episode {
    val entryInformation = getModule(PodcastModuleDtd) as? EntryInformation
    val syndEnclosure = if(enclosures.isEmpty()) null else enclosures[0]
    return Episode(
        uri = uri,
        podcastUri = podcastUri,
        title = title,
        author = author,
        summary = entryInformation?.summary ?: description?.value,
        subtitle = entryInformation?.subtitle,
        published = Instant.ofEpochMilli(publishedDate.time).atOffset(ZoneOffset.UTC),
        duration = entryInformation?.duration?.milliseconds?.let { Duration.ofMillis(it) },
        enclosureUrl = syndEnclosure?.url,
        enclosureLength = syndEnclosure?.length?:0,
        enclosureType = syndEnclosure?.type,
        isDownloaded = DownloadState.NOT_DOWNLOAD.value,
        filePath = "",
        downloadTime = 0
    )
}

/**
 * Most feeds use the following DTD to include extra information related to
 * their podcast. Info such as images, summaries, duration, categories is sometimes only available
 * via this attributes in this DTD.
 */
private const val PodcastModuleDtd = "http://www.itunes.com/dtds/podcast-1.0.dtd"
