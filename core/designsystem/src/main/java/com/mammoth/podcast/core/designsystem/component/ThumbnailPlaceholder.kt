package com.mammoth.podcast.core.designsystem.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import com.mammoth.podcast.core.designsystem.theme.surfaceVariantDark
import com.mammoth.podcast.core.designsystem.theme.surfaceVariantLight

@Composable
internal fun thumbnailPlaceholderDefaultBrush(
    color: Color = thumbnailPlaceHolderDefaultColor()
): Brush {
    return SolidColor(color)
}

@Composable
private fun thumbnailPlaceHolderDefaultColor(
    isInDarkMode: Boolean = isSystemInDarkTheme()
): Color {
    return if (isInDarkMode) {
        surfaceVariantDark
    } else {
        surfaceVariantLight
    }
}
