package mammoth.mollie.caster.playback

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mammoth.mollie.caster.data.cache.validatePlayableMedia
import mammoth.mollie.caster.downloads.IosEpisodeDownloadGateway
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.platform.currentTimeMillis
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.defaultRate
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.playImmediatelyAtRate
import platform.AVFoundation.rate
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL

/** AVPlayer/AVAudioSession adapter for iOS. Background audio entitlement remains an app-level setting. */
@OptIn(ExperimentalForeignApi::class)
class IosPodcastPlayer(private val mediaFiles: IosEpisodeDownloadGateway) : PodcastPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val player = AVPlayer()
    private val mutableState = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = mutableState.asStateFlow()
    override val capabilities = PlayerCapabilities(realPlayback = true, backgroundPlayback = true, lockScreenControls = false)

    init {
        runCatching {
            AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, error = null)
            AVAudioSession.sharedInstance().setActive(true, error = null)
        }
        scope.launch {
            while (isActive) {
                delay(500)
                val deadline = state.value.sleepTimerEndsAtMillis
                if (deadline != null && currentTimeMillis() >= deadline) {
                    player.pause()
                    mutableState.value = mutableState.value.copy(sleepTimerEndsAtMillis = null)
                }
                publish()
            }
        }
    }

    override fun play(episode: Episode) {
        val enclosure = episode.enclosures.firstOrNull()
            ?: return fail(episode, "This episode has no playable audio URL")
        validatePlayableMedia(episode.id.value, enclosure.url)?.let { return fail(episode, it) }
        // Local playlists already point to an app-owned file. Do not route those files
        // through the podcast download/cache resolver before handing them to AVPlayer.
        val isLocalFile = enclosure.url.startsWith("file:", ignoreCase = true)
        val source = if (isLocalFile) {
            enclosure.url
        } else {
            mediaFiles.playbackSource(episode)
        }.ifBlank { return fail(episode, "This episode has no playable audio URL") }
        val url = if (isLocalFile) localFileUrl(source) else NSURL(string = source)
        if (url == null) return fail(
            episode,
            if (isLocalFile) "This local audio file is unavailable. Remove it and add it again." else "This episode has an invalid audio URL",
        )
        val speed = state.value.speed
        mutableState.value = PlayerState(episode = episode, status = PlayerStatus.Loading, positionMillis = episode.playbackPositionMillis, speed = speed)
        player.replaceCurrentItemWithPlayerItem(AVPlayerItem(uRL = url))
        player.defaultRate = speed
        if (episode.playbackPositionMillis > 0) player.seekToTime(CMTimeMakeWithSeconds(episode.playbackPositionMillis / 1000.0, 1_000))
        player.playImmediatelyAtRate(speed)
        publish()
    }

    override fun toggle() { if (player.rate > 0f) player.pause() else player.playImmediatelyAtRate(state.value.speed); publish() }
    override fun seekTo(positionMillis: Long) { player.seekToTime(CMTimeMakeWithSeconds(positionMillis.coerceAtLeast(0) / 1000.0, 1_000)); publish() }
    override fun skipBy(deltaMillis: Long) = seekTo(state.value.positionMillis + deltaMillis)
    override fun setSpeed(speed: Float) {
        if (speed in PreviewPodcastPlayer.SPEEDS) {
            player.defaultRate = speed
            if (player.rate > 0f) player.rate = speed
            mutableState.value = mutableState.value.copy(speed = speed)
        }
    }
    override fun setSleepTimer(minutes: Int?) { mutableState.value = mutableState.value.copy(sleepTimerEndsAtMillis = minutes?.let { currentTimeMillis() + it * 60_000L }) }

    private fun publish() {
        val value = mutableState.value
        val item = player.currentItem
        if (item?.status == AVPlayerItemStatusFailed) {
            player.pause()
            val message = item.error?.localizedDescription ?: "iOS could not play this audio file"
            mutableState.value = PlayerState(episode = value.episode, status = PlayerStatus.Failed, errorMessage = message)
            return
        }
        val position = (CMTimeGetSeconds(player.currentTime()) * 1000).toLong().coerceAtLeast(0)
        val duration = item?.duration?.let { (CMTimeGetSeconds(it) * 1000).toLong().takeIf { value -> value > 0 } } ?: value.durationMillis
        val status = when {
            item == null -> PlayerStatus.Idle
            player.rate > 0f -> PlayerStatus.Playing
            value.status == PlayerStatus.Loading -> PlayerStatus.Ready
            else -> PlayerStatus.Paused
        }
        mutableState.value = value.copy(status = status, isPlaying = player.rate > 0f, positionMillis = position, bufferedPositionMillis = duration, durationMillis = duration)
    }

    private fun fail(episode: Episode, message: String) {
        mutableState.value = PlayerState(episode = episode, status = PlayerStatus.Failed, errorMessage = message)
    }

    private fun localFileUrl(source: String): NSURL? {
        val path = NSURL(string = source)?.path ?: return null
        if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return null
        return NSURL.fileURLWithPath(path)
    }
}
