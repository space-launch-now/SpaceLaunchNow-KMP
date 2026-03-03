# Implementation Plan: Fix iOS Widget Image/Launch Mismatch on Refresh

**Branch**: `003-fix-widget-image-mismatch` | **Date**: 2026-03-02 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/003-fix-widget-image-mismatch/spec.md`

## Summary

The iOS widget displays the wrong image for launches after refresh due to a cache key collision bug. The `cacheFile(for:)` function in `LaunchData.swift` uses base64 encoding truncated to 64 characters as a cache key. Since all launch image URLs share the same CDN domain prefix, the first 64 base64 characters are identical for every URL — causing all images to map to the same cache file. The fix replaces the base64 + truncation approach with a SHA-256 hash (via `CryptoKit`) that produces a unique, collision-resistant key for each URL.

## Technical Context

**Language/Version**: Swift 5.9+ (iOS widget extension)
**Primary Dependencies**: `CryptoKit` (Apple system framework, iOS 13+), `WidgetKit`, `SwiftUI`
**Storage**: Disk-based image cache in app group container (`group.me.calebjones.spacelaunchnow/widget_image_cache/`)
**Testing**: Manual verification on device/simulator, cache key unit test
**Target Platform**: iOS 13.0+ (widget requires iOS 14+, but LaunchData targets iOS 13+)
**Project Type**: Mobile (KMP with iOS target — this fix is iOS-only)
**Performance Goals**: N/A (single function change; SHA-256 is faster than base64 + string operations)
**Constraints**: Widget extension memory limits (~30MB); must not increase cache file count beyond current behavior
**Scale/Scope**: Single file change — 1 function rewrite + 1 import addition in `LaunchData.swift`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Mobile-First (Android & iOS Equal Priority) | ✅ PASS | iOS-only bug in iOS-only widget code; Android has separate widget implementation |
| II. Pattern-Based Consistency | ✅ PASS | Fix follows existing code patterns; no new patterns introduced |
| III. Accessibility & User Experience | ✅ PASS | No UI changes; fixes incorrect content (wrong image is an accessibility issue) |
| IV. CI/CD & Conventional Commits | ✅ PASS | Commit: `fix(ios): use SHA-256 hash for widget image cache keys to prevent collisions` |
| V. Code Generation & API Management | ✅ PASS | No generated code changes |
| VI. Multiplatform Architecture | ✅ PASS | Platform-specific fix in iOS widget extension; no impact on shared KMP code |
| VII. Testing Standards | ✅ PASS | Manual verification + cache key collision test |

**Post-Phase 1 re-check**: All gates still pass. No violations.

## Project Structure

### Documentation (this feature)

```text
specs/003-fix-widget-image-mismatch/
├── plan.md              # This file
├── spec.md              # Feature specification
├── research.md          # Phase 0: Root cause analysis & research
├── data-model.md        # Phase 1: Cache key data model
├── quickstart.md        # Phase 1: Step-by-step fix guide
├── contracts/           # Phase 1: Cache contract definition
│   └── image-cache-contract.md
└── tasks.md             # Phase 2 output (NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
iosApp/
└── LaunchWidget/
    └── LaunchData.swift  # Single file to modify (fix cacheFile function + add CryptoKit import)
```

**Structure Decision**: This is a single-file bug fix in the iOS widget extension. Only `LaunchData.swift` needs modification — the `cacheFile(for:)` function (5 lines) and an `import CryptoKit` statement.

## Complexity Tracking

> No constitution violations. No complexity tracking needed.
