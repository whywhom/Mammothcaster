# Molliecaster working agreement

## Scope

- Treat `../Jetcaster/` as the read-only source project and `./` as the Kotlin Multiplatform destination.
- Use application/package namespace `mammoth.mollie.caster`.
- Target Android, iOS, web, and desktop with Kotlin and Compose Multiplatform.
- Preserve useful Jetcaster product behavior and visual ideas, but replace Android-only architecture where it blocks shared code.
- Do not add TV, Wear, or widget targets unless the user expands scope.

## Collaboration

- For migration work, delegate bounded independent work to the custom agents in `.codex/agents/`.
- Use `source_mapper` before porting an unfamiliar Jetcaster flow.
- Let only one implementation agent own a file or module at a time; parallelize read-heavy analysis and non-overlapping modules.
- Use the matching repository skill in `.agents/skills/` for migration, data, playback, or UI work.
- Keep the primary agent responsible for architecture decisions, integration, and final verification.

## Engineering rules

- Keep domain models, use cases, repository contracts, navigation contracts, and shared UI in `commonMain` when platform APIs are not required.
- Hide platform media, notification, background execution, filesystem, and sharing behavior behind explicit interfaces with platform implementations.
- Use Media3 for Android playback, media sessions, notification/lock-screen control, and Android downloads. Do not pretend Media3 runs on iOS, web, or desktop.
- Use Ktor for network access, Room KMP for durable user and catalog data, and a KMP-compatible Coil version for images.
- Model subscriptions, favorites, downloads, and playback history as separate user-owned state.
- Preserve playback position durably and update history without blocking the player loop.
- Treat OPML import as idempotent and export as deterministic.

## Verification

- Build or test every target affected by a change; report unavailable toolchains separately from code failures.
- Add common tests for RSS mapping, persistence/repository behavior, sorting, history, search, and OPML parsing/serialization.
- Add platform tests around playback adapters and lifecycle behavior where feasible.
- Do not call a cross-platform feature complete when it is only wired on Android; use an explicit platform capability matrix.

