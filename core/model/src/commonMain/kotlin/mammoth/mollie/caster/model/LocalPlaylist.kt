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
