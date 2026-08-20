import org.gradle.api.provider.Provider

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

fun Provider<String>.orEmptyValue(): String = orElse("").get()
fun String.asBuildConfigString(): String = "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

val podcastIndexProxyUrl = providers.gradleProperty("MOLLIE_PODCAST_INDEX_PROXY_URL")
    .orElse(providers.environmentVariable("MOLLIE_PODCAST_INDEX_PROXY_URL"))
    .orEmptyValue()
val podcastIndexApiKey = providers.gradleProperty("MOLLIE_PODCAST_INDEX_API_KEY")
    .orElse(providers.environmentVariable("MOLLIE_PODCAST_INDEX_API_KEY"))
    .orEmptyValue()
val podcastIndexApiSecret = providers.gradleProperty("MOLLIE_PODCAST_INDEX_API_SECRET")
    .orElse(providers.environmentVariable("MOLLIE_PODCAST_INDEX_API_SECRET"))
    .orEmptyValue()
val appleStorefront = providers.gradleProperty("MOLLIE_APPLE_STOREFRONT")
    .orElse(providers.environmentVariable("MOLLIE_APPLE_STOREFRONT"))
    .orElse("us")
    .get()

android {
    namespace = "mammoth.mollie.caster"
    compileSdk = 36

    defaultConfig {
        applicationId = "mammoth.mollie.caster"
        minSdk = 23
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.2"
        buildConfigField("String", "PODCAST_INDEX_PROXY_URL", podcastIndexProxyUrl.asBuildConfigString())
        buildConfigField("String", "PODCAST_INDEX_API_KEY", podcastIndexApiKey.asBuildConfigString())
        buildConfigField("String", "PODCAST_INDEX_API_SECRET", podcastIndexApiSecret.asBuildConfigString())
        buildConfigField("String", "APPLE_STOREFRONT", appleStorefront.asBuildConfigString())
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity.compose)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.datasource.okhttp)
    implementation(libs.kotlinx.coroutines.core)
}
