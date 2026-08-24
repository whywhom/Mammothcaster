package mammoth.mollie.caster.data

import kotlinx.coroutines.flow.StateFlow
import mammoth.mollie.caster.model.*

data class LibraryState(
    val podcasts: List<Podcast>,
    val episodes: List<Episode>,
    val favoriteIds: Set<EpisodeId> = emptySet(),
    val downloads: List<Download> = emptyList(),
    val history: List<PlaybackHistory> = emptyList(),
    val downloadsSupported: Boolean = false,
    val cellularDownloadControlSupported: Boolean = false,
    val cellularDownloadsAllowed: Boolean = false,
    val popularPodcasts: List<Podcast> = emptyList(),
    val discoveryLoading: Boolean = false,
    val discoveryWarnings: List<String> = emptyList(),
    val appleSearchQuery: String = "",
    val appleSearchResults: List<Podcast> = emptyList(),
    val appleSearchLoading: Boolean = false,
    val appleSearchError: String? = null,
    val appleCategoryKey: String? = null,
    val appleCategoryResults: List<Podcast> = emptyList(),
    val appleCategoryLoading: Boolean = false,
    val appleCategoryError: String? = null,
    val feedPreview: FeedPreview? = null,
    val feedPreviewUrl: String? = null,
    val feedPreviewLoading: Boolean = false,
    val feedPreviewError: String? = null,
    val busy: Boolean = false,
    val message: String? = null,
)
data class OpmlImportReport(val imported: Int, val duplicates: Int, val failures: List<String>)
data class DownloadSnapshot(val initialized: Boolean = false, val items: List<Download> = emptyList())
data class FeedPreview(val feedUrl: String, val podcast: Podcast, val episodes: List<Episode>)
interface EpisodeDownloadGateway {
    val downloads: StateFlow<DownloadSnapshot>
    val supported: Boolean
    val cellularDownloadControlSupported: Boolean
    val cellularDownloadsAllowed: StateFlow<Boolean>
    fun download(episode: Episode, podcastTitle: String)
    fun delete(episodeId: EpisodeId)
    fun setCellularDownloadsAllowed(allowed: Boolean)
}
