package mammoth.mollie.caster.playback

import kotlin.test.Test
import kotlin.test.assertEquals
import mammoth.mollie.caster.model.LocalAudioFile
import mammoth.mollie.caster.model.LocalPlaylist

class LocalPlaylistTest {
    @Test
    fun localFilesBecomeOrderedPlayableEpisodes() {
        val playlist = LocalPlaylist(
            id = "morning",
            name = "Morning mix",
            files = listOf(
                LocalAudioFile("file:///music/one.mp3", "one.mp3", "audio/mpeg"),
                LocalAudioFile("content://music/two", "two.m4a", "audio/mp4"),
            ),
        )

        val episodes = playlist.asEpisodes()

        assertEquals(listOf("local:morning:0", "local:morning:1"), episodes.map { it.id.value })
        assertEquals(listOf("file:///music/one.mp3", "content://music/two"), episodes.map { it.enclosures.single().url })
        assertEquals("Morning mix", episodes.first().author)
    }
}
