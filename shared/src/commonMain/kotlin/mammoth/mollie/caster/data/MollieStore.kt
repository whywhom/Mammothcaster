package mammoth.mollie.caster.data

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mammoth.mollie.caster.data.apple.ApplePodcastClient
import mammoth.mollie.caster.data.apple.ApplePodcastSearchResult
import mammoth.mollie.caster.data.cache.InMemoryObjectStorage
import mammoth.mollie.caster.data.cache.MetadataCacheKind
import mammoth.mollie.caster.data.cache.ObjectStorage
import mammoth.mollie.caster.data.cache.TTL
import mammoth.mollie.caster.data.cache.isFresh
import mammoth.mollie.caster.data.cache.validateRemoteMedia
import mammoth.mollie.caster.data.database.CategoryEntity
import mammoth.mollie.caster.data.database.DownloadEntity
import mammoth.mollie.caster.data.database.EpisodeEnclosureEntity
import mammoth.mollie.caster.data.database.EpisodeEntity
import mammoth.mollie.caster.data.database.FavoriteEntity
import mammoth.mollie.caster.data.database.FeedAliasEntity
import mammoth.mollie.caster.data.database.FeedSyncStateEntity
import mammoth.mollie.caster.data.database.MollieDatabase
import mammoth.mollie.caster.data.database.LocalPlaylistEntity
import mammoth.mollie.caster.data.database.LocalPlaylistItemEntity
import mammoth.mollie.caster.data.database.PodcastCategoryEntity
import mammoth.mollie.caster.data.database.PodcastEntity
import mammoth.mollie.caster.data.database.SubscriptionEntity
import mammoth.mollie.caster.data.discovery.DiscoveryConfig
import mammoth.mollie.caster.data.discovery.PodcastDiscoveryService
import mammoth.mollie.caster.data.opml.OpmlCodec
import mammoth.mollie.caster.data.rss.FeedClient
import mammoth.mollie.caster.data.rss.FeedRefreshResult
import mammoth.mollie.caster.data.rss.createHttpClient
import mammoth.mollie.caster.model.Download
import mammoth.mollie.caster.model.DownloadState
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.model.EpisodeId
import mammoth.mollie.caster.model.PlaybackHistory
import mammoth.mollie.caster.model.LocalAudioFile
import mammoth.mollie.caster.model.LocalPlaylist
import mammoth.mollie.caster.model.sortedByFileName
import mammoth.mollie.caster.model.sortedByPlaylistName
import mammoth.mollie.caster.model.Podcast
import mammoth.mollie.caster.model.PodcastCategory
import mammoth.mollie.caster.model.PodcastId
import mammoth.mollie.caster.platform.currentTimeMillis
import mammoth.mollie.caster.util.normalizeFeedUrl
import mammoth.mollie.caster.util.podcastIdFor

private const val LEGACY_PLACEHOLDER_MEDIA_URL = "https://ondemand.npr.org/anon.npr-mp3/npr/pmoney/2025/01/placeholder.mp3"

private object NoOpDownloadGateway : EpisodeDownloadGateway {
    override val downloads: StateFlow<DownloadSnapshot> = MutableStateFlow(DownloadSnapshot())
    override val supported: Boolean = false
    override val cellularDownloadControlSupported: Boolean = false
    override val cellularDownloadsAllowed: StateFlow<Boolean> = MutableStateFlow(false)
    override fun download(episode: Episode, podcastTitle: String) = Unit
    override fun delete(episodeId: EpisodeId) = Unit
    override fun setCellularDownloadsAllowed(allowed: Boolean) = Unit
}

class MollieStore(
    httpClient: HttpClient = createHttpClient(),
    private val downloadGateway: EpisodeDownloadGateway = NoOpDownloadGateway,
    private val database: MollieDatabase? = null,
    discoveryConfig: DiscoveryConfig = DiscoveryConfig(),
    private val clock: () -> Long = ::currentTimeMillis,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val feedClient = FeedClient(httpClient)
    private val applePodcastClient = ApplePodcastClient(httpClient, discoveryConfig.appleStorefront)
    private val discoveryService = PodcastDiscoveryService(httpClient, discoveryConfig)
    private val mutableState = MutableStateFlow(seedState().copy(
        downloadsSupported = downloadGateway.supported,
        cellularDownloadControlSupported = downloadGateway.cellularDownloadControlSupported,
        cellularDownloadsAllowed = downloadGateway.cellularDownloadsAllowed.value,
    ))
    private val playbackWriteMutex = Mutex()
    private val previewCacheMutex = Mutex()
    private var lastPlaybackTimestamp = 0L
    private var discoveryValidatedAtMillis: Long? = null
    private val feedPreviewCache: ObjectStorage = InMemoryObjectStorage(MAX_FEED_PREVIEWS, clock)
    private val latestPreviewRequest = mutableMapOf<String, Long>()
    private var previewRequestSequence = 0L
    val state: StateFlow<LibraryState> = mutableState.asStateFlow()

    init {
        scope.launch {
            val localDatabase = database
            if (localDatabase != null) restore(localDatabase)
            scope.launch { collectDownloadSnapshots() }
            scope.launch { downloadGateway.cellularDownloadsAllowed.collect { allowed ->
                mutableState.update { it.copy(cellularDownloadsAllowed = allowed) }
            } }
            scope.launch { refreshDiscovery(force = false) }
            refreshSubscriptions(force = false)
        }
    }

    private suspend fun collectDownloadSnapshots() {
        downloadGateway.downloads.collect { snapshot ->
            if (!snapshot.initialized) return@collect
            val updates = snapshot.items
            mutableState.update { value -> value.copy(downloads = updates) }
            database?.let { db ->
                val knownEpisodeIds = state.value.episodes.mapTo(mutableSetOf()) { it.id.value }
                val persistable = updates.filter { it.episodeId.value in knownEpisodeIds }
                runCatching {
                    val existing = db.downloadDao().allDownloads().associateBy { it.episodeId }
                    db.downloadDao().replaceAll(persistable.map { item ->
                        val now = clock()
                        val previous = existing[item.episodeId.value]
                        DownloadEntity(
                            item.episodeId.value, item.sourceUrl, item.state.name, item.localReference,
                            item.receivedBytes, item.totalBytes, null, item.failureMessage,
                            previous?.requestedAt ?: now,
                            now,
                            if (item.state == DownloadState.Completed) previous?.completedAt ?: now else null,
                        )
                    })
                }.onFailure { error ->
                    mutableState.update { it.copy(message = error.message ?: "Could not reconcile downloaded episodes") }
                }
            }
        }
    }

    suspend fun refreshDiscovery(force: Boolean = false) {
        val now = clock()
        if (!force && state.value.popularPodcasts.isNotEmpty() && MetadataCacheKind.PodcastList.isFresh(discoveryValidatedAtMillis, now)) return
        mutableState.update { it.copy(discoveryLoading = true, discoveryWarnings = emptyList()) }
        val snapshot = discoveryService.refresh()
        if (snapshot.merged.isNotEmpty()) discoveryValidatedAtMillis = now
        mutableState.update { value ->
            value.copy(
                popularPodcasts = snapshot.merged.ifEmpty { value.popularPodcasts },
                discoveryLoading = false,
                discoveryWarnings = snapshot.warnings,
            )
        }
    }

    suspend fun subscribeFeed(feedUrl: String): FeedRefreshResult {
        mutableState.update { it.copy(busy = true, message = null) }
        val normalizedUrl = normalizeFeedUrl(feedUrl)
        val cachedPodcastId = database?.podcastDao()?.resolveByFeedAlias(normalizedUrl) ?: podcastIdFor(feedUrl).value
        val cached = database?.syncDao()?.state(cachedPodcastId)
        val result = feedClient.fetch(feedUrl, cached?.etag, cached?.lastModified)
        when (result) {
            is FeedRefreshResult.Updated -> {
                val podcast = result.feed.podcast.copy(
                    artworkUrl = result.feed.podcast.artworkUrl ?: cachedArtworkFor(normalizedUrl),
                    isSubscribed = true,
                    lastRefreshAtMillis = clock(),
                )
                val syncedFeed = result.feed.copy(podcast = podcast)
                database?.let { persistFeed(it, result.copy(feed = syncedFeed)) }
                mutableState.update { old ->
                    old.copy(
                        podcasts = old.podcasts.filterNot { it.id == podcast.id } + podcast,
                        episodes = old.episodes.filterNot { oldEpisode -> syncedFeed.episodes.any { it.id == oldEpisode.id } } + syncedFeed.episodes,
                        busy = false,
                        message = "Subscribed to ${podcast.title}",
                    )
                }
            }
            is FeedRefreshResult.NotModified -> {
                val id = PodcastId(cachedPodcastId)
                val validatedAt = clock()
                mutableState.update { value ->
                    value.copy(
                        podcasts = value.podcasts.map { if (it.id == id) it.copy(isSubscribed = true, lastRefreshAtMillis = validatedAt) else it },
                        busy = false,
                        message = "Feed has not changed",
                    )
                }
                database?.podcastDao()?.updateLastRefreshAt(id.value, validatedAt)
                database?.userDataDao()?.addSubscription(SubscriptionEntity(id.value, validatedAt))
            }
            else -> mutableState.update { it.copy(busy = false, message = refreshError(result)) }
        }
        database?.let { db -> persistSyncState(db, feedUrl, result) }
        return result
    }

    suspend fun refreshSubscriptions(force: Boolean = false): Int {
        val now = clock()
        val feeds = state.value.podcasts.filter { podcast ->
            podcast.isSubscribed && (
                force || podcast.artworkUrl.isNullOrBlank() ||
                    !MetadataCacheKind.EpisodeList.isFresh(podcast.lastRefreshAtMillis, now)
                )
        }.map { it.feedUrl }
        var updated = 0
        feeds.forEach { if (subscribeFeed(it) is FeedRefreshResult.Updated) updated++ }
        mutableState.update {
            it.copy(
                message = if (force) "Refreshed $updated of ${feeds.size} subscriptions" else it.message,
                busy = false,
            )
        }
        return updated
    }

    /** Search Apple's directory. Results stay out of the library until RSS subscription succeeds. */
    suspend fun searchApplePodcasts(query: String) {
        if (query.isBlank()) {
            mutableState.update { it.copy(appleSearchQuery = "", appleSearchResults = emptyList(), appleSearchLoading = false, appleSearchError = null) }
            return
        }
        val normalizedQuery = query.trim()
        mutableState.update { it.copy(appleSearchQuery = normalizedQuery, appleSearchResults = emptyList(), appleSearchLoading = true, appleSearchError = null) }
        when (val result = applePodcastClient.search(query)) {
            is ApplePodcastSearchResult.Success -> mutableState.update {
                if (it.appleSearchQuery == normalizedQuery) it.copy(appleSearchResults = result.podcasts, appleSearchLoading = false) else it
            }
            is ApplePodcastSearchResult.HttpFailure -> mutableState.update {
                if (it.appleSearchQuery == normalizedQuery) it.copy(appleSearchResults = emptyList(), appleSearchLoading = false, appleSearchError = "Apple Podcasts returned HTTP ${result.status}") else it
            }
            is ApplePodcastSearchResult.NetworkFailure -> mutableState.update {
                if (it.appleSearchQuery == normalizedQuery) it.copy(appleSearchResults = emptyList(), appleSearchLoading = false, appleSearchError = result.message) else it
            }
            ApplePodcastSearchResult.TimedOut -> mutableState.update {
                if (it.appleSearchQuery == normalizedQuery) it.copy(
                    appleSearchResults = emptyList(),
                    appleSearchLoading = false,
                    appleSearchError = "Apple Podcasts took too long to respond. Check the connection and try again.",
                ) else it
            }
        }
    }

    /** Search Apple's directory by its podcast genre index without persisting results. */
    suspend fun searchAppleCategory(category: PodcastCategory) {
        mutableState.update {
            it.copy(
                appleCategoryKey = category.key,
                appleCategoryResults = emptyList(),
                appleCategoryLoading = true,
                appleCategoryError = null,
            )
        }
        when (val result = applePodcastClient.searchCategory(category)) {
            is ApplePodcastSearchResult.Success -> mutableState.update {
                if (it.appleCategoryKey == category.key) {
                    it.copy(appleCategoryResults = result.podcasts, appleCategoryLoading = false)
                } else it
            }
            is ApplePodcastSearchResult.HttpFailure -> mutableState.update {
                if (it.appleCategoryKey == category.key) it.copy(
                    appleCategoryResults = emptyList(),
                    appleCategoryLoading = false,
                    appleCategoryError = "Apple Podcasts returned HTTP ${result.status}",
                ) else it
            }
            is ApplePodcastSearchResult.NetworkFailure -> mutableState.update {
                if (it.appleCategoryKey == category.key) it.copy(
                    appleCategoryResults = emptyList(),
                    appleCategoryLoading = false,
                    appleCategoryError = result.message,
                ) else it
            }
            ApplePodcastSearchResult.TimedOut -> mutableState.update {
                if (it.appleCategoryKey == category.key) it.copy(
                    appleCategoryResults = emptyList(),
                    appleCategoryLoading = false,
                    appleCategoryError = "Apple Podcasts took too long to respond. Check the connection and try again.",
                ) else it
            }
        }
    }

    /** Fetch a search result's RSS metadata and episodes without changing subscriptions. */
    suspend fun previewFeed(feedUrl: String, force: Boolean = false) {
        val normalizedUrl = normalizeFeedUrl(feedUrl)
        val cachedPreview = if (force) null else feedPreviewCache.getCurrent(normalizedUrl, FeedPreview::class)
        if (!force && cachedPreview != null) {
            mutableState.update {
                it.copy(feedPreview = cachedPreview, feedPreviewUrl = feedUrl, feedPreviewLoading = false, feedPreviewError = null)
            }
            return
        }
        val cachedPodcast = state.value.podcasts.firstOrNull {
            normalizeFeedUrl(it.feedUrl) == normalizedUrl || normalizeFeedUrl(it.canonicalFeedUrl) == normalizedUrl
        }
        if (!force && cachedPodcast != null && MetadataCacheKind.EpisodeDetail.isFresh(cachedPodcast.lastRefreshAtMillis, clock())) {
            mutableState.update {
                it.copy(
                    feedPreview = FeedPreview(feedUrl, cachedPodcast, it.episodes.filter { episode -> episode.podcastId == cachedPodcast.id }),
                    feedPreviewUrl = feedUrl,
                    feedPreviewLoading = false,
                    feedPreviewError = null,
                )
            }
            return
        }
        mutableState.update { it.copy(feedPreview = null, feedPreviewUrl = feedUrl, feedPreviewLoading = true, feedPreviewError = null) }
        val requestId = previewCacheMutex.withLock {
            (++previewRequestSequence).also { latestPreviewRequest[normalizedUrl] = it }
        }
        when (val result = feedClient.fetch(feedUrl)) {
            is FeedRefreshResult.Updated -> {
                val preview = FeedPreview(feedUrl, result.feed.podcast, result.feed.episodes)
                val accepted = previewCacheMutex.withLock {
                    if (latestPreviewRequest[normalizedUrl] != requestId) return@withLock false
                    latestPreviewRequest.remove(normalizedUrl)
                    true
                }
                if (accepted) feedPreviewCache.save(normalizedUrl, preview, TTL(MetadataCacheKind.EpisodeDetail.ttlMillis))
                if (accepted) mutableState.update {
                    if (it.feedPreviewUrl == feedUrl) it.copy(feedPreview = preview, feedPreviewLoading = false) else it
                }
            }
            else -> {
                val accepted = previewCacheMutex.withLock {
                    (latestPreviewRequest[normalizedUrl] == requestId).also { if (it) latestPreviewRequest.remove(normalizedUrl) }
                }
                if (accepted) mutableState.update {
                    if (it.feedPreviewUrl == feedUrl) it.copy(feedPreviewLoading = false, feedPreviewError = refreshError(result)) else it
                }
            }
        }
    }

    fun setSubscribed(podcastId: PodcastId, subscribed: Boolean) {
        mutableState.update { value ->
            value.copy(podcasts = value.podcasts.map { if (it.id == podcastId) it.copy(isSubscribed = subscribed) else it })
        }
        database?.let { db -> scope.launch {
            if (subscribed) db.userDataDao().addSubscription(SubscriptionEntity(podcastId.value, clock()))
            else db.userDataDao().removeSubscription(podcastId.value)
        } }
    }

    fun setFavorite(episodeId: EpisodeId, favorite: Boolean) {
        mutableState.update { value ->
            value.copy(favoriteIds = if (favorite) value.favoriteIds + episodeId else value.favoriteIds - episodeId)
        }
        val belongsToStoredEpisode = state.value.episodes.any { it.id == episodeId }
        if (database != null && !belongsToStoredEpisode) {
            mutableState.update {
                it.copy(message = "This favorite will be saved after you subscribe to the podcast")
            }
            return
        }
        database?.let { db -> scope.launch {
            runCatching {
                if (favorite) db.userDataDao().addFavorite(FavoriteEntity(episodeId.value, clock()))
                else db.userDataDao().removeFavorite(episodeId.value)
            }.onFailure {
                mutableState.update { value ->
                    val rolledBack = when {
                        favorite && episodeId in value.favoriteIds -> value.favoriteIds - episodeId
                        !favorite && episodeId !in value.favoriteIds -> value.favoriteIds + episodeId
                        else -> value.favoriteIds
                    }
                    value.copy(favoriteIds = rolledBack, message = "Could not save this favorite. Please try again.")
                }
            }
        } }
    }

    fun enqueueDownload(episode: Episode) {
        if (!downloadGateway.supported) {
            mutableState.update { it.copy(message = "Downloads are not available on this platform yet") }
            return
        }
        val source = episode.enclosures.firstOrNull()?.url ?: return
        validateRemoteMedia(episode.id.value, source)?.let { error ->
            mutableState.update { it.copy(message = error) }
            return
        }
        val podcastTitle = state.value.podcasts.firstOrNull { it.id == episode.podcastId }?.title.orEmpty().ifBlank { "Podcast" }
        mutableState.update { value ->
            value.copy(downloads = value.downloads.filterNot { it.episodeId == episode.id } + Download(episode.id, source, DownloadState.Queued))
        }
        downloadGateway.download(episode, podcastTitle)
    }

    fun removeDownload(episodeId: EpisodeId) {
        downloadGateway.delete(episodeId)
        mutableState.update { it.copy(downloads = it.downloads.filterNot { item -> item.episodeId == episodeId }) }
    }

    fun setCellularDownloadsAllowed(allowed: Boolean) = downloadGateway.setCellularDownloadsAllowed(allowed)

    fun recordPlayback(episode: Episode, positionMillis: Long, durationMillis: Long) {
        // Local-playlist tracks are file references, not RSS episode rows. Keep their current-session
        // history visible, but never insert them into playback_history's episode foreign key.
        val hasPersistedEpisode = state.value.episodes.any { it.id == episode.id }
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            playbackWriteMutex.withLock {
                val now = nextPlaybackTimestamp()
                val clampedPosition = positionMillis.coerceIn(0, durationMillis.takeIf { it > 0 } ?: Long.MAX_VALUE)
                var persistedRecord: PlaybackHistory? = null
                mutableState.update { value ->
                    val previous = value.history.firstOrNull { it.episodeId == episode.id }
                    val sampleDelta = previous?.let { (clampedPosition - it.positionMillis).coerceIn(0, 10_000) } ?: 0
                    val completed = durationMillis > 0 && clampedPosition * 100 >= durationMillis * 95
                    val record = PlaybackHistory(
                        episode.id, now, clampedPosition, durationMillis.takeIf { it > 0 },
                        (previous?.totalPlayedMillis ?: 0) + sampleDelta, completed,
                    )
                    persistedRecord = record
                    value.copy(history = listOf(record) + value.history.filterNot { it.episodeId == episode.id })
                }
                val record = persistedRecord ?: return@withLock
                if (hasPersistedEpisode) {
                    runCatching {
                        database?.userDataDao()?.recordPlayback(
                            episodeId = episode.id.value,
                            lastPlayedAt = now,
                            positionMs = record.positionMillis,
                            durationMs = durationMillis.takeIf { it > 0 },
                            totalPlayedMs = record.totalPlayedMillis,
                            completed = record.completed,
                            completedAt = now.takeIf { record.completed },
                            updatedAt = now,
                        )
                    }.onFailure {
                        mutableState.update { current ->
                            current.copy(message = "Could not save playback progress")
                        }
                    }
                }
            }
        }
    }

    fun markPlayed(episode: Episode) = recordPlayback(
        episode = episode,
        positionMillis = episode.playbackPositionMillis,
        durationMillis = episode.durationMillis ?: 0,
    )

    /** Stores selected-file references; platforms report playback errors if a file is later moved or revoked. */
    fun addLocalPlaylist(playlist: LocalPlaylist) {
        val sortedPlaylist = playlist.copy(files = playlist.files.sortedByFileName())
        mutableState.update { it.copy(localPlaylists = (it.localPlaylists + sortedPlaylist).sortedByPlaylistName()) }
        database?.let { db -> scope.launch {
            runCatching {
                db.localPlaylistDao().add(
                    LocalPlaylistEntity(sortedPlaylist.id, sortedPlaylist.name, clock(), sortedPlaylist.isPinned),
                    sortedPlaylist.files.mapIndexed { position, file ->
                        LocalPlaylistItemEntity(sortedPlaylist.id, position, file.source, file.displayName, file.mimeType)
                    },
                )
            }.onFailure { error ->
                mutableState.update { current ->
                    current.copy(
                        localPlaylists = current.localPlaylists.filterNot { it.id == sortedPlaylist.id },
                        message = error.message ?: "Could not save local playlist",
                    )
                }
            }
        } }
    }

    fun renameLocalPlaylist(playlistId: String, name: String) = updateLocalPlaylist(playlistId) { it.copy(name = name) }

    fun setLocalPlaylistPinned(playlistId: String, pinned: Boolean) {
        val original = state.value.localPlaylists.firstOrNull { it.id == playlistId } ?: return
        val updated = original.copy(isPinned = pinned)
        mutableState.update { current ->
            current.copy(localPlaylists = current.localPlaylists.map { if (it.id == playlistId) updated else it }.sortedByPlaylistName())
        }
        database?.let { db -> scope.launch {
            runCatching { db.localPlaylistDao().setPinned(playlistId, pinned) }.onFailure { error ->
                mutableState.update { current ->
                    current.copy(localPlaylists = current.localPlaylists.map { if (it.id == playlistId) original else it }.sortedByPlaylistName(), message = error.message ?: "Could not update playlist pin")
                }
            }
        } }
    }

    fun addLocalPlaylistFiles(playlistId: String, files: List<LocalAudioFile>) =
        updateLocalPlaylist(playlistId) { playlist ->
            playlist.copy(files = (playlist.files + files).sortedByFileName())
        }

    fun removeLocalPlaylistFile(playlistId: String, index: Int) = updateLocalPlaylist(playlistId) { playlist ->
        playlist.copy(files = playlist.files.filterIndexed { position, _ -> position != index })
    }

    fun moveLocalPlaylistFile(playlistId: String, fromIndex: Int, toIndex: Int) = updateLocalPlaylist(playlistId) { playlist ->
        if (fromIndex !in playlist.files.indices || toIndex !in playlist.files.indices || fromIndex == toIndex) return@updateLocalPlaylist playlist
        playlist.copy(files = playlist.files.toMutableList().apply { add(toIndex, removeAt(fromIndex)) })
    }

    fun deleteLocalPlaylist(playlistId: String) {
        val original = state.value.localPlaylists.firstOrNull { it.id == playlistId } ?: return
        mutableState.update { it.copy(localPlaylists = it.localPlaylists.filterNot { playlist -> playlist.id == playlistId }) }
        database?.let { db -> scope.launch {
            runCatching { db.localPlaylistDao().delete(playlistId) }.onFailure { error ->
                mutableState.update { current ->
                    current.copy(localPlaylists = (current.localPlaylists + original).sortedByPlaylistName(), message = error.message ?: "Could not delete local playlist")
                }
            }
        } }
    }

    private fun updateLocalPlaylist(playlistId: String, transform: (LocalPlaylist) -> LocalPlaylist) {
        val original = state.value.localPlaylists.firstOrNull { it.id == playlistId } ?: return
        val updated = transform(original)
        mutableState.update { current ->
            current.copy(localPlaylists = current.localPlaylists.map { if (it.id == playlistId) updated else it }.sortedByPlaylistName())
        }
        database?.let { db -> scope.launch {
            runCatching {
                db.localPlaylistDao().replace(
                    playlistId = updated.id,
                    name = updated.name,
                    items = updated.files.mapIndexed { position, file ->
                        LocalPlaylistItemEntity(updated.id, position, file.source, file.displayName, file.mimeType)
                    },
                )
            }.onFailure { error ->
                mutableState.update { current ->
                    current.copy(
                        localPlaylists = current.localPlaylists.map { if (it.id == playlistId) original else it }.sortedByPlaylistName(),
                        message = error.message ?: "Could not update local playlist",
                    )
                }
            }
        } }
    }

    suspend fun importOpml(document: String): OpmlImportReport {
        val parsed = runCatching { OpmlCodec.parse(document) }.getOrElse {
            return OpmlImportReport(0, 0, listOf(it.message ?: "Invalid OPML document"))
        }
        var imported = 0
        var duplicates = parsed.duplicateCount
        val existing = state.value.podcasts.flatMap { listOf(it.feedUrl, it.canonicalFeedUrl) }.map(::normalizeFeedUrl).toMutableSet()
        val failures = mutableListOf<String>()
        parsed.entries.forEach { entry ->
            if (!existing.add(normalizeFeedUrl(entry.feedUrl))) {
                duplicates++
                return@forEach
            }
            when (val result = subscribeFeed(entry.feedUrl)) {
                is FeedRefreshResult.Updated -> imported++
                else -> failures += "${entry.feedUrl}: ${refreshError(result)}"
            }
        }
        return OpmlImportReport(imported, duplicates, failures)
    }

    fun exportOpml(): String = OpmlCodec.export(state.value.podcasts.filter { it.isSubscribed })

    fun clearMessage() { mutableState.update { it.copy(message = null) } }

    /** Keeps directory artwork when an RSS publisher omits cover metadata. */
    private fun cachedArtworkFor(normalizedFeedUrl: String): String? = sequenceOf(
        state.value.podcasts,
        state.value.appleSearchResults,
        state.value.appleCategoryResults,
        listOfNotNull(state.value.feedPreview?.podcast),
    ).flatMap { it.asSequence() }
        .filter { podcast ->
            normalizeFeedUrl(podcast.feedUrl) == normalizedFeedUrl ||
                normalizeFeedUrl(podcast.canonicalFeedUrl) == normalizedFeedUrl
        }
        .mapNotNull(Podcast::artworkUrl)
        .firstOrNull()

    private suspend fun persistFeed(db: MollieDatabase, result: FeedRefreshResult.Updated) {
        val podcast = result.feed.podcast
        val normalizedTitle = podcast.title.lowercase().trim().replace(Regex("\\s+"), " ")
        val normalizedAuthor = podcast.author.lowercase().trim().replace(Regex("\\s+"), " ")
        val podcastEntity = PodcastEntity(
            podcast.id.value, podcast.feedUrl, podcast.canonicalFeedUrl, podcast.websiteUrl, podcast.title,
            podcast.author, podcast.description, podcast.artworkUrl, podcast.language, podcast.copyright,
            podcast.isExplicit, normalizedTitle, normalizedAuthor, podcast.episodeCount, podcast.latestEpisodeAtMillis,
            clock(),
        )
        val aliases = listOf(
            FeedAliasEntity(normalizeFeedUrl(podcast.feedUrl), podcast.id.value),
            FeedAliasEntity(normalizeFeedUrl(podcast.canonicalFeedUrl), podcast.id.value),
        ).distinctBy { it.aliasUrl }
        val entities = result.feed.episodes.map { it.toEntity() }
        val enclosures = result.feed.episodes.flatMap { episode ->
            episode.enclosures.mapIndexed { index, item -> EpisodeEnclosureEntity(episode.id.value, index, item.url, item.mimeType, item.lengthBytes) }
        }
        val categories = podcast.categories.map { CategoryEntity(it.key, it.displayName) }.distinctBy { it.categoryKey }
        val mappings = podcast.categories.map { category ->
            PodcastCategoryEntity(podcast.id.value, category.key, category.displayName, category.key)
        }
        db.feedStoreDao().replaceFeed(
            podcastEntity,
            aliases,
            entities,
            enclosures,
            categories,
            mappings,
            SubscriptionEntity(podcast.id.value, clock()),
        )
    }

    private suspend fun persistSyncState(db: MollieDatabase, feedUrl: String, result: FeedRefreshResult) {
        val now = clock()
        val resolvedId = db.podcastDao().resolveByFeedAlias(normalizeFeedUrl(feedUrl))
        val podcastId = when (result) {
            is FeedRefreshResult.Updated -> result.feed.podcast.id.value
            else -> resolvedId ?: return
        }
        val previous = db.syncDao().state(podcastId)
        val state = when (result) {
            is FeedRefreshResult.Updated -> FeedSyncStateEntity(podcastId, result.etag, result.lastModified, now, now, 200, null, null)
            is FeedRefreshResult.NotModified -> FeedSyncStateEntity(podcastId, previous?.etag, previous?.lastModified, now, now, 304, null, null)
            is FeedRefreshResult.HttpFailure -> FeedSyncStateEntity(podcastId, previous?.etag, previous?.lastModified, now, previous?.lastSuccessAt, result.status, "http", refreshError(result))
            is FeedRefreshResult.NetworkFailure -> FeedSyncStateEntity(podcastId, previous?.etag, previous?.lastModified, now, previous?.lastSuccessAt, null, "network", result.message)
            is FeedRefreshResult.ParseFailure -> FeedSyncStateEntity(podcastId, previous?.etag, previous?.lastModified, now, previous?.lastSuccessAt, 200, "parse", result.message)
        }
        db.syncDao().upsertState(state)
    }

    private suspend fun restore(db: MollieDatabase) {
        val localPlaylistItems = db.localPlaylistDao().allItems().groupBy { it.playlistId }
        val localPlaylists = db.localPlaylistDao().allPlaylists().map { playlist ->
            LocalPlaylist(
                id = playlist.playlistId,
                name = playlist.name,
                isPinned = playlist.isPinned,
                files = localPlaylistItems[playlist.playlistId].orEmpty().map { item ->
                    LocalAudioFile(item.source, item.displayName, item.mimeType)
                },
            )
        }.sortedByPlaylistName()
        val subscriptions = db.userDataDao().allSubscriptions().map { it.podcastId }.toSet()
        val categoryNames = db.categoryDao().allCategories().associate { it.categoryKey to it.displayName }
        val categoryMappings = db.categoryDao().allMappings().groupBy { it.podcastId }
        val podcasts = db.podcastDao().allPodcasts().map { entity ->
            val categories = categoryMappings[entity.podcastId].orEmpty().map { mapping ->
                PodcastCategory(mapping.canonicalCategoryKey, categoryNames[mapping.canonicalCategoryKey] ?: mapping.sourceName)
            }
            entity.toModel(entity.podcastId in subscriptions, categories)
        }
        if (podcasts.isEmpty() && localPlaylists.isEmpty()) return
        val history = db.userDataDao().recentHistoryForRestore(RESTORE_HISTORY_LIMIT)
            .map { PlaybackHistory(EpisodeId(it.episodeId), it.lastPlayedAt, it.positionMs, it.durationSnapshotMs, it.totalPlayedMs, it.completed) }
        playbackWriteMutex.withLock {
            lastPlaybackTimestamp = maxOf(lastPlaybackTimestamp, history.maxOfOrNull(PlaybackHistory::lastPlayedAtMillis) ?: 0L)
        }
        val positions = history.associate { it.episodeId.value to if (it.completed) 0L else it.positionMillis }
        val episodeEntities = db.episodeDao().latestForRestore(RESTORE_EPISODE_LIMIT)
        val enclosures = db.episodeDao().enclosuresFor(episodeEntities.map { it.episodeId }).groupBy { it.episodeId }
        val restoredEpisodes = episodeEntities.map { entity -> entity.toModel(enclosures[entity.episodeId].orEmpty(), positions[entity.episodeId] ?: 0) }
        // The first prototype shipped a non-existent placeholder MP3. Do not expose it as playable media.
        val episodes = restoredEpisodes.filterNot { episode -> episode.enclosures.any { it.url == LEGACY_PLACEHOLDER_MEDIA_URL } }
        val favorites = db.userDataDao().allFavorites().mapTo(mutableSetOf()) { EpisodeId(it.episodeId) }
        val downloads = db.downloadDao().allDownloads().map { Download(EpisodeId(it.episodeId), it.sourceUrl, runCatching { DownloadState.valueOf(it.state) }.getOrDefault(DownloadState.Failed), it.localReference, it.receivedBytes, it.totalBytes, it.failureMessage) }
        mutableState.update { current ->
            current.copy(
                podcasts = podcasts,
                episodes = episodes,
                favoriteIds = favorites,
                downloads = downloads,
                history = history,
                localPlaylists = localPlaylists,
                downloadsSupported = downloadGateway.supported,
            )
        }
    }

    private fun nextPlaybackTimestamp(): Long {
        val now = clock()
        return maxOf(now, lastPlaybackTimestamp + 1).also { lastPlaybackTimestamp = it }
    }

    private companion object {
        const val MAX_FEED_PREVIEWS = 20
        // Episode descriptions and HTML can be large; keep startup well below Android's 192 MB heap budget.
        const val RESTORE_EPISODE_LIMIT = 250
        const val RESTORE_HISTORY_LIMIT = 500
    }
}

private fun Episode.toEntity() = EpisodeEntity(
    id.value, podcastId.value, guid, permalinkUrl, title, subtitle, summary, descriptionHtml, author,
    publishedAtMillis, durationMillis, artworkUrl, seasonNumber, episodeNumber, episodeType, isExplicit, null,
)

private fun PodcastEntity.toModel(subscribed: Boolean, categories: List<PodcastCategory>) = Podcast(
    PodcastId(podcastId), feedUrl, canonicalFeedUrl, websiteUrl, title, author, description, artworkUrl,
    language, copyright, isExplicit, categories = categories, episodeCount = episodeCount, latestEpisodeAtMillis = latestEpisodeAt,
    isSubscribed = subscribed, lastRefreshAtMillis = lastRefreshAt,
)

private fun EpisodeEntity.toModel(enclosureEntities: List<EpisodeEnclosureEntity>, playbackPositionMillis: Long) = Episode(
    EpisodeId(episodeId), PodcastId(podcastId), guid, permalinkUrl, title, subtitle, summaryText, descriptionHtml,
    author, publishedAt, durationMs, artworkUrl, seasonNumber, episodeNumber, episodeType, isExplicit,
    enclosures = enclosureEntities.map { mammoth.mollie.caster.model.Enclosure(it.url, it.mimeType, it.lengthBytes) },
    playbackPositionMillis = playbackPositionMillis,
)

private fun refreshError(result: FeedRefreshResult): String = when (result) {
    is FeedRefreshResult.HttpFailure -> "Feed returned HTTP ${result.status}"
    is FeedRefreshResult.NetworkFailure -> result.message
    is FeedRefreshResult.ParseFailure -> result.message
    is FeedRefreshResult.NotModified -> "Feed has not changed"
    is FeedRefreshResult.Updated -> "Updated"
}

private fun seedState(): LibraryState {
    return LibraryState(emptyList(), emptyList())
}
