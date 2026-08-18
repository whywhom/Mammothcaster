# Podcast data contract

## Core records

- Podcast: stable ID, feed URL, canonical URL if known, title, author, description, artwork URL, categories, episode count, last update, subscription state.
- Episode: stable ID, podcast ID, GUID if present, media URL, title, summary/description, publication time, duration, artwork URL if present.
- Favorite: episode ID and created time.
- Playback history: episode ID, last played time, position, duration snapshot, completed flag.
- Download: episode ID, state, local reference, received bytes, total bytes if known, failure reason, updated time.

## Identity fallback

Prefer explicit feed GUIDs. When absent or unstable, derive an internal identity from normalized feed identity plus media URL, then durable item URL, then a carefully normalized title/publication tuple. Never use list position.

## Categories

Seed at least Technology, Business, Artificial Intelligence, Health, News, Comedy, Education, Science, Society & Culture, Arts, Sports, History, Music, True Crime, Kids & Family, and Government. Preserve feed-provided category text and map aliases to display categories without destroying the source value.

## OPML acceptance

- Import nested outlines and ignore non-feed grouping nodes.
- Normalize and de-duplicate feed URLs.
- Report imported, duplicate, and failed counts with per-feed failure detail.
- Export subscribed podcasts only unless the caller explicitly requests another set.
- Round-trip Unicode titles, ampersands, quotes, and non-ASCII URLs.

