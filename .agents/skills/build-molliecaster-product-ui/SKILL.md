---
name: build-molliecaster-product-ui
description: Build or review Molliecaster Compose Multiplatform product screens and presentation state for Android, iOS, web, and desktop. Use for home recommendations, popular/latest podcasts, categories, podcast/author search, podcast detail, episode sorting, player controls, subscriptions, favorites, history, downloads, OPML entry points, adaptive layouts, accessibility, or visual state handling. Do not use for media engine or RSS/database internals.
---

# Build Molliecaster Product UI

## Workflow

1. Define screen state and user events from repository/player contracts before composing layout.
2. Build shared composables with loading, content, empty, and recoverable error states.
3. Adapt navigation and panes for compact, medium, and expanded windows without forking business logic.
4. Keep the mini-player persistent where appropriate and make the full player reachable from episode actions and current playback.
5. Verify keyboard, mouse, touch, focus, back behavior, semantics, dynamic text, long descriptions, and missing artwork.
6. Test reducers/presentation state in common tests and key user flows in available target UI tests.

## Product surface

- Home: recommended content, popular podcasts, latest podcasts, and category recommendations.
- Categories: Technology, Business, Artificial Intelligence, Health, News, Comedy, Education, plus the extended catalog reference.
- Search: podcast title and author, with recent/empty/error handling.
- Podcast detail: artwork, title, author, description, episode count, latest update, subscription action, and episode list.
- Episode list: title, publish time, duration, summary, newest-first and oldest-first.
- Player: play/pause, 15-second skips, scrubber, required speeds, sleep timer, and platform control status.
- Library: subscriptions, favorite episodes, history/progress, downloads, OPML import/export.

## Guardrails

- Do not call repositories or platform engines directly from composables.
- Do not show controls that the current platform declares unsupported without an explanatory disabled state.
- Preserve media-app readability: artwork hierarchy, strong current-playback affordance, and high-contrast controls.
- Do not reuse sample-only fake data in production flows.

## Reference

Read [references/screen-contract.md](references/screen-contract.md) before changing navigation or claiming screen coverage.

