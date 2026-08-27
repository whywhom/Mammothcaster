package mammoth.mollie.caster.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mammoth.mollie.caster.platform.platformName
import molliecaster.shared.generated.resources.Res
import molliecaster.shared.generated.resources.inter_regular
import molliecaster.shared.generated.resources.inter_semibold
import org.jetbrains.compose.resources.Font

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF3C0091),
    primaryContainer = Color(0xFFA078FF),
    onPrimaryContainer = Color(0xFF340080),
    secondary = Color(0xFF44E2CD),
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF03C6B2),
    onSecondaryContainer = Color(0xFF004D44),
    tertiary = Color(0xFFD3BEEB),
    onTertiary = Color(0xFF38294D),
    tertiaryContainer = Color(0xFF9C89B3),
    onTertiaryContainer = Color(0xFF312246),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF131318),
    onBackground = Color(0xFFE4E1E9),
    surface = Color(0xFF131318),
    onSurface = Color(0xFFE4E1E9),
    surfaceVariant = Color(0xFF35343A),
    onSurfaceVariant = Color(0xFFCBC3D7),
    outline = Color(0xFF958EA0),
    outlineVariant = Color(0xFF494454),
    inverseSurface = Color(0xFFE4E1E9),
    inverseOnSurface = Color(0xFF303036),
    inversePrimary = Color(0xFF6D3BD7),
    surfaceTint = Color(0xFFD0BCFF),
    surfaceBright = Color(0xFF39383E),
    surfaceDim = Color(0xFF131318),
    surfaceContainerLowest = Color(0xFF0E0E13),
    surfaceContainerLow = Color(0xFF1B1B20),
    surfaceContainer = Color(0xFF1F1F25),
    surfaceContainerHigh = Color(0xFF2A292F),
    surfaceContainerHighest = Color(0xFF35343A),
    primaryFixed = Color(0xFFE9DDFF),
    primaryFixedDim = Color(0xFFD0BCFF),
    onPrimaryFixed = Color(0xFF23005C),
    onPrimaryFixedVariant = Color(0xFF5516BE),
    secondaryFixed = Color(0xFF62FAE3),
    secondaryFixedDim = Color(0xFF3CDDC7),
    onSecondaryFixed = Color(0xFF00201C),
    onSecondaryFixedVariant = Color(0xFF005047),
    tertiaryFixed = Color(0xFFEDDCFF),
    tertiaryFixedDim = Color(0xFFD3BEEB),
    onTertiaryFixed = Color(0xFF231437),
    onTertiaryFixedVariant = Color(0xFF4F4065),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF5517BE),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF6D3BD7),
    onPrimaryContainer = Color(0xFFE0D2FF),
    secondary = Color(0xFF006B60),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF92F4E4),
    onSecondaryContainer = Color(0xFF007166),
    tertiary = Color(0xFF713800),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF944B00),
    onTertiaryContainer = Color(0xFFFFD0B0),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFFEF7FF),
    onBackground = Color(0xFF1D1A23),
    surface = Color(0xFFFEF7FF),
    onSurface = Color(0xFF1D1A23),
    surfaceVariant = Color(0xFFE7E0ED),
    onSurfaceVariant = Color(0xFF494454),
    outline = Color(0xFF7B7486),
    outlineVariant = Color(0xFFCBC3D7),
    inverseSurface = Color(0xFF322F39),
    inverseOnSurface = Color(0xFFF5EEFB),
    inversePrimary = Color(0xFFD0BCFF),
    surfaceTint = Color(0xFF6D3BD7),
    surfaceBright = Color(0xFFFEF7FF),
    surfaceDim = Color(0xFFDED7E4),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8F1FE),
    surfaceContainer = Color(0xFFF2EBF8),
    surfaceContainerHigh = Color(0xFFEDE5F3),
    surfaceContainerHighest = Color(0xFFE7E0ED),
    primaryFixed = Color(0xFFE9DDFF),
    primaryFixedDim = Color(0xFFD0BCFF),
    onPrimaryFixed = Color(0xFF23005C),
    onPrimaryFixedVariant = Color(0xFF5417BE),
    secondaryFixed = Color(0xFF92F4E4),
    secondaryFixedDim = Color(0xFF75D7C8),
    onSecondaryFixed = Color(0xFF00201C),
    onSecondaryFixedVariant = Color(0xFF005048),
    tertiaryFixed = Color(0xFFFFDCC5),
    tertiaryFixedDim = Color(0xFFFFB783),
    onTertiaryFixed = Color(0xFF301400),
    onTertiaryFixedVariant = Color(0xFF703700),
)

@Immutable
data class AetherAccentColors(
    val teal: Color,
    val purple: Color,
    val glass: Color,
    val glassStrong: Color,
    val glow: Color,
    val isDark: Boolean,
) {
    val actionGradient: Brush
        get() = Brush.linearGradient(listOf(teal, purple))

    val ambientGradient: Brush
        get() = if (isDark) {
            Brush.linearGradient(
                listOf(Color(0xFF131318), Color(0xFF211832), Color(0xFF102925), Color(0xFF131318)),
            )
        } else {
            Brush.linearGradient(
                listOf(Color(0xFFFEF7FF), Color(0xFFF2EAFE), Color(0xFFE9FAF6), Color(0xFFFEF7FF)),
            )
        }
}

private val LocalAetherColors = staticCompositionLocalOf {
    AetherAccentColors(
        teal = Color(0xFF44E2CD),
        purple = Color(0xFFD0BCFF),
        glass = Color(0x991F1F25),
        glassStrong = Color(0xCC2A292F),
        glow = Color(0x665516BE),
        isDark = true,
    )
}

object AetherTheme {
    val colors: AetherAccentColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAetherColors.current
}

private fun aetherTypography(fontFamily: FontFamily) = Typography(
    displayLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 42.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.8).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 38.sp,
        lineHeight = 43.sp,
        letterSpacing = (-0.5).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 34.sp,
        lineHeight = 40.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp,
        lineHeight = 38.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 23.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.2.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 13.sp,
        letterSpacing = 1.sp,
    ),
)

private val AetherShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun MolliecasterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // iOS resolves the generic sans-serif family to SF Pro. Desktop and web use their
    // platform sans-serif, while Android gets the bundled Inter family.
    val appFontFamily = if (platformName == "Android") {
        FontFamily(
            Font(Res.font.inter_regular, FontWeight.Normal),
            Font(Res.font.inter_semibold, FontWeight.SemiBold),
        )
    } else {
        FontFamily.SansSerif
    }
    val typography = aetherTypography(appFontFamily)
    val accents = if (darkTheme) {
        AetherAccentColors(
            teal = Color(0xFF44E2CD),
            purple = Color(0xFFD0BCFF),
            glass = Color(0x991F1F25),
            glassStrong = Color(0xE62A292F),
            glow = Color(0x665516BE),
            isDark = true,
        )
    } else {
        AetherAccentColors(
            teal = Color(0xFF008174),
            purple = Color(0xFF6D3BD7),
            glass = Color(0xBFFFFFFF),
            glassStrong = Color(0xF2FFFFFF),
            glow = Color(0x1F303036),
            isDark = false,
        )
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalAetherColors provides accents) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = typography,
            shapes = AetherShapes,
            content = content,
        )
    }
}
