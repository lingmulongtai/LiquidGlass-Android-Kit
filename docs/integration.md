# Compose integration

The starter uses SDK 36, minimum API 26, Gradle 9.4.1, Android Gradle Plugin
9.2.1, Kotlin Compose compiler 2.3.21 and Compose BOM 2026.06.00. Use the same
baseline when importing the source module into another project. The Maven
distribution carries Compose dependency metadata alongside the AAR.

## Add a glass surface behind your own content

The kit works with your image or composable. Both layers must share a Compose
window for the surface to sample the backdrop.

```kotlin
GlassTheme {
    GlassBackdropLayout(Modifier.fillMaxSize(), background = {
        // Render your wallpaper here. The default gradient is already painted.
    }) {
        GlassCard(Modifier.padding(24.dp)) {
            Text("Account", Modifier.padding(20.dp))
        }
    }
}
```

The basic backdrop host records only the background. To make tabs sample scrolling
content, record a second, separate layer with
`Modifier.glassBackdropSource(page, behind = LocalGlassBackdrop.current)`,
then pass `backdrop = page` to the sibling tab bar. The starter shows this
structure in `KitGallery.kt`. Avoid recording a component into the same layer
that it samples.

## Use navigation or controls alone

```kotlin
var selected by rememberSaveable { mutableIntStateOf(0) }
GlassNavigationBar(
    tabs = listOf(GlassTab("Home", homeIcon), GlassTab("Profile", profileIcon)),
    selectedIndex = selected,
    onSelect = { selected = it },
)
```

Render your destination from `selected`. Supply your own vectors for the icons.
The bar manages selection gestures, semantics and the navigation-bar inset.
For a continuous pager, provide its fractional `pagePosition` and the current
programmatic `requestedTab` together. Update the request synchronously when
`onSelect` starts the page scroll, and clear it when the scroll finishes or is
interrupted. This lets a released lens settle at the destination while the
page catches up.

`GlassSwitch`, `GlassSlider`, and `GlassSegmentedControl` are controlled
components. Keep the values in your own Compose state or view model. Set
`hapticsEnabled = false` on `GlassTheme` when your app's preference requests it.

## Adjust the glass

```kotlin
val standard = GlassStyle()
GlassTheme(style = standard.copy(containers = standard.containers.copy(frost = 12f))) {
    Content()
}
```

The Containers preset matches the source app. For each group, `GlassTuning()`
inherits the individual component material. Bend and reach multiply the base
refraction. Passing your own `colors` to `GlassTheme` changes the Material
palette while keeping the typography and shape system.

## Use source or build the archive

Copy the `designkit/` module with `LICENSE` and `NOTICE.md`, include it in
`settings.gradle.kts`, merge the required entries from
`gradle/libs.versions.toml`, and depend on `implementation(project(":designkit"))`.
Use the component API you need from that module. The shared renderer and theme
keep the same effects across screens.

Build a self-contained Maven directory archive with
`./gradlew :designkit:packageKit`. The ZIP is written to
`designkit/build/distributions/`. The repository portion contains the AAR,
sources JAR, POM and Gradle Module Metadata. Standard AndroidX dependencies
resolve from Google's Maven repository.

To verify binary consumption with the example app:

```sh
./gradlew :designkit:packageKit
./gradlew :app:assembleDebug -PkitRepository=designkit/build/maven
```
