pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        val kitRepository = providers.gradleProperty("kitRepository").orNull
        if (kitRepository != null) {
            maven {
                url = uri(kitRepository)
                content { includeGroup("io.github.lingmulongtai") }
            }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "LiquidGlassAndroidKit"
include(":app")
if (!providers.gradleProperty("kitRepository").isPresent) include(":designkit")
