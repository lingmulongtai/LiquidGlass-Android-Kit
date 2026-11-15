plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    `maven-publish`
}

android {
    namespace = "io.github.lingmulongtai.liquidglass"
    compileSdk = 36

    defaultConfig { minSdk = 26 }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }
    publishing { singleVariant("release") { withSourcesJar() } }
}

dependencies {
    api(platform(libs.compose.bom))
    api(libs.compose.ui)
    api(libs.compose.foundation)
    api(libs.compose.material3)
    testImplementation(libs.junit)
}

val kitVersion = providers.gradleProperty("kitVersion").orElse("0.1.0").get()
val localMaven = layout.buildDirectory.dir("maven")
val kitPublication = publishing.publications.register<MavenPublication>("release") {
    groupId = "io.github.lingmulongtai"
    artifactId = "liquidglass-compose"
    version = kitVersion
    pom {
        name.set("Liquid Glass Android Kit")
        description.set("Native Jetpack Compose glass components for Android")
        url.set("https://github.com/lingmulongtai/LiquidGlass-Android-Kit")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/license/mit")
            }
        }
    }
}

afterEvaluate {
    kitPublication.configure { from(components["release"]) }
}

publishing.repositories.maven {
    name = "kit"
    url = localMaven.get().asFile.toURI()
}

tasks.register<Zip>("packageKit") {
    group = "distribution"
    description = "Package the portable Maven repository and licenses."
    dependsOn("publishReleasePublicationToKitRepository")
    from(localMaven) { into("maven") }
    from(rootProject.file("LICENSE"))
    from(rootProject.file("NOTICE.md"))
    from("src/main/assets/liquidglass/licenses") { into("licenses") }
    archiveFileName.set("liquidglass-compose-${kitVersion}-maven.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}
