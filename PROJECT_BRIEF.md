# Molliecaster project brief

Status: Confirmed on 2026-08-17 — implementation in progress. See `CAPABILITY_MATRIX.md` for verified and pending platform capabilities.

## Goal

Convert the Android-only `../Jetcaster/` sample into a Kotlin and Compose Multiplatform podcast application in this directory.

- Project name: Molliecaster
- Package/application namespace: `mammoth.mollie.caster`
- Targets: Android, iOS, web, and desktop
- Source project: `../Jetcaster/` (read-only reference)
- Destination: `./`
- Out of scope unless requested later: TV, Wear OS, and Glance widgets

## Required product scope

### Discovery and catalog

- RSS subscription and refresh
- Podcast list and episode list
- Home sections: recommended content, popular podcasts, latest podcasts, and category recommendations
- Categories: Technology, Business, Artificial Intelligence, Health, News, Comedy, Education, Science, Society & Culture, Arts, Sports, History, Music, True Crime, Kids & Family, and Government
- Search podcasts by title and author

### Podcast detail

- Artwork, title, author, and description
- Episode count and latest update time
- Subscribe/unsubscribe action
- Episodes show title, publication time, duration, and summary
- Sort episodes newest-first or oldest-first

### Playback

- Real playback, not Jetcaster's mocked player
- Play, pause, scrub, skip forward 15 seconds, and skip backward 15 seconds
- Speeds: 0.8x, 1x, 1.25x, 1.5x, and 2x
- Sleep timer: 15, 30, and 60 minutes
- Durable resume position and playback history
- Platform background/system controls where the operating system and browser support them

### Downloads and user data

- Download episode, delete download, and view downloads
- Favorite/unfavorite episode
- Subscribe/unsubscribe podcast
- History includes recent playback, progress, and last-played time

### OPML migration

- Import and export OPML
- Accept common exports from Apple Podcasts, AntennaPod, and Pocket Casts
- De-duplicate imports and report failures without discarding successful feeds

## Technical direction

- Kotlin + Compose Multiplatform for shared domain, presentation state, navigation model, and UI
- Ktor for network requests
- Room KMP for catalog and user persistence
- Coil for multiplatform artwork loading
- Android Media3 for Android playback, media session, notification/lock-screen control, and downloads
- Shared player/download contracts with platform adapters for iOS, web, and desktop because Media3 is Android-only
- GitHub Actions runners on Ubuntu, Windows, and macOS for platform-specific builds and native packaging
- Proposed non-Android engines: AVPlayer/AVAudioSession on iOS, browser HTML media + Media Session APIs on web, and a JVM desktop media adapter selected during the foundation milestone

## Proposed architecture

- `composeApp`: platform entry points and shared Compose UI
- `core:model`: domain models and identifiers
- `core:data`: repository contracts and implementations
- `core:network`: Ktor RSS/discovery clients and mapping
- `core:database`: Room entities, DAOs, migrations, and database builders
- `core:playback`: shared player state/contracts and platform implementations
- `core:downloads`: shared download state/contracts and platform implementations
- `feature:*`: home, search, podcast detail, library, downloads, and player presentation

The exact module count may be reduced initially to preserve fast builds, then split only when boundaries are proven.

## Milestones and acceptance gates

1. Foundation: all four targets build a minimal shell with the final namespace.
2. Data: RSS fixtures, Room schema, subscriptions, favorites, history, categories, search, and OPML pass common tests.
3. Catalog UI: home, search, podcast detail, episode ordering, and library states work against real repositories.
4. Playback: Android Media3 is fully functional; iOS, web, and desktop adapters provide real basic playback and expose platform capabilities honestly.
5. Downloads and system integration: downloads reconcile files and database state; background/system controls are verified per platform.
6. Release verification: available target builds/tests pass, accessibility and adaptive layouts are reviewed, and any toolchain-only gaps are documented.

## Confirmation assumptions

Replying `确认` accepts these defaults:

1. Use Kotlin/Wasm for the web Compose target. Kotlin/Compose web is currently Beta.
2. Use stable Room 3.0.1 so Room can also back JavaScript/Wasm persistence. Web persistence still requires the SQLite Web Worker described in the capability matrix.
3. Treat Media3 as the Android implementation, with native/browser/desktop adapters on other targets.
4. Reuse Jetcaster behavior and suitable visual patterns, but do not port TV, Wear, Glance, Hilt, Android-only Navigation, OkHttp, or Rome as-is.
5. “Popular” and “recommended” content must come from an explicit data source or deterministic seed strategy; the app will not fabricate popularity statistics.
