# Molliecaster capability matrix

| Capability | Android | iOS | Desktop | Web |
|---|---|---|---|---|
| Shared Compose UI | Implemented | Implemented | Implemented | Implemented (Wasm Beta) |
| RSS fetch and parse | Ktor/OkHttp | Ktor/Darwin | Ktor/CIO | Ktor/Fetch; feed must allow CORS |
| Room 3 schema | Bundled SQLite | Bundled SQLite | Bundled SQLite | Schema/compiler included; Web Worker wiring pending |
| Search title + author | Implemented | Implemented | Implemented | Implemented |
| Apple Podcasts discovery / RSS preview | iTunes Search API + RSS preview | iTunes Search API + RSS preview | iTunes Search API + RSS preview | iTunes Search API + RSS preview; subject to CORS |
| Popular/recommended discovery | Apple Top 10; Podcast Index adapter retained but disabled | Apple Top 10; Podcast Index adapter retained but disabled | Apple Top 10; Podcast Index adapter retained but disabled | Apple Top 10 subject to CORS; Podcast Index adapter retained but disabled |
| Subscribe/favorite/history state | Durable Room | Durable Room | Durable Room | UI; durable Room pending Worker |
| OPML import/export | Implemented codec/UI | Implemented codec/UI | Implemented codec/UI | Implemented codec/UI |
| Audio playback | Media3 service | AVPlayer | JavaFX Media | HTMLAudio |
| Background/lock screen/notification | MediaSessionService | AVAudioSession playback; Now Playing/remote commands pending | OS media-key integration pending | Browser-managed background playback; Media Session metadata pending |
| Downloads | Media3 persistent cache | Disabled | Disabled | Disabled; browser limitations |

## Honest platform constraints

- Browser RSS requests are subject to CORS. A production deployment needs a trusted fetch proxy for feeds that do not opt into cross-origin access.
- Apple Podcasts discovery uses Apple's public iTunes Search API to resolve a podcast's RSS `feedUrl`; search results are not stored until RSS subscription succeeds. Web directory and feed requests can independently be blocked by CORS.
- Popular results currently use the Apple storefront Top 10. Recommendations are re-ranked on-device from user-owned activity. Podcast Index code and source merging remain available, but the source is bypassed unless `DiscoveryConfig.podcastIndexEnabled` is explicitly enabled.
- Podcast Index credentials embedded in a client binary are extractable. If the source is enabled in the future, production builds should use a trusted proxy; direct keys are only a local-development fallback.
- Room 3.0.1 supports Wasm, but `WebWorkerSQLiteDriver` needs a compatible SQLite worker and OPFS/static asset setup. Until that worker is added, Web uses the shared in-memory store for the visible app state.
- Android is the complete playback target: Media3 owns playback, the media session, notification, lock-screen controls, 15-second seek increments, speed and persistent download cache.
- iOS, desktop and Web now provide basic real playback through AVPlayer, JavaFX Media and HTMLAudio respectively. Downloads remain Android-only, and non-Android lock-screen/media-key integration is still a separate milestone.
