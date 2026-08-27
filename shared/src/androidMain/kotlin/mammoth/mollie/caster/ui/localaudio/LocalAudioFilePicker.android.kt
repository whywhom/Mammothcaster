package mammoth.mollie.caster.ui.localaudio

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import mammoth.mollie.caster.model.LocalAudioFile

@Composable
actual fun rememberLocalAudioFilePicker(): LocalAudioFilePicker {
    val context = LocalContext.current
    val callbacks = remember { LocalAudioCallbacks() }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult callbacks.failure("Audio selection cancelled")
        val files = uris.mapNotNull { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                LocalAudioFile(
                    source = uri.toString(),
                    displayName = context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
                        ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
                        ?: "Local audio",
                    mimeType = context.contentResolver.getType(uri),
                )
            }.getOrNull()
        }
        if (files.isEmpty()) callbacks.failure("Could not open the selected audio files") else callbacks.success(files)
    }
    return remember(picker) {
        object : LocalAudioFilePicker {
            override fun pickMultiple(onFiles: (List<LocalAudioFile>) -> Unit, onFailure: (String) -> Unit) {
                callbacks.success = onFiles
                callbacks.failure = onFailure
                picker.launch(arrayOf("audio/*"))
            }
        }
    }
}

private class LocalAudioCallbacks {
    var success: (List<LocalAudioFile>) -> Unit = {}
    var failure: (String) -> Unit = {}
}
