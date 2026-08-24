package mammoth.mollie.caster

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mammoth.mollie.caster.data.OpmlImportReport
import mammoth.mollie.caster.ui.localization.stringResource
import mammoth.mollie.caster.ui.opml.rememberOpmlFileTransfer
import mammoth.mollie.caster.ui.theme.AetherTheme
import molliecaster.shared.generated.resources.Res
import molliecaster.shared.generated.resources.data_management
import molliecaster.shared.generated.resources.export_library
import molliecaster.shared.generated.resources.export_opml
import molliecaster.shared.generated.resources.export_opml_description
import molliecaster.shared.generated.resources.import_from_file
import molliecaster.shared.generated.resources.import_subscriptions_via_opml
import molliecaster.shared.generated.resources.importing_subscriptions
import molliecaster.shared.generated.resources.library_sync
import molliecaster.shared.generated.resources.library_sync_description
import molliecaster.shared.generated.resources.settings
import molliecaster.shared.generated.resources.working

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarySyncScreen(
    onBack: () -> Unit,
    onImportOpml: suspend (String) -> OpmlImportReport,
    exportDocument: () -> String,
) {
    val files = rememberOpmlFileTransfer()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var busy by remember { mutableStateOf(false) }
    var importing by remember { mutableStateOf(false) }
    val syncRotation by rememberInfiniteTransition(label = "library-sync").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1_100, easing = LinearEasing), RepeatMode.Restart),
        label = "sync-rotation",
    )

    fun showResult(message: String) {
        scope.launch { snackbar.showSnackbar(message, duration = SnackbarDuration.Long) }
    }

    fun importFile() {
        busy = true
        importing = true
        files.importFile(
            onDocument = { document ->
                scope.launch {
                    val report = onImportOpml(document)
                    busy = false
                    importing = false
                    showResult(report.summary())
                }
            },
            onFailure = { message ->
                busy = false
                importing = false
                showResult(message)
            },
        )
    }

    fun exportFile() {
        busy = true
        importing = false
        files.exportFile(
            document = exportDocument(),
            onComplete = { message -> busy = false; showResult(message) },
            onFailure = { message -> busy = false; showResult(message) },
        )
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(AetherTheme.colors.ambientGradient)) {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.settings), style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                ),
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = if (importing) "Importing OPML" else null,
                                modifier = Modifier.size(36.dp).graphicsLayer { rotationZ = if (importing) syncRotation else 0f },
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    Text(stringResource(Res.string.library_sync), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                    Text(
                        stringResource(Res.string.library_sync_description),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(Res.string.data_management).uppercase(),
                        modifier = Modifier.padding(start = 16.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    SyncImportCard(enabled = !busy, onClick = ::importFile)
                    SyncExportCard(enabled = !busy, onClick = ::exportFile)
                    if (busy) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(if (importing) Res.string.importing_subscriptions else Res.string.working), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        }
        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
        )
    }
}

@Composable
private fun SyncImportCard(enabled: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Download, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(Res.string.import_from_file), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Text(stringResource(Res.string.import_subscriptions_via_opml), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun SyncExportCard(enabled: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(Res.string.export_opml), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
                Text(
                    stringResource(Res.string.export_opml_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = enabled,
                onClick = onClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Icon(Icons.Default.Upload, null)
                Spacer(Modifier.width(10.dp))
                Text(stringResource(Res.string.export_library), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

private fun OpmlImportReport.summary(): String = buildString {
    append("Imported $imported, duplicates $duplicates, failed ${failures.size}").append('.')
    failures.firstOrNull()?.let { append(" $it") }
}
