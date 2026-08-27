import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JvmVendorSpec

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

// Android Studio's bundled JBR can compile the project but does not include
// jpackage. Resolve a complete JDK only when a native desktop package is asked
// for, and still permit a release machine to provide a specific JDK explicitly.
val desktopPackagingTaskNames = setOf(
    "checkRuntime",
    "suggestRuntimeModules",
    "createRuntimeImage",
    "createDistributable",
    "runDistributable",
    "packageDistributionForCurrentOS",
    "packageDmg",
    "packageMsi",
    "packageDeb",
    "packageReleaseDmg",
    "packageReleaseMsi",
    "packageReleaseDeb",
    "notarizeDmg",
)
val desktopPackagingRequested = gradle.startParameter.taskNames.any { task ->
    val name = task.substringAfterLast(':')
    name in desktopPackagingTaskNames ||
        (name.startsWith("package") && listOf("Dmg", "Msi", "Deb", "DistributionForCurrentOS").any(name::endsWith)) ||
        (name.startsWith("notarize") && name.endsWith("Dmg"))
}
val desktopPackagingJavaHome = if (desktopPackagingRequested) {
    providers.gradleProperty("molliecaster.desktop.javaHome").orNull
        ?: extensions.getByType(JavaToolchainService::class.java)
            .compilerFor {
                languageVersion.set(JavaLanguageVersion.of(21))
                vendor.set(JvmVendorSpec.ADOPTIUM)
            }
            .get()
            .metadata
            .installationPath
            .asFile
            .absolutePath
} else {
    null
}

compose.desktop {
    application {
        mainClass = "mammoth.mollie.caster.MainKt"
        desktopPackagingJavaHome?.let { javaHome = it }
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Molliecaster"
            packageVersion = "1.0.2"
            // JavaFX is supplied as automatic modules, so jpackage cannot infer every JDK
            // dependency it needs for the macOS graphics and media toolkits. Keep the full
            // runtime rather than producing a smaller DMG that cannot play audio.
            includeAllModules = true
            modules("java.net.http", "java.desktop", "jdk.unsupported")
            macOS {
                iconFile.set(project.file("src/desktopMain/resources/Molliecaster.icns"))
            }
        }
        buildTypes.release.proguard {
            // Room finds MollieDatabase_Impl and Ktor finds its CIO engine at runtime.
            // Keep their reflective entry points when preparing a shrinked release bundle.
            configurationFiles.from(project(":shared").file("proguard-rules.pro"))
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
