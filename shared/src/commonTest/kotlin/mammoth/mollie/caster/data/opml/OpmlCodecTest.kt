package mammoth.mollie.caster.data.opml

import mammoth.mollie.caster.model.Podcast
import mammoth.mollie.caster.util.podcastIdFor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OpmlCodecTest {
    @Test
    fun nestedGroupsAndNormalizedDuplicatesAreHandled() {
        val result = OpmlCodec.parse("""
            <opml version="2.0"><body><outline text="Technology">
              <outline text="One" xmlUrl="https://EXAMPLE.com:443/feed.xml#top"/>
              <outline text="Duplicate" xmlUrl="https://example.com/feed.xml"/>
            </outline></body></opml>
        """.trimIndent())
        assertEquals(1, result.entries.size)
        assertEquals(1, result.duplicateCount)
    }

    @Test
    fun legacyHttpFeedsAreUpgradedToHttpsBeforeImport() {
        val result = OpmlCodec.parse("""
            <opml version="2.0"><body>
              <outline text="BBC" xmlUrl="http://newsrss.bbc.co.uk/rss/news.xml"/>
            </body></opml>
        """.trimIndent())

        assertEquals("https://newsrss.bbc.co.uk/rss/news.xml", result.entries.single().feedUrl)
    }

    @Test
    fun exportIsDeterministicAndEscapesXml() {
        val item = Podcast(podcastIdFor("https://example.com/?a=1&b=2"), "https://example.com/?a=1&b=2", title = "A & B", isSubscribed = true)
        val first = OpmlCodec.export(listOf(item))
        assertEquals(first, OpmlCodec.export(listOf(item)))
        assertTrue("A &amp; B" in first)
        assertTrue("a=1&amp;b=2" in first)
    }
}
