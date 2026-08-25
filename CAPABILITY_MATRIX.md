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
| OPML import/export | Android system document picker | iOS Files document picker | Native file dialogs on macOS, Windows, and Linux | Browser file picker / `.opml` download |
| Audio playback | Media3 service | AVPlayer | JavaFX Media | HTMLAudio |
| Local audio playlists | Multi-file system picker; persisted `content://` grants; sequential/shuffle queue | Multi-file Files picker; imported sandbox files; sequential/shuffle queue | Multi-file chooser; file URI playlist; sequential/shuffle queue | Multi-file picker; sequential/shuffle queue for current page session |
| Background/lock screen/notification | MediaSessionService | AVAudioSession playback; Now Playing/remote commands pending | OS media-key integration pending | Browser-managed background playback; Media Session metadata pending |
| Downloads | Media3 transfer/index plus MediaStore export to public `Downloads/Molliecaster/<podcast>/<episode>` | NSURLSession to Application Support | OS `Downloads/Molliecaster/<podcast>/<episode>` | Cache Storage plus browser-managed public save |
| Local-first playback / stream cache | Public download first; 5 GB Media3 LRU stream cache then network | Manual download first; otherwise AVPlayer streams | Manual download first; otherwise JavaFX streams | Manual Cache Storage download first; otherwise HTMLAudio streams |
| Partial cached playback | Implemented by Media3 segmented cache | Pending single-request cache adapter | Pending range-aware cache adapter | Pending service-worker/range cache adapter |
| Metadata freshness | Durable subscribed RSS aggregate 6h; discovery 1h and unsubscribed detail 24h in-session | Same | Same | All metadata in-session until durable Web database wiring |

## Honest platform constraints

- Browser RSS requests are subject to CORS. A production deployment needs a trusted fetch proxy for feeds that do not opt into cross-origin access.
- Apple Podcasts discovery uses Apple's public iTunes Search API to resolve a podcast's RSS `feedUrl`; search results are not stored until RSS subscription succeeds. Web directory and feed requests can independently be blocked by CORS.
- Popular results currently use the Apple storefront Top 10. Recommendations are re-ranked on-device from user-owned activity. Podcast Index code and source merging remain available, but the source is bypassed unless `DiscoveryConfig.podcastIndexEnabled` is explicitly enabled.
- Podcast Index credentials embedded in a client binary are extractable. If the source is enabled in the future, production builds should use a trusted proxy; direct keys are only a local-development fallback.
- Room 3.0.1 supports Wasm, but `WebWorkerSQLiteDriver` needs a compatible SQLite worker and OPFS/static asset setup. Until that worker is added, Web uses the shared in-memory store for the visible app state.
- Android uses Media3 for durable transfer/index state, then exports completed bytes through MediaStore (or the legacy public directory on API 23–28). Media3 separately owns playback, the media session, notification, lock-screen controls, 15-second seek increments, speed, and a 5 GB automatic LRU streaming cache. Local `file://`/`content://` downloads bypass that stream cache to avoid another copy. A settings UI for the 1/2/5/10+ GB policy presets is pending.
- iOS stores user-requested files under Application Support (the standard sandboxed durable location). Automatic cache fill is disabled until AVPlayer can consume the same transfer; starting a second full download during streaming would waste bandwidth. Files-app export is a separate product choice.
- Desktop uses the user's Downloads directory for explicit downloads. Automatic cache fill is disabled until the JavaFX player can consume a shared range-aware transfer.
- Browsers cannot silently create `Molliecaster/<podcast>` folders in the user's Downloads directory. Web therefore keeps an offline copy in origin-scoped Cache Storage and asks the browser to save a sanitized flat `Podcast - Episode.ext` public copy.
- iOS, desktop and Web provide real playback through AVPlayer, JavaFX Media and HTMLAudio respectively; non-Android lock-screen/media-key integration remains a separate milestone.
- Local playlists persist their file references on Android, iOS, and desktop. Android retains read grants for selected documents; iOS imports selected files into the app sandbox. If a retained desktop file is moved, playback surfaces the native engine error. Browser `blob:` URLs are intentionally page-session-only and must be selected again after a reload until persistent browser file-handle support is added.
- Playback progress is durable locally on Android, iOS, and desktop and uses serialized, timestamp-guarded latest-write-wins updates. Web resume/history is in-memory and is lost on reload until its SQLite Worker is wired. Cross-device/server synchronization, Wi-Fi-only policy enforcement, and network-restored/background sync require an account/authenticated progress API that is not present in this repository.
- The shared retry policy defines 30-second, 2-minute, and 10-minute delays for timeouts, connection loss, and 5xx responses only. Desktop manual downloads apply it; exact scheduler integration remains pending for Android, iOS, and Web.
- Episode IDs and remote HTTP(S) audio URLs are validated before playback/download. Checksum verification is conditional on feeds or a future API supplying a trusted checksum; current RSS models do not provide one.
- At startup, each platform reconciles its completed-download index against the authoritative local asset. Missing, externally deleted, malformed, or browser-evicted files are removed from the platform index, shared UI state, and Room download records, so the episode returns to the normal download action instead of appearing failed or downloaded.
