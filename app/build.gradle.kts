plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "io.github.lingmulongtai.liquidglass.sample"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.lingmulongtai.liquidglass.sample"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = providers.gradleProperty("kitVersion").get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }
}

dependencies {
    if (providers.gradleProperty("kitRepository").isPresent) {
        implementation("io.github.lingmulongtai:liquidglass-compose:${providers.gradleProperty("kitVersion").get()}")
    } else {
        implementation(project(":designkit"))
    }
    implementation(libs.activity.compose)
    implementation(libs.compose.icons)
    implementation(libs.compose.preview)
    debugImplementation(libs.compose.tooling)
}
