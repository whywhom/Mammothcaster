package mammoth.mollie.caster.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LocalPlaylistSortingTest {
    @Test
    fun sortsLatinNamesCaseInsensitivelyAndRetainsChineseNames() {
        val files = listOf(
            LocalAudioFile("file:///3", "zebra.mp3"),
            LocalAudioFile("file:///2", "Apple.mp3"),
            LocalAudioFile("file:///4", "中文播客.mp3"),
            LocalAudioFile("file:///1", "alpha.mp3"),
        )

        assertEquals(
            listOf("alpha.mp3", "Apple.mp3", "zebra.mp3", "中文播客.mp3"),
            files.sortedByFileName().map(LocalAudioFile::displayName),
        )
    }

    @Test
    fun sortsPlaylistsByNameAfterPinnedPlaylists() {
        val playlists = listOf(
            LocalPlaylist("3", "Zebra", emptyList()),
            LocalPlaylist("2", "Apple", emptyList()),
            LocalPlaylist("1", "Pinned mix", emptyList(), isPinned = true),
        )

        assertEquals(listOf("Pinned mix", "Apple", "Zebra"), playlists.sortedByPlaylistName().map(LocalPlaylist::name))
    }
}
