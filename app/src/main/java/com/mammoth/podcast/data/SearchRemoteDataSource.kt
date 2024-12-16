package com.mammoth.podcast.data

import com.mammoth.podcast.data.model.PodcastSearchResult
import com.mammoth.podcast.data.model.ResultItem
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class SearchRemoteDataSource {
    private val client = OkHttpClient()

    fun getTop(url: String): List<PodcastSearchResult> {
        val results = mutableListOf<PodcastSearchResult>()
        // Perform the network call asynchronously
        val request = Request.Builder().url(url).build()

        try {
            client.newCall(request).execute().use { response ->
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
            client.newCall(request).execute().use { response ->
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
            val trackName = item.optString("trackName", "Unknown Track")
            val artistName = item.optString("artistName", "Unknown Artist")
            val artworkUrl = item.optString("artworkUrl100", "")
            results.add(ResultItem(trackName, artistName, artworkUrl))
        }
        return results
    }
}