package com.mammoth.podcast.glancewidget

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.mammoth.podcast.theme.backgroundDark
import com.mammoth.podcast.theme.backgroundLight
import com.mammoth.podcast.theme.errorContainerDark
import com.mammoth.podcast.theme.errorContainerLight
import com.mammoth.podcast.theme.errorDark
import com.mammoth.podcast.theme.errorLight
import com.mammoth.podcast.theme.inverseOnSurfaceDark
import com.mammoth.podcast.theme.inverseOnSurfaceLight
import com.mammoth.podcast.theme.inversePrimaryDark
import com.mammoth.podcast.theme.inversePrimaryLight
import com.mammoth.podcast.theme.inverseSurfaceDark
import com.mammoth.podcast.theme.inverseSurfaceLight
import com.mammoth.podcast.theme.onBackgroundDark
import com.mammoth.podcast.theme.onBackgroundLight
import com.mammoth.podcast.theme.onErrorContainerDark
import com.mammoth.podcast.theme.onErrorContainerLight
import com.mammoth.podcast.theme.onErrorDark
import com.mammoth.podcast.theme.onErrorLight
import com.mammoth.podcast.theme.onPrimaryContainerDark
import com.mammoth.podcast.theme.onPrimaryContainerLight
import com.mammoth.podcast.theme.onPrimaryDark
import com.mammoth.podcast.theme.onPrimaryLight
import com.mammoth.podcast.theme.onSecondaryContainerDark
import com.mammoth.podcast.theme.onSecondaryContainerLight
import com.mammoth.podcast.theme.onSecondaryDark
import com.mammoth.podcast.theme.onSecondaryLight
import com.mammoth.podcast.theme.onSurfaceDark
import com.mammoth.podcast.theme.onSurfaceLight
import com.mammoth.podcast.theme.onSurfaceVariantDark
import com.mammoth.podcast.theme.onSurfaceVariantLight
import com.mammoth.podcast.theme.onTertiaryContainerDark
import com.mammoth.podcast.theme.onTertiaryContainerLight
import com.mammoth.podcast.theme.onTertiaryDark
import com.mammoth.podcast.theme.onTertiaryLight
import com.mammoth.podcast.theme.outlineDark
import com.mammoth.podcast.theme.outlineLight
import com.mammoth.podcast.theme.outlineVariantDark
import com.mammoth.podcast.theme.outlineVariantLight
import com.mammoth.podcast.theme.primaryContainerDark
import com.mammoth.podcast.theme.primaryContainerLight
import com.mammoth.podcast.theme.primaryDark
import com.mammoth.podcast.theme.primaryLight
import com.mammoth.podcast.theme.scrimDark
import com.mammoth.podcast.theme.scrimLight
import com.mammoth.podcast.theme.secondaryContainerDark
import com.mammoth.podcast.theme.secondaryContainerLight
import com.mammoth.podcast.theme.secondaryDark
import com.mammoth.podcast.theme.secondaryLight
import com.mammoth.podcast.theme.surfaceBrightDark
import com.mammoth.podcast.theme.surfaceBrightLight
import com.mammoth.podcast.theme.surfaceContainerDark
import com.mammoth.podcast.theme.surfaceContainerHighDark
import com.mammoth.podcast.theme.surfaceContainerHighLight
import com.mammoth.podcast.theme.surfaceContainerHighestDark
import com.mammoth.podcast.theme.surfaceContainerHighestLight
import com.mammoth.podcast.theme.surfaceContainerLight
import com.mammoth.podcast.theme.surfaceContainerLowDark
import com.mammoth.podcast.theme.surfaceContainerLowLight
import com.mammoth.podcast.theme.surfaceContainerLowestDark
import com.mammoth.podcast.theme.surfaceContainerLowestLight
import com.mammoth.podcast.theme.surfaceDark
import com.mammoth.podcast.theme.surfaceDimDark
import com.mammoth.podcast.theme.surfaceDimLight
import com.mammoth.podcast.theme.surfaceLight
import com.mammoth.podcast.theme.surfaceVariantDark
import com.mammoth.podcast.theme.surfaceVariantLight
import com.mammoth.podcast.theme.tertiaryContainerDark
import com.mammoth.podcast.theme.tertiaryContainerLight
import com.mammoth.podcast.theme.tertiaryDark
import com.mammoth.podcast.theme.tertiaryLight

/**
 * Todo, this is copied from the core module. Refactor colors out of that so we can reference them.
 */
private val lightJetcasterColors = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

/**
 * Todo, this is copied from the core module. Refactor colors out of that so we can reference them.
 */
internal val DarkJetcasterColors = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)
