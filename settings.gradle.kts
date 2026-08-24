pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Molliecaster"

include(":shared")
include(":core:model")
include(":core:data")
include(":core:playback")
include(":core:ui")
include(":app")
include(":feature:home")
include(":feature:search")
include(":feature:library")
include(":feature:podcast")
include(":feature:player")
include(":feature:settings")
include(":androidApp")
