package mammoth.mollie.caster.data.opml

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parser.Parser
import mammoth.mollie.caster.model.Podcast
import mammoth.mollie.caster.util.normalizeFeedUrl

data class OpmlEntry(val title: String, val feedUrl: String, val websiteUrl: String? = null)
data class OpmlParseResult(val entries: List<OpmlEntry>, val duplicateCount: Int)

object OpmlCodec {
    fun parse(document: String): OpmlParseResult {
        val xml = Ksoup.parse(document, parser = Parser.xmlParser())
        val seen = mutableSetOf<String>()
        var duplicates = 0
        val entries = xml.select("outline").mapNotNull { outline ->
            // Legacy OPML exports frequently contain HTTP feed URLs. Android blocks cleartext
            // requests, so prefer TLS before de-duplicating and fetching the subscription.
            val feed = preferHttps(outline.attr("xmlUrl").ifBlank { outline.attr("xmlurl") }.trim())
            if (feed.isBlank()) return@mapNotNull null
            val normalized = normalizeFeedUrl(feed)
            if (!seen.add(normalized)) {
                duplicates++
                return@mapNotNull null
            }
            OpmlEntry(
                title = outline.attr("title").ifBlank { outline.attr("text") }.ifBlank { feed },
                feedUrl = feed,
                websiteUrl = outline.attr("htmlUrl").ifBlank { outline.attr("htmlurl") }.takeIf(String::isNotBlank),
            )
        }
        return OpmlParseResult(entries, duplicates)
    }

    fun export(subscriptions: List<Podcast>): String {
        val outlines = subscriptions.sortedWith(compareBy<Podcast>({ it.title.lowercase() }, { it.id.value }, { it.feedUrl })).joinToString("\n") { podcast ->
            val html = podcast.websiteUrl?.let { " htmlUrl=\"${escape(it)}\"" }.orEmpty()
            "    <outline type=\"rss\" text=\"${escape(podcast.title)}\" title=\"${escape(podcast.title)}\" xmlUrl=\"${escape(podcast.feedUrl)}\"$html/>"
        }
        return """<?xml version="1.0" encoding="UTF-8"?>
<opml version="2.0">
  <head><title>Molliecaster subscriptions</title></head>
  <body>
$outlines
  </body>
</opml>
"""
    }

    private fun escape(value: String): String = value
        .replace("&", "&amp;")
        .replace("\"", "&quot;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")

    private fun preferHttps(feedUrl: String): String = when {
        feedUrl.startsWith("http://", ignoreCase = true) -> buildString {
            append("https://")
            append(feedUrl.substringAfter("://"))
        }
        else -> feedUrl
    }
}
