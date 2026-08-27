package mammoth.mollie.caster.playback

/** Android-facing media description. It deliberately contains no Media3 types. */
data class AndroidPlaybackItem(
    val episodeId: String,
    val mediaUrl: String,
    val title: String,
    val podcastTitle: String,
    val artworkUrl: String? = null,
    val resumePositionMillis: Long = 0L,
    val knownDurationMillis: Long? = null,
)

enum class AndroidPlaybackStatus {
    Idle,
    Loading,
    Ready,
    Playing,
    Paused,
    Ended,
    Failed,
}

data class AndroidPlaybackState(
    val episodeId: String? = null,
    val title: String? = null,
    val podcastTitle: String? = null,
    val artworkUrl: String? = null,
    val mediaUrl: String? = null,
    val status: AndroidPlaybackStatus = AndroidPlaybackStatus.Idle,
    val positionMillis: Long = 0L,
    val bufferedPositionMillis: Long = 0L,
    val durationMillis: Long = 0L,
    val speed: Float = 1f,
    val sleepTimerRemainingMillis: Long? = null,
    val errorMessage: String? = null,
)

data class AndroidPlaybackCapabilities(
    val backgroundPlayback: Boolean = true,
    val lockScreenControls: Boolean = true,
    val notificationControls: Boolean = true,
    val downloads: Boolean = true,
    val supportedSpeeds: Set<Float> = AndroidPlaybackController.SUPPORTED_SPEEDS,
)

enum class EpisodeDownloadStatus {
    Queued,
    Downloading,
    Paused,
    Completed,
    Failed,
    Removing,
    Restarting,
}

data class EpisodeDownloadState(
    val episodeId: String,
    val mediaUrl: String,
    val status: EpisodeDownloadStatus,
    val percentDownloaded: Float,
    val bytesDownloaded: Long,
    val totalBytes: Long? = null,
    val localReference: String? = null,
    val failureReason: Int? = null,
)
