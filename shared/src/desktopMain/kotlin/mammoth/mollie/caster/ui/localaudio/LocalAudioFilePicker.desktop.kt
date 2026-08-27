package mammoth.mollie.caster.ui.localaudio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import mammoth.mollie.caster.model.LocalAudioFile
import java.io.File
import javax.swing.JFileChooser

@Composable
actual fun rememberLocalAudioFilePicker(): LocalAudioFilePicker = remember {
    object : LocalAudioFilePicker {
        override fun pickMultiple(onFiles: (List<LocalAudioFile>) -> Unit, onFailure: (String) -> Unit) {
            val chooser = JFileChooser().apply {
                dialogTitle = "Add local audio"
                isMultiSelectionEnabled = true
                fileSelectionMode = JFileChooser.FILES_ONLY
                fileFilter = javax.swing.filechooser.FileNameExtensionFilter("Audio files", "mp3", "m4a", "aac", "wav", "ogg", "flac")
            }
            if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return onFailure("Audio selection cancelled")
            val files = chooser.selectedFiles.map(File::toLocalAudioFile)
            if (files.isEmpty()) onFailure("No audio files were selected") else onFiles(files)
        }
    }
}

private fun File.toLocalAudioFile() = LocalAudioFile(toURI().toString(), name, when (extension.lowercase()) {
    "mp3" -> "audio/mpeg"; "m4a" -> "audio/mp4"; "ogg" -> "audio/ogg"; "wav" -> "audio/wav"; else -> "audio/*"
})
