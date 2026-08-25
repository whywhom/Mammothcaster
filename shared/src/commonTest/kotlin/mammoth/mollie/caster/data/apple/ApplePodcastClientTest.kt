package mammoth.mollie.caster.data.apple

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import mammoth.mollie.caster.model.PodcastCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ApplePodcastClientTest {
    @Test
    fun searchesAppleAndOnlyReturnsResultsWithRssFeeds() = runTest {
        val client = HttpClient(MockEngine { request ->
            assertEquals("podcast", request.url.parameters["media"])
            assertEquals("podcast", request.url.parameters["entity"])
            assertEquals("Mollie Talks", request.url.parameters["term"])
            respond(RESPONSE, HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
        })

        val result = ApplePodcastClient(client).search("Mollie Talks")

        val success = assertIs<ApplePodcastSearchResult.Success>(result)
        assertEquals(1, success.podcasts.size)
        assertEquals("Mollie Talks", success.podcasts.single().title)
        assertEquals("https://feeds.example.com/mollie.xml", success.podcasts.single().feedUrl)
        assertTrue(success.podcasts.single().isExplicit)
    }

    @Test
    fun searchesCategoryByNameAndRequestsAppleMaximum() = runTest {
        val technology = PodcastCategory("technology", "Technology")
        val client = HttpClient(MockEngine { request ->
            assertEquals("Technology", request.url.parameters["term"])
            assertEquals(null, request.url.parameters["attribute"])
            assertEquals("podcast", request.url.parameters["media"])
            assertEquals("podcast", request.url.parameters["entity"])
            assertEquals("200", request.url.parameters["limit"])
            assertEquals("nz", request.url.parameters["country"])
            respond(RESPONSE, HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
        })

        val result = ApplePodcastClient(client, country = "NZ").searchCategory(technology)

        val podcast = assertIs<ApplePodcastSearchResult.Success>(result).podcasts.single()
        assertEquals(listOf(technology), podcast.categories)
    }

    private companion object {
        const val RESPONSE = """{
          "resultCount": 2,
          "results": [
            {
              "collectionName": "Mollie Talks",
              "artistName": "Mammoth",
              "feedUrl": "https://feeds.example.com/mollie.xml",
              "artworkUrl100": "https://images.example.com/mollie.jpg",
              "collectionExplicitness": "explicit",
              "trackCount": 42
            },
            { "collectionName": "No feed result", "artistName": "Mammoth" }
          ]
        }"""
    }
}
