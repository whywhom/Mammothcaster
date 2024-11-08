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
import com.mammoth.podcast.theme.JetcasterTypography
import com.mammoth.podcast.theme.backgroundDark
import com.mammoth.podcast.theme.backgroundDarkHighContrast
import com.mammoth.podcast.theme.backgroundDarkMediumContrast
import com.mammoth.podcast.theme.backgroundLight
import com.mammoth.podcast.theme.backgroundLightHighContrast
import com.mammoth.podcast.theme.backgroundLightMediumContrast
import com.mammoth.podcast.theme.errorContainerDark
import com.mammoth.podcast.theme.errorContainerDarkHighContrast
import com.mammoth.podcast.theme.errorContainerDarkMediumContrast
import com.mammoth.podcast.theme.errorContainerLight
import com.mammoth.podcast.theme.errorContainerLightHighContrast
import com.mammoth.podcast.theme.errorContainerLightMediumContrast
import com.mammoth.podcast.theme.errorDark
import com.mammoth.podcast.theme.errorDarkHighContrast
import com.mammoth.podcast.theme.errorDarkMediumContrast
import com.mammoth.podcast.theme.errorLight
import com.mammoth.podcast.theme.errorLightHighContrast
import com.mammoth.podcast.theme.errorLightMediumContrast
import com.mammoth.podcast.theme.inverseOnSurfaceDark
import com.mammoth.podcast.theme.inverseOnSurfaceDarkHighContrast
import com.mammoth.podcast.theme.inverseOnSurfaceDarkMediumContrast
import com.mammoth.podcast.theme.inverseOnSurfaceLight
import com.mammoth.podcast.theme.inverseOnSurfaceLightHighContrast
import com.mammoth.podcast.theme.inverseOnSurfaceLightMediumContrast
import com.mammoth.podcast.theme.inversePrimaryDark
import com.mammoth.podcast.theme.inversePrimaryDarkHighContrast
import com.mammoth.podcast.theme.inversePrimaryDarkMediumContrast
import com.mammoth.podcast.theme.inversePrimaryLight
import com.mammoth.podcast.theme.inversePrimaryLightHighContrast
import com.mammoth.podcast.theme.inversePrimaryLightMediumContrast
import com.mammoth.podcast.theme.inverseSurfaceDark
import com.mammoth.podcast.theme.inverseSurfaceDarkHighContrast
import com.mammoth.podcast.theme.inverseSurfaceDarkMediumContrast
import com.mammoth.podcast.theme.inverseSurfaceLight
import com.mammoth.podcast.theme.inverseSurfaceLightHighContrast
import com.mammoth.podcast.theme.inverseSurfaceLightMediumContrast
import com.mammoth.podcast.theme.onBackgroundDark
import com.mammoth.podcast.theme.onBackgroundDarkHighContrast
import com.mammoth.podcast.theme.onBackgroundDarkMediumContrast
import com.mammoth.podcast.theme.onBackgroundLight
import com.mammoth.podcast.theme.onBackgroundLightHighContrast
import com.mammoth.podcast.theme.onBackgroundLightMediumContrast
import com.mammoth.podcast.theme.onErrorContainerDark
import com.mammoth.podcast.theme.onErrorContainerDarkHighContrast
import com.mammoth.podcast.theme.onErrorContainerDarkMediumContrast
import com.mammoth.podcast.theme.onErrorContainerLight
import com.mammoth.podcast.theme.onErrorContainerLightHighContrast
import com.mammoth.podcast.theme.onErrorContainerLightMediumContrast
import com.mammoth.podcast.theme.onErrorDark
import com.mammoth.podcast.theme.onErrorDarkHighContrast
import com.mammoth.podcast.theme.onErrorDarkMediumContrast
import com.mammoth.podcast.theme.onErrorLight
import com.mammoth.podcast.theme.onErrorLightHighContrast
import com.mammoth.podcast.theme.onErrorLightMediumContrast
import com.mammoth.podcast.theme.onPrimaryContainerDark
import com.mammoth.podcast.theme.onPrimaryContainerDarkHighContrast
import com.mammoth.podcast.theme.onPrimaryContainerDarkMediumContrast
import com.mammoth.podcast.theme.onPrimaryContainerLight
import com.mammoth.podcast.theme.onPrimaryContainerLightHighContrast
import com.mammoth.podcast.theme.onPrimaryContainerLightMediumContrast
import com.mammoth.podcast.theme.onPrimaryDark
import com.mammoth.podcast.theme.onPrimaryDarkHighContrast
import com.mammoth.podcast.theme.onPrimaryDarkMediumContrast
import com.mammoth.podcast.theme.onPrimaryLight
import com.mammoth.podcast.theme.onPrimaryLightHighContrast
import com.mammoth.podcast.theme.onPrimaryLightMediumContrast
import com.mammoth.podcast.theme.onSecondaryContainerDark
import com.mammoth.podcast.theme.onSecondaryContainerDarkHighContrast
import com.mammoth.podcast.theme.onSecondaryContainerDarkMediumContrast
import com.mammoth.podcast.theme.onSecondaryContainerLight
import com.mammoth.podcast.theme.onSecondaryContainerLightHighContrast
import com.mammoth.podcast.theme.onSecondaryContainerLightMediumContrast
import com.mammoth.podcast.theme.onSecondaryDark
import com.mammoth.podcast.theme.onSecondaryDarkHighContrast
import com.mammoth.podcast.theme.onSecondaryDarkMediumContrast
import com.mammoth.podcast.theme.onSecondaryLight
import com.mammoth.podcast.theme.onSecondaryLightHighContrast
import com.mammoth.podcast.theme.onSecondaryLightMediumContrast
import com.mammoth.podcast.theme.onSurfaceDark
import com.mammoth.podcast.theme.onSurfaceDarkHighContrast
import com.mammoth.podcast.theme.onSurfaceDarkMediumContrast
import com.mammoth.podcast.theme.onSurfaceLight
import com.mammoth.podcast.theme.onSurfaceLightHighContrast
import com.mammoth.podcast.theme.onSurfaceLightMediumContrast
import com.mammoth.podcast.theme.onSurfaceVariantDark
import com.mammoth.podcast.theme.onSurfaceVariantDarkHighContrast
import com.mammoth.podcast.theme.onSurfaceVariantDarkMediumContrast
import com.mammoth.podcast.theme.onSurfaceVariantLight
import com.mammoth.podcast.theme.onSurfaceVariantLightHighContrast
import com.mammoth.podcast.theme.onSurfaceVariantLightMediumContrast
import com.mammoth.podcast.theme.onTertiaryContainerDark
import com.mammoth.podcast.theme.onTertiaryContainerDarkHighContrast
import com.mammoth.podcast.theme.onTertiaryContainerDarkMediumContrast
import com.mammoth.podcast.theme.onTertiaryContainerLight
import com.mammoth.podcast.theme.onTertiaryContainerLightHighContrast
import com.mammoth.podcast.theme.onTertiaryContainerLightMediumContrast
import com.mammoth.podcast.theme.onTertiaryDark
import com.mammoth.podcast.theme.onTertiaryDarkHighContrast
import com.mammoth.podcast.theme.onTertiaryDarkMediumContrast
import com.mammoth.podcast.theme.onTertiaryLight
import com.mammoth.podcast.theme.onTertiaryLightHighContrast
import com.mammoth.podcast.theme.onTertiaryLightMediumContrast
import com.mammoth.podcast.theme.outlineDark
import com.mammoth.podcast.theme.outlineDarkHighContrast
import com.mammoth.podcast.theme.outlineDarkMediumContrast
import com.mammoth.podcast.theme.outlineLight
import com.mammoth.podcast.theme.outlineLightHighContrast
import com.mammoth.podcast.theme.outlineLightMediumContrast
import com.mammoth.podcast.theme.outlineVariantDark
import com.mammoth.podcast.theme.outlineVariantDarkHighContrast
import com.mammoth.podcast.theme.outlineVariantDarkMediumContrast
import com.mammoth.podcast.theme.outlineVariantLight
import com.mammoth.podcast.theme.outlineVariantLightHighContrast
import com.mammoth.podcast.theme.outlineVariantLightMediumContrast
import com.mammoth.podcast.theme.primaryContainerDark
import com.mammoth.podcast.theme.primaryContainerDarkHighContrast
import com.mammoth.podcast.theme.primaryContainerDarkMediumContrast
import com.mammoth.podcast.theme.primaryContainerLight
import com.mammoth.podcast.theme.primaryContainerLightHighContrast
import com.mammoth.podcast.theme.primaryContainerLightMediumContrast
import com.mammoth.podcast.theme.primaryDark
import com.mammoth.podcast.theme.primaryDarkHighContrast
import com.mammoth.podcast.theme.primaryDarkMediumContrast
import com.mammoth.podcast.theme.primaryLight
import com.mammoth.podcast.theme.primaryLightHighContrast
import com.mammoth.podcast.theme.primaryLightMediumContrast
import com.mammoth.podcast.theme.scrimDark
import com.mammoth.podcast.theme.scrimDarkHighContrast
import com.mammoth.podcast.theme.scrimDarkMediumContrast
import com.mammoth.podcast.theme.scrimLight
import com.mammoth.podcast.theme.scrimLightHighContrast
import com.mammoth.podcast.theme.scrimLightMediumContrast
import com.mammoth.podcast.theme.secondaryContainerDark
import com.mammoth.podcast.theme.secondaryContainerDarkHighContrast
import com.mammoth.podcast.theme.secondaryContainerDarkMediumContrast
import com.mammoth.podcast.theme.secondaryContainerLight
import com.mammoth.podcast.theme.secondaryContainerLightHighContrast
import com.mammoth.podcast.theme.secondaryContainerLightMediumContrast
import com.mammoth.podcast.theme.secondaryDark
import com.mammoth.podcast.theme.secondaryDarkHighContrast
import com.mammoth.podcast.theme.secondaryDarkMediumContrast
import com.mammoth.podcast.theme.secondaryLight
import com.mammoth.podcast.theme.secondaryLightHighContrast
import com.mammoth.podcast.theme.secondaryLightMediumContrast
import com.mammoth.podcast.theme.surfaceBrightDark
import com.mammoth.podcast.theme.surfaceBrightDarkHighContrast
import com.mammoth.podcast.theme.surfaceBrightDarkMediumContrast
import com.mammoth.podcast.theme.surfaceBrightLight
import com.mammoth.podcast.theme.surfaceBrightLightHighContrast
import com.mammoth.podcast.theme.surfaceBrightLightMediumContrast
import com.mammoth.podcast.theme.surfaceContainerDark
import com.mammoth.podcast.theme.surfaceContainerDarkHighContrast
import com.mammoth.podcast.theme.surfaceContainerDarkMediumContrast
import com.mammoth.podcast.theme.surfaceContainerHighDark
import com.mammoth.podcast.theme.surfaceContainerHighDarkHighContrast
import com.mammoth.podcast.theme.surfaceContainerHighDarkMediumContrast
import com.mammoth.podcast.theme.surfaceContainerHighLight
import com.mammoth.podcast.theme.surfaceContainerHighLightHighContrast
import com.mammoth.podcast.theme.surfaceContainerHighLightMediumContrast
import com.mammoth.podcast.theme.surfaceContainerHighestDark
import com.mammoth.podcast.theme.surfaceContainerHighestDarkHighContrast
import com.mammoth.podcast.theme.surfaceContainerHighestDarkMediumContrast
import com.mammoth.podcast.theme.surfaceContainerHighestLight
import com.mammoth.podcast.theme.surfaceContainerHighestLightHighContrast
import com.mammoth.podcast.theme.surfaceContainerHighestLightMediumContrast
import com.mammoth.podcast.theme.surfaceContainerLight
import com.mammoth.podcast.theme.surfaceContainerLightHighContrast
import com.mammoth.podcast.theme.surfaceContainerLightMediumContrast
import com.mammoth.podcast.theme.surfaceContainerLowDark
import com.mammoth.podcast.theme.surfaceContainerLowDarkHighContrast
import com.mammoth.podcast.theme.surfaceContainerLowDarkMediumContrast
import com.mammoth.podcast.theme.surfaceContainerLowLight
import com.mammoth.podcast.theme.surfaceContainerLowLightHighContrast
import com.mammoth.podcast.theme.surfaceContainerLowLightMediumContrast
import com.mammoth.podcast.theme.surfaceContainerLowestDark
import com.mammoth.podcast.theme.surfaceContainerLowestDarkHighContrast
import com.mammoth.podcast.theme.surfaceContainerLowestDarkMediumContrast
import com.mammoth.podcast.theme.surfaceContainerLowestLight
import com.mammoth.podcast.theme.surfaceContainerLowestLightHighContrast
import com.mammoth.podcast.theme.surfaceContainerLowestLightMediumContrast
import com.mammoth.podcast.theme.surfaceDark
import com.mammoth.podcast.theme.surfaceDarkHighContrast
import com.mammoth.podcast.theme.surfaceDarkMediumContrast
import com.mammoth.podcast.theme.surfaceDimDark
import com.mammoth.podcast.theme.surfaceDimDarkHighContrast
import com.mammoth.podcast.theme.surfaceDimDarkMediumContrast
import com.mammoth.podcast.theme.surfaceDimLight
import com.mammoth.podcast.theme.surfaceDimLightHighContrast
import com.mammoth.podcast.theme.surfaceDimLightMediumContrast
import com.mammoth.podcast.theme.surfaceLight
import com.mammoth.podcast.theme.surfaceLightHighContrast
import com.mammoth.podcast.theme.surfaceLightMediumContrast
import com.mammoth.podcast.theme.surfaceVariantDark
import com.mammoth.podcast.theme.surfaceVariantDarkHighContrast
import com.mammoth.podcast.theme.surfaceVariantDarkMediumContrast
import com.mammoth.podcast.theme.surfaceVariantLight
import com.mammoth.podcast.theme.surfaceVariantLightHighContrast
import com.mammoth.podcast.theme.surfaceVariantLightMediumContrast
import com.mammoth.podcast.theme.tertiaryContainerDark
import com.mammoth.podcast.theme.tertiaryContainerDarkHighContrast
import com.mammoth.podcast.theme.tertiaryContainerDarkMediumContrast
import com.mammoth.podcast.theme.tertiaryContainerLight
import com.mammoth.podcast.theme.tertiaryContainerLightHighContrast
import com.mammoth.podcast.theme.tertiaryContainerLightMediumContrast
import com.mammoth.podcast.theme.tertiaryDark
import com.mammoth.podcast.theme.tertiaryDarkHighContrast
import com.mammoth.podcast.theme.tertiaryDarkMediumContrast
import com.mammoth.podcast.theme.tertiaryLight
import com.mammoth.podcast.theme.tertiaryLightHighContrast
import com.mammoth.podcast.theme.tertiaryLightMediumContrast
import com.mammoth.podcast.theme.JetcasterShapes

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
