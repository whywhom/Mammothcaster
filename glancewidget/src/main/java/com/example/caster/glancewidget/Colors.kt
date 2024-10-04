/*
 * Copyright 2024 The Android Open Source Project
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

package com.mammoth.caster.glancewidget

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.mammoth.caster.designsystem.theme.backgroundDark
import com.mammoth.caster.designsystem.theme.backgroundLight
import com.mammoth.caster.designsystem.theme.errorContainerDark
import com.mammoth.caster.designsystem.theme.errorContainerLight
import com.mammoth.caster.designsystem.theme.errorDark
import com.mammoth.caster.designsystem.theme.errorLight
import com.mammoth.caster.designsystem.theme.inverseOnSurfaceDark
import com.mammoth.caster.designsystem.theme.inverseOnSurfaceLight
import com.mammoth.caster.designsystem.theme.inversePrimaryDark
import com.mammoth.caster.designsystem.theme.inversePrimaryLight
import com.mammoth.caster.designsystem.theme.inverseSurfaceDark
import com.mammoth.caster.designsystem.theme.inverseSurfaceLight
import com.mammoth.caster.designsystem.theme.onBackgroundDark
import com.mammoth.caster.designsystem.theme.onBackgroundLight
import com.mammoth.caster.designsystem.theme.onErrorContainerDark
import com.mammoth.caster.designsystem.theme.onErrorContainerLight
import com.mammoth.caster.designsystem.theme.onErrorDark
import com.mammoth.caster.designsystem.theme.onErrorLight
import com.mammoth.caster.designsystem.theme.onPrimaryContainerDark
import com.mammoth.caster.designsystem.theme.onPrimaryContainerLight
import com.mammoth.caster.designsystem.theme.onPrimaryDark
import com.mammoth.caster.designsystem.theme.onPrimaryLight
import com.mammoth.caster.designsystem.theme.onSecondaryContainerDark
import com.mammoth.caster.designsystem.theme.onSecondaryContainerLight
import com.mammoth.caster.designsystem.theme.onSecondaryDark
import com.mammoth.caster.designsystem.theme.onSecondaryLight
import com.mammoth.caster.designsystem.theme.onSurfaceDark
import com.mammoth.caster.designsystem.theme.onSurfaceLight
import com.mammoth.caster.designsystem.theme.onSurfaceVariantDark
import com.mammoth.caster.designsystem.theme.onSurfaceVariantLight
import com.mammoth.caster.designsystem.theme.onTertiaryContainerDark
import com.mammoth.caster.designsystem.theme.onTertiaryContainerLight
import com.mammoth.caster.designsystem.theme.onTertiaryDark
import com.mammoth.caster.designsystem.theme.onTertiaryLight
import com.mammoth.caster.designsystem.theme.outlineDark
import com.mammoth.caster.designsystem.theme.outlineLight
import com.mammoth.caster.designsystem.theme.outlineVariantDark
import com.mammoth.caster.designsystem.theme.outlineVariantLight
import com.mammoth.caster.designsystem.theme.primaryContainerDark
import com.mammoth.caster.designsystem.theme.primaryContainerLight
import com.mammoth.caster.designsystem.theme.primaryDark
import com.mammoth.caster.designsystem.theme.primaryLight
import com.mammoth.caster.designsystem.theme.scrimDark
import com.mammoth.caster.designsystem.theme.scrimLight
import com.mammoth.caster.designsystem.theme.secondaryContainerDark
import com.mammoth.caster.designsystem.theme.secondaryContainerLight
import com.mammoth.caster.designsystem.theme.secondaryDark
import com.mammoth.caster.designsystem.theme.secondaryLight
import com.mammoth.caster.designsystem.theme.surfaceBrightDark
import com.mammoth.caster.designsystem.theme.surfaceBrightLight
import com.mammoth.caster.designsystem.theme.surfaceContainerDark
import com.mammoth.caster.designsystem.theme.surfaceContainerHighDark
import com.mammoth.caster.designsystem.theme.surfaceContainerHighLight
import com.mammoth.caster.designsystem.theme.surfaceContainerHighestDark
import com.mammoth.caster.designsystem.theme.surfaceContainerHighestLight
import com.mammoth.caster.designsystem.theme.surfaceContainerLight
import com.mammoth.caster.designsystem.theme.surfaceContainerLowDark
import com.mammoth.caster.designsystem.theme.surfaceContainerLowLight
import com.mammoth.caster.designsystem.theme.surfaceContainerLowestDark
import com.mammoth.caster.designsystem.theme.surfaceContainerLowestLight
import com.mammoth.caster.designsystem.theme.surfaceDark
import com.mammoth.caster.designsystem.theme.surfaceDimDark
import com.mammoth.caster.designsystem.theme.surfaceDimLight
import com.mammoth.caster.designsystem.theme.surfaceLight
import com.mammoth.caster.designsystem.theme.surfaceVariantDark
import com.mammoth.caster.designsystem.theme.surfaceVariantLight
import com.mammoth.caster.designsystem.theme.tertiaryContainerDark
import com.mammoth.caster.designsystem.theme.tertiaryContainerLight
import com.mammoth.caster.designsystem.theme.tertiaryDark
import com.mammoth.caster.designsystem.theme.tertiaryLight

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
