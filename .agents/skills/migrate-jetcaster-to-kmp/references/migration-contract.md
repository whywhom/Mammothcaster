# Migration contract

## Source baseline

- Jetcaster is an Android sample with `mobile`, shared Android library modules, plus TV, Wear, and Glance targets.
- Useful behavior includes RSS-backed podcasts, Room-backed stores, subscriptions, categories, responsive Compose screens, StateFlow screen state, podcast details, and episode lists.
- The sample README explicitly describes mobile playback as mocked. Molliecaster requires a real player.
- Jetcaster uses Android-specific Hilt, Navigation, lifecycle collection, OkHttp/Rome parsing, Android Room setup, and platform date/time assumptions that require reassessment.

## Destination boundaries

- `commonMain`: domain models, repository contracts, use cases, Ktor client logic, portable RSS mapping, Room KMP schema/DAOs where supported, presentation state, shared navigation model, shared Compose UI.
- `androidMain`: Media3, MediaSession, notification, background service, Android download integration, Android filesystem/share picker.
- `iosMain`: native audio/session adapter, background audio integration, iOS file/document picker and sharing.
- `webMain`: browser audio adapter, web storage/download capability, browser file import/export.
- `desktopMain`: JVM audio adapter, desktop filesystem import/export, desktop lifecycle integration.

## Milestone gates

1. All four targets configure and compile a minimal application shell.
2. Shared domain and data contracts compile with common tests.
3. RSS ingestion and persistence work with deterministic fixtures.
4. Catalog, discovery, search, detail, and user-library UI work against real repositories.
5. Real Android Media3 playback and downloads work; other targets expose honest adapters and documented capability.
6. OPML round-trip and migration workflows pass fixtures.
7. All available target builds and tests pass, and unresolved platform gaps are explicit.

