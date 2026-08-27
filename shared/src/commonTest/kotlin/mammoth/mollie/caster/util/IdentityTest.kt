package mammoth.mollie.caster.util

import mammoth.mollie.caster.model.Enclosure
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class IdentityTest {
    @Test
    fun sha256MatchesKnownVector() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", sha256("abc"))
    }

    @Test
    fun sha1MatchesKnownVector() {
        assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", sha1("abc"))
    }

    @Test
    fun feedNormalizationPreservesSemanticPathAndQuery() {
        assertEquals("https://example.com/Feed/?b=2&a=1", normalizeFeedUrl(" HTTPS://Example.COM:443/Feed/?b=2&a=1#player "))
    }

    @Test
    fun episodeIdentityPrefersGuidAndIgnoresOrdering() {
        val podcast = podcastIdFor("https://example.com/feed.xml")
        val first = episodeIdFor(podcast, "episode-7", listOf(Enclosure("https://cdn.example.com/a.mp3")), null, "A", 10)
        val second = episodeIdFor(podcast, "episode-7", emptyList(), "https://example.com/changed", "Changed", 20)
        assertEquals(first, second)
        assertNotEquals(first, episodeIdFor(podcast, "episode-8", emptyList(), null, "A", 10))
    }
}
