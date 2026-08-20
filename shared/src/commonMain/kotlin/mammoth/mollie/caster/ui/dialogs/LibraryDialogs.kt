package mammoth.mollie.caster.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mammoth.mollie.caster.data.MollieStore
import molliecaster.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AddFeedDialog(busy: Boolean, onDismiss: () -> Unit, onSubscribe: (String) -> Unit) {
    var url by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.subscribe_by_rss)) },
        text = { OutlinedTextField(url, { url = it }, label = { Text(stringResource(Res.string.feed_url)) }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { Button(enabled = !busy && url.startsWith("http"), onClick = { onSubscribe(url.trim()) }) { Text(if (busy) stringResource(Res.string.loading) else stringResource(Res.string.subscribe)) } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) } },
    )
}

@Composable
internal fun OpmlDialog(store: MollieStore, onDismiss: () -> Unit) {
    var document by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val exportReady = stringResource(Res.string.export_ready)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.opml_import_export)) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Paste an OPML document to import Apple Podcasts, AntennaPod or Pocket Casts subscriptions.")
            OutlinedTextField(document, { document = it }, minLines = 6, maxLines = 12, modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(Res.string.opml)) })
            if (result.isNotBlank()) Text(result, style = MaterialTheme.typography.bodySmall)
        } },
        confirmButton = { Button(onClick = { scope.launch {
            val report = store.importOpml(document)
            result = "Imported ${report.imported}, duplicates ${report.duplicates}, failed ${report.failures.size}"
        } }) { Text(stringResource(Res.string.import_opml)) } },
        dismissButton = { Row { OutlinedButton(onClick = { document = store.exportOpml(); result = exportReady }) { Text(stringResource(Res.string.export)) }; Spacer(Modifier.width(8.dp)); OutlinedButton(onClick = onDismiss) { Text(stringResource(Res.string.close)) } } },
    )
}
