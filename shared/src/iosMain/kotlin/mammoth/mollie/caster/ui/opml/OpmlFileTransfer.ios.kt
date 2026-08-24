package mammoth.mollie.caster.ui.opml

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerModeExportToService
import platform.UIKit.UIDocumentPickerModeImport
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.darwin.NSObject

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
                    NSString.stringWithContentsOfURL(url, NSUTF8StringEncoding, null).toString()
                }.fold(onDocument, { onFailure("Could not read OPML: ${it.message.orEmpty()}") })
                activeDelegate = null
            },
            onCancelled = { onFailure("Import cancelled"); activeDelegate = null },
        )
        activeDelegate = delegate
        UIDocumentPickerViewController(
            // OPML is often tagged as generic data by Files providers, not public.xml.
            allowedUTIs = listOf("public.data"),
            inMode = UIDocumentPickerModeImport,
        ).also { picker ->
            picker.delegate = delegate
            host.presentViewController(picker, animated = true, completion = null)
        }
    }

    override fun exportFile(document: String, onComplete: (String) -> Unit, onFailure: (String) -> Unit) {
        val host = presentingController() ?: return onFailure("Could not show the document picker")
        val temporary = NSTemporaryDirectory() + "molliecaster-subscriptions.opml"
        if (!document.writeToFile(temporary, atomically = true, encoding = NSUTF8StringEncoding, error = null)) {
            return onFailure("Could not prepare the OPML export")
        }
        val delegate = OpmlPickerDelegate(
            onUrl = { onComplete("Saved molliecaster-subscriptions.opml"); activeDelegate = null },
            onCancelled = { onFailure("Export cancelled"); activeDelegate = null },
        )
        activeDelegate = delegate
        UIDocumentPickerViewController(
            url = NSURL.fileURLWithPath(temporary),
            inMode = UIDocumentPickerModeExportToService,
        ).also { picker ->
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
