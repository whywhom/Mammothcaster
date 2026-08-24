package mammoth.mollie.caster.ui.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import molliecaster.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddFeedDialog(busy: Boolean, onDismiss: () -> Unit, onSubscribe: (String) -> Unit) {
    var url by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.subscribe_by_rss)) },
        text = { OutlinedTextField(url, { url = it }, label = { Text(stringResource(Res.string.feed_url)) }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { Button(enabled = !busy && url.startsWith("http"), onClick = { onSubscribe(url.trim()) }) { Text(if (busy) stringResource(Res.string.loading) else stringResource(Res.string.subscribe)) } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) } },
    )
}
