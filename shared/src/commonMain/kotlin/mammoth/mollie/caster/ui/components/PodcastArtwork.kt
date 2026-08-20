package mammoth.mollie.caster.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import mammoth.mollie.caster.ui.theme.AetherTheme
import molliecaster.shared.generated.resources.Res
import molliecaster.shared.generated.resources.cover
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PodcastArtwork(url: String?, title: String, size: Int) {
    AsyncImage(
        model = url,
        contentDescription = stringResource(Res.string.cover, title),
        modifier = Modifier.size(size.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentScale = ContentScale.Crop,
    )
}

@Composable
internal fun EmptyHint(text: String) {
    Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
internal fun SectionTitle(text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text, style = MaterialTheme.typography.titleLarge)
        Box(Modifier.width(42.dp).height(2.dp).background(AetherTheme.colors.actionGradient, CircleShape))
    }
}
