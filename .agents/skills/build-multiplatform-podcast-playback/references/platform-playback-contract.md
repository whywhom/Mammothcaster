# Platform playback contract

## Shared surface

Expose current episode, playback state, position, buffered position, duration, speed, error, sleep timer, and platform capabilities. Commands include load, play, pause, seek, skip, set speed, set/cancel timer, and release.

## Platform truth

- Android: Media3 is mandatory for the player, media session, background service, notification/lock-screen controls, and preferably download integration.
- iOS: use a native audio/session implementation behind the shared contract; integrate now-playing and remote commands when in scope.
- Web: use browser media APIs; background and lock-screen behavior depends on browser Media Session support and must be capability-driven.
- Desktop: use an appropriate JVM/native audio implementation; OS media-key integration is separate from basic playback.

Media3 is not a multiplatform engine. Never add Media3 dependencies to `commonMain`, `iosMain`, `webMain`, or `desktopMain`.

## Progress and history

- Throttle writes during active playback; always write important lifecycle transitions.
- Ignore tiny accidental starts when deciding whether to create history, using an explicit product threshold.
- Mark complete from a documented remaining-time or percentage rule and allow replay from the beginning.
- Keep engine callbacks off UI assumptions and serialize conflicting commands.

## Downloads

- Use stable episode identity for download ownership.
- Keep database state and actual files reconcilable after crashes.
- Delete only the selected episode asset and clear its record transactionally when possible.
- Surface queued, downloading, paused/cancelled, completed, failed, and missing-file states.

