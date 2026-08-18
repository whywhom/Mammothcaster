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
import mammoth.mollie.caster.platform.currentTimeMillis
import mammoth.mollie.caster.util.podcastIdFor
import mammoth.mollie.caster.util.sha1

class PodcastIndexTrendingClient(
    private val client: HttpClient,
    private val config: DiscoveryConfig,
    private val nowMillis: () -> Long = ::currentTimeMillis,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun top(limit: Int = DISCOVERY_SOURCE_LIMIT): DiscoverySourceResult {
        if (!config.podcastIndexEnabled) {
            return DiscoverySourceResult.Unavailable("Podcast Index is disabled")
        }
        val baseUrl = config.podcastIndexBaseUrl.trimEnd('/')
        val key = config.podcastIndexApiKey.trim()
        val secret = config.podcastIndexApiSecret.trim()
        if (baseUrl.isBlank()) return DiscoverySourceResult.Unavailable("Podcast Index endpoint is not configured")
        if (!config.podcastIndexUsesTrustedProxy && (key.isBlank() || secret.isBlank())) {
            return DiscoverySourceResult.Unavailable(
                "Podcast Index credentials are missing; configure a trusted proxy or API key and secret",
            )
        }

        return runCatching {
            val response = client.get("$baseUrl/podcasts/trending") {
                parameter("max", limit.coerceIn(1, DISCOVERY_SOURCE_LIMIT))
                header(HttpHeaders.Accept, "application/json")
                header(HttpHeaders.UserAgent, USER_AGENT)
                if (!config.podcastIndexUsesTrustedProxy) {
                    val timestamp = (nowMillis() / 1_000L).toString()
                    header("X-Auth-Key", key)
                    header("X-Auth-Date", timestamp)
                    header("Authorization", sha1(key + secret + timestamp))
                }
            }
            if (!response.status.isSuccess()) {
                return@runCatching DiscoverySourceResult.Failure("Podcast Index returned HTTP ${response.status.value}")
            }
            val root = json.parseToJsonElement(response.body<String>()).jsonObject
            val podcasts = root["feeds"]?.jsonArray.orEmpty()
                .mapNotNull { it.jsonObject.toPodcastOrNull() }
                .distinctBy { it.id }
                .take(limit)
            DiscoverySourceResult.Success(podcasts)
        }.getOrElse { DiscoverySourceResult.Failure(it.message ?: "Podcast Index request failed") }
    }

    private fun JsonObject.toPodcastOrNull(): Podcast? {
        val feedUrl = string("url")?.takeIf(::isHttpUrl) ?: return null
        val title = string("title")?.trim().orEmpty().ifBlank { return null }
        val categoryNames = get("categories")?.let { categories ->
            runCatching { categories.jsonObject.values.mapNotNull { it.jsonPrimitive.contentOrNull } }.getOrDefault(emptyList())
        }.orEmpty()
        return Podcast(
            id = podcastIdFor(feedUrl),
            feedUrl = feedUrl,
            canonicalFeedUrl = feedUrl,
            websiteUrl = string("link")?.takeIf(::isHttpUrl),
            title = title,
            author = string("author") ?: string("ownerName").orEmpty(),
            description = string("description").orEmpty(),
            artworkUrl = (string("artwork") ?: string("image"))?.takeIf(::isHttpUrl),
            language = string("language"),
            isExplicit = booleanish("explicit"),
            categories = categoriesFromNames(categoryNames),
            episodeCount = get("episodeCount")?.jsonPrimitive?.intOrNull ?: 0,
            latestEpisodeAtMillis = get("newestItemPubdate")?.jsonPrimitive?.longOrNull?.times(1_000L),
        )
    }

    private fun JsonObject.string(name: String): String? = get(name)?.jsonPrimitive?.contentOrNull

    private fun JsonObject.booleanish(name: String): Boolean {
        val raw = get(name)?.jsonPrimitive ?: return false
        return raw.contentOrNull?.lowercase() in setOf("true", "yes", "explicit", "1") || raw.intOrNull == 1
    }

    private companion object {
        const val USER_AGENT = "Molliecaster/0.1 (podcast discovery)"
    }
}

private fun isHttpUrl(value: String): Boolean = value.startsWith("http://") || value.startsWith("https://")
