package io.github.lingmulongtai.liquidglass

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

fun glassColorScheme(darkTheme: Boolean): ColorScheme =
    if (darkTheme) LiquidGlassDarkColors else LiquidGlassLightColors

private val LiquidGlassLightColors = lightColorScheme(
    primary = Color(0xFF2F6BFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE6FF),
    onPrimaryContainer = Color(0xFF0A2A6B),
    inversePrimary = Color(0xFF8AB0FF),
    secondary = Color(0xFF5E5E66),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9E9EE),
    onSecondaryContainer = Color(0xFF1C1C1E),
    tertiary = Color(0xFF7A4FD6),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEFE5FF),
    onTertiaryContainer = Color(0xFF2A0F5C),
    error = Color(0xFFD93025),
    onError = Color.White,
    errorContainer = Color(0xFFFDE3E1),
    onErrorContainer = Color(0xFF5C0B05),
    background = Color(0xFFF2F2F7),
    onBackground = Color(0xFF111114),
    surface = Color(0xFFF2F2F7),
    onSurface = Color(0xFF111114),
    surfaceVariant = Color(0xFFE9E9EE),
    onSurfaceVariant = Color(0xFF6C6C70),
    surfaceTint = Color.White,
    inverseSurface = Color(0xFF1C1C1E),
    inverseOnSurface = Color(0xFFF2F2F7),
    outline = Color(0xFF8E8E93),
    outlineVariant = Color(0xFFD1D1D6),
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFE3E3E8),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color(0xFFF7F7FA),
    surfaceContainerHigh = Color(0xFFE9E9EE),
    surfaceContainerHighest = Color(0xFFE3E3E8)
)

private val LiquidGlassDarkColors = darkColorScheme(
    primary = Color(0xFF6E9BFF),
    onPrimary = Color(0xFF00215E),
    primaryContainer = Color(0xFF1D3A7A),
    onPrimaryContainer = Color(0xFFDCE6FF),
    inversePrimary = Color(0xFF2F6BFF),
    secondary = Color(0xFFA1A1AA),
    onSecondary = Color(0xFF1C1C1E),
    secondaryContainer = Color(0xFF2C2C2E),
    onSecondaryContainer = Color(0xFFF2F2F7),
    tertiary = Color(0xFFC3A6FF),
    onTertiary = Color(0xFF2A0F5C),
    tertiaryContainer = Color(0xFF3B2470),
    onTertiaryContainer = Color(0xFFEFE5FF),
    error = Color(0xFFFF6961),
    onError = Color(0xFF450704),
    errorContainer = Color(0xFF4A1210),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color.Black,
    onBackground = Color(0xFFF5F5F7),
    surface = Color.Black,
    onSurface = Color(0xFFF5F5F7),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFF98989F),
    surfaceTint = Color.White,
    inverseSurface = Color(0xFFF2F2F7),
    inverseOnSurface = Color(0xFF1C1C1E),
    outline = Color(0xFF8E8E93),
    outlineVariant = Color(0xFF38383A),
    surfaceBright = Color(0xFF2C2C2E),
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF1C1C1E),
    surfaceContainer = Color(0xFF232326),
    surfaceContainerHigh = Color(0xFF2C2C2E),
    surfaceContainerHighest = Color(0xFF3A3A3C)
)
