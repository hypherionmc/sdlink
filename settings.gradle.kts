pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven("https://mcentral.firstdark.dev/releases")
        maven("https://maven.firstdark.dev/releases")
        maven("https://maven.firstdark.dev/snapshots")
    }
}

rootProject.name = "sdlink"
include("Common", "ModLoaders", "PluginVersion")
