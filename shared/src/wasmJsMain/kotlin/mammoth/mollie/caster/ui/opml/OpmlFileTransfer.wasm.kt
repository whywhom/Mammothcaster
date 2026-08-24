package mammoth.mollie.caster.ui.opml

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberOpmlFileTransfer(): OpmlFileTransfer = remember {
    object : OpmlFileTransfer {
        override fun importFile(onDocument: (String) -> Unit, onFailure: (String) -> Unit) =
            chooseOpmlFile(onDocument, onFailure)

        override fun exportFile(document: String, onComplete: (String) -> Unit, onFailure: (String) -> Unit) =
            saveOpmlFile(document, onComplete, onFailure)
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""(onDocument, onFailure) => {
  const input = document.createElement('input');
  input.type = 'file';
  // Hosts do not agree on an OPML MIME type. Keep the browser picker unrestricted.
  input.accept = '';
  input.addEventListener('change', () => {
    const file = input.files && input.files[0];
    if (!file) { onFailure('Import cancelled'); return; }
    file.text().then(onDocument).catch(error => onFailure(`Could not read OPML: ${'$'}{error.message || error}`));
  }, { once: true });
  input.click();
}""")
private external fun chooseOpmlFile(onDocument: (String) -> Unit, onFailure: (String) -> Unit)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""(contents, onComplete, onFailure) => {
  try {
    const blob = new Blob([contents], { type: 'application/xml;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = 'molliecaster-subscriptions.opml';
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
    onComplete('Downloaded molliecaster-subscriptions.opml');
  } catch (error) {
    onFailure(`Could not export OPML: ${'$'}{error.message || error}`);
  }
}""")
private external fun saveOpmlFile(contents: String, onComplete: (String) -> Unit, onFailure: (String) -> Unit)
