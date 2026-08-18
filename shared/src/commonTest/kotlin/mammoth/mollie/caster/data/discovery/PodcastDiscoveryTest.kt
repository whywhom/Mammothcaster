package mammoth.mollie.caster.data.discovery

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import mammoth.mollie.caster.model.Podcast
import mammoth.mollie.caster.model.PodcastCategory
import mammoth.mollie.caster.util.podcastIdFor
import mammoth.mollie.caster.util.sha1
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PodcastDiscoveryTest {
    @Test
    fun podcastIndexSignsTrendingRequestAndMapsTopTen() = runTest {
        val timestamp = "1700000000"
        val client = HttpClient(MockEngine { request ->
            assertTrue(request.url.encodedPath.endsWith("/podcasts/trending"))
            assertEquals("10", request.url.parameters["max"])
            assertEquals("test-key", request.headers["X-Auth-Key"])
            assertEquals(timestamp, request.headers["X-Auth-Date"])
            assertEquals(sha1("test-key" + "test-secret" + timestamp), request.headers["Authorization"])
            respond(PODCAST_INDEX_RESPONSE, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        })

        val result = PodcastIndexTrendingClient(
            client,
            DiscoveryConfig(
                podcastIndexEnabled = true,
                podcastIndexApiKey = "test-key",
                podcastIndexApiSecret = "test-secret",
            ),
            nowMillis = { 1_700_000_000_000L },
        ).top()

        val podcast = assertIs<DiscoverySourceResult.Success>(result, result.failureMessage()).podcasts.single()
        assertEquals("Trending Show", podcast.title)
        assertEquals("https://feeds.example.com/trending.xml", podcast.feedUrl)
        assertEquals("Technology", podcast.categories.single().displayName)
        assertEquals(12, podcast.episodeCount)
    }

    @Test
    fun discoveryDoesNotCallPodcastIndexWhenItIsDisabled() = runTest {
        var podcastIndexRequests = 0
        val client = HttpClient(MockEngine { request ->
            when (request.url.host) {
                "rss.marketingtools.apple.com" -> respond(
                    APPLE_CHART_RESPONSE,
                    HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, "application/json"),
                )
                "itunes.apple.com" -> respond(
                    APPLE_LOOKUP_RESPONSE,
                    HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, "application/json"),
                )
                else -> {
                    podcastIndexRequests++
                    error("Podcast Index must not be called while disabled")
                }
            }
        })

        val snapshot = PodcastDiscoveryService(client, DiscoveryConfig()).refresh()

        assertEquals(0, podcastIndexRequests)
        assertEquals(listOf("Apple Chart Show"), snapshot.merged.map(Podcast::title))
        assertTrue(snapshot.warnings.isEmpty())
    }

    @Test
    fun appleChartUsesTopTenThenResolvesRssUrlsWithLookup() = runTest {
        var requests = 0
        val client = HttpClient(MockEngine { request ->
            requests++
            when {
                request.url.host == "rss.marketingtools.apple.com" -> {
                    assertTrue(request.url.encodedPath.endsWith("/nz/podcasts/top/10/podcasts.json"))
                    respond(APPLE_CHART_RESPONSE, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }
                request.url.host == "itunes.apple.com" -> {
                    assertEquals("123", request.url.parameters["id"])
                    assertEquals("nz", request.url.parameters["country"])
                    respond(APPLE_LOOKUP_RESPONSE, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }
                else -> error("Unexpected request: ${request.url}")
            }
        })

        val result = AppleTopPodcastsClient(client, "NZ").top()

        val podcast = assertIs<DiscoverySourceResult.Success>(result, result.failureMessage()).podcasts.single()
        assertEquals(2, requests)
        assertEquals("Apple Chart Show", podcast.title)
        assertEquals("https://feeds.example.com/apple.xml", podcast.feedUrl)
        assertEquals("Technology", podcast.categories.single().displayName)
    }

    @Test
    fun mergeInterleavesSourcesAndDeduplicatesNormalizedFeedUrls() {
        val sharedFromIndex = podcast("Index shared", "https://example.com/shared.xml")
        val sharedFromApple = podcast("Apple shared", "https://EXAMPLE.com:443/shared.xml#fragment")
        val result = mergeRankedDiscoverySources(
            podcastIndex = listOf(sharedFromIndex, podcast("Index second", "https://example.com/index.xml")),
            apple = listOf(podcast("Apple first", "https://example.com/apple.xml"), sharedFromApple),
        )

        assertEquals(listOf("Index shared", "Apple first", "Index second"), result.map { it.title })
    }

    @Test
    fun recommendationBoostsLocallyEngagedCategories() {
        val technology = PodcastCategory("technology", "Technology")
        val comedy = PodcastCategory("comedy", "Comedy")
        val subscribed = podcast("Subscribed", "https://example.com/subscribed.xml", technology).copy(isSubscribed = true)
        val candidates = listOf(
            podcast("Comedy", "https://example.com/comedy.xml", comedy),
            podcast("Technology", "https://example.com/technology.xml", technology),
        )

        val result = recommendFromDiscovery(candidates, listOf(subscribed), emptyList(), emptySet(), emptySet())

        assertEquals(listOf("Technology", "Comedy"), result.map { it.title })
    }

    private fun podcast(title: String, url: String, vararg categories: PodcastCategory) = Podcast(
        id = podcastIdFor(url),
        feedUrl = url,
        title = title,
        categories = categories.toList(),
    )

    private companion object {
        const val PODCAST_INDEX_RESPONSE = """{
          "status": "true",
          "feeds": [{
            "id": 1,
            "title": "Trending Show",
            "url": "https://feeds.example.com/trending.xml",
            "link": "https://example.com/trending",
            "description": "A trending podcast",
            "author": "Mollie",
            "artwork": "https://images.example.com/trending.jpg",
            "explicit": false,
            "episodeCount": 12,
            "newestItemPubdate": 1700000000,
            "categories": { "102": "Technology" }
          }]
        }"""

        const val APPLE_CHART_RESPONSE = """{
          "feed": { "results": [{
            "artistName": "Mammoth",
            "id": "123",
            "name": "Apple Chart Show",
            "artworkUrl100": "https://images.example.com/apple.jpg",
            "genres": [{ "genreId": "1318", "name": "Technology" }]
          }] }
        }"""

        const val APPLE_LOOKUP_RESPONSE = """{
          "resultCount": 1,
          "results": [{
            "collectionId": 123,
            "collectionName": "Apple Chart Show",
            "artistName": "Mammoth",
            "feedUrl": "https://feeds.example.com/apple.xml",
            "trackCount": 20
          }]
        }"""
    }
}

private fun DiscoverySourceResult.failureMessage(): String = when (this) {
    is DiscoverySourceResult.Failure -> message
    is DiscoverySourceResult.Unavailable -> message
    is DiscoverySourceResult.Success -> "success"
}
