package mammoth.mollie.caster.playback

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import mammoth.mollie.caster.model.Enclosure
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.model.EpisodeId
import mammoth.mollie.caster.model.PodcastId

class QueuedPodcastPlayerTest {
    @Test
    fun advancesToTheNextPlaylistTrackWhenPlaybackEnds() = runTest {
        val delegate = FakePlayer()
        val queue = QueuedPodcastPlayer(delegate, backgroundScope)
        val first = episode("first")
        val second = episode("second")

        queue.playQueue(listOf(first, second))
        advanceUntilIdle()
        delegate.endCurrentTrack()
        advanceUntilIdle()

        assertEquals(second, delegate.state.value.episode)
        assertEquals(1, queue.queue.value.currentIndex)
    }

    private fun episode(id: String) = Episode(
        id = EpisodeId(id),
        podcastId = PodcastId("local-playlist:test"),
        title = id,
        enclosures = listOf(Enclosure("file:///$id.mp3", "audio/mpeg")),
    )

    private class FakePlayer : PodcastPlayer {
        private val mutableState = MutableStateFlow(PlayerState())
        override val state: StateFlow<PlayerState> = mutableState
        override val capabilities = PlayerCapabilities(realPlayback = true)

        override fun play(episode: Episode) {
            mutableState.value = PlayerState(episode = episode, status = PlayerStatus.Playing, isPlaying = true)
        }

        fun endCurrentTrack() {
            mutableState.value = mutableState.value.copy(status = PlayerStatus.Ended, isPlaying = false)
        }

        override fun toggle() = Unit
        override fun seekTo(positionMillis: Long) = Unit
        override fun skipBy(deltaMillis: Long) = Unit
        override fun setSpeed(speed: Float) = Unit
        override fun setSleepTimer(minutes: Int?) = Unit
    }
}
