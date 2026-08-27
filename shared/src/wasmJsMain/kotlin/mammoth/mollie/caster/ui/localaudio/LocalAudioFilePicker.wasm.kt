package mammoth.mollie.caster.ui.localaudio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import mammoth.mollie.caster.model.LocalAudioFile

@Composable
actual fun rememberLocalAudioFilePicker(): LocalAudioFilePicker = remember {
    object : LocalAudioFilePicker {
        override fun pickMultiple(onFiles: (List<LocalAudioFile>) -> Unit, onFailure: (String) -> Unit) =
            chooseAudioFiles(
                onFiles = { records ->
                    val files = records.lineSequence().mapNotNull { line ->
                        line.split('\t', limit = 3).takeIf { it.size >= 2 }?.let { LocalAudioFile(it[0], it[1], it.getOrNull(2).orEmpty().ifBlank { null }) }
                    }.toList()
                    if (files.isEmpty()) onFailure("Audio selection cancelled") else onFiles(files)
                },
                onFailure = onFailure,
            )
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""(onFiles, onFailure) => {
  const input = document.createElement('input');
  input.type = 'file'; input.accept = 'audio/*'; input.multiple = true;
  input.addEventListener('change', () => {
    const files = Array.from(input.files || []);
    if (!files.length) { onFailure('Audio selection cancelled'); return; }
    onFiles(files.map(file => [URL.createObjectURL(file), file.name.replace(/[\\t\\n]/g, ' '), file.type || 'audio/*'].join('\\t')).join('\\n'));
  }, { once: true });
  input.click();
}""")
private external fun chooseAudioFiles(onFiles: (String) -> Unit, onFailure: (String) -> Unit)
