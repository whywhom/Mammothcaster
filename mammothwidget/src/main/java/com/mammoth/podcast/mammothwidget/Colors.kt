package com.mammoth.podcast.mammothwidget

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.mammoth.podcast.core.designsystem.theme.backgroundDark
import com.mammoth.podcast.core.designsystem.theme.backgroundLight
import com.mammoth.podcast.core.designsystem.theme.errorContainerDark
import com.mammoth.podcast.core.designsystem.theme.errorContainerLight
import com.mammoth.podcast.core.designsystem.theme.errorDark
import com.mammoth.podcast.core.designsystem.theme.errorLight
import com.mammoth.podcast.core.designsystem.theme.inverseOnSurfaceDark
import com.mammoth.podcast.core.designsystem.theme.inverseOnSurfaceLight
import com.mammoth.podcast.core.designsystem.theme.inversePrimaryDark
import com.mammoth.podcast.core.designsystem.theme.inversePrimaryLight
import com.mammoth.podcast.core.designsystem.theme.inverseSurfaceDark
import com.mammoth.podcast.core.designsystem.theme.inverseSurfaceLight
import com.mammoth.podcast.core.designsystem.theme.onBackgroundDark
import com.mammoth.podcast.core.designsystem.theme.onBackgroundLight
import com.mammoth.podcast.core.designsystem.theme.onErrorContainerDark
import com.mammoth.podcast.core.designsystem.theme.onErrorContainerLight
import com.mammoth.podcast.core.designsystem.theme.onErrorDark
import com.mammoth.podcast.core.designsystem.theme.onErrorLight
import com.mammoth.podcast.core.designsystem.theme.onPrimaryContainerDark
import com.mammoth.podcast.core.designsystem.theme.onPrimaryContainerLight
import com.mammoth.podcast.core.designsystem.theme.onPrimaryDark
import com.mammoth.podcast.core.designsystem.theme.onPrimaryLight
import com.mammoth.podcast.core.designsystem.theme.onSecondaryContainerDark
import com.mammoth.podcast.core.designsystem.theme.onSecondaryContainerLight
import com.mammoth.podcast.core.designsystem.theme.onSecondaryDark
import com.mammoth.podcast.core.designsystem.theme.onSecondaryLight
import com.mammoth.podcast.core.designsystem.theme.onSurfaceDark
import com.mammoth.podcast.core.designsystem.theme.onSurfaceLight
import com.mammoth.podcast.core.designsystem.theme.onSurfaceVariantDark
import com.mammoth.podcast.core.designsystem.theme.onSurfaceVariantLight
import com.mammoth.podcast.core.designsystem.theme.onTertiaryContainerDark
import com.mammoth.podcast.core.designsystem.theme.onTertiaryContainerLight
import com.mammoth.podcast.core.designsystem.theme.onTertiaryDark
import com.mammoth.podcast.core.designsystem.theme.onTertiaryLight
import com.mammoth.podcast.core.designsystem.theme.outlineDark
import com.mammoth.podcast.core.designsystem.theme.outlineLight
import com.mammoth.podcast.core.designsystem.theme.outlineVariantDark
import com.mammoth.podcast.core.designsystem.theme.outlineVariantLight
import com.mammoth.podcast.core.designsystem.theme.primaryContainerDark
import com.mammoth.podcast.core.designsystem.theme.primaryContainerLight
import com.mammoth.podcast.core.designsystem.theme.primaryDark
import com.mammoth.podcast.core.designsystem.theme.primaryLight
import com.mammoth.podcast.core.designsystem.theme.scrimDark
import com.mammoth.podcast.core.designsystem.theme.scrimLight
import com.mammoth.podcast.core.designsystem.theme.secondaryContainerDark
import com.mammoth.podcast.core.designsystem.theme.secondaryContainerLight
import com.mammoth.podcast.core.designsystem.theme.secondaryDark
import com.mammoth.podcast.core.designsystem.theme.secondaryLight
import com.mammoth.podcast.core.designsystem.theme.surfaceBrightDark
import com.mammoth.podcast.core.designsystem.theme.surfaceBrightLight
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerDark
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerHighDark
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerHighLight
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerHighestDark
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerHighestLight
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLight
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLowDark
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLowLight
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLowestDark
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLowestLight
import com.mammoth.podcast.core.designsystem.theme.surfaceDark
import com.mammoth.podcast.core.designsystem.theme.surfaceDimDark
import com.mammoth.podcast.core.designsystem.theme.surfaceDimLight
import com.mammoth.podcast.core.designsystem.theme.surfaceLight
import com.mammoth.podcast.core.designsystem.theme.surfaceVariantDark
import com.mammoth.podcast.core.designsystem.theme.surfaceVariantLight
import com.mammoth.podcast.core.designsystem.theme.tertiaryContainerDark
import com.mammoth.podcast.core.designsystem.theme.tertiaryContainerLight
import com.mammoth.podcast.core.designsystem.theme.tertiaryDark
import com.mammoth.podcast.core.designsystem.theme.tertiaryLight

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
