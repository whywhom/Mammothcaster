package mammoth.mollie.caster.model

@JvmInline
value class PodcastId(val value: String)

@JvmInline
value class EpisodeId(val value: String)

enum class EpisodeOrder { Newest, Oldest }

enum class DownloadState { Queued, Downloading, Completed, Failed, Removing }

data class Podcast(
    val id: PodcastId,
    val feedUrl: String,
    val canonicalFeedUrl: String = feedUrl,
    val websiteUrl: String? = null,
    val title: String,
    val author: String = "",
    val description: String = "",
    val artworkUrl: String? = null,
    val language: String? = null,
    val copyright: String? = null,
    val isExplicit: Boolean = false,
    val categories: List<PodcastCategory> = emptyList(),
    val episodeCount: Int = 0,
    val latestEpisodeAtMillis: Long? = null,
    val isSubscribed: Boolean = false,
    val lastRefreshAtMillis: Long? = null,
)

data class Episode(
    val id: EpisodeId,
    val podcastId: PodcastId,
    val guid: String? = null,
    val permalinkUrl: String? = null,
    val title: String,
    val subtitle: String = "",
    val summary: String = "",
    val descriptionHtml: String = "",
    val author: String = "",
    val publishedAtMillis: Long? = null,
    val durationMillis: Long? = null,
    val artworkUrl: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val episodeType: String? = null,
    val isExplicit: Boolean = false,
    val enclosures: List<Enclosure> = emptyList(),
    val isFavorite: Boolean = false,
    val playbackPositionMillis: Long = 0,
)

data class Enclosure(
    val url: String,
    val mimeType: String? = null,
    val lengthBytes: Long? = null,
)

data class PodcastCategory(val key: String, val displayName: String)

data class PlaybackHistory(
    val episodeId: EpisodeId,
    val lastPlayedAtMillis: Long,
    val positionMillis: Long,
    val durationMillis: Long?,
    val totalPlayedMillis: Long,
    val completed: Boolean,
)

data class Download(
    val episodeId: EpisodeId,
    val sourceUrl: String,
    val state: DownloadState,
    val localReference: String? = null,
    val receivedBytes: Long = 0,
    val totalBytes: Long? = null,
    val failureMessage: String? = null,
)

object PodcastCategories {
    /** Apple Podcasts top-level categories, plus AI as a Molliecaster discovery category. */
    val all = listOf(
        PodcastCategory("arts", "Arts"),
        PodcastCategory("business", "Business"),
        PodcastCategory("comedy", "Comedy"),
        PodcastCategory("education", "Education"),
        PodcastCategory("fiction", "Fiction"),
        PodcastCategory("government", "Government"),
        PodcastCategory("history", "History"),
        PodcastCategory("health", "Health & Fitness"),
        PodcastCategory("kids-family", "Kids & Family"),
        PodcastCategory("leisure", "Leisure"),
        PodcastCategory("music", "Music"),
        PodcastCategory("news", "News"),
        PodcastCategory("religion-spirituality", "Religion & Spirituality"),
        PodcastCategory("science", "Science"),
        PodcastCategory("society-culture", "Society & Culture"),
        PodcastCategory("sports", "Sports"),
        PodcastCategory("technology", "Technology"),
        PodcastCategory("artificial-intelligence", "AI"),
        PodcastCategory("true-crime", "True Crime"),
        PodcastCategory("tv-film", "TV & Film"),
    )

    /** Categories actually present in the user's subscribed RSS feeds. */
    fun fromSubscriptions(podcasts: List<Podcast>): List<PodcastCategory> {
        val subscribed = podcasts.asSequence()
            .filter(Podcast::isSubscribed)
            .flatMap { it.categories.asSequence() }
            .distinctBy(PodcastCategory::key)
            .toList()
        val subscribedByKey = subscribed.associateBy(PodcastCategory::key)
        val known = all.mapNotNull { category -> subscribedByKey[category.key]?.let { category } }
        val knownKeys = all.mapTo(mutableSetOf(), PodcastCategory::key)
        val sourceSpecific = subscribed.filterNot { it.key in knownKeys }.sortedBy { it.displayName.lowercase() }
        return known + sourceSpecific
    }
}
