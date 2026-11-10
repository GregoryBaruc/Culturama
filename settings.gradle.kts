pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Culturama"
include(":app")

// O projeto fica no OneDrive, que bloqueia criar e apagar app/build.
// A compilação (inclusive pelo Android Studio) grava fora dessa pasta.
gradle.beforeProject {
    val base = java.io.File(
        System.getenv("LOCALAPPDATA") ?: System.getProperty("java.io.tmpdir"),
        "culturama-build"
    )
    layout.buildDirectory.set(base.resolve(name))
}
