---
name: build-podcast-data-layer
description: Build or review Molliecaster podcast data flows using Ktor, portable RSS parsing, Room KMP, and repository/use-case boundaries. Use for RSS subscriptions, podcast and episode storage, favorites, history, playback progress, categories, title/author search, refresh policy, or OPML import/export. Do not use for media engine implementation or Compose screen layout.
---

# Build Podcast Data Layer

## Workflow

1. Define stable podcast and episode identities before schema or synchronization work.
2. Keep network DTOs, database entities, and domain models distinct; map explicitly.
3. Fetch feeds with Ktor using timeouts, redirect handling, conditional requests when available, and actionable errors.
4. Parse RSS 2.0 plus common Atom and iTunes podcast fields defensively. Preserve feed and enclosure URLs.
5. Upsert feed metadata and episodes transactionally without deleting user-owned state.
6. Expose repositories as flows and suspend commands; keep UI state derived from domain contracts.
7. Add deterministic fixtures for malformed dates, missing duration, duplicate GUIDs, namespaces, redirects, and reordered items.

## User data invariants

- Subscriptions belong to podcasts; favorites belong to episodes.
- Downloads, favorites, subscriptions, and playback history remain independent.
- History records last-played time and resumable position; completed state follows an explicit threshold.
- Search matches normalized podcast title and author without requiring a network request for the local catalog.
- Episode sorting supports newest-first and oldest-first with stable tie-breaking.
- OPML imports are idempotent and tolerate the common `xmlUrl`, `htmlUrl`, `title`, and `text` variants.
- OPML export has stable ordering and valid XML escaping.

## Reference

Read [references/podcast-data-contract.md](references/podcast-data-contract.md) before changing entities, repositories, sync, search, or OPML behavior.

