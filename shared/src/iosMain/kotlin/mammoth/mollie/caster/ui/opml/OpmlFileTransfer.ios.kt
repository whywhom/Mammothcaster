package mammoth.mollie.caster.ui.opml

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTTypeData
import platform.darwin.NSObject
import platform.posix.SEEK_END
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.fwrite
import platform.posix.rewind

@Composable
actual fun rememberOpmlFileTransfer(): OpmlFileTransfer = remember { IosOpmlFileTransfer() }

private class IosOpmlFileTransfer : OpmlFileTransfer {
    // UIKit keeps this reference weakly; retaining it here keeps callbacks alive while the picker is visible.
    private var activeDelegate: OpmlPickerDelegate? = null

    override fun importFile(onDocument: (String) -> Unit, onFailure: (String) -> Unit) {
        val host = presentingController() ?: return onFailure("Could not show the document picker")
        val delegate = OpmlPickerDelegate(
            onUrl = { url ->
                runCatching {
                    readUtf8File(url)
                }.fold(onDocument, { onFailure("Could not read OPML: ${it.message.orEmpty()}") })
                activeDelegate = null
            },
            onCancelled = { onFailure("Import cancelled"); activeDelegate = null },
        )
        activeDelegate = delegate
        // OPML is often tagged as generic data by Files providers, not public.xml.
        UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeData), asCopy = true).also { picker ->
            picker.delegate = delegate
            host.presentViewController(picker, animated = true, completion = null)
        }
    }

    override fun exportFile(document: String, onComplete: (String) -> Unit, onFailure: (String) -> Unit) {
        val host = presentingController() ?: return onFailure("Could not show the document picker")
        val temporary = NSTemporaryDirectory() + "molliecaster-subscriptions.opml"
        val temporaryUrl = NSURL.fileURLWithPath(temporary)
        runCatching { writeUtf8File(temporaryUrl, document) }
            .onFailure { return onFailure("Could not prepare the OPML export: ${it.message.orEmpty()}") }
        val delegate = OpmlPickerDelegate(
            onUrl = { onComplete("Saved molliecaster-subscriptions.opml"); activeDelegate = null },
            onCancelled = { onFailure("Export cancelled"); activeDelegate = null },
        )
        activeDelegate = delegate
        UIDocumentPickerViewController(forExportingURLs = listOf(temporaryUrl), asCopy = true).also { picker ->
            picker.delegate = delegate
            host.presentViewController(picker, animated = true, completion = null)
        }
    }
}

private class OpmlPickerDelegate(
    private val onUrl: (NSURL) -> Unit,
    private val onCancelled: () -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentAtURL: NSURL) = onUrl(didPickDocumentAtURL)

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) = onCancelled()
}

private fun presentingController(): UIViewController? = UIApplication.sharedApplication.keyWindow?.rootViewController

@OptIn(ExperimentalForeignApi::class)
private fun readUtf8File(url: NSURL): String {
    val path = url.path ?: error("Selected document has no file path")
    val stream = fopen(path, "rb") ?: error("Could not open the selected file")
    try {
        check(fseek(stream, 0, SEEK_END) == 0) { "Could not seek the selected file" }
        val byteCount = ftell(stream)
        check(byteCount in 0..Int.MAX_VALUE.toLong()) { "Selected file is too large" }
        rewind(stream)
        val bytes = ByteArray(byteCount.toInt())
        if (bytes.isNotEmpty()) bytes.usePinned { pinned ->
            check(fread(pinned.addressOf(0), 1UL, bytes.size.toULong(), stream) == bytes.size.toULong()) { "Could not read the selected file" }
        }
        return bytes.decodeToString()
    } finally {
        fclose(stream)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun writeUtf8File(url: NSURL, contents: String) {
    val path = url.path ?: error("Temporary OPML file has no path")
    val stream = fopen(path, "wb") ?: error("Could not create the temporary OPML file")
    try {
        val bytes = contents.encodeToByteArray()
        if (bytes.isNotEmpty()) bytes.usePinned { pinned ->
            check(fwrite(pinned.addressOf(0), 1UL, bytes.size.toULong(), stream) == bytes.size.toULong()) { "Could not write the temporary OPML file" }
        }
    } finally {
        fclose(stream)
    }
}
