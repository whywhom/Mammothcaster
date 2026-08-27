# Molliecaster

Molliecaster is a Compose Multiplatform podcast client migrated from the product ideas in Jetcaster. The application namespace is `mammoth.mollie.caster` and the project targets Android, iOS, desktop JVM and Kotlin/Wasm.

## Current features

- Discover podcasts through Apple Podcasts search, RSS previews, the Apple storefront Top 10, and 50 browse categories (including Apple categories such as Books, Parenting, Business, Education, News, and Society & Culture). Recommendations are re-ranked locally from subscriptions, favourites, playback history, and category affinity.
- Subscribe to RSS feeds; browse podcast details and episodes; search by podcast title or author; and sort episodes newest- or oldest-first. Episode save/favourite actions are available for subscribed shows.
- Retain playback history and resume positions, and import or export OPML subscriptions. OPML imports are de-duplicated and report failures without discarding valid feeds.
- Play real episode audio with play/pause, seeking, 15-second skip controls, playback speeds from 0.8x to 2x, and a 15-, 30-, or 60-minute sleep timer. Playlists continue automatically through their remaining items.
- Download, remove, browse, and play episodes offline on Android, iOS, desktop, and Web within each platform's storage constraints. Android also provides persistent playback, a Media3 media session, notification, lock-screen controls, and transparent segmented stream caching.
- Create local-audio playlists from device files; add, remove, rename, pin, search, and reorder playlists and tracks; play in filename order or shuffle; and surface playlists from Home. New files are sorted by filename (including Chinese names), while pinned playlists appear before alphabetical playlist names.
- Use a consistent toolbar back action and contextual title on secondary screens, keeping screen content focused on the playlist, library, or podcast itself.

### Platform support

| Capability | Android | iOS | Desktop | Web |
|---|---|---|---|---|
| Shared UI, RSS, search, discovery, subscriptions, favourites, history, and OPML | Yes | Yes | Yes | Yes* |
| Local persistence | Room + bundled SQLite | Room + bundled SQLite | Room + bundled SQLite | In-memory until SQLite Web Worker wiring is added |
| Audio engine | Media3 | AVPlayer | JavaFX Media | HTMLAudio |
| System playback controls | Media session, notification, lock screen | AVAudioSession, background audio, lock-screen/Control Center Now Playing and play/pause controls | Media-key support pending | Browser-managed background playback; Media Session metadata pending |
| Downloads | Media3 + public Downloads export | Application Support | Application Support/Molliecaster/Downloads | Origin Cache Storage + browser save |
| Local-audio playlists | Multi-file picker and playback | Multi-file picker and playback | Multi-file picker and playback | Browser file picker and playback |
| Transparent stream cache | 5 GB Media3 LRU | Pending | Pending | Pending |

\* Browser RSS and Apple Podcasts requests require the relevant server to allow CORS. A production web deployment needs a trusted fetch proxy for feeds that do not.

## Technical stack

- Kotlin 2.4.10 and Compose Multiplatform 1.11.1, targeting Android, iOS, desktop JVM, and Kotlin/Wasm web.
- Material 3 for the shared UI, Kotlin coroutines and Kotlinx Serialization for asynchronous state and data mapping.
- Ktor 3.5.1 for network access: OkHttp on Android, Darwin on iOS, CIO on desktop, and Fetch on web.
- Room 3.0.1 with bundled SQLite for durable catalog and user data on Android, iOS, and desktop; SQLite Web Worker integration remains pending for web.
- Coil 3.5.0 for multiplatform artwork loading and KSoup for RSS/HTML parsing.
- Media3 1.10.1 for Android playback, media sessions, and persistent downloads; AVPlayer, JavaFX Media, and HTMLAudio adapters provide basic playback elsewhere.

## Run and verify

The project uses a JDK 21 toolchain while emitting JVM 17 bytecode. It also requires Gradle 9.3.1+, Android SDK 36 and Xcode 26 or later.

Android Studio's bundled JBR can compile and run the project, but it does not include the `jpackage` tool required for native desktop distributions. When a desktop packaging task is requested, Gradle automatically provisions a full Adoptium JDK 21 through the Foojay toolchain resolver. To use an existing full JDK instead, pass `-Pmolliecaster.desktop.javaHome=/absolute/path/to/jdk`.

```bash
# Shared/Desktop tests
./gradlew :shared:desktopTest

# Web tests
./gradlew :shared:wasmJsBrowserTest

# Android debug build
./gradlew :androidApp:assembleDebug

# iOS Simulator framework
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Run the Desktop and Web applications with:

```bash
./gradlew :app:run
./gradlew :app:wasmJsBrowserDevelopmentRun
```

Open `iosApp/iosApp.xcodeproj` for iOS.

## Continuous integration

GitHub Actions builds each platform on a compatible hosted runner:

| Runner | Targets |
|---|---|
| Ubuntu 24.04 | Shared tests, web tests, Android AAB, Kotlin/Wasm distribution, and Debian package |
| Windows Server 2025 | Windows MSI installer |
| macOS 26 | macOS DMG and unsigned iOS Simulator app |

The workflow runs for pushes, pull requests, and manual dispatches. Successful build products are retained as workflow artifacts for 14 days. Release signing, Apple notarization, and store upload remain separate credentialed release steps.

Check the Desktop packaging runtime and create a macOS package with:

```bash
./gradlew :shared:checkRuntime
./gradlew :app:packageDmg
```

The first packaging build downloads the full JDK once into the Gradle user home; later builds reuse it.

## Popular and recommended podcasts

Home discovery currently fetches the Apple Podcasts chart Top 10 and resolves every entry to its RSS URL. “Recommended for you” re-ranks that candidate set locally from subscriptions, favorites, history, and category affinity. Discovery results stay ephemeral until the user successfully subscribes to the RSS feed.

The Podcast Index trending adapter and ranked-source merge are retained for future use, but `DiscoveryConfig.podcastIndexEnabled` defaults to `false`; disabled builds do not send a Podcast Index request or show a missing-credentials warning. To re-enable it later, set that flag to `true` and use a trusted proxy that injects the required headers, or supply direct development credentials. Set the two-letter Apple storefront with `MOLLIE_APPLE_STOREFRONT` (default `us`).

The future credential plumbing remains in place: Android reads those names from ignored `local.properties`, Gradle properties (`-P...`), or environment variables; Gradle properties and environment variables take precedence for CI. Desktop and iOS read environment variables. Never put Podcast Index secrets in a Web build; use a trusted same-origin proxy if the source is enabled later. A direct Android API key is embedded in the app package, so it is suitable only for local development—not a production secret.

The source Jetcaster project remains read-only. See [CAPABILITY_MATRIX.md](CAPABILITY_MATRIX.md) for platform-specific behavior and known constraints.

## Build release products

Before shipping, update Android `versionCode`/`versionName` in `androidApp/build.gradle.kts` and iOS `CURRENT_PROJECT_VERSION`/`MARKETING_VERSION` in `iosApp/iosApp.xcodeproj/project.pbxproj`. The current release version is `1.0.2` (build `2`).

### Android

The repository deliberately contains no signing key or release signing configuration. Build the artifact, then sign it with the release key held by the release owner:

```bash
# Google Play bundle (preferred)
./gradlew :androidApp:bundleRelease

# APK for direct distribution or testing
./gradlew :androidApp:assembleRelease
```

Artifacts are placed under `androidApp/build/outputs/bundle/release/` and `androidApp/build/outputs/apk/release/`. Configure release signing in secure CI or sign the bundle/APK with Android SDK tools before upload. Do not commit a keystore, passwords, or Podcast Index credentials.

### iOS

Open `iosApp/iosApp.xcodeproj` in Xcode, select the `Molliecaster` scheme and a Release configuration, then use **Product → Archive**. Configure the production Apple Developer team, bundle identifier, provisioning profile, and distribution certificate before exporting for TestFlight or the App Store.

For a command-line archive on macOS, use the same signing setup:

```bash
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme Molliecaster \
  -configuration Release \
  -archivePath build/ios/Molliecaster.xcarchive \
  archive
```

Export the archive with the appropriate App Store or ad-hoc export options for the selected distribution channel.

### Desktop

Compose Desktop packages the native installer format supported by the host operating system:

```bash
# Run the command on the matching host platform.
./gradlew :app:packageReleaseDmg  # macOS
./gradlew :app:packageReleaseMsi  # Windows
./gradlew :app:packageReleaseDeb  # Debian/Ubuntu Linux
```

Packages are written below `app/build/compose/binaries/main-release/`. Code-sign and notarize the macOS package, and sign the Windows installer, with credentials managed outside this repository.

### Web

Create the optimized Kotlin/Wasm browser distribution with:

```bash
./gradlew :app:wasmJsBrowserDistribution
```

Deploy the generated static files from `app/build/dist/wasmJs/productionExecutable/` behind HTTPS. Configure a trusted RSS fetch proxy for feeds without CORS support; never expose Podcast Index credentials in client-side assets.
