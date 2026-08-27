---
name: migrate-jetcaster-to-kmp
description: Migrate or restructure the Android Jetcaster sample into the Molliecaster Kotlin and Compose Multiplatform project for Android, iOS, web, and desktop. Use for Gradle topology, source-set boundaries, package migration to mammoth.mollie.caster, staged porting, Android-only dependency replacement, or cross-target build verification. Do not use for isolated RSS, playback, or product-UI implementation when their focused skills apply.
---

# Migrate Jetcaster to KMP

## Workflow

1. Inspect the requested Jetcaster flow and record observable behavior, owning files, models, dependencies, and tests.
2. Classify each dependency as common-compatible, replaceable, or platform-only. Never move Android framework types into `commonMain`.
3. Define the smallest destination slice and its shared contracts before copying code.
4. Port domain behavior first, then data/platform adapters, then shared presentation state and UI.
5. Rename packages to `mammoth.mollie.caster`; do not preserve `com.example.jetcaster` references.
6. Compile and test each affected target after every slice. Keep failures attributable and record unavailable SDKs separately.
7. Update the platform capability matrix and completion evidence.

## Guardrails

- Treat `../Jetcaster/` as read-only.
- Preserve behavior, accessibility intent, and useful Compose structure; rewrite Android-only plumbing.
- Prefer shared contracts plus platform implementations over conditional checks scattered through common code.
- Keep Android, iOS, web, and desktop targets buildable throughout migration.
- Do not port Jetcaster TV, Wear, or Glance modules unless explicitly requested.
- Do not inherit the sample's mocked player as finished functionality.

## Reference

Read [references/migration-contract.md](references/migration-contract.md) before choosing modules, source sets, or milestone gates.

