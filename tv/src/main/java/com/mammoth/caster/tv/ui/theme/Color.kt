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

package com.mammoth.caster.tv.ui.theme

import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.lightColorScheme
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
import com.mammoth.caster.designsystem.theme.surfaceDark
import com.mammoth.caster.designsystem.theme.surfaceLight
import com.mammoth.caster.designsystem.theme.surfaceVariantDark
import com.mammoth.caster.designsystem.theme.surfaceVariantLight
import com.mammoth.caster.designsystem.theme.tertiaryContainerDark
import com.mammoth.caster.designsystem.theme.tertiaryContainerLight
import com.mammoth.caster.designsystem.theme.tertiaryDark
import com.mammoth.caster.designsystem.theme.tertiaryLight

val colorSchemeForDarkMode = darkColorScheme(
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
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    border = outlineDark,
    borderVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
)

// Todo: specify surfaceTint
val colorSchemeForLightMode = lightColorScheme(
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
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    border = outlineLight,
    borderVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
)
