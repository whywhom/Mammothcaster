package com.mammoth.podcast.core.data.model

import org.json.JSONException
import org.json.JSONObject

data class PodcastSearchResult(
    /**
     * The name of the podcast
     */
    val title: String? = null,

    /**
     * URL of the podcast image
     */
    val imageUrl: String? = null,

    /**
     * URL of the podcast feed
     */
    val feedUrl: String? = null,

    /**
     * artistName of the podcast feed
     */
    val author: String? = null,
) {
    companion object {
        /**
         * Constructs a Podcast instance from iTunes toplist entry
         *
         * @param json object holding the podcast information
         * @throws JSONException
         */
        @Throws(JSONException::class)
        fun fromItunesToplist(json: JSONObject): PodcastSearchResult {
            val title = json.getJSONObject("im:name").getString("label")
            var imageUrl: String? = null
            val images = json.getJSONArray("im:image")
            var i = 0
            while (imageUrl == null && i < images.length()) {
                val image = images.getJSONObject(i)
                val height = image.getJSONObject("attributes").getString("height")
                if (height.toInt() >= 100) {
                    imageUrl = image.getString("label")
                }
                i++
            }
            val feedUrl = "https://itunes.apple.com/lookup?id=" +
                    json.getJSONObject("id").getJSONObject("attributes").getString("im:id")

            var author: String? = null
            try {
                author = json.getJSONObject("im:artist").getString("label")
            } catch (e: Exception) {
                // Some feeds have empty artist
            }
            return PodcastSearchResult(title, imageUrl, feedUrl, author)
        }
    }
}