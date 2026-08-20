package mammoth.mollie.caster.downloads

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import mammoth.mollie.caster.model.Enclosure
import mammoth.mollie.caster.model.Download
import mammoth.mollie.caster.model.DownloadState
import mammoth.mollie.caster.model.EpisodeId

class MediaFilesTest {
    @Test
    fun replacesReservedCharactersAndKeepsUnicode() {
        assertEquals("Mollie_ a podcast_ 猫", safeMediaPathComponent(" Mollie: a podcast? 猫. ", "Podcast"))
    }

    @Test
    fun protectsReservedAndEmptyNames() {
        assertEquals("_CON", safeMediaPathComponent("CON", "Podcast"))
        assertEquals("Episode", safeMediaPathComponent("...", "Episode"))
    }

    @Test
    fun derivesSafeAudioExtension() {
        assertEquals("An episode.m4a", mediaFileName("An episode", Enclosure("https://example.test/audio?id=1", "audio/mp4")))
        assertTrue(mediaFileName("Episode", Enclosure("https://example.test/show.MP3?token=x")).endsWith(".mp3"))
    }

    @Test
    fun sameTitleEpisodesReceiveStableDistinctNames() {
        val enclosure = Enclosure("https://example.test/show.mp3")
        val first = uniqueMediaFileName("Weekly update", EpisodeId("one"), enclosure)
        val second = uniqueMediaFileName("Weekly update", EpisodeId("two"), enclosure)
        assertTrue(first.startsWith("Weekly update-") && first.endsWith(".mp3"))
        assertTrue(first != second)
    }

    @Test
    fun missingCompletedFilesAreMarkedUnloadedByRemovingTheirRecords() {
        val present = Download(EpisodeId("present"), "https://example.test/present.mp3", DownloadState.Completed, "file:///present.mp3")
        val missing = Download(EpisodeId("missing"), "https://example.test/missing.mp3", DownloadState.Completed, "file:///missing.mp3")

        assertEquals(listOf(present), retainExistingDownloads(listOf(present, missing)) { it == present.localReference })
    }
}
