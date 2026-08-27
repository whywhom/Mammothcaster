package mammoth.mollie.caster.data.rss

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

sealed interface FeedRefreshResult {
    val feedUrl: String

    data class Updated(override val feedUrl: String, val feed: ParsedFeed, val etag: String?, val lastModified: String?) : FeedRefreshResult
    data class NotModified(override val feedUrl: String) : FeedRefreshResult
    data class HttpFailure(override val feedUrl: String, val status: Int) : FeedRefreshResult
    data class NetworkFailure(override val feedUrl: String, val message: String) : FeedRefreshResult
    data class ParseFailure(override val feedUrl: String, val message: String) : FeedRefreshResult
}

class FeedClient(
    private val client: HttpClient,
    private val parser: RssParser = RssParser(),
) {
    suspend fun fetch(feedUrl: String, etag: String? = null, lastModified: String? = null): FeedRefreshResult =
        runCatching {
            val response = client.get(feedUrl) {
                etag?.let { header(HttpHeaders.IfNoneMatch, it) }
                lastModified?.let { header(HttpHeaders.IfModifiedSince, it) }
            }
            when {
                response.status.value == 304 -> FeedRefreshResult.NotModified(feedUrl)
                !response.status.isSuccess() -> FeedRefreshResult.HttpFailure(feedUrl, response.status.value)
                else -> runCatching {
                    val parsed = parser.parse(response.body<String>(), feedUrl)
                    parsed.copy(podcast = parsed.podcast.copy(canonicalFeedUrl = response.call.request.url.toString()))
                }
                    .fold(
                        onSuccess = { FeedRefreshResult.Updated(feedUrl, it, response.headers[HttpHeaders.ETag], response.headers[HttpHeaders.LastModified]) },
                        onFailure = { FeedRefreshResult.ParseFailure(feedUrl, it.message ?: "Invalid feed") },
                    )
            }
        }.getOrElse { FeedRefreshResult.NetworkFailure(feedUrl, it.message ?: "Network request failed") }
}

fun createHttpClient() = HttpClient {
    followRedirects = true
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 30_000
    }
}
