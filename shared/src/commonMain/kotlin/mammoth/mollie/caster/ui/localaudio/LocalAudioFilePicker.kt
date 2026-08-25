package mammoth.mollie.caster.ui.localaudio

import androidx.compose.runtime.Composable
import mammoth.mollie.caster.model.LocalAudioFile

interface LocalAudioFilePicker {
    fun pickMultiple(onFiles: (List<LocalAudioFile>) -> Unit, onFailure: (String) -> Unit)
}

@Composable
expect fun rememberLocalAudioFilePicker(): LocalAudioFilePicker
