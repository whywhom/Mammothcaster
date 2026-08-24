package mammoth.mollie.caster

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mammoth.mollie.caster.playback.PodcastPlayer
import mammoth.mollie.caster.playback.PreviewPodcastPlayer
import mammoth.mollie.caster.ui.format.formatPlaybackSpeed
import mammoth.mollie.caster.ui.theme.AetherTheme
import molliecaster.shared.generated.resources.Res
import molliecaster.shared.generated.resources.app_name
import molliecaster.shared.generated.resources.appearance
import molliecaster.shared.generated.resources.dark_theme
import molliecaster.shared.generated.resources.download_over_mobile_data
import molliecaster.shared.generated.resources.downloads
import molliecaster.shared.generated.resources.library_and_data
import molliecaster.shared.generated.resources.manage_downloads
import molliecaster.shared.generated.resources.playback
import molliecaster.shared.generated.resources.playback_speed
import molliecaster.shared.generated.resources.refresh_subscriptions
import molliecaster.shared.generated.resources.refreshing_subscriptions
import molliecaster.shared.generated.resources.settings
import molliecaster.shared.generated.resources.sleep_timer
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    darkTheme: Boolean,
    player: PodcastPlayer,
    playerState: mammoth.mollie.caster.playback.PlayerState,
    downloadsSupported: Boolean,
    cellularDownloadControlSupported: Boolean,
    cellularDownloadsAllowed: Boolean,
    refreshing: Boolean,
    onToggleTheme: () -> Unit,
    onRefresh: () -> Unit,
    onManageDownloads: () -> Unit,
    onCellularDownloadsAllowedChange: (Boolean) -> Unit,
    onOpml: () -> Unit,
) {
    var speedExpanded by remember { mutableStateOf(false) }
    var sleepExpanded by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(Res.string.settings), style = MaterialTheme.typography.headlineLarge)
                    Text(
                        "Control appearance, listening, downloads, and your library data.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                SettingsGroup(stringResource(Res.string.appearance)) {
                    SettingsSwitchRow(
                        title = stringResource(Res.string.dark_theme),
                        summary = if (darkTheme) "Using the dark Aether theme" else "Using the light Aether theme",
                        checked = darkTheme,
                        onCheckedChange = { onToggleTheme() },
                    )
                }
            }
            item {
                SettingsGroup(stringResource(Res.string.playback)) {
                    Box {
                        SettingsActionRow(
                            title = stringResource(Res.string.playback_speed),
                            summary = "${formatPlaybackSpeed(playerState.speed)}x",
                            onClick = { speedExpanded = true },
                        )
                        DropdownMenu(expanded = speedExpanded, onDismissRequest = { speedExpanded = false }) {
                            PreviewPodcastPlayer.SPEEDS.forEach { speed ->
                                DropdownMenuItem(
                                    text = { Text("${formatPlaybackSpeed(speed)}x") },
                                    onClick = {
                                        player.setSpeed(speed)
                                        speedExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Box {
                        SettingsActionRow(
                            title = stringResource(Res.string.sleep_timer),
                            summary = playerState.sleepTimerEndsAtMillis?.let { "Active" } ?: "Off",
                            onClick = { sleepExpanded = true },
                        )
                        DropdownMenu(expanded = sleepExpanded, onDismissRequest = { sleepExpanded = false }) {
                            listOf(15, 30, 60).forEach { minutes ->
                                DropdownMenuItem(
                                    text = { Text("$minutes minutes") },
                                    onClick = {
                                        player.setSleepTimer(minutes)
                                        sleepExpanded = false
                                    },
                                )
                            }
                            if (playerState.sleepTimerEndsAtMillis != null) {
                                DropdownMenuItem(
                                    text = { Text("Turn off sleep timer") },
                                    onClick = {
                                        player.setSleepTimer(null)
                                        sleepExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingsActionRow(
                        title = "Skip interval",
                        summary = "15 seconds backward and forward",
                        enabled = false,
                        onClick = {},
                    )
                }
            }
            item {
                SettingsGroup(stringResource(Res.string.downloads)) {
                    if (cellularDownloadControlSupported) {
                        SettingsSwitchRow(
                            title = stringResource(Res.string.download_over_mobile_data),
                            summary = if (cellularDownloadsAllowed) "Downloads can use Wi-Fi or mobile data" else "Downloads use Wi-Fi only",
                            checked = cellularDownloadsAllowed,
                            onCheckedChange = onCellularDownloadsAllowedChange,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                    SettingsActionRow(
                        title = stringResource(Res.string.manage_downloads),
                        summary = if (downloadsSupported) "View downloaded episodes" else "Downloads are not available on this platform",
                        enabled = downloadsSupported,
                        onClick = onManageDownloads,
                    )
                }
            }
            item {
                SettingsGroup(stringResource(Res.string.library_and_data)) {
                    SettingsActionRow(
                        title = if (refreshing) stringResource(Res.string.refreshing_subscriptions) else stringResource(Res.string.refresh_subscriptions),
                        summary = "Check your followed podcasts for new episodes",
                        enabled = !refreshing,
                        onClick = onRefresh,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingsActionRow(
                        title = "Library sync",
                        summary = "Move subscriptions between podcast apps",
                        onClick = onOpml,
                    )
                }
            }
            item {
                Text(
                    stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
        Surface(
            color = AetherTheme.colors.glass,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        ) {
            Column { content() }
        }
    }
}

@Composable
fun SettingsActionRow(title: String, summary: String, enabled: Boolean = true, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick).padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SettingsSwitchRow(title: String, summary: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
