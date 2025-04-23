package com.mammoth.podcast.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.mammoth.podcast.core.designsystem.theme.JetcasterShapes
import com.mammoth.podcast.core.designsystem.theme.JetcasterTypography
import com.mammoth.podcast.core.designsystem.theme.backgroundDark
import com.mammoth.podcast.core.designsystem.theme.backgroundDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.backgroundDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.backgroundLight
import com.mammoth.podcast.core.designsystem.theme.backgroundLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.backgroundLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.errorContainerDark
import com.mammoth.podcast.core.designsystem.theme.errorContainerDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.errorContainerDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.errorContainerLight
import com.mammoth.podcast.core.designsystem.theme.errorContainerLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.errorContainerLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.errorDark
import com.mammoth.podcast.core.designsystem.theme.errorDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.errorDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.errorLight
import com.mammoth.podcast.core.designsystem.theme.errorLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.errorLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.inverseOnSurfaceDark
import com.mammoth.podcast.core.designsystem.theme.inverseOnSurfaceDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.inverseOnSurfaceDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.inverseOnSurfaceLight
import com.mammoth.podcast.core.designsystem.theme.inverseOnSurfaceLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.inverseOnSurfaceLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.inversePrimaryDark
import com.mammoth.podcast.core.designsystem.theme.inversePrimaryDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.inversePrimaryDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.inversePrimaryLight
import com.mammoth.podcast.core.designsystem.theme.inversePrimaryLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.inversePrimaryLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.inverseSurfaceDark
import com.mammoth.podcast.core.designsystem.theme.inverseSurfaceDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.inverseSurfaceDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.inverseSurfaceLight
import com.mammoth.podcast.core.designsystem.theme.inverseSurfaceLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.inverseSurfaceLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onBackgroundDark
import com.mammoth.podcast.core.designsystem.theme.onBackgroundDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.onBackgroundDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onBackgroundLight
import com.mammoth.podcast.core.designsystem.theme.onBackgroundLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.onBackgroundLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onErrorContainerDark
import com.mammoth.podcast.core.designsystem.theme.onErrorContainerDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.onErrorContainerDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onErrorContainerLight
import com.mammoth.podcast.core.designsystem.theme.onErrorContainerLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.onErrorContainerLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onErrorDark
import com.mammoth.podcast.core.designsystem.theme.onErrorDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.onErrorDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onErrorLight
import com.mammoth.podcast.core.designsystem.theme.onErrorLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.onErrorLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onPrimaryContainerDark
import com.mammoth.podcast.core.designsystem.theme.onPrimaryContainerDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.onPrimaryContainerDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onPrimaryContainerLight
import com.mammoth.podcast.core.designsystem.theme.onPrimaryContainerLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.onPrimaryContainerLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onPrimaryDark
import com.mammoth.podcast.core.designsystem.theme.onPrimaryDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.onPrimaryDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onPrimaryLight
import com.mammoth.podcast.core.designsystem.theme.onPrimaryLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.onPrimaryLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onSecondaryContainerDark
import com.mammoth.podcast.core.designsystem.theme.onSecondaryContainerDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.onSecondaryContainerDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onSecondaryContainerLight
import com.mammoth.podcast.core.designsystem.theme.onSecondaryContainerLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.onSecondaryContainerLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onSecondaryDark
import com.mammoth.podcast.core.designsystem.theme.onSecondaryDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.onSecondaryDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onSecondaryLight
import com.mammoth.podcast.core.designsystem.theme.onSecondaryLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.onSecondaryLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onSurfaceDark
import com.mammoth.podcast.core.designsystem.theme.onSurfaceDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.onSurfaceDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onSurfaceLight
import com.mammoth.podcast.core.designsystem.theme.onSurfaceLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.onSurfaceLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onSurfaceVariantDark
import com.mammoth.podcast.core.designsystem.theme.onSurfaceVariantDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.onSurfaceVariantDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onSurfaceVariantLight
import com.mammoth.podcast.core.designsystem.theme.onSurfaceVariantLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.onSurfaceVariantLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onTertiaryContainerDark
import com.mammoth.podcast.core.designsystem.theme.onTertiaryContainerDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.onTertiaryContainerDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onTertiaryContainerLight
import com.mammoth.podcast.core.designsystem.theme.onTertiaryContainerLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.onTertiaryContainerLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onTertiaryDark
import com.mammoth.podcast.core.designsystem.theme.onTertiaryDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.onTertiaryDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.onTertiaryLight
import com.mammoth.podcast.core.designsystem.theme.onTertiaryLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.onTertiaryLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.outlineDark
import com.mammoth.podcast.core.designsystem.theme.outlineDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.outlineDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.outlineLight
import com.mammoth.podcast.core.designsystem.theme.outlineLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.outlineLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.outlineVariantDark
import com.mammoth.podcast.core.designsystem.theme.outlineVariantDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.outlineVariantDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.outlineVariantLight
import com.mammoth.podcast.core.designsystem.theme.outlineVariantLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.outlineVariantLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.primaryContainerDark
import com.mammoth.podcast.core.designsystem.theme.primaryContainerDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.primaryContainerDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.primaryContainerLight
import com.mammoth.podcast.core.designsystem.theme.primaryContainerLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.primaryContainerLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.primaryDark
import com.mammoth.podcast.core.designsystem.theme.primaryDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.primaryDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.primaryLight
import com.mammoth.podcast.core.designsystem.theme.primaryLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.primaryLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.scrimDark
import com.mammoth.podcast.core.designsystem.theme.scrimDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.scrimDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.scrimLight
import com.mammoth.podcast.core.designsystem.theme.scrimLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.scrimLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.secondaryContainerDark
import com.mammoth.podcast.core.designsystem.theme.secondaryContainerDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.secondaryContainerDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.secondaryContainerLight
import com.mammoth.podcast.core.designsystem.theme.secondaryContainerLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.secondaryContainerLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.secondaryDark
import com.mammoth.podcast.core.designsystem.theme.secondaryDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.secondaryDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.secondaryLight
import com.mammoth.podcast.core.designsystem.theme.secondaryLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.secondaryLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceBrightDark
import com.mammoth.podcast.core.designsystem.theme.surfaceBrightDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceBrightDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceBrightLight
import com.mammoth.podcast.core.designsystem.theme.surfaceBrightLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceBrightLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerDark
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerHighDark
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerHighDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerHighDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerHighLight
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerHighLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerHighLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerHighestDark
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerHighestDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerHighestDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerHighestLight
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerHighestLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerHighestLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLight
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLowDark
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLowDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLowDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLowLight
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLowLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLowLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLowestDark
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLowestDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLowestDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLowestLight
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLowestLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceContainerLowestLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceDark
import com.mammoth.podcast.core.designsystem.theme.surfaceDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceDimDark
import com.mammoth.podcast.core.designsystem.theme.surfaceDimDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceDimDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceDimLight
import com.mammoth.podcast.core.designsystem.theme.surfaceDimLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceDimLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceLight
import com.mammoth.podcast.core.designsystem.theme.surfaceLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceVariantDark
import com.mammoth.podcast.core.designsystem.theme.surfaceVariantDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceVariantDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceVariantLight
import com.mammoth.podcast.core.designsystem.theme.surfaceVariantLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.surfaceVariantLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.tertiaryContainerDark
import com.mammoth.podcast.core.designsystem.theme.tertiaryContainerDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.tertiaryContainerDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.tertiaryContainerLight
import com.mammoth.podcast.core.designsystem.theme.tertiaryContainerLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.tertiaryContainerLightMediumContrast
import com.mammoth.podcast.core.designsystem.theme.tertiaryDark
import com.mammoth.podcast.core.designsystem.theme.tertiaryDarkHighContrast
import com.mammoth.podcast.core.designsystem.theme.tertiaryDarkMediumContrast
import com.mammoth.podcast.core.designsystem.theme.tertiaryLight
import com.mammoth.podcast.core.designsystem.theme.tertiaryLightHighContrast
import com.mammoth.podcast.core.designsystem.theme.tertiaryLightMediumContrast

private val lightScheme = lightColorScheme(
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

private val darkScheme = darkColorScheme(
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

private val mediumContrastLightColorScheme = lightColorScheme(
    primary = primaryLightMediumContrast,
    onPrimary = onPrimaryLightMediumContrast,
    primaryContainer = primaryContainerLightMediumContrast,
    onPrimaryContainer = onPrimaryContainerLightMediumContrast,
    secondary = secondaryLightMediumContrast,
    onSecondary = onSecondaryLightMediumContrast,
    secondaryContainer = secondaryContainerLightMediumContrast,
    onSecondaryContainer = onSecondaryContainerLightMediumContrast,
    tertiary = tertiaryLightMediumContrast,
    onTertiary = onTertiaryLightMediumContrast,
    tertiaryContainer = tertiaryContainerLightMediumContrast,
    onTertiaryContainer = onTertiaryContainerLightMediumContrast,
    error = errorLightMediumContrast,
    onError = onErrorLightMediumContrast,
    errorContainer = errorContainerLightMediumContrast,
    onErrorContainer = onErrorContainerLightMediumContrast,
    background = backgroundLightMediumContrast,
    onBackground = onBackgroundLightMediumContrast,
    surface = surfaceLightMediumContrast,
    onSurface = onSurfaceLightMediumContrast,
    surfaceVariant = surfaceVariantLightMediumContrast,
    onSurfaceVariant = onSurfaceVariantLightMediumContrast,
    outline = outlineLightMediumContrast,
    outlineVariant = outlineVariantLightMediumContrast,
    scrim = scrimLightMediumContrast,
    inverseSurface = inverseSurfaceLightMediumContrast,
    inverseOnSurface = inverseOnSurfaceLightMediumContrast,
    inversePrimary = inversePrimaryLightMediumContrast,
    surfaceDim = surfaceDimLightMediumContrast,
    surfaceBright = surfaceBrightLightMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestLightMediumContrast,
    surfaceContainerLow = surfaceContainerLowLightMediumContrast,
    surfaceContainer = surfaceContainerLightMediumContrast,
    surfaceContainerHigh = surfaceContainerHighLightMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestLightMediumContrast,
)

private val highContrastLightColorScheme = lightColorScheme(
    primary = primaryLightHighContrast,
    onPrimary = onPrimaryLightHighContrast,
    primaryContainer = primaryContainerLightHighContrast,
    onPrimaryContainer = onPrimaryContainerLightHighContrast,
    secondary = secondaryLightHighContrast,
    onSecondary = onSecondaryLightHighContrast,
    secondaryContainer = secondaryContainerLightHighContrast,
    onSecondaryContainer = onSecondaryContainerLightHighContrast,
    tertiary = tertiaryLightHighContrast,
    onTertiary = onTertiaryLightHighContrast,
    tertiaryContainer = tertiaryContainerLightHighContrast,
    onTertiaryContainer = onTertiaryContainerLightHighContrast,
    error = errorLightHighContrast,
    onError = onErrorLightHighContrast,
    errorContainer = errorContainerLightHighContrast,
    onErrorContainer = onErrorContainerLightHighContrast,
    background = backgroundLightHighContrast,
    onBackground = onBackgroundLightHighContrast,
    surface = surfaceLightHighContrast,
    onSurface = onSurfaceLightHighContrast,
    surfaceVariant = surfaceVariantLightHighContrast,
    onSurfaceVariant = onSurfaceVariantLightHighContrast,
    outline = outlineLightHighContrast,
    outlineVariant = outlineVariantLightHighContrast,
    scrim = scrimLightHighContrast,
    inverseSurface = inverseSurfaceLightHighContrast,
    inverseOnSurface = inverseOnSurfaceLightHighContrast,
    inversePrimary = inversePrimaryLightHighContrast,
    surfaceDim = surfaceDimLightHighContrast,
    surfaceBright = surfaceBrightLightHighContrast,
    surfaceContainerLowest = surfaceContainerLowestLightHighContrast,
    surfaceContainerLow = surfaceContainerLowLightHighContrast,
    surfaceContainer = surfaceContainerLightHighContrast,
    surfaceContainerHigh = surfaceContainerHighLightHighContrast,
    surfaceContainerHighest = surfaceContainerHighestLightHighContrast,
)

private val mediumContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkMediumContrast,
    onPrimary = onPrimaryDarkMediumContrast,
    primaryContainer = primaryContainerDarkMediumContrast,
    onPrimaryContainer = onPrimaryContainerDarkMediumContrast,
    secondary = secondaryDarkMediumContrast,
    onSecondary = onSecondaryDarkMediumContrast,
    secondaryContainer = secondaryContainerDarkMediumContrast,
    onSecondaryContainer = onSecondaryContainerDarkMediumContrast,
    tertiary = tertiaryDarkMediumContrast,
    onTertiary = onTertiaryDarkMediumContrast,
    tertiaryContainer = tertiaryContainerDarkMediumContrast,
    onTertiaryContainer = onTertiaryContainerDarkMediumContrast,
    error = errorDarkMediumContrast,
    onError = onErrorDarkMediumContrast,
    errorContainer = errorContainerDarkMediumContrast,
    onErrorContainer = onErrorContainerDarkMediumContrast,
    background = backgroundDarkMediumContrast,
    onBackground = onBackgroundDarkMediumContrast,
    surface = surfaceDarkMediumContrast,
    onSurface = onSurfaceDarkMediumContrast,
    surfaceVariant = surfaceVariantDarkMediumContrast,
    onSurfaceVariant = onSurfaceVariantDarkMediumContrast,
    outline = outlineDarkMediumContrast,
    outlineVariant = outlineVariantDarkMediumContrast,
    scrim = scrimDarkMediumContrast,
    inverseSurface = inverseSurfaceDarkMediumContrast,
    inverseOnSurface = inverseOnSurfaceDarkMediumContrast,
    inversePrimary = inversePrimaryDarkMediumContrast,
    surfaceDim = surfaceDimDarkMediumContrast,
    surfaceBright = surfaceBrightDarkMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkMediumContrast,
    surfaceContainerLow = surfaceContainerLowDarkMediumContrast,
    surfaceContainer = surfaceContainerDarkMediumContrast,
    surfaceContainerHigh = surfaceContainerHighDarkMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkMediumContrast,
)

private val highContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkHighContrast,
    onPrimary = onPrimaryDarkHighContrast,
    primaryContainer = primaryContainerDarkHighContrast,
    onPrimaryContainer = onPrimaryContainerDarkHighContrast,
    secondary = secondaryDarkHighContrast,
    onSecondary = onSecondaryDarkHighContrast,
    secondaryContainer = secondaryContainerDarkHighContrast,
    onSecondaryContainer = onSecondaryContainerDarkHighContrast,
    tertiary = tertiaryDarkHighContrast,
    onTertiary = onTertiaryDarkHighContrast,
    tertiaryContainer = tertiaryContainerDarkHighContrast,
    onTertiaryContainer = onTertiaryContainerDarkHighContrast,
    error = errorDarkHighContrast,
    onError = onErrorDarkHighContrast,
    errorContainer = errorContainerDarkHighContrast,
    onErrorContainer = onErrorContainerDarkHighContrast,
    background = backgroundDarkHighContrast,
    onBackground = onBackgroundDarkHighContrast,
    surface = surfaceDarkHighContrast,
    onSurface = onSurfaceDarkHighContrast,
    surfaceVariant = surfaceVariantDarkHighContrast,
    onSurfaceVariant = onSurfaceVariantDarkHighContrast,
    outline = outlineDarkHighContrast,
    outlineVariant = outlineVariantDarkHighContrast,
    scrim = scrimDarkHighContrast,
    inverseSurface = inverseSurfaceDarkHighContrast,
    inverseOnSurface = inverseOnSurfaceDarkHighContrast,
    inversePrimary = inversePrimaryDarkHighContrast,
    surfaceDim = surfaceDimDarkHighContrast,
    surfaceBright = surfaceBrightDarkHighContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkHighContrast,
    surfaceContainerLow = surfaceContainerLowDarkHighContrast,
    surfaceContainer = surfaceContainerDarkHighContrast,
    surfaceContainerHigh = surfaceContainerHighDarkHighContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkHighContrast,
)

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified
)

@Composable
fun MammothTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkScheme
        else -> lightScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = JetcasterShapes,
        typography = JetcasterTypography,
        content = content
    )
}
