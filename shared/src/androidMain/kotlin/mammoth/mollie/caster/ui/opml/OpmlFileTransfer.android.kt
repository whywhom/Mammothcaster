package mammoth.mollie.caster.ui.opml

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberOpmlFileTransfer(): OpmlFileTransfer {
    val context = LocalContext.current
    val callbacks = remember { Callbacks() }
    val openDocument = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        when {
            uri == null -> callbacks.importFailure("Import cancelled")
            else -> runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: error("Could not open the selected file")
            }.fold(callbacks.importSuccess, { callbacks.importFailure("Could not read OPML: ${it.message.orEmpty()}") })
        }
    }
    val createDocument = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/xml")) { uri ->
        when {
            uri == null -> callbacks.exportFailure("Export cancelled")
            else -> runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(callbacks.exportDocument) }
                    ?: error("Could not create the selected file")
            }.fold(
                onSuccess = { callbacks.exportSuccess("Saved molliecaster-subscriptions.opml") },
                onFailure = { callbacks.exportFailure("Could not save OPML: ${it.message.orEmpty()}") },
            )
        }
    }
    return remember(openDocument, createDocument) {
        object : OpmlFileTransfer {
            override fun importFile(onDocument: (String) -> Unit, onFailure: (String) -> Unit) {
                callbacks.importSuccess = onDocument
                callbacks.importFailure = onFailure
                // Android file providers frequently report .opml as application/octet-stream.
                // Show all files and let the OPML parser validate the selected document.
                openDocument.launch(arrayOf("*/*"))
            }

            override fun exportFile(document: String, onComplete: (String) -> Unit, onFailure: (String) -> Unit) {
                callbacks.exportDocument = document
                callbacks.exportSuccess = onComplete
                callbacks.exportFailure = onFailure
                createDocument.launch("molliecaster-subscriptions.opml")
            }
        }
    }
}

private class Callbacks {
    var importSuccess: (String) -> Unit = {}
    var importFailure: (String) -> Unit = {}
    var exportDocument: String = ""
    var exportSuccess: (String) -> Unit = {}
    var exportFailure: (String) -> Unit = {}
}
