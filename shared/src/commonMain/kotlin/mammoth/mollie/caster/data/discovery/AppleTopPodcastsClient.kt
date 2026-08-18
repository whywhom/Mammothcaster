package mammoth.mollie.caster.data.discovery

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import mammoth.mollie.caster.model.Podcast
import mammoth.mollie.caster.util.podcastIdFor

class AppleTopPodcastsClient(
    private val client: HttpClient,
    private val storefront: String,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun top(limit: Int = DISCOVERY_SOURCE_LIMIT): DiscoverySourceResult = runCatching {
        val safeLimit = limit.coerceIn(1, DISCOVERY_SOURCE_LIMIT)
        val country = storefront.trim().lowercase().takeIf { it.length == 2 } ?: "us"
        val chartResponse = client.get("$RSS_BASE_URL/$country/podcasts/top/$safeLimit/podcasts.json") {
            header(HttpHeaders.Accept, "application/json")
            header(HttpHeaders.UserAgent, USER_AGENT)
        }
        if (!chartResponse.status.isSuccess()) {
            return@runCatching DiscoverySourceResult.Failure("Apple RSS Builder returned HTTP ${chartResponse.status.value}")
        }
        val chartResults = json.parseToJsonElement(chartResponse.body<String>()).jsonObject["feed"]
            ?.jsonObject?.get("results")?.jsonArray.orEmpty().map { it.jsonObject }
        val ids = chartResults.mapNotNull { it.string("id")?.toLongOrNull() }.distinct().take(safeLimit)
        if (ids.isEmpty()) return@runCatching DiscoverySourceResult.Success(emptyList())

        val lookupResponse = client.get(LOOKUP_URL) {
            parameter("id", ids.joinToString(","))
            parameter("media", "podcast")
            parameter("entity", "podcast")
            parameter("country", country)
            header(HttpHeaders.Accept, "application/json")
            header(HttpHeaders.UserAgent, USER_AGENT)
        }
        if (!lookupResponse.status.isSuccess()) {
            return@runCatching DiscoverySourceResult.Failure("Apple Podcasts lookup returned HTTP ${lookupResponse.status.value}")
        }
        val lookups = json.parseToJsonElement(lookupResponse.body<String>()).jsonObject["results"]
            ?.jsonArray.orEmpty().map { it.jsonObject }
            .mapNotNull { item ->
                val id = item.appleId() ?: return@mapNotNull null
                val podcast = item.toPodcastOrNull() ?: return@mapNotNull null
                id to podcast
            }
            .toMap()
        val chartById = chartResults.associateBy { it.string("id")?.toLongOrNull() }
        val ranked = ids.mapNotNull { id ->
            lookups[id]?.let { podcast ->
                val chart = chartById[id]
                podcast.copy(
                    title = chart?.string("name")?.takeIf(String::isNotBlank) ?: podcast.title,
                    author = chart?.string("artistName")?.takeIf(String::isNotBlank) ?: podcast.author,
                    artworkUrl = chart?.string("artworkUrl100") ?: podcast.artworkUrl,
                    categories = chart?.get("genres")?.jsonArray.orEmpty().mapNotNull { genre ->
                        runCatching { genre.jsonObject.string("name") }.getOrNull()
                    }.let(::categoriesFromNames).ifEmpty { podcast.categories },
                )
            }
        }
        DiscoverySourceResult.Success(ranked)
    }.getOrElse { DiscoverySourceResult.Failure(it.message ?: "Apple podcast chart request failed") }

    private fun JsonObject.toPodcastOrNull(): Podcast? {
        val feedUrl = string("feedUrl")?.takeIf { it.startsWith("http://") || it.startsWith("https://") } ?: return null
        val title = (string("collectionName") ?: string("trackName"))?.trim().orEmpty().ifBlank { return null }
        val genres = get("genres")?.jsonArray.orEmpty().mapNotNull { it.jsonPrimitive.contentOrNull }
        return Podcast(
            id = podcastIdFor(feedUrl),
            feedUrl = feedUrl,
            websiteUrl = string("collectionViewUrl"),
            title = title,
            author = string("artistName").orEmpty(),
            description = string("description").orEmpty(),
            artworkUrl = string("artworkUrl600") ?: string("artworkUrl100") ?: string("artworkUrl60"),
            isExplicit = string("collectionExplicitness") == "explicit",
            categories = categoriesFromNames(genres),
            episodeCount = get("trackCount")?.jsonPrimitive?.intOrNull ?: 0,
            latestEpisodeAtMillis = get("releaseDateMillis")?.jsonPrimitive?.longOrNull,
        )
    }

    private fun JsonObject.appleId(): Long? =
        (string("collectionId") ?: string("trackId"))?.toLongOrNull()

    private fun JsonObject.string(name: String): String? = get(name)?.jsonPrimitive?.contentOrNull

    private companion object {
        const val RSS_BASE_URL = "https://rss.marketingtools.apple.com/api/v2"
        const val LOOKUP_URL = "https://itunes.apple.com/lookup"
        const val USER_AGENT = "Molliecaster/0.1 (podcast discovery)"
    }
}
