package mammoth.mollie.caster.model

/** A source returned by a platform file picker. It can become unavailable if its file is moved. */
data class LocalAudioFile(
    val source: String,
    val displayName: String,
    val mimeType: String? = null,
)

data class LocalPlaylist(
    val id: String,
    val name: String,
    val files: List<LocalAudioFile>,
)

/**
 * Keeps picker results deterministic across platforms. Unicode comparison preserves Chinese filenames
 * while Latin filenames are ordered case-insensitively from A to Z.
 */
fun List<LocalAudioFile>.sortedByFileName(): List<LocalAudioFile> = sortedWith(
    compareBy<LocalAudioFile> { it.displayName.lowercase() }
        .thenBy { it.displayName }
        .thenBy { it.source },
)
