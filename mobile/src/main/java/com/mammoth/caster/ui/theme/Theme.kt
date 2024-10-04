/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mammoth.caster.ui.theme

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
import com.mammoth.caster.designsystem.theme.JetcasterShapes
import com.mammoth.caster.designsystem.theme.JetcasterTypography
import com.mammoth.caster.designsystem.theme.backgroundDark
import com.mammoth.caster.designsystem.theme.backgroundDarkHighContrast
import com.mammoth.caster.designsystem.theme.backgroundDarkMediumContrast
import com.mammoth.caster.designsystem.theme.backgroundLight
import com.mammoth.caster.designsystem.theme.backgroundLightHighContrast
import com.mammoth.caster.designsystem.theme.backgroundLightMediumContrast
import com.mammoth.caster.designsystem.theme.errorContainerDark
import com.mammoth.caster.designsystem.theme.errorContainerDarkHighContrast
import com.mammoth.caster.designsystem.theme.errorContainerDarkMediumContrast
import com.mammoth.caster.designsystem.theme.errorContainerLight
import com.mammoth.caster.designsystem.theme.errorContainerLightHighContrast
import com.mammoth.caster.designsystem.theme.errorContainerLightMediumContrast
import com.mammoth.caster.designsystem.theme.errorDark
import com.mammoth.caster.designsystem.theme.errorDarkHighContrast
import com.mammoth.caster.designsystem.theme.errorDarkMediumContrast
import com.mammoth.caster.designsystem.theme.errorLight
import com.mammoth.caster.designsystem.theme.errorLightHighContrast
import com.mammoth.caster.designsystem.theme.errorLightMediumContrast
import com.mammoth.caster.designsystem.theme.inverseOnSurfaceDark
import com.mammoth.caster.designsystem.theme.inverseOnSurfaceDarkHighContrast
import com.mammoth.caster.designsystem.theme.inverseOnSurfaceDarkMediumContrast
import com.mammoth.caster.designsystem.theme.inverseOnSurfaceLight
import com.mammoth.caster.designsystem.theme.inverseOnSurfaceLightHighContrast
import com.mammoth.caster.designsystem.theme.inverseOnSurfaceLightMediumContrast
import com.mammoth.caster.designsystem.theme.inversePrimaryDark
import com.mammoth.caster.designsystem.theme.inversePrimaryDarkHighContrast
import com.mammoth.caster.designsystem.theme.inversePrimaryDarkMediumContrast
import com.mammoth.caster.designsystem.theme.inversePrimaryLight
import com.mammoth.caster.designsystem.theme.inversePrimaryLightHighContrast
import com.mammoth.caster.designsystem.theme.inversePrimaryLightMediumContrast
import com.mammoth.caster.designsystem.theme.inverseSurfaceDark
import com.mammoth.caster.designsystem.theme.inverseSurfaceDarkHighContrast
import com.mammoth.caster.designsystem.theme.inverseSurfaceDarkMediumContrast
import com.mammoth.caster.designsystem.theme.inverseSurfaceLight
import com.mammoth.caster.designsystem.theme.inverseSurfaceLightHighContrast
import com.mammoth.caster.designsystem.theme.inverseSurfaceLightMediumContrast
import com.mammoth.caster.designsystem.theme.onBackgroundDark
import com.mammoth.caster.designsystem.theme.onBackgroundDarkHighContrast
import com.mammoth.caster.designsystem.theme.onBackgroundDarkMediumContrast
import com.mammoth.caster.designsystem.theme.onBackgroundLight
import com.mammoth.caster.designsystem.theme.onBackgroundLightHighContrast
import com.mammoth.caster.designsystem.theme.onBackgroundLightMediumContrast
import com.mammoth.caster.designsystem.theme.onErrorContainerDark
import com.mammoth.caster.designsystem.theme.onErrorContainerDarkHighContrast
import com.mammoth.caster.designsystem.theme.onErrorContainerDarkMediumContrast
import com.mammoth.caster.designsystem.theme.onErrorContainerLight
import com.mammoth.caster.designsystem.theme.onErrorContainerLightHighContrast
import com.mammoth.caster.designsystem.theme.onErrorContainerLightMediumContrast
import com.mammoth.caster.designsystem.theme.onErrorDark
import com.mammoth.caster.designsystem.theme.onErrorDarkHighContrast
import com.mammoth.caster.designsystem.theme.onErrorDarkMediumContrast
import com.mammoth.caster.designsystem.theme.onErrorLight
import com.mammoth.caster.designsystem.theme.onErrorLightHighContrast
import com.mammoth.caster.designsystem.theme.onErrorLightMediumContrast
import com.mammoth.caster.designsystem.theme.onPrimaryContainerDark
import com.mammoth.caster.designsystem.theme.onPrimaryContainerDarkHighContrast
import com.mammoth.caster.designsystem.theme.onPrimaryContainerDarkMediumContrast
import com.mammoth.caster.designsystem.theme.onPrimaryContainerLight
import com.mammoth.caster.designsystem.theme.onPrimaryContainerLightHighContrast
import com.mammoth.caster.designsystem.theme.onPrimaryContainerLightMediumContrast
import com.mammoth.caster.designsystem.theme.onPrimaryDark
import com.mammoth.caster.designsystem.theme.onPrimaryDarkHighContrast
import com.mammoth.caster.designsystem.theme.onPrimaryDarkMediumContrast
import com.mammoth.caster.designsystem.theme.onPrimaryLight
import com.mammoth.caster.designsystem.theme.onPrimaryLightHighContrast
import com.mammoth.caster.designsystem.theme.onPrimaryLightMediumContrast
import com.mammoth.caster.designsystem.theme.onSecondaryContainerDark
import com.mammoth.caster.designsystem.theme.onSecondaryContainerDarkHighContrast
import com.mammoth.caster.designsystem.theme.onSecondaryContainerDarkMediumContrast
import com.mammoth.caster.designsystem.theme.onSecondaryContainerLight
import com.mammoth.caster.designsystem.theme.onSecondaryContainerLightHighContrast
import com.mammoth.caster.designsystem.theme.onSecondaryContainerLightMediumContrast
import com.mammoth.caster.designsystem.theme.onSecondaryDark
import com.mammoth.caster.designsystem.theme.onSecondaryDarkHighContrast
import com.mammoth.caster.designsystem.theme.onSecondaryDarkMediumContrast
import com.mammoth.caster.designsystem.theme.onSecondaryLight
import com.mammoth.caster.designsystem.theme.onSecondaryLightHighContrast
import com.mammoth.caster.designsystem.theme.onSecondaryLightMediumContrast
import com.mammoth.caster.designsystem.theme.onSurfaceDark
import com.mammoth.caster.designsystem.theme.onSurfaceDarkHighContrast
import com.mammoth.caster.designsystem.theme.onSurfaceDarkMediumContrast
import com.mammoth.caster.designsystem.theme.onSurfaceLight
import com.mammoth.caster.designsystem.theme.onSurfaceLightHighContrast
import com.mammoth.caster.designsystem.theme.onSurfaceLightMediumContrast
import com.mammoth.caster.designsystem.theme.onSurfaceVariantDark
import com.mammoth.caster.designsystem.theme.onSurfaceVariantDarkHighContrast
import com.mammoth.caster.designsystem.theme.onSurfaceVariantDarkMediumContrast
import com.mammoth.caster.designsystem.theme.onSurfaceVariantLight
import com.mammoth.caster.designsystem.theme.onSurfaceVariantLightHighContrast
import com.mammoth.caster.designsystem.theme.onSurfaceVariantLightMediumContrast
import com.mammoth.caster.designsystem.theme.onTertiaryContainerDark
import com.mammoth.caster.designsystem.theme.onTertiaryContainerDarkHighContrast
import com.mammoth.caster.designsystem.theme.onTertiaryContainerDarkMediumContrast
import com.mammoth.caster.designsystem.theme.onTertiaryContainerLight
import com.mammoth.caster.designsystem.theme.onTertiaryContainerLightHighContrast
import com.mammoth.caster.designsystem.theme.onTertiaryContainerLightMediumContrast
import com.mammoth.caster.designsystem.theme.onTertiaryDark
import com.mammoth.caster.designsystem.theme.onTertiaryDarkHighContrast
import com.mammoth.caster.designsystem.theme.onTertiaryDarkMediumContrast
import com.mammoth.caster.designsystem.theme.onTertiaryLight
import com.mammoth.caster.designsystem.theme.onTertiaryLightHighContrast
import com.mammoth.caster.designsystem.theme.onTertiaryLightMediumContrast
import com.mammoth.caster.designsystem.theme.outlineDark
import com.mammoth.caster.designsystem.theme.outlineDarkHighContrast
import com.mammoth.caster.designsystem.theme.outlineDarkMediumContrast
import com.mammoth.caster.designsystem.theme.outlineLight
import com.mammoth.caster.designsystem.theme.outlineLightHighContrast
import com.mammoth.caster.designsystem.theme.outlineLightMediumContrast
import com.mammoth.caster.designsystem.theme.outlineVariantDark
import com.mammoth.caster.designsystem.theme.outlineVariantDarkHighContrast
import com.mammoth.caster.designsystem.theme.outlineVariantDarkMediumContrast
import com.mammoth.caster.designsystem.theme.outlineVariantLight
import com.mammoth.caster.designsystem.theme.outlineVariantLightHighContrast
import com.mammoth.caster.designsystem.theme.outlineVariantLightMediumContrast
import com.mammoth.caster.designsystem.theme.primaryContainerDark
import com.mammoth.caster.designsystem.theme.primaryContainerDarkHighContrast
import com.mammoth.caster.designsystem.theme.primaryContainerDarkMediumContrast
import com.mammoth.caster.designsystem.theme.primaryContainerLight
import com.mammoth.caster.designsystem.theme.primaryContainerLightHighContrast
import com.mammoth.caster.designsystem.theme.primaryContainerLightMediumContrast
import com.mammoth.caster.designsystem.theme.primaryDark
import com.mammoth.caster.designsystem.theme.primaryDarkHighContrast
import com.mammoth.caster.designsystem.theme.primaryDarkMediumContrast
import com.mammoth.caster.designsystem.theme.primaryLight
import com.mammoth.caster.designsystem.theme.primaryLightHighContrast
import com.mammoth.caster.designsystem.theme.primaryLightMediumContrast
import com.mammoth.caster.designsystem.theme.scrimDark
import com.mammoth.caster.designsystem.theme.scrimDarkHighContrast
import com.mammoth.caster.designsystem.theme.scrimDarkMediumContrast
import com.mammoth.caster.designsystem.theme.scrimLight
import com.mammoth.caster.designsystem.theme.scrimLightHighContrast
import com.mammoth.caster.designsystem.theme.scrimLightMediumContrast
import com.mammoth.caster.designsystem.theme.secondaryContainerDark
import com.mammoth.caster.designsystem.theme.secondaryContainerDarkHighContrast
import com.mammoth.caster.designsystem.theme.secondaryContainerDarkMediumContrast
import com.mammoth.caster.designsystem.theme.secondaryContainerLight
import com.mammoth.caster.designsystem.theme.secondaryContainerLightHighContrast
import com.mammoth.caster.designsystem.theme.secondaryContainerLightMediumContrast
import com.mammoth.caster.designsystem.theme.secondaryDark
import com.mammoth.caster.designsystem.theme.secondaryDarkHighContrast
import com.mammoth.caster.designsystem.theme.secondaryDarkMediumContrast
import com.mammoth.caster.designsystem.theme.secondaryLight
import com.mammoth.caster.designsystem.theme.secondaryLightHighContrast
import com.mammoth.caster.designsystem.theme.secondaryLightMediumContrast
import com.mammoth.caster.designsystem.theme.surfaceBrightDark
import com.mammoth.caster.designsystem.theme.surfaceBrightDarkHighContrast
import com.mammoth.caster.designsystem.theme.surfaceBrightDarkMediumContrast
import com.mammoth.caster.designsystem.theme.surfaceBrightLight
import com.mammoth.caster.designsystem.theme.surfaceBrightLightHighContrast
import com.mammoth.caster.designsystem.theme.surfaceBrightLightMediumContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerDark
import com.mammoth.caster.designsystem.theme.surfaceContainerDarkHighContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerDarkMediumContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerHighDark
import com.mammoth.caster.designsystem.theme.surfaceContainerHighDarkHighContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerHighDarkMediumContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerHighLight
import com.mammoth.caster.designsystem.theme.surfaceContainerHighLightHighContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerHighLightMediumContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerHighestDark
import com.mammoth.caster.designsystem.theme.surfaceContainerHighestDarkHighContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerHighestDarkMediumContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerHighestLight
import com.mammoth.caster.designsystem.theme.surfaceContainerHighestLightHighContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerHighestLightMediumContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerLight
import com.mammoth.caster.designsystem.theme.surfaceContainerLightHighContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerLightMediumContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerLowDark
import com.mammoth.caster.designsystem.theme.surfaceContainerLowDarkHighContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerLowDarkMediumContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerLowLight
import com.mammoth.caster.designsystem.theme.surfaceContainerLowLightHighContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerLowLightMediumContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerLowestDark
import com.mammoth.caster.designsystem.theme.surfaceContainerLowestDarkHighContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerLowestDarkMediumContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerLowestLight
import com.mammoth.caster.designsystem.theme.surfaceContainerLowestLightHighContrast
import com.mammoth.caster.designsystem.theme.surfaceContainerLowestLightMediumContrast
import com.mammoth.caster.designsystem.theme.surfaceDark
import com.mammoth.caster.designsystem.theme.surfaceDarkHighContrast
import com.mammoth.caster.designsystem.theme.surfaceDarkMediumContrast
import com.mammoth.caster.designsystem.theme.surfaceDimDark
import com.mammoth.caster.designsystem.theme.surfaceDimDarkHighContrast
import com.mammoth.caster.designsystem.theme.surfaceDimDarkMediumContrast
import com.mammoth.caster.designsystem.theme.surfaceDimLight
import com.mammoth.caster.designsystem.theme.surfaceDimLightHighContrast
import com.mammoth.caster.designsystem.theme.surfaceDimLightMediumContrast
import com.mammoth.caster.designsystem.theme.surfaceLight
import com.mammoth.caster.designsystem.theme.surfaceLightHighContrast
import com.mammoth.caster.designsystem.theme.surfaceLightMediumContrast
import com.mammoth.caster.designsystem.theme.surfaceVariantDark
import com.mammoth.caster.designsystem.theme.surfaceVariantDarkHighContrast
import com.mammoth.caster.designsystem.theme.surfaceVariantDarkMediumContrast
import com.mammoth.caster.designsystem.theme.surfaceVariantLight
import com.mammoth.caster.designsystem.theme.surfaceVariantLightHighContrast
import com.mammoth.caster.designsystem.theme.surfaceVariantLightMediumContrast
import com.mammoth.caster.designsystem.theme.tertiaryContainerDark
import com.mammoth.caster.designsystem.theme.tertiaryContainerDarkHighContrast
import com.mammoth.caster.designsystem.theme.tertiaryContainerDarkMediumContrast
import com.mammoth.caster.designsystem.theme.tertiaryContainerLight
import com.mammoth.caster.designsystem.theme.tertiaryContainerLightHighContrast
import com.mammoth.caster.designsystem.theme.tertiaryContainerLightMediumContrast
import com.mammoth.caster.designsystem.theme.tertiaryDark
import com.mammoth.caster.designsystem.theme.tertiaryDarkHighContrast
import com.mammoth.caster.designsystem.theme.tertiaryDarkMediumContrast
import com.mammoth.caster.designsystem.theme.tertiaryLight
import com.mammoth.caster.designsystem.theme.tertiaryLightHighContrast
import com.mammoth.caster.designsystem.theme.tertiaryLightMediumContrast

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
fun JetcasterTheme(
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
