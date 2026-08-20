import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    android {
        namespace = "mammoth.mollie.caster.shared"
        compileSdk = 36
        minSdk = 23
        androidResources.enable = true
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }

    iosArm64()
    iosSimulatorArm64()
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "MolliecasterShared"
            isStatic = true
        }
    }

    jvm("desktop") {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }

    wasmJs {
        outputModuleName = "molliecaster"
        browser {
            commonWebpackConfig {
                outputFileName = "molliecaster.js"
            }
        }
        binaries.executable()
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            // MollieDatabase and databaseBuilder expose Room types to platform launchers.
            api(libs.room.runtime)
            implementation(libs.ksoup)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqlite.bundled)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqlite.bundled)
        }
        named("desktopMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.cio)
                implementation(libs.sqlite.bundled)
                val javafxVersion = libs.versions.javafx.get()
                val javafxPlatform = javafxClassifier()
                implementation("org.openjfx:javafx-base:$javafxVersion:$javafxPlatform")
                implementation("org.openjfx:javafx-graphics:$javafxVersion:$javafxPlatform")
                implementation("org.openjfx:javafx-media:$javafxVersion:$javafxPlatform")
            }
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
            implementation(libs.sqlite.web)
            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
        }
    }

    jvmToolchain(21)
}

fun javafxClassifier(): String {
    val os = System.getProperty("os.name").lowercase()
    val arch = System.getProperty("os.arch").lowercase()
    return when {
        os.contains("mac") && (arch.contains("aarch64") || arch.contains("arm64")) -> "mac-aarch64"
        os.contains("mac") -> "mac"
        os.contains("win") && (arch.contains("aarch64") || arch.contains("arm64")) -> "win-aarch64"
        os.contains("win") -> "win"
        arch.contains("aarch64") || arch.contains("arm64") -> "linux-aarch64"
        else -> "linux"
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspDesktop", libs.room.compiler)
    add("kspWasmJs", libs.room.compiler)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

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
    "notarizeDmg",
)

fun String.isDesktopPackagingTask(): Boolean =
    this in desktopPackagingTaskNames ||
        (startsWith("package") && listOf("Dmg", "Msi", "Deb", "DistributionForCurrentOS").any(::endsWith)) ||
        (startsWith("notarize") && endsWith("Dmg")) ||
        (startsWith("create") && endsWith("Distributable")) ||
        (startsWith("run") && endsWith("Distributable"))

val desktopPackagingRequested = gradle.startParameter.taskNames.any { requestedTask ->
    val segments = requestedTask.split(':').filter(String::isNotBlank)
    val taskName = segments.lastOrNull().orEmpty()
    val requestedProject = segments.dropLast(1).joinToString(":")
    val targetsThisProject = requestedProject.isEmpty() || requestedProject == project.path.removePrefix(":")
    targetsThisProject && taskName.isDesktopPackagingTask()
}

val desktopPackagingJavaHome = if (desktopPackagingRequested) {
    providers.gradleProperty("molliecaster.desktop.javaHome").orNull
        ?: project.extensions.getByType(JavaToolchainService::class.java)
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
        buildTypes.release.proguard {
            configurationFiles.from(project.file("proguard-rules.pro"))
        }
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Molliecaster"
            packageVersion = "1.0.2"
        }
    }
}
