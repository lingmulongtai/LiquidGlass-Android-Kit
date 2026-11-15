package io.github.lingmulongtai.liquidglass

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

/** A label and icon supplied by the host application. */
data class GlassTab(val label: String, val icon: ImageVector)

private data class NavigationEntry(
    val label: String,
    val icon: ImageVector,
    val selected: Boolean,
    val onClick: () -> Unit
)

private val GlassBarHeight = 60.dp
private val GlassBarMargin = 12.dp

/**
 * Floating tabs with a press-and-drag lens. The host owns the selected index and navigation.
 * Use [pagePosition] and [requestedTab] together when the host uses a continuous horizontal pager.
 */
@Composable
fun GlassNavigationBar(
    tabs: List<GlassTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: GlassBackdrop? = LocalGlassBackdrop.current,
    pagePosition: (() -> Float)? = null,
    requestedTab: (() -> Int?)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    if (tabs.isEmpty()) return
    val dark = LocalGlassDark.current
    val selected = selectedIndex.coerceIn(tabs.indices)
    val entries = tabs.mapIndexed { index, tab ->
        NavigationEntry(tab.label, tab.icon, index == selected) { onSelect(index) }
    }
    val tint = if (dark) Color(0xFF282828).copy(alpha = 0.62f) else Color.White.copy(alpha = 0.60f)
    val fallback = if (dark) Color(0xFF1C1C1E).copy(alpha = 0.97f) else Color.White.copy(alpha = 0.97f)
    val rim = if (dark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
    Row(
        modifier.widthIn(max = 560.dp).fillMaxWidth().navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = GlassBarMargin),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassTabRow(entries, backdrop, tint, fallback, rim,
            Modifier.weight(1f).height(GlassBarHeight), pagePosition, requestedTab)
        trailing?.invoke()
    }
}

/** The lens refracts the bar and labels without scaling the original controls. */
@Composable
private fun GlassTabRow(
    entries: List<NavigationEntry>, backdrop: GlassBackdrop?, glassTint: Color,
    glassFallback: Color, rim: Color, modifier: Modifier = Modifier,
    pagePosition: (() -> Float)?,
    requestedTab: (() -> Int?)?
) {
    if (entries.isEmpty()) return
    val dark = LocalGlassDark.current
    val colors = MaterialTheme.colorScheme
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val latestEntries by rememberUpdatedState(entries)
    val lensBackdrop = rememberGlassBackdrop()
    var dragPosition by remember { mutableStateOf<Float?>(null) }
    var releasedTab by remember { mutableStateOf<Int?>(null) }
    var selectedTabPressed by remember { mutableStateOf(false) }
    val lifted = selectedTabPressed || dragPosition != null
    GlassLiftHaptics(lifted)
    var anchor by remember { mutableStateOf(0f) }
    var downX by remember { mutableStateOf(0f) }
    val selectedIndex = entries.indexOfFirst { it.selected }.coerceAtLeast(0)
    val pagerPosition = pagePosition?.invoke()?.coerceIn(0f, entries.lastIndex.toFloat())
    val landing = releasedTab?.let { index ->
        if (pagerPosition == null || requestedTab?.invoke() == index) index.toFloat() else pagerPosition
    }
    val target = dragPosition ?: landing ?: selectedIndex.toFloat()
    val settledPosition by animateFloatAsState(target,
        spring(dampingRatio = if (dragPosition != null) 0.94f else 0.92f,
            stiffness = if (dragPosition != null) 2500f else 1024f), label = "glass_tab_position")
    // Keep the released lens at its destination while the pager catches up underneath it.
    val canFollowPager = landing != null && settledPosition == landing &&
        (pagerPosition == null || abs(pagerPosition - landing) < 0.001f)
    LaunchedEffect(releasedTab, canFollowPager) {
        if (canFollowPager) releasedTab = null
    }
    val lift by animateFloatAsState(if (lifted) 1f else 0f,
        spring(dampingRatio = 0.92f, stiffness = 676f), label = "glass_tab_lift")
    val selectedColor = if (dark) Color(0xFF0091FF) else Color(0xFF0088FF)
    BoxWithConstraints(
        modifier.selectableGroup()
            .pointerInput(entries.size, rtl) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val inset = 4.dp.toPx()
                    downX = down.position.x
                    val index = glassTabPosition(downX - inset, size.width - 2 * inset,
                        latestEntries.size, rtl).roundToInt()
                    anchor = index.toFloat()
                    selectedTabPressed = latestEntries[index].selected
                    try { waitForUpOrCancellation() } finally { selectedTabPressed = false }
                }
            }
            .pointerInput(entries.size, rtl) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        dragPosition = anchor
                        releasedTab = null
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        val movement = change.position.x - downX
                        val slot = (size.width - 8.dp.toPx()) / latestEntries.size
                        dragPosition = glassTabDragPosition(anchor, movement, slot, latestEntries.size, rtl)
                    },
                    onDragEnd = {
                        val index = dragPosition?.roundToInt()
                        releasedTab = index
                        dragPosition = null
                        latestEntries.getOrNull(index ?: -1)?.onClick?.invoke()
                    },
                    onDragCancel = { dragPosition = null }
                )
            }
    ) {
        val cell = (maxWidth - 8.dp) / entries.size
        val logical = if (dragPosition != null || releasedTab != null) settledPosition
            else pagerPosition ?: settledPosition
        val visual = if (rtl) entries.lastIndex - logical else logical
        val liftHeight = 16.dp * lift
        // Keep the source independent from the lens to avoid recording it recursively.
        Box(Modifier.fillMaxSize().glassBackdropSource(lensBackdrop)) {
            GlassSurface(backdrop, CircleShape, glassTint, glassFallback, rim,
                tuningGroup = GlassSurfaceGroup.NAVIGATION,
                modifier = Modifier.fillMaxSize(), blurRadius = 3.dp, refraction = 0f,
                elevation = if (dark) 0.dp else 1.dp) {}
            Row(Modifier.fillMaxSize().padding(4.dp)) {
                entries.forEachIndexed { index, entry ->
                    val highlighted = index == (dragPosition?.roundToInt() ?: landing?.roundToInt() ?: selectedIndex)
                    val tint = if (highlighted) selectedColor else colors.onSurface
                    val interaction = remember { MutableInteractionSource() }
                    val focused by interaction.collectIsFocusedAsState()
                    Box(
                        Modifier.weight(1f).fillMaxHeight()
                            .then(if (focused) Modifier.border(2.dp, colors.primary, CircleShape) else Modifier)
                            .selectable(entry.selected, onClick = entry.onClick, role = Role.Tab,
                                interactionSource = interaction, indication = null),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Icon(entry.icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
                            Text(entry.label, color = tint, fontSize = 10.sp, lineHeight = 14.sp,
                                fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
        GlassSurface(
            tuningGroup = GlassSurfaceGroup.NAVIGATION,
            backdrop = lensBackdrop, shape = CircleShape,
            tint = if (dark) Color.White.copy(alpha = 0.145f * (1f - lift * 0.62f))
                else Color.Black.copy(alpha = 0.10f * (1f - lift * 0.70f)),
            fallback = if (dark) Color.White.copy(alpha = 0.145f) else Color.Black.copy(alpha = 0.10f),
            rim = Color.White.copy(alpha = if (dark) 0.18f else 0.45f),
            blurRadius = 0.1.dp, refraction = 0.55f * lift, elevation = lift.dp * if (dark) 0 else 7,
            modifier = Modifier.align(AbsoluteAlignment.TopLeft)
                .absoluteOffset {
                    IntOffset((4.dp.toPx() + cell.toPx() * visual - 12.dp.toPx() * lift).roundToInt(),
                        (4.dp.toPx() - liftHeight.toPx() / 2f).roundToInt())
                }
                .width(cell + 24.dp * lift)
                // Let the lens draw past both edges of the tab row while it lifts.
                .wrapContentHeight(Alignment.Top, unbounded = true)
                .height(GlassBarHeight - 8.dp + liftHeight)
        ) {}
    }
}
