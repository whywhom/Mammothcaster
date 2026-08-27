package mammoth.mollie.caster.data.cache

enum class MetadataCacheKind(val ttlMillis: Long) {
    PodcastList(60 * 60 * 1_000L),
    EpisodeList(6 * 60 * 60 * 1_000L),
    EpisodeDetail(24 * 60 * 60 * 1_000L),
}

fun MetadataCacheKind.isFresh(validatedAtMillis: Long?, nowMillis: Long): Boolean =
    validatedAtMillis != null && nowMillis >= validatedAtMillis && nowMillis - validatedAtMillis < ttlMillis

data class MediaCachePolicy(
    val maxBytes: Long = DEFAULT_MAX_BYTES,
    val maxConcurrentDownloads: Int = DEFAULT_MAX_CONCURRENT_DOWNLOADS,
) {
    init {
        require(maxBytes > 0) { "Cache size must be positive" }
        require(maxConcurrentDownloads > 0) { "Concurrent download count must be positive" }
    }

    companion object {
        const val DEFAULT_MAX_BYTES = 5L * 1_024L * 1_024L * 1_024L
        const val DEFAULT_MAX_CONCURRENT_DOWNLOADS = 3
    }
}

enum class TransferFailure { Timeout, ConnectionLost, ServerError, Unauthorized, Forbidden, NotFound, Other }

data class DownloadRetryPolicy(
    val delaysMillis: List<Long> = listOf(30_000L, 2 * 60_000L, 10 * 60_000L),
) {
    fun delayBeforeRetry(failure: TransferFailure, failuresSoFar: Int): Long? {
        if (failure !in retryableFailures || failuresSoFar !in delaysMillis.indices) return null
        return delaysMillis[failuresSoFar]
    }

    companion object {
        val retryableFailures = setOf(TransferFailure.Timeout, TransferFailure.ConnectionLost, TransferFailure.ServerError)

        fun classifyHttpStatus(status: Int): TransferFailure = when (status) {
            401 -> TransferFailure.Unauthorized
            403 -> TransferFailure.Forbidden
            404 -> TransferFailure.NotFound
            in 500..599 -> TransferFailure.ServerError
            else -> TransferFailure.Other
        }
    }
}

fun validateRemoteMedia(episodeId: String, url: String): String? = when {
    episodeId.isBlank() -> "Episode ID is missing"
    url.isBlank() -> "Audio URL is missing"
    !url.startsWith("https://", ignoreCase = true) && !url.startsWith("http://", ignoreCase = true) ->
        "Audio URL must use HTTP or HTTPS"
    else -> null
}

/** Validates both RSS media and user-selected local media without widening download input rules. */
fun validatePlayableMedia(episodeId: String, url: String): String? = when {
    episodeId.isBlank() -> "Episode ID is missing"
    url.isBlank() -> "Audio source is missing"
    url.startsWith("https://", ignoreCase = true) || url.startsWith("http://", ignoreCase = true) -> null
    url.startsWith("content:", ignoreCase = true) ||
        url.startsWith("file:", ignoreCase = true) ||
        url.startsWith("blob:", ignoreCase = true) -> null
    else -> "Audio must be selected from this device or use HTTP or HTTPS"
}
