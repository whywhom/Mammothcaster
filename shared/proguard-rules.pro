# Room resolves generated database implementations by their original class name.
# Keep this implementation and its constructors when creating a desktop release.
-keep class mammoth.mollie.caster.data.database.MollieDatabase_Impl { *; }

# Ktor locates the CIO HTTP engine through Java ServiceLoader at runtime.
-keep class io.ktor.client.engine.cio.CIOEngineContainer { *; }
