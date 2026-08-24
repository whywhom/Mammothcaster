import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

compose.desktop {
    application {
        mainClass = "mammoth.mollie.caster.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Molliecaster"
            packageVersion = "1.0.2"
        }
    }
}

kotlin {
    android {
        namespace = "mammoth.mollie.caster.app"
        compileSdk = 36
        minSdk = 23
    }
    iosArm64()
    iosSimulatorArm64()
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "MolliecasterApp"
            isStatic = true
        }
    }
    jvm("desktop")
    wasmJs {
        outputModuleName = "molliecaster"
        browser()
        binaries.executable()
    }
    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
                implementation(project(":shared"))
                implementation(project(":feature:home"))
                implementation(project(":feature:search"))
                implementation(project(":feature:library"))
                implementation(project(":feature:podcast"))
                implementation(project(":feature:player"))
                implementation(project(":feature:settings"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.coil.compose)
        }
        named("desktopMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }

    jvmToolchain(21)
}
