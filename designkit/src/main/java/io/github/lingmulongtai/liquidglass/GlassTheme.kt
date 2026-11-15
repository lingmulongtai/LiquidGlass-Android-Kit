package io.github.lingmulongtai.liquidglass

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal val LocalGlassDark = staticCompositionLocalOf { false }
internal val LocalGlassPanelDepth = staticCompositionLocalOf { 0 }

/** The background-only layer shared by cards, controls and headers. */
val LocalGlassBackdrop = staticCompositionLocalOf<GlassBackdrop?> { null }

private val GlassShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

/** Installs the glass palette, Inter typography, materials and optional lens haptics. */
@Composable
fun GlassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colors: ColorScheme = glassColorScheme(darkTheme),
    style: GlassStyle = GlassStyle(),
    hapticsEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalGlassDark provides darkTheme, LocalGlassStyle provides style) {
        MaterialTheme(colorScheme = colors, shapes = GlassShapes, typography = LiquidGlassTypography) {
            ProvideGlassHaptics(hapticsEnabled) {
                CompositionLocalProvider(LocalContentColor provides colors.onBackground, content = content)
            }
        }
    }
}

fun glassBackgroundBrush(darkTheme: Boolean): Brush = Brush.linearGradient(
    if (darkTheme) listOf(Color(0xFF111B36), Color(0xFF29234A), Color(0xFF162C47))
    else listOf(Color(0xFFDCEBFF), Color(0xFFEEE7F8), Color(0xFFF7E9EF), Color(0xFFD8E9FC))
)

/** Records [background] behind the content. Put glass consumers in [content], outside the recorded layer. */
@Composable
fun GlassBackdropLayout(
    modifier: Modifier = Modifier,
    brush: Brush = glassBackgroundBrush(LocalGlassDark.current),
    background: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val backdrop = rememberGlassBackdrop()
    Box(modifier) {
        Box(Modifier.matchParentSize().glassBackdropSource(backdrop).background(brush), content = background)
        CompositionLocalProvider(LocalGlassBackdrop provides backdrop) { content() }
    }
}
