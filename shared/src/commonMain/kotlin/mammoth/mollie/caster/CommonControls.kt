package mammoth.mollie.caster

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import molliecaster.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ThemeToggle(darkTheme: Boolean, onToggle: () -> Unit) {
    IconButton(modifier = Modifier.size(40.dp), onClick = onToggle) {
        Icon(
            if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
            if (darkTheme) stringResource(Res.string.use_light_theme) else stringResource(Res.string.use_dark_theme),
        )
    }
}

@Composable
fun AetherFilterChip(selected: Boolean, onClick: () -> Unit, label: String) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.78f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = Color.Transparent,
        ),
    )
}

@Composable
fun LibraryDetailHeader(title: String, backDescription: String, onBack: () -> Unit) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, backDescription)
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineLarge,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
}
