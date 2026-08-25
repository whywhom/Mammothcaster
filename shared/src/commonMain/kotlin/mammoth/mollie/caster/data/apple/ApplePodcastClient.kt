package mammoth.mollie.caster.data.apple

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mammoth.mollie.caster.model.Podcast
import mammoth.mollie.caster.model.PodcastCategory
import mammoth.mollie.caster.util.podcastIdFor

/**
 * Discovery adapter for Apple's public iTunes Search API. Search results are deliberately
 * ephemeral: a feed is only persisted after its RSS subscription succeeds.
 */
class ApplePodcastClient(
    private val client: HttpClient,
    private val country: String = "us",
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun search(query: String, limit: Int = DEFAULT_LIMIT): ApplePodcastSearchResult =
        search(query = query, limit = limit, attribute = null, category = null)

    /** Searches Apple by its official category/subcategory name. */
    suspend fun searchCategory(
        category: PodcastCategory,
        limit: Int = MAX_LIMIT,
    ): ApplePodcastSearchResult = search(
        query = category.displayName,
        limit = limit,
        attribute = if (category.key == "artificial-intelligence") "keywordsTerm" else null,
        category = category,
    )

    private suspend fun search(
        query: String,
        limit: Int,
        attribute: String?,
        category: PodcastCategory?,
    ): ApplePodcastSearchResult {
        val term = query.trim()
        if (term.isBlank()) return ApplePodcastSearchResult.Success(emptyList())

        return runCatching {
            val response = client.get(SEARCH_URL) {
                parameter("term", term)
                parameter("country", country.trim().lowercase().ifBlank { "us" })
                parameter("media", "podcast")
                parameter("entity", "podcast")
                attribute?.let { parameter("attribute", it) }
                parameter("limit", limit.coerceIn(1, MAX_LIMIT))
                header(HttpHeaders.Accept, "application/json")
                header(HttpHeaders.UserAgent, "Molliecaster/0.1 (podcast discovery)")
                timeout {
                    requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
                    connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
                    socketTimeoutMillis = REQUEST_TIMEOUT_MILLIS
                }
            }
            if (!response.status.isSuccess()) return@runCatching ApplePodcastSearchResult.HttpFailure(response.status.value)

            val results = json.parseToJsonElement(response.body<String>()).jsonObject["results"]?.jsonArray.orEmpty()
            val podcasts = results.mapNotNull { item -> item.jsonObject.toPodcastOrNull() }
                .distinctBy { it.id }
                .map { podcast -> category?.let { podcast.copy(categories = listOf(it)) } ?: podcast }
            ApplePodcastSearchResult.Success(podcasts)
        }.getOrElse {
            if (it is HttpRequestTimeoutException || it.message?.contains("timeout", ignoreCase = true) == true) {
                ApplePodcastSearchResult.TimedOut
            } else {
                ApplePodcastSearchResult.NetworkFailure(it.message ?: "Apple Podcasts search failed")
            }
        }
    }

    private fun Map<String, kotlinx.serialization.json.JsonElement>.toPodcastOrNull(): Podcast? {
        val feedUrl = string("feedUrl")?.takeIf { it.startsWith("http://") || it.startsWith("https://") } ?: return null
        val title = string("collectionName")?.trim().orEmpty().ifBlank { return null }
        return Podcast(
            id = podcastIdFor(feedUrl),
            feedUrl = feedUrl,
            websiteUrl = string("collectionViewUrl"),
            title = title,
            author = string("artistName").orEmpty(),
            description = string("description").orEmpty(),
            artworkUrl = string("artworkUrl600") ?: string("artworkUrl100") ?: string("artworkUrl60"),
            isExplicit = string("collectionExplicitness") == "explicit",
            episodeCount = string("trackCount")?.toIntOrNull() ?: 0,
        )
    }

    private fun Map<String, kotlinx.serialization.json.JsonElement>.string(name: String): String? =
        get(name)?.jsonPrimitive?.contentOrNull

    private companion object {
        const val SEARCH_URL = "https://itunes.apple.com/search"
        const val DEFAULT_LIMIT = 10
        const val MAX_LIMIT = 200
        const val REQUEST_TIMEOUT_MILLIS = 12_000L
        const val CONNECT_TIMEOUT_MILLIS = 8_000L
    }
}

sealed interface ApplePodcastSearchResult {
    data class Success(val podcasts: List<Podcast>) : ApplePodcastSearchResult
    data class HttpFailure(val status: Int) : ApplePodcastSearchResult
    data class NetworkFailure(val message: String) : ApplePodcastSearchResult
    data object TimedOut : ApplePodcastSearchResult
}
