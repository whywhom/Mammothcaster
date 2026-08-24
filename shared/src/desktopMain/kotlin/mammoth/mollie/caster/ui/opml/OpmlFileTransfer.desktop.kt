package mammoth.mollie.caster.ui.opml

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
actual fun rememberOpmlFileTransfer(): OpmlFileTransfer = remember {
    object : OpmlFileTransfer {
        override fun importFile(onDocument: (String) -> Unit, onFailure: (String) -> Unit) {
            val chooser = FileDialog(null as Frame?, "Import OPML", FileDialog.LOAD).apply {
                file = "*.opml"
                isVisible = true
            }
            val selected = chooser.file?.let { File(chooser.directory, it) }
            if (selected == null) {
                onFailure("Import cancelled")
            } else {
                runCatching { selected.readText() }
                    .fold(onDocument, { onFailure("Could not read OPML: ${it.message.orEmpty()}") })
            }
        }

        override fun exportFile(document: String, onComplete: (String) -> Unit, onFailure: (String) -> Unit) {
            val chooser = FileDialog(null as Frame?, "Export OPML", FileDialog.SAVE).apply {
                file = "molliecaster-subscriptions.opml"
                isVisible = true
            }
            val selected = chooser.file?.let { name ->
                File(chooser.directory, if (name.endsWith(".opml", ignoreCase = true)) name else "$name.opml")
            }
            if (selected == null) {
                onFailure("Export cancelled")
            } else {
                runCatching { selected.writeText(document) }
                    .fold({ onComplete("Saved ${selected.name}") }, { onFailure("Could not save OPML: ${it.message.orEmpty()}") })
            }
        }
    }
}
