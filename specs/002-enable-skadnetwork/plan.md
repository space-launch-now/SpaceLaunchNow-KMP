# Implementation Plan: Enable SKAdNetwork for Conversion Tracking

**Branch**: `002-enable-skadnetwork` | **Date**: 2026-03-02 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/002-enable-skadnetwork/spec.md`

## Summary

Add the missing BidMachine SKAdNetwork identifier (`wg4vff78zm.skadnetwork`) to the iOS app's Info.plist to complete the Google-recommended set of 50 SKAdNetwork identifiers for ad conversion tracking. The Google Mobile Ads SDK prerequisite (>=7.64.0) is already satisfied (current: 12.12.0 via SPM). This is a configuration-only change to a single plist file.

## Technical Context

**Language/Version**: Swift (iOS app), Kotlin Multiplatform (shared code) — no code changes needed  
**Primary Dependencies**: Google Mobile Ads SDK >=12.12.0 (via SPM), Google UMP >=3.0.0 (via SPM)  
**Storage**: N/A  
**Testing**: Manual build verification, plist validation script  
**Target Platform**: iOS 13.0+ (SKAdNetwork supported on iOS 14+, backward-compatible)  
**Project Type**: Mobile (KMP with iOS target)  
**Performance Goals**: N/A (configuration change only)  
**Constraints**: Info.plist must be valid XML, no duplicate identifiers  
**Scale/Scope**: Single file change (1 identifier addition in plist)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Mobile-First (Android & iOS Equal Priority) | ✅ PASS | iOS-only change (SKAdNetwork is Apple framework); Android unaffected |
| II. Pattern-Based Consistency | ✅ PASS | Following existing plist pattern for SKAdNetworkItems |
| III. Accessibility & User Experience | ✅ PASS | No UI changes; configuration-only |
| IV. CI/CD & Conventional Commits | ✅ PASS | Will use `chore(ios): add missing BidMachine SKAdNetwork identifier` |
| V. Code Generation & API Management | ✅ PASS | No generated code changes |
| VI. Multiplatform Architecture | ✅ PASS | Platform-specific iOS configuration, no impact on shared code |
| VII. Testing Standards | ✅ PASS | Build verification confirms valid plist |

**Post-Phase 1 re-check**: All gates still pass. No violations detected.

## Project Structure

### Documentation (this feature)

```text
specs/002-enable-skadnetwork/
├── plan.md              # This file
├── spec.md              # Feature specification
├── research.md          # Phase 0: Research findings
├── data-model.md        # Phase 1: Data model (SKAdNetwork identifier schema)
├── quickstart.md        # Phase 1: Implementation quickstart
├── contracts/           # Phase 1: Plist contract definition
│   └── skadnetwork-identifiers.md
└── tasks.md             # Phase 2 output (NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
iosApp/
└── iosApp/
    └── Info.plist       # Single file to modify (add 1 SKAdNetworkIdentifier entry)
```

**Structure Decision**: This is an iOS-only configuration change. Only the main app's Info.plist needs modification. Widget extensions and notification service extensions do not serve ads and do not need SKAdNetworkItems.

## Complexity Tracking

> No constitution violations. No complexity tracking needed.
