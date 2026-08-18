# Molliecaster

Molliecaster is a Compose Multiplatform podcast client migrated from the product ideas in Jetcaster. The application namespace is `mammoth.mollie.caster` and the project targets Android, iOS, desktop JVM and Kotlin/Wasm.

## Current features

- Discover podcasts through Apple Podcasts search, RSS previews, and the Apple storefront Top 10. Recommendations are re-ranked locally from subscriptions, favourites, playback history, and category affinity.
- Subscribe to RSS feeds; browse podcast details and episodes; search by podcast title or author; and sort episodes newest- or oldest-first.
- Favourite episodes, retain playback history and resume positions, and import or export OPML subscriptions. OPML imports are de-duplicated and report failures without discarding valid feeds.
- Play real episode audio with play/pause, seeking, 15-second skip controls, playback speeds from 0.8x to 2x, and a 15-, 30-, or 60-minute sleep timer.
- Download, remove, and browse downloaded episodes on Android. Android also provides persistent playback, a Media3 media session, notification, and lock-screen controls.

### Platform support

| Capability | Android | iOS | Desktop | Web |
|---|---|---|---|---|
| Shared UI, RSS, search, discovery, subscriptions, favourites, history, and OPML | Yes | Yes | Yes | Yes* |
| Local persistence | Room + bundled SQLite | Room + bundled SQLite | Room + bundled SQLite | In-memory until SQLite Web Worker wiring is added |
| Audio engine | Media3 | AVPlayer | JavaFX Media | HTMLAudio |
| System playback controls | Media session, notification, lock screen | AVAudioSession; Now Playing/remote commands pending | Media-key support pending | Browser-managed background playback; Media Session metadata pending |
| Downloads | Persistent Media3 cache | Not available | Not available | Not available |

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
./gradlew :shared:run
./gradlew :shared:wasmJsBrowserDevelopmentRun
```

Open `iosApp/iosApp.xcodeproj` for iOS.

Check the Desktop packaging runtime and create a macOS package with:

```bash
./gradlew :shared:checkRuntime
./gradlew :shared:packageDmg
```

The first packaging build downloads the full JDK once into the Gradle user home; later builds reuse it.

## Popular and recommended podcasts

Home discovery currently fetches the Apple Podcasts chart Top 10 and resolves every entry to its RSS URL. “Recommended for you” re-ranks that candidate set locally from subscriptions, favorites, history, and category affinity. Discovery results stay ephemeral until the user successfully subscribes to the RSS feed.

The Podcast Index trending adapter and ranked-source merge are retained for future use, but `DiscoveryConfig.podcastIndexEnabled` defaults to `false`; disabled builds do not send a Podcast Index request or show a missing-credentials warning. To re-enable it later, set that flag to `true` and use a trusted proxy that injects the required headers, or supply direct development credentials. Set the two-letter Apple storefront with `MOLLIE_APPLE_STOREFRONT` (default `us`).

The future credential plumbing remains in place: Android reads those names as Gradle properties (`-P...`) or environment variables, while desktop and iOS read environment variables. Never put Podcast Index secrets in a Web build; use a trusted same-origin proxy if the source is enabled later.

The source Jetcaster project remains read-only. See [CAPABILITY_MATRIX.md](CAPABILITY_MATRIX.md) for platform-specific behavior and known constraints.

## Build release products

Before shipping, update Android `versionCode`/`versionName` in `androidApp/build.gradle.kts` and iOS `CURRENT_PROJECT_VERSION`/`MARKETING_VERSION` in `iosApp/iosApp.xcodeproj/project.pbxproj`. The current release version is `0.1.0` (build `1`).

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

Open `iosApp/iosApp.xcodeproj` in Xcode, select the `iosApp` scheme and a Release configuration, then use **Product → Archive**. Configure the production Apple Developer team, bundle identifier, provisioning profile, and distribution certificate before exporting for TestFlight or the App Store.

For a command-line archive on macOS, use the same signing setup:

```bash
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Release \
  -archivePath build/ios/Molliecaster.xcarchive \
  archive
```

Export the archive with the appropriate App Store or ad-hoc export options for the selected distribution channel.

### Desktop

Compose Desktop packages the native installer format supported by the host operating system:

```bash
# Run the command on the matching host platform.
./gradlew :shared:packageReleaseDmg  # macOS
./gradlew :shared:packageReleaseMsi  # Windows
./gradlew :shared:packageReleaseDeb  # Debian/Ubuntu Linux
```

Packages are written below `shared/build/compose/binaries/main-release/`. Code-sign and notarize the macOS package, and sign the Windows installer, with credentials managed outside this repository.

### Web

Create the optimized Kotlin/Wasm browser distribution with:

```bash
./gradlew :shared:wasmJsBrowserDistribution
```

Deploy the generated static files from `shared/build/dist/wasmJs/productionExecutable/` behind HTTPS. Configure a trusted RSS fetch proxy for feeds without CORS support; never expose Podcast Index credentials in client-side assets.
