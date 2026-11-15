package io.github.lingmulongtai.liquidglass.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.lingmulongtai.liquidglass.GlassBackdropLayout
import io.github.lingmulongtai.liquidglass.GlassButton
import io.github.lingmulongtai.liquidglass.GlassCard
import io.github.lingmulongtai.liquidglass.GlassFilterChip
import io.github.lingmulongtai.liquidglass.GlassIconButton
import io.github.lingmulongtai.liquidglass.GlassNavigationBar
import io.github.lingmulongtai.liquidglass.GlassOutlinedButton
import io.github.lingmulongtai.liquidglass.GlassSegmentedControl
import io.github.lingmulongtai.liquidglass.GlassSlider
import io.github.lingmulongtai.liquidglass.GlassStyle
import io.github.lingmulongtai.liquidglass.GlassSwitch
import io.github.lingmulongtai.liquidglass.GlassTab
import io.github.lingmulongtai.liquidglass.GlassTextField
import io.github.lingmulongtai.liquidglass.GlassTheme
import io.github.lingmulongtai.liquidglass.GlassTopBar
import io.github.lingmulongtai.liquidglass.LocalGlassBackdrop
import io.github.lingmulongtai.liquidglass.glassBackdropSource
import io.github.lingmulongtai.liquidglass.rememberGlassBackdrop
import kotlin.math.roundToInt

private val tabs = listOf(
    GlassTab("Overview", Icons.Rounded.Home),
    GlassTab("Controls", Icons.Rounded.Tune),
    GlassTab("Materials", Icons.Rounded.Palette)
)

@Composable
internal fun KitGallery(dark: Boolean, onToggleTheme: () -> Unit) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var title by rememberSaveable { mutableStateOf("") }
    var running by rememberSaveable { mutableStateOf(false) }
    var completed by rememberSaveable { mutableIntStateOf(0) }
    var duration by rememberSaveable { mutableFloatStateOf(25f) }
    var haptics by rememberSaveable { mutableStateOf(true) }
    val defaults = GlassStyle()
    var tint by rememberSaveable { mutableFloatStateOf(defaults.containers.tint ?: .2f) }
    var frost by rememberSaveable { mutableFloatStateOf(defaults.containers.frost ?: 8f) }

    GlassTheme(
        darkTheme = dark,
        hapticsEnabled = haptics,
        style = defaults.copy(containers = defaults.containers.copy(tint = tint, frost = frost))
    ) {
        GlassBackdropLayout(Modifier.fillMaxSize()) {
            val wallpaper = LocalGlassBackdrop.current
            val page = rememberGlassBackdrop()
            val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
            Scaffold(
                modifier = Modifier.fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    .imePadding(),
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    GlassTopBar(tabs[selected].label) {
                        GlassIconButton(onClick = onToggleTheme) {
                            Icon(
                                if (dark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                contentDescription = if (dark) "Use light theme" else "Use dark theme",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                bottomBar = {
                    if (!keyboardVisible) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            GlassNavigationBar(tabs, selected, { selected = it }, backdrop = page)
                        }
                    }
                }
            ) { insets ->
                Box(Modifier.fillMaxSize().glassBackdropSource(page, behind = wallpaper), contentAlignment = Alignment.TopCenter) {
                    Column(
                        Modifier.widthIn(max = 640.dp).fillMaxWidth().verticalScroll(rememberScrollState())
                            .padding(insets).padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        when (selected) {
                            0 -> {
                                DemoCard(if (running) "Session in progress" else "Focus time") {
                                    Text("${duration.roundToInt()} min", style = MaterialTheme.typography.displayMedium,
                                        color = MaterialTheme.colorScheme.primary)
                                    Text(title.ifBlank { "Focus session" } + " · $completed completed")
                                    GlassButton(onClick = {
                                        if (running) completed++
                                        running = !running
                                    }) { Text(if (running) "Finish session" else "Start session") }
                                }
                                DemoCard("Session setup") {
                                    GlassTextField(title, { title = it }, label = { Text("Session name") }, singleLine = true)
                                    GlassOutlinedButton(onClick = { selected = 1 }) { Text("Change duration") }
                                }
                            }
                            1 -> {
                                val presets = listOf(15, 25, 45)
                                val nearest = presets.indices.minBy { kotlin.math.abs(presets[it] - duration) }
                                DemoCard("Duration") {
                                    GlassSegmentedControl(presets.map { "$it min" }, nearest, { duration = presets[it].toFloat() })
                                    GlassSlider(duration, { duration = it }, Modifier.semantics {
                                        contentDescription = "Session duration"
                                    }, valueRange = 5f..60f, steps = 10)
                                    Text("${duration.roundToInt()} minutes")
                                }
                                DemoCard("Feedback") {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Text("Lens haptics")
                                        GlassSwitch(haptics, { haptics = it }, Modifier.semantics {
                                            contentDescription = "Lens haptics"
                                        })
                                    }
                                }
                            }
                            2 -> {
                                DemoCard("Container material") {
                                    Text("Frost · ${frost.roundToInt()} dp")
                                    GlassSlider(frost, { frost = it }, Modifier.semantics {
                                        contentDescription = "Container frost"
                                    }, valueRange = 0f..40f)
                                    Text("Tint · ${(tint * 100).roundToInt()}%")
                                    GlassSlider(tint, { tint = it }, Modifier.semantics {
                                        contentDescription = "Container tint"
                                    })
                                    GlassOutlinedButton(onClick = {
                                        tint = defaults.containers.tint ?: .2f
                                        frost = defaults.containers.frost ?: 8f
                                    }) { Text("Reset material") }
                                }
                                DemoCard("Appearance") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        GlassFilterChip(dark, { if (!dark) onToggleTheme() }, { Text("Dark") })
                                        GlassFilterChip(!dark, { if (dark) onToggleTheme() }, { Text("Light") })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DemoCard(title: String, content: @Composable () -> Unit) {
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 890)
@Composable
private fun DarkGalleryPreview() = KitGallery(dark = true, onToggleTheme = {})
