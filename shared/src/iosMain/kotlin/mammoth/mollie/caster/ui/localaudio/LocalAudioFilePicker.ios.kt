package mammoth.mollie.caster.ui.localaudio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import mammoth.mollie.caster.model.LocalAudioFile
import mammoth.mollie.caster.platform.currentTimeMillis
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTTypeAudio
import platform.darwin.NSObject

@Composable
actual fun rememberLocalAudioFilePicker(): LocalAudioFilePicker = remember { IosLocalAudioFilePicker() }

private class IosLocalAudioFilePicker : LocalAudioFilePicker {
    private var activeDelegate: LocalAudioPickerDelegate? = null

    override fun pickMultiple(onFiles: (List<LocalAudioFile>) -> Unit, onFailure: (String) -> Unit) {
        val host = presentingController() ?: return onFailure("Could not show the Files picker")
        val delegate = LocalAudioPickerDelegate(
            onUrls = { urls ->
                // The picker URL may belong to an external Files provider.  Copy it while
                // access is still granted so a persisted playlist never depends on that URL.
                val files = urls.mapIndexedNotNull { index, url -> importAudio(url, index) }
                if (files.isEmpty()) onFailure("Could not import the selected audio files") else onFiles(files)
                activeDelegate = null
            },
            onCancelled = { onFailure("Audio selection cancelled"); activeDelegate = null },
        )
        activeDelegate = delegate
        UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeAudio), asCopy = true).also { picker ->
            picker.allowsMultipleSelection = true
            picker.delegate = delegate
            host.presentViewController(picker, animated = true, completion = null)
        }
    }
}

private class LocalAudioPickerDelegate(
    private val onUrls: (List<NSURL>) -> Unit,
    private val onCancelled: () -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentAtURL: NSURL) = onUrls(listOf(didPickDocumentAtURL))
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) =
        onUrls(didPickDocumentsAtURLs.filterIsInstance<NSURL>())
    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) = onCancelled()
}

private fun presentingController(): UIViewController? = UIApplication.sharedApplication.keyWindow?.rootViewController

@OptIn(ExperimentalForeignApi::class)
private fun importAudio(source: NSURL, index: Int): LocalAudioFile? {
    val fileManager = NSFileManager.defaultManager
    val hasSecurityScope = source.startAccessingSecurityScopedResource()
    return try {
        val root = fileManager.URLForDirectory(NSApplicationSupportDirectory, NSUserDomainMask, null, true, null)?.path ?: return null
        val directory = "$root/Molliecaster/LocalAudio"
        if (!fileManager.createDirectoryAtPath(directory, true, null, null)) return null

        val displayName = source.lastPathComponent?.takeIf { it.isNotBlank() } ?: "Local audio"
        val safeName = displayName.replace(Regex("[^A-Za-z0-9._ -]"), "_")
        val target = NSURL.fileURLWithPath("$directory/${currentTimeMillis()}-$index-$safeName")
        if (!fileManager.copyItemAtURL(source, target, error = null)) return null
        LocalAudioFile(target.absoluteString ?: return null, displayName, "audio/*")
    } finally {
        if (hasSecurityScope) source.stopAccessingSecurityScopedResource()
    }
}
