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

# Coil discovers its Ktor network fetcher through Java ServiceLoader.  Its
# implementation is otherwise only referenced from META-INF/services, which
# the desktop release shrinker cannot see as a normal code reference.
-keep class coil3.network.ktor3.** { *; }
