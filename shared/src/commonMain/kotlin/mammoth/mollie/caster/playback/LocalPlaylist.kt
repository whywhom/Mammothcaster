package mammoth.mollie.caster.playback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mammoth.mollie.caster.model.Enclosure
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.model.EpisodeId
import mammoth.mollie.caster.model.LocalPlaylist
import mammoth.mollie.caster.model.PodcastId

data class PlaybackQueue(
    val items: List<Episode> = emptyList(),
    val currentIndex: Int = -1,
    val shuffled: Boolean = false,
) {
    val current: Episode? get() = items.getOrNull(currentIndex)
    val hasNext: Boolean get() = currentIndex in 0 until items.lastIndex
    val hasPrevious: Boolean get() = currentIndex > 0
}

fun LocalPlaylist.asEpisodes(): List<Episode> = files.mapIndexed { index, file ->
    Episode(
        id = EpisodeId("local:$id:$index"),
        podcastId = PodcastId("local-playlist:$id"),
        title = file.displayName.substringBeforeLast('.').ifBlank { "Local audio" },
        author = name,
        enclosures = listOf(Enclosure(file.source, file.mimeType ?: "audio/*")),
    )
}

/** Adds ordered/shuffle queue behaviour to every native [PodcastPlayer] implementation. */
class QueuedPodcastPlayer(
    private val delegate: PodcastPlayer,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : PodcastPlayer {
    private val mutableQueue = MutableStateFlow(PlaybackQueue())
    private var handledEndedEpisodeId: String? = null

    override val state: StateFlow<PlayerState> = delegate.state
    override val capabilities: PlayerCapabilities = delegate.capabilities
    val queue: StateFlow<PlaybackQueue> = mutableQueue.asStateFlow()

    init {
        scope.launch {
            delegate.state.collect { playerState ->
                if (playerState.status != PlayerStatus.Ended) {
                    handledEndedEpisodeId = null
                    return@collect
                }
                val endedId = playerState.episode?.id?.value ?: return@collect
                if (handledEndedEpisodeId == endedId) return@collect
                handledEndedEpisodeId = endedId
                playNext()
            }
        }
    }

    fun playPlaylist(playlist: LocalPlaylist, shuffle: Boolean, startIndex: Int = 0) =
        playQueue(playlist.asEpisodes(), startIndex = startIndex, shuffle = shuffle)

    fun playQueue(items: List<Episode>, startIndex: Int = 0, shuffle: Boolean = false) {
        if (items.isEmpty()) return
        val ordered = if (shuffle) items.shuffled() else items
        val index = startIndex.coerceIn(0, ordered.lastIndex)
        mutableQueue.value = PlaybackQueue(ordered, index, shuffle)
        delegate.play(ordered[index])
    }

    fun playNext() {
        val value = mutableQueue.value
        if (!value.hasNext) return
        val nextIndex = value.currentIndex + 1
        mutableQueue.value = value.copy(currentIndex = nextIndex)
        delegate.play(value.items[nextIndex])
    }

    fun playPrevious() {
        val value = mutableQueue.value
        if (!value.hasPrevious) return
        val previousIndex = value.currentIndex - 1
        mutableQueue.value = value.copy(currentIndex = previousIndex)
        delegate.play(value.items[previousIndex])
    }

    override fun play(episode: Episode) {
        mutableQueue.value = PlaybackQueue(listOf(episode), 0)
        delegate.play(episode)
    }
    override fun toggle() = delegate.toggle()
    override fun seekTo(positionMillis: Long) = delegate.seekTo(positionMillis)
    override fun skipBy(deltaMillis: Long) = delegate.skipBy(deltaMillis)
    override fun setSpeed(speed: Float) = delegate.setSpeed(speed)
    override fun setSleepTimer(minutes: Int?) = delegate.setSleepTimer(minutes)
}
