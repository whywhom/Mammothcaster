package mammoth.mollie.caster.data.rss

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RssParserTest {
    @Test
    fun parsesRssItunesAndMissingOptionalFields() {
        val parsed = RssParser().parse(RSS, "https://Example.com:443/feed.xml#fragment")
        assertEquals("Mollie Talks", parsed.podcast.title)
        assertEquals("Mollie", parsed.podcast.author)
        assertEquals(2, parsed.episodes.size)
        assertEquals(3_723_000, parsed.episodes.first().durationMillis)
        assertNotNull(parsed.episodes.first().publishedAtMillis)
        assertNull(parsed.episodes.last().publishedAtMillis)
        assertTrue(parsed.episodes.first().enclosures.first().url.contains(",part"))
    }

    @Test
    fun durationAcceptsRequiredFormats() {
        assertEquals(12_000, parseDurationMillis("12"))
        assertEquals(62_000, parseDurationMillis("1:02"))
        assertEquals(3_723_000, parseDurationMillis("1:02:03"))
        assertNull(parseDurationMillis("bad"))
        assertNull(parseDurationMillis("-1"))
    }

    @Test
    fun parsesMediaRssArtworkAndUpgradesCleartextUrls() {
        val parsed = RssParser().parse(
            """
                <rss xmlns:media="http://search.yahoo.com/mrss/"><channel>
                  <title>BBC-style feed</title>
                  <media:thumbnail url="http://images.example.com/cover.jpg"/>
                </channel></rss>
            """.trimIndent(),
            "https://example.com/feed.xml",
        )

        assertEquals("https://images.example.com/cover.jpg", parsed.podcast.artworkUrl)
    }

    private companion object {
        val RSS = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd">
              <channel>
                <title>Mollie Talks</title>
                <link>https://example.com/show</link>
                <description><![CDATA[A <b>friendly</b> show.]]></description>
                <itunes:author>Mollie</itunes:author>
                <itunes:category text="Technology"/>
                <item>
                  <guid>ep-one</guid><title>Episode one</title>
                  <pubDate>Tue, 12 Aug 2025 10:30:00 +1200</pubDate>
                  <itunes:duration>1:02:03</itunes:duration>
                  <description><![CDATA[Hello <em>world</em>]]></description>
                  <enclosure url="https://cdn.example.com/audio,part.mp3" type="audio/mpeg" length="42"/>
                </item>
                <item><guid>ep-two</guid><title>Episode two</title><itunes:duration>broken</itunes:duration></item>
              </channel>
            </rss>
        """.trimIndent()
    }
}
