package mammoth.mollie.caster.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun PodcastArtwork(url: String?, title: String, size: Int) {
    var imageFailed by remember(url) { mutableStateOf(false) }
    val artworkModifier = Modifier.size(size.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainerHighest)
    if (url.isNullOrBlank() || imageFailed) {
        Box(artworkModifier, contentAlignment = androidx.compose.ui.Alignment.Center) {
            Icon(Icons.Default.LibraryMusic, contentDescription = stringResource(Res.string.cover, title), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        AsyncImage(
            model = url,
            contentDescription = stringResource(Res.string.cover, title),
            modifier = artworkModifier,
            contentScale = ContentScale.Crop,
            onError = { imageFailed = true },
        )
    }
}

@Composable
fun EmptyHint(text: String) {
    Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
fun SectionTitle(text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text, style = MaterialTheme.typography.titleLarge)
        Box(Modifier.width(42.dp).height(2.dp).background(AetherTheme.colors.actionGradient, CircleShape))
    }
}
