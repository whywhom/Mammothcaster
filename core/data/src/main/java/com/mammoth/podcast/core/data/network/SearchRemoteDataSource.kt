package com.mammoth.podcast.core.data.network

import com.mammoth.podcast.core.data.Dispatcher
import com.mammoth.podcast.core.data.MammothDispatchers
import com.mammoth.podcast.core.data.model.PodcastSearchResult
import com.mammoth.podcast.core.data.model.ResultItem
import com.rometools.rome.io.SyndFeedInput
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

class SearchRemoteDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val syndFeedInput: SyndFeedInput,
    @Dispatcher(MammothDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {

    fun getTop(url: String): List<PodcastSearchResult> {
        val results = mutableListOf<PodcastSearchResult>()
        // Perform the network call asynchronously
        val request = Request.Builder().url(url).build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let {
                        return parseTopResults(responseBody)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return results
    }

    fun search(url: String): List<ResultItem> {
        val results = mutableListOf<ResultItem>()
        // Perform the network call asynchronously
        val request = Request.Builder().url(url).build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let {
                        return parseResults(responseBody)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return results
    }

    fun fetchFeed(feedLookupUrl: String): List<ResultItem> {
        val request = Request.Builder().url(feedLookupUrl).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                responseBody?.let {
                    return parseResults(responseBody)
                }
            }
        }
        return listOf()
    }

    private fun parseTopResults(json: String): List<PodcastSearchResult> {
        val result = JSONObject(json)
        val feed: JSONObject
        val entries: JSONArray
        try {
            feed = result.getJSONObject("feed")
            entries = feed.getJSONArray("entry")
        } catch (e: JSONException) {
            return ArrayList()
        }
        val results = mutableListOf<PodcastSearchResult>()
        for (i in 0 until entries.length()) {
            val jsonObj = entries.getJSONObject(i)
            jsonObj?.let {
                results.add(PodcastSearchResult.fromItunesToplist(it))
            }
        }
        return results
    }

    private fun parseResults(json: String): List<ResultItem> {
        val results = mutableListOf<ResultItem>()
        val jsonObject = JSONObject(json)
        val jsonArray = jsonObject.getJSONArray("results")
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val trackId = item.optInt("trackId", 0)
            val trackName = item.optString("trackName", "Unknown Track")
            val artistName = item.optString("artistName", "Unknown Artist")
            val wrapperType = item.optString("wrapperType", "Unknown Type")
            val kind = item.optString("kind", "Unknown kind")
            val artistId = item.optInt("artistId", 0)
            val collectionId = item.optInt("collectionId", 0)
            val collectionName = item.optString("collectionName", "Unknown")
            val collectionCensoredName = item.optString("collectionCensoredName", "Unknown")
            val trackCensoredName = item.optString("trackCensoredName", "Unknown")
            val artistViewUrl = item.optString("artistViewUrl", "Unknown")
            val collectionViewUrl = item.optString("collectionViewUrl", "Unknown")
            val feedUrl= item.optString("feedUrl", "Unknown")
            val trackViewUrl= item.optString("trackViewUrl", "Unknown")
            val artworkUrl30= item.optString("artworkUrl30", "Unknown")
            val artworkUrl60= item.optString("artworkUrl60", "Unknown")
            val artworkUrl100 = item.optString("artworkUrl100", "")
            val collectionPrice = item.optInt("collectionPrice", 0)
            val trackPrice = item.optInt("trackPrice", 0)
            val collectionHdPrice = item.optInt("collectionHdPrice", 0)
            val releaseDate = item.optString("releaseDate", "")
            val collectionExplicitness = item.optString("collectionExplicitness", "")
            val trackExplicitness = item.optString("trackExplicitness", "")
            val trackCount = item.optInt("trackCount", 0)
            val trackTimeMillis = item.optInt("trackTimeMillis", 0)
            val country = item.optString("country", "")
            val currency = item.optString("currency", "")
            val primaryGenreName = item.optString("primaryGenreName", "")
            val contentAdvisoryRating = item.optString("contentAdvisoryRating", "")
            val artworkUrl600 = item.optString("artworkUrl600", "")
            val genreIds:List<String> = item.getJSONArray("genreIds").let { genreIdsJsonArray ->
                List(genreIdsJsonArray.length()) { index -> genreIdsJsonArray.getString(index) }
            }
            val genres:List<String> = item.getJSONArray("genres").let { genresJsonArray ->
                List(genresJsonArray.length()) { index -> genresJsonArray.getString(index) }
            }
            results.add(
                ResultItem(
                    trackId = trackId,
                    trackName = trackName,
                    artistName = artistName,
                    wrapperType = wrapperType,
                    kind = kind,
                    artistId = artistId,
                    collectionId = collectionId,
                    collectionName = collectionName,
                    collectionCensoredName = collectionCensoredName,
                    trackCensoredName = trackCensoredName,
                    artistViewUrl = artistViewUrl,
                    collectionViewUrl = collectionViewUrl,
                    feedUrl = feedUrl,
                    trackViewUrl = trackViewUrl,
                    artworkUrl30 = artworkUrl30,
                    artworkUrl60 = artworkUrl60,
                    artworkUrl100 = artworkUrl100,
                    collectionPrice = collectionPrice,
                    trackPrice = trackPrice,
                    collectionHdPrice = collectionHdPrice,
                    releaseDate = releaseDate,
                    collectionExplicitness = collectionExplicitness,
                    trackExplicitness = trackExplicitness,
                    trackCount = trackCount,
                    trackTimeMillis = trackTimeMillis,
                    country = country,
                    currency = currency,
                    primaryGenreName = primaryGenreName,
                    contentAdvisoryRating = contentAdvisoryRating,
                    artworkUrl600 = artworkUrl600,
                    genreIds = genreIds,
                    genres = genres,
                )
            )
        }
        return results
    }
}