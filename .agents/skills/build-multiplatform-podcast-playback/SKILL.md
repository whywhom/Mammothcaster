---
name: build-multiplatform-podcast-playback
description: Build or review Molliecaster audio playback and episode download behavior across Android, iOS, web, and desktop. Use for shared player state/contracts, Android Media3 and MediaSession, play/pause/seek/15-second skips, playback speeds, sleep timer, background or lock-screen controls, progress persistence, downloads, or platform capability mapping. Do not use for RSS parsing or general screen construction.
---

# Build Multiplatform Podcast Playback

## Workflow

1. Define a common player contract and immutable state without platform types.
2. Model loading, ready, playing, paused, ended, and failed transitions explicitly.
3. Implement Android with Media3, MediaSession, background service, notification/lock-screen controls, audio focus, and noisy-output handling.
4. Implement platform adapters for iOS, web, and desktop with native capabilities; expose unsupported capabilities explicitly.
5. Persist position at bounded intervals and on pause, item change, backgrounding, and termination opportunities.
6. Implement downloads as a separate state machine and resolve local media before remote media.
7. Test contract semantics independently from engines, then test platform lifecycle integration.

## Required behavior

- Play, pause, seek, skip forward 15 seconds, skip backward 15 seconds.
- Speeds: 0.8x, 1x, 1.25x, 1.5x, and 2x.
- Sleep timer: 15, 30, and 60 minutes, with cancellation and visible remaining state.
- Resume from durable progress without seeking beyond known duration.
- Android background playback, MediaStyle notification, lock-screen controls, and media-button handling.
- Download, cancel or fail safely, delete, list, and play local media.

## Reference

Read [references/platform-playback-contract.md](references/platform-playback-contract.md) before selecting engines or declaring feature parity.

