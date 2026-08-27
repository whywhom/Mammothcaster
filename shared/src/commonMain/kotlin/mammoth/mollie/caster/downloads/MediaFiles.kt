package mammoth.mollie.caster.downloads

import mammoth.mollie.caster.model.Enclosure
import mammoth.mollie.caster.model.Download
import mammoth.mollie.caster.model.DownloadState
import mammoth.mollie.caster.model.EpisodeId

private val invalidFileNameCharacters = Regex("[\\u0000-\\u001f<>:\"/\\\\|?*]")
private val repeatedWhitespace = Regex("\\s+")
private val repeatedReplacement = Regex("_+")
private val windowsReservedNames = Regex("(?i)^(con|prn|aux|nul|com[1-9]|lpt[1-9])(?:\\..*)?$")

/**
 * Produces a portable path component while preserving readable Unicode titles.
 * Invalid filesystem characters are replaced instead of silently removed.
 */
fun safeMediaPathComponent(value: String, fallback: String, maxLength: Int = 120): String {
    var result = value
        .replace(invalidFileNameCharacters, "_")
        .replace(repeatedWhitespace, " ")
        .replace(repeatedReplacement, "_")
        .trim(' ', '.')
    if (result.isBlank()) result = fallback
    if (windowsReservedNames.matches(result)) result = "_$result"
    if (result.length > maxLength) result = result.take(maxLength).trimEnd(' ', '.')
    return result.ifBlank { fallback }
}

fun mediaFileExtension(enclosure: Enclosure): String {
    val fromUrl = enclosure.url.substringBefore('#').substringBefore('?')
        .substringAfterLast('/', "")
        .substringAfterLast('.', "")
        .lowercase()
        .takeIf { it.length in 2..5 && it.all(Char::isLetterOrDigit) }
    if (fromUrl in setOf("mp3", "m4a", "aac", "ogg", "opus", "wav", "flac", "mp4")) return ".$fromUrl"
    return when (enclosure.mimeType?.substringBefore(';')?.lowercase()) {
        "audio/aac" -> ".aac"
        "audio/mp4", "audio/x-m4a" -> ".m4a"
        "audio/ogg" -> ".ogg"
        "audio/opus" -> ".opus"
        "audio/wav", "audio/x-wav" -> ".wav"
        "audio/flac" -> ".flac"
        else -> ".mp3"
    }
}

fun mediaFileName(title: String, enclosure: Enclosure): String =
    safeMediaPathComponent(title, "Episode") + mediaFileExtension(enclosure)

fun uniqueMediaFileName(title: String, episodeId: EpisodeId, enclosure: Enclosure): String =
    safeMediaPathComponent(title, "Episode", maxLength = 108) +
        "-${episodeId.value.hashCode().toUInt().toString(16)}" + mediaFileExtension(enclosure)

/** Stable, filesystem-safe key for private playback caches. */
fun mediaStorageKey(episodeId: EpisodeId, enclosure: Enclosure): String {
    val readable = safeMediaPathComponent(episodeId.value, "episode", maxLength = 72)
    return "$readable-${episodeId.value.hashCode().toUInt().toString(16)}${mediaFileExtension(enclosure)}"
}

/** Drops completed records whose canonical local asset no longer exists. */
fun retainExistingDownloads(
    downloads: List<Download>,
    referenceExists: (String) -> Boolean,
): List<Download> = downloads.filter { download ->
    download.state != DownloadState.Completed || download.localReference?.let(referenceExists) == true
}
