package io.github.lingmulongtai.liquidglass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Transparent state tints sit over the neutral glass, retaining their original lightness. */
internal fun glassPanelTint(color: Color, base: Color, dark: Boolean): Color =
    color.compositeOver(base).copy(alpha = if (dark) 0.74f else 0.72f)

/** Passive glass grouping, with no spurious click action on charts or settings sections. */
@Composable
fun GlassCard(modifier: Modifier = Modifier, shape: Shape = MaterialTheme.shapes.large,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow, border: BorderStroke? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable ColumnScope.() -> Unit) {
    val dark = LocalGlassDark.current
    val depth = LocalGlassPanelDepth.current
    val base = MaterialTheme.colorScheme.surfaceContainerLow
    val flat = if (dark) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.05f)
    GlassSurface(if (depth == 0) LocalGlassBackdrop.current else null, shape,
        glassPanelTint(color, base, dark), if (depth == 0) color.compositeOver(base) else flat,
        Color.White.copy(alpha = if (dark) 0.18f else 0.70f), modifier.then(if (border != null) Modifier.border(border, shape) else Modifier),
        tuningGroup = GlassSurfaceGroup.CONTAINERS, blurRadius = 16.dp, refraction = 0.18f, elevation = if (depth == 0) 2.dp else 0.dp) {
        CompositionLocalProvider(LocalContentColor provides contentColor, LocalGlassPanelDepth provides depth + 1) { Column(content = content) }
    }
}

@Composable
fun GlassButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    shape: Shape = CircleShape, colors: ButtonColors = ButtonDefaults.buttonColors(), border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding, content: @Composable RowScope.() -> Unit) {
    val defaultColors = ButtonDefaults.buttonColors()
    val neutral = colors.containerColor == defaultColors.containerColor && colors.contentColor == defaultColors.contentColor
    val tint = if (enabled && neutral) MaterialTheme.colorScheme.surface.copy(alpha = 0.48f)
        else if (enabled) colors.containerColor else colors.disabledContainerColor
    val foreground = if (enabled && neutral) MaterialTheme.colorScheme.onSurface
        else if (enabled) colors.contentColor else colors.disabledContentColor
    GlassAction(onClick, modifier, enabled, shape, tint, foreground, border, contentPadding, content = content)
}

@Composable
fun GlassOutlinedButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    shape: Shape = CircleShape, colors: ButtonColors = ButtonDefaults.outlinedButtonColors(), border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding, content: @Composable RowScope.() -> Unit) {
    val defaultColors = ButtonDefaults.outlinedButtonColors()
    val foreground = if (enabled && colors.contentColor == defaultColors.contentColor) MaterialTheme.colorScheme.onSurface
        else if (enabled) colors.contentColor else colors.disabledContentColor
    GlassAction(onClick, modifier, enabled, shape, MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        foreground, border, contentPadding, content = content)
}

@Composable
fun GlassTextButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.textButtonColors(), content: @Composable RowScope.() -> Unit) {
    val defaultColors = ButtonDefaults.textButtonColors()
    val foreground = if (enabled && colors.contentColor == defaultColors.contentColor) MaterialTheme.colorScheme.onSurface
        else if (enabled) colors.contentColor else colors.disabledContentColor
    GlassAction(onClick, modifier, enabled, CircleShape, MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
        foreground, null, PaddingValues(horizontal = 12.dp, vertical = 10.dp), content = content)
}

@Composable
private fun GlassAction(onClick: () -> Unit, modifier: Modifier, enabled: Boolean, shape: Shape,
    tint: Color, foreground: Color, border: BorderStroke?, padding: PaddingValues, selectedState: Boolean? = null, content: @Composable RowScope.() -> Unit) {
    val dark = LocalGlassDark.current
    val interaction = remember { MutableInteractionSource() }
    val nested = LocalGlassPanelDepth.current > 0
    val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()
    var width by remember { mutableIntStateOf(1) }
    val lift by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = if (pressed) spring(dampingRatio = 0.60f, stiffness = 515f)
            else spring(dampingRatio = 0.37f, stiffness = 250f),
        label = "glass_action_press"
    )
    BoxWithConstraints(modifier.defaultMinSize(minHeight = 48.dp)
        .then(if (focused) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape) else Modifier)
        .clickable(enabled = enabled, interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
        .semantics { if (selectedState != null) selected = selectedState }, contentAlignment = Alignment.Center) {
        val innerWidth = if (constraints.hasFixedWidth) Modifier.fillMaxWidth() else Modifier
        Box(innerWidth, contentAlignment = Alignment.Center) {
            val flat = if (tint.alpha >= 0.9f) tint else if (dark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.07f)
            GlassSurface(if (nested) null else LocalGlassBackdrop.current, shape, tint, if (nested) flat else tint.copy(alpha = 1f),
                if (dark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f),
                modifier = Modifier.matchParentSize().onSizeChanged { width = it.width }.graphicsLayer {
                    scaleX = 1f + lift * (15.dp.toPx() / width.coerceAtLeast(1))
                    alpha = if (enabled) 1f else 0.35f
                }.then(if (border != null) Modifier.border(border, shape) else Modifier),
                blurRadius = 3.dp, elevation = if (dark) 0.dp else 1.dp, refraction = 0f) {}
            CompositionLocalProvider(LocalContentColor provides foreground) {
                ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                    Row(Modifier.defaultMinSize(minHeight = 40.dp).padding(padding),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, content = content)
                }
            }
        }
    }
}

@Composable
fun GlassFilterChip(selected: Boolean, onClick: () -> Unit, label: @Composable () -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true) {
    GlassAction(onClick, modifier, enabled, CircleShape, MaterialTheme.colorScheme.surface.copy(alpha = 0.70f),
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)) else null,
        PaddingValues(horizontal = 14.dp, vertical = 8.dp), selectedState = selected) { label() }
}

@Composable
fun GlassIconButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(), content: @Composable () -> Unit) {
    GlassAction(onClick, modifier.size(48.dp), enabled, CircleShape,
        MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
        if (enabled) colors.contentColor else colors.disabledContentColor, null, PaddingValues(12.dp)) { content() }
}
