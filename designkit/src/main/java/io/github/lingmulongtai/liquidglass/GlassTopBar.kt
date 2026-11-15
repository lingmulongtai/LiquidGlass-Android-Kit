package io.github.lingmulongtai.liquidglass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Bottom-rounded title bar for edge-to-edge screens. */
@Composable
fun GlassTopBar(
    title: String,
    modifier: Modifier = Modifier,
    backdrop: GlassBackdrop? = LocalGlassBackdrop.current,
    navigationIcon: (@Composable () -> Unit)? = null,
    subtitle: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    GlassSurface(
        backdrop = backdrop,
        shape = shape,
        tint = Color.Transparent,
        fallback = Color.Transparent,
        rim = Color.White.copy(alpha = .13f),
        modifier = modifier.fillMaxWidth(),
        blurRadius = 0.dp,
        refraction = .45f,
        allowAsymmetricCorners = true,
        bendReachScale = 4f,
        shaderRimShine = 0f,
        saturation = 1f,
        elevation = 0.dp,
        tuningGroup = GlassSurfaceGroup.HEADER
    ) {
        Box(Modifier.matchParentSize().clip(shape).background(Brush.verticalGradient(
            0f to Color.Black,
            .32f to Color.Black.copy(alpha = .98f),
            .70f to Color.Black.copy(alpha = .76f),
            1f to Color.Black.copy(alpha = .16f)
        )))
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().heightIn(min = 72.dp)
                    .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                navigationIcon?.invoke()
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    subtitle?.invoke()
                }
                Row(content = actions)
            }
        }
    }
}
