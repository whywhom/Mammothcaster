# Room resolves generated database implementations by their original class name.
# Keep this implementation and its constructors when creating a desktop release.
-keep class mammoth.mollie.caster.data.database.MollieDatabase_Impl { *; }

# BundledSQLiteDriver loads libsqliteJni from its JAR through a static loader.
# Keep the loader and every JNI-facing member unchanged in a desktop release.
-keep class androidx.sqlite.driver.bundled.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

# Ktor locates the CIO HTTP engine through Java ServiceLoader at runtime.
-keep class io.ktor.client.engine.cio.CIOEngineContainer { *; }

# Coil discovers its Ktor network fetcher through Java ServiceLoader. Keep the
# full image-loading and HTTP stack, including their service contracts.
-keep class coil3.** { *; }
-keep class io.ktor.** { *; }

# ProGuard can rewrite coroutine interface default methods into bytecode that
# the desktop JVM rejects at launch. Preserve the coroutine runtime intact.
-keep class kotlinx.coroutines.** { *; }

# JavaFX discovers its macOS graphics and media implementations reflectively.
# Removing these classes leaves the release bundle without a JavaFX toolkit,
# even though the native libraries are still packaged.
-keep class javafx.** { *; }
-keep class com.sun.javafx.** { *; }
-keep class com.sun.glass.** { *; }
-keep class com.sun.prism.** { *; }
-keep class com.sun.scenario.** { *; }
-keep class com.sun.media.** { *; }
