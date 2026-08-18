package mammoth.mollie.caster.data.discovery

import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.model.Podcast
import mammoth.mollie.caster.model.PodcastCategories
import mammoth.mollie.caster.model.PodcastCategory
import mammoth.mollie.caster.util.normalizeFeedUrl

data class DiscoveryConfig(
    val appleStorefront: String = "us",
    /** Disabled by default so credential-free builds never contact Podcast Index. */
    val podcastIndexEnabled: Boolean = false,
    val podcastIndexBaseUrl: String = PODCAST_INDEX_API_BASE_URL,
    val podcastIndexApiKey: String = "",
    val podcastIndexApiSecret: String = "",
    /** A trusted proxy mirrors /podcasts/trending and injects Podcast Index authentication. */
    val podcastIndexUsesTrustedProxy: Boolean = false,
)

data class DiscoverySnapshot(
    val podcastIndexTop: List<Podcast> = emptyList(),
    val appleTop: List<Podcast> = emptyList(),
    val merged: List<Podcast> = emptyList(),
    val warnings: List<String> = emptyList(),
)

sealed interface DiscoverySourceResult {
    data class Success(val podcasts: List<Podcast>) : DiscoverySourceResult
    data class Failure(val message: String) : DiscoverySourceResult
    data class Unavailable(val message: String) : DiscoverySourceResult
}

internal const val PODCAST_INDEX_API_BASE_URL = "https://api.podcastindex.org/api/1.0"
internal const val DISCOVERY_SOURCE_LIMIT = 10

/** Interleaves both ranked lists and de-duplicates aliases by normalized RSS URL. */
fun mergeRankedDiscoverySources(
    podcastIndex: List<Podcast>,
    apple: List<Podcast>,
    perSourceLimit: Int = DISCOVERY_SOURCE_LIMIT,
): List<Podcast> {
    val first = podcastIndex.take(perSourceLimit)
    val second = apple.take(perSourceLimit)
    val merged = ArrayList<Podcast>(first.size + second.size)
    val seen = mutableSetOf<String>()
    repeat(maxOf(first.size, second.size)) { rank ->
        listOfNotNull(first.getOrNull(rank), second.getOrNull(rank)).forEach { podcast ->
            val identity = normalizeFeedUrl(podcast.canonicalFeedUrl.ifBlank { podcast.feedUrl })
            if (seen.add(identity)) merged += podcast
        }
    }
    return merged
}

/**
 * Lightweight on-device ranking. It preserves source rank for a cold start and boosts categories
 * represented in subscriptions, favorites, and playback history without uploading user activity.
 */
fun recommendFromDiscovery(
    candidates: List<Podcast>,
    libraryPodcasts: List<Podcast>,
    episodes: List<Episode>,
    favoriteEpisodeIds: Set<String>,
    historyEpisodeIds: Set<String>,
): List<Podcast> {
    if (candidates.isEmpty()) return emptyList()
    val engagedPodcastIds = buildSet {
        libraryPodcasts.filter { it.isSubscribed }.forEach { add(it.id) }
        episodes.filter { it.id.value in favoriteEpisodeIds || it.id.value in historyEpisodeIds }
            .forEach { add(it.podcastId) }
    }
    val categoryWeights = mutableMapOf<String, Int>()
    libraryPodcasts.filter { it.id in engagedPodcastIds }.forEach { podcast ->
        podcast.categories.forEach { category -> categoryWeights[category.key] = (categoryWeights[category.key] ?: 0) + 1 }
    }
    val subscribedUrls = libraryPodcasts.filter { it.isSubscribed }
        .flatMap { listOf(it.feedUrl, it.canonicalFeedUrl) }
        .mapTo(mutableSetOf(), ::normalizeFeedUrl)
    val eligible = candidates.filterNot { normalizeFeedUrl(it.feedUrl) in subscribedUrls }
        .ifEmpty { candidates }
    return eligible.withIndex()
        .sortedWith(compareByDescending<IndexedValue<Podcast>> { indexed ->
            indexed.value.categories.sumOf { categoryWeights[it.key] ?: 0 }
        }.thenBy { it.index })
        .map { it.value }
}

internal fun categoriesFromNames(names: List<String>): List<PodcastCategory> = names
    .map(String::trim)
    .filter(String::isNotBlank)
    .distinctBy { it.lowercase() }
    .map { name ->
        PodcastCategories.all.firstOrNull { known ->
            val normalized = name.lowercase().replace("&", "and")
            normalized == known.displayName.lowercase().replace("&", "and") ||
                normalized == known.key.replace("-", " ")
        } ?: PodcastCategory(
            key = name.lowercase().replace("&", "and").replace(Regex("[^a-z0-9]+"), "-").trim('-'),
            displayName = name,
        )
    }
