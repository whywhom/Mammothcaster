plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

kotlin {
    android {
        namespace = "mammoth.mollie.caster.core.playback"
        compileSdk = 36
        minSdk = 23
    }
    iosArm64()
    iosSimulatorArm64()
    jvm("desktop")
    wasmJs()
    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain.dependencies {
                api(project(":core:model"))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
    jvmToolchain(21)
}
