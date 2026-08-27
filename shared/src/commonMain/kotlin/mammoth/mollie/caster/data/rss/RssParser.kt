package mammoth.mollie.caster.data.rss

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.parser.Parser
import mammoth.mollie.caster.model.Enclosure
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.model.Podcast
import mammoth.mollie.caster.model.PodcastCategory
import mammoth.mollie.caster.util.episodeIdFor
import mammoth.mollie.caster.util.podcastIdFor

data class ParsedFeed(val podcast: Podcast, val episodes: List<Episode>, val warnings: List<String>)

class RssParser {
    fun parse(xml: String, sourceUrl: String): ParsedFeed {
        val document = Ksoup.parse(xml, parser = Parser.xmlParser())
        val channel = document.getAllElements().firstOrNull { it.localName() in setOf("channel", "feed") }
            ?: error("Document is not an RSS or Atom feed")
        val isAtom = channel.localName() == "feed"
        val title = channel.childText("title").ifBlank { error("Feed title is missing") }
        val description = channel.childText("description", "summary", "subtitle")
        val author = channel.childText("author", "itunes:author", "managingEditor")
            .ifBlank { channel.firstChildNamed("author")?.childText("name").orEmpty() }
        val selfUrl = channel.directChildren("link").firstOrNull { it.attr("rel") == "self" }?.attr("href")
        val website = channel.directChildren("link").firstOrNull { it.attr("rel").let { rel -> rel.isBlank() || rel == "alternate" } }
            ?.let { if (isAtom) it.attr("href") else it.text() }
            ?.takeIf(String::isNotBlank)
        val artwork = channel.artworkUrl()
        val rawCategories = channel.directChildren("category", "itunes:category").mapNotNull {
            (it.attr("text").ifBlank { it.text() }).takeIf(String::isNotBlank)
        }
        val categories = rawCategories.distinct().map { PodcastCategory(categoryKey(it), it) }
        val podcastId = podcastIdFor(sourceUrl)
        val itemElements = channel.directChildren(if (isAtom) "entry" else "item")
        val warnings = mutableListOf<String>()
        val episodes = itemElements.mapNotNull { item ->
            runCatching {
                val episodeTitle = item.childText("title").ifBlank { "Untitled episode" }
                val guid = item.childText("guid", "id").takeIf(String::isNotBlank)
                val permalink = item.directChildren("link").firstOrNull { it.attr("rel") != "enclosure" }
                    ?.let { if (isAtom) it.attr("href") else it.text() }?.takeIf(String::isNotBlank)
                val enclosures = buildList {
                    item.directChildren("enclosure").forEach { node ->
                        node.attr("url").takeIf(String::isNotBlank)?.let {
                            add(Enclosure(it, node.attr("type").takeIf(String::isNotBlank), node.attr("length").toLongOrNull()))
                        }
                    }
                    item.directChildren("link").filter { it.attr("rel") == "enclosure" }.forEach { node ->
                        node.attr("href").takeIf(String::isNotBlank)?.let {
                            add(Enclosure(it, node.attr("type").takeIf(String::isNotBlank), node.attr("length").toLongOrNull()))
                        }
                    }
                }.distinctBy { it.url }
                val date = parseDateMillis(item.childText("pubDate", "published", "updated", "dc:date"))
                val html = item.childText("content:encoded", "description", "summary")
                Episode(
                    id = episodeIdFor(podcastId, guid, enclosures, permalink, episodeTitle, date),
                    podcastId = podcastId,
                    guid = guid,
                    permalinkUrl = permalink,
                    title = episodeTitle,
                    subtitle = item.childText("itunes:subtitle"),
                    summary = plainText(item.childText("itunes:summary").ifBlank { html }),
                    descriptionHtml = html,
                    author = item.childText("itunes:author", "author"),
                    publishedAtMillis = date,
                    durationMillis = parseDurationMillis(item.childText("itunes:duration")),
                    artworkUrl = item.artworkUrl(),
                    seasonNumber = item.childText("itunes:season").toIntOrNull(),
                    episodeNumber = item.childText("itunes:episode").toIntOrNull(),
                    episodeType = item.childText("itunes:episodeType").takeIf(String::isNotBlank),
                    isExplicit = item.childText("itunes:explicit").lowercase() in setOf("yes", "true", "explicit"),
                    enclosures = enclosures,
                )
            }.onFailure { warnings += "Skipped an invalid episode: ${it.message.orEmpty()}" }.getOrNull()
        }.distinctBy { it.id }
        val latest = episodes.mapNotNull { it.publishedAtMillis }.maxOrNull()
        return ParsedFeed(
            podcast = Podcast(
                id = podcastId,
                feedUrl = sourceUrl,
                canonicalFeedUrl = selfUrl?.takeIf(String::isNotBlank) ?: sourceUrl,
                websiteUrl = website,
                title = title,
                author = author,
                description = plainText(description),
                artworkUrl = artwork ?: episodes.firstNotNullOfOrNull(Episode::artworkUrl),
                language = channel.childText("language").takeIf(String::isNotBlank),
                copyright = channel.childText("copyright").takeIf(String::isNotBlank),
                isExplicit = channel.childText("itunes:explicit").lowercase() in setOf("yes", "true", "explicit"),
                categories = categories,
                episodeCount = episodes.size,
                latestEpisodeAtMillis = latest,
            ),
            episodes = episodes,
            warnings = warnings,
        )
    }
}

private fun Element.localName() = tagName().lowercase().substringAfter(':')

private fun Element.directChildren(vararg names: String): List<Element> {
    val accepted = names.map { it.lowercase() }.toSet()
    return children().filter { child ->
        val full = child.tagName().lowercase()
        full in accepted || child.localName() in accepted.map { it.substringAfter(':') }
    }
}

private fun Element.firstChildNamed(name: String): Element? = directChildren(name).firstOrNull()

private fun Element.childText(vararg names: String): String = names.firstNotNullOfOrNull { name ->
    firstChildNamed(name)?.let { child -> child.text().ifBlank { child.html() } }?.takeIf(String::isNotBlank)
}.orEmpty().trim()

/** Supports common RSS, iTunes, Atom, and Media RSS cover declarations. */
private fun Element.artworkUrl(): String? = listOfNotNull(
    firstChildNamed("itunes:image")?.attr("href"),
    firstChildNamed("itunes:image")?.attr("url"),
    firstChildNamed("image")?.attr("href"),
    firstChildNamed("image")?.childText("url"),
    firstChildNamed("media:thumbnail")?.attr("url"),
    firstChildNamed("media:content")
        ?.takeIf { it.attr("medium").equals("image", ignoreCase = true) || it.attr("type").startsWith("image/", ignoreCase = true) }
        ?.attr("url"),
    firstChildNamed("logo")?.text(),
    firstChildNamed("icon")?.text(),
).firstNotNullOfOrNull(::httpsArtworkUrl)

/** Android blocks clear-text images; protocol-relative values should also resolve securely. */
private fun httpsArtworkUrl(raw: String): String? {
    val url = raw.trim()
    return when {
        url.startsWith("https://", ignoreCase = true) -> url
        url.startsWith("http://", ignoreCase = true) -> "https" + url.drop(4)
        url.startsWith("//") -> "https:$url"
        else -> null
    }
}

private fun plainText(html: String): String = if (html.isBlank()) "" else Ksoup.parse(html).text().trim()

fun parseDurationMillis(raw: String): Long? {
    if (raw.isBlank()) return null
    val parts = raw.trim().split(':').map { it.toLongOrNull() ?: return null }
    if (parts.any { it < 0 } || parts.size !in 1..3) return null
    val seconds = when (parts.size) {
        1 -> parts[0]
        2 -> parts[0] * 60 + parts[1]
        else -> parts[0] * 3600 + parts[1] * 60 + parts[2]
    }
    return seconds * 1000
}

internal fun parseDateMillis(raw: String): Long? {
    if (raw.isBlank()) return null
    val normalized = raw.trim().substringAfter(", ")
    val tokens = normalized.split(Regex("\\s+")).filter(String::isNotBlank)
    if (tokens.size >= 4 && tokens[1].take(3).lowercase() in monthNames) {
        val day = tokens[0].toIntOrNull() ?: return null
        val month = monthNames.indexOf(tokens[1].take(3).lowercase()) + 1
        val year = tokens[2].toIntOrNull() ?: return null
        val time = tokens[3].split(':').mapNotNull(String::toIntOrNull)
        if (time.size < 2) return null
        val zoneOffsetSeconds = parseZoneOffset(tokens.getOrNull(4).orEmpty())
        return (daysFromCivil(year, month, day) * 86_400L + time[0] * 3600L + time[1] * 60L + time.getOrElse(2) { 0 } - zoneOffsetSeconds) * 1000L
    }
    val match = Regex("(\\d{4})-(\\d{2})-(\\d{2})[Tt ](\\d{2}):(\\d{2})(?::(\\d{2})(?:\\.\\d+)?)?(?:[Zz]|([+-])(\\d{2}):?(\\d{2}))?").matchEntire(normalized)
        ?: return null
    val year = match.groupValues[1].toIntOrNull() ?: return null
    val month = match.groupValues[2].toIntOrNull() ?: return null
    val day = match.groupValues[3].toIntOrNull() ?: return null
    val hour = match.groupValues[4].toIntOrNull() ?: return null
    val minute = match.groupValues[5].toIntOrNull() ?: return null
    val second = match.groupValues[6].toIntOrNull() ?: 0
    val sign = if (match.groupValues[7] == "-") -1 else 1
    val offset = sign * ((match.groupValues[8].toIntOrNull() ?: 0) * 3600 + (match.groupValues[9].toIntOrNull() ?: 0) * 60)
    return (daysFromCivil(year, month, day) * 86_400L + hour * 3600L + minute * 60L + second - offset) * 1000L
}

private val monthNames = listOf("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec")
private fun parseZoneOffset(zone: String): Int = when {
    zone.equals("GMT", true) || zone.equals("UTC", true) || zone.equals("UT", true) -> 0
    Regex("[+-]\\d{4}").matches(zone) -> {
        val sign = if (zone[0] == '-') -1 else 1
        sign * (zone.substring(1, 3).toInt() * 3600 + zone.substring(3, 5).toInt() * 60)
    }
    else -> 0
}

private fun daysFromCivil(year: Int, month: Int, day: Int): Long {
    val adjustedYear = year - if (month <= 2) 1 else 0
    val era = if (adjustedYear >= 0) adjustedYear / 400 else (adjustedYear - 399) / 400
    val yearOfEra = adjustedYear - era * 400
    val adjustedMonth = month + if (month > 2) -3 else 9
    val dayOfYear = (153 * adjustedMonth + 2) / 5 + day - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    return era.toLong() * 146097L + dayOfEra - 719468L
}

private fun categoryKey(value: String): String = when (value.trim().lowercase()) {
    "ai", "artificial intelligence", "machine learning" -> "artificial-intelligence"
    "tech" -> "technology"
    "health and fitness", "health & fitness" -> "health"
    "society and culture", "society & culture" -> "society-culture"
    "kids and family", "kids & family" -> "kids-family"
    else -> value.trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
}
