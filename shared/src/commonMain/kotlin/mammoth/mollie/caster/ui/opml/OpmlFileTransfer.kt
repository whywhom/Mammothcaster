package mammoth.mollie.caster.ui.opml

import androidx.compose.runtime.Composable

/** Opens and saves OPML using the user-visible file UI provided by each platform. */
interface OpmlFileTransfer {
    fun importFile(onDocument: (String) -> Unit, onFailure: (String) -> Unit)

    fun exportFile(document: String, onComplete: (String) -> Unit, onFailure: (String) -> Unit)
}

@Composable
expect fun rememberOpmlFileTransfer(): OpmlFileTransfer
