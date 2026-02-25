# Implementation Plan: V5 Client-Side Notification System

**Branch**: `dev` | **Date**: 2026-01-26 | **Spec**: [specs/dev/spec.md](spec.md)
**Input**: Feature specification from `/specs/dev/spec.md`

## Summary

Upgrade the notification system from V4 to V5 with platform-specific topic handling, extended filtering capabilities, and improved notification construction. Android receives data-only FCM messages and constructs notifications client-side. iOS intercepts `mutable-content` notifications via Notification Service Extension for client-side filtering before display.

## Technical Context

**Language/Version**: Kotlin 2.0.21, Swift 5.9, Java 21  
**Primary Dependencies**: Firebase Messaging (Android), Firebase iOS SDK, Compose Multiplatform, Koin DI, WorkManager  
**Storage**: DataStore (Android), UserDefaults + App Groups (iOS), SQLite (notification history)  
**Testing**: JUnit5 + kotlinx-coroutines-test (Android), XCTest (iOS)  
**Target Platform**: Android 8.0+ (API 26), iOS 15+, Desktop (no-op)  
**Project Type**: Mobile (Kotlin Multiplatform - Android, iOS, Desktop)  
**Performance Goals**: Notification display < 500ms, filter evaluation < 10ms  
**Constraints**: Must work offline for filtering, battery efficient, backward compatible with V4 during rollout  
**Scale/Scope**: ~50k active users, 100-500 notifications/day/user depending on preferences

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Pre-Phase 0 Check (PASSED)

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Mobile-First (Android & iOS Equal Priority) | ✅ PASS | Both platforms implemented with full parity |
| II. Pattern-Based Consistency | ✅ PASS | Uses existing `NotificationData`, `NotificationFilter`, `NotificationDisplayHelper` patterns |
| III. Accessibility & User Experience | ✅ PASS | Settings UI with proper accessibility, notification content properly formatted |
| IV. CI/CD & Conventional Commits | ✅ PASS | Feature branch, conventional commits required |
| V. Code Generation & API Management | ✅ N/A | No API spec changes needed |
| VI. Multiplatform Architecture | ✅ PASS | Common code in `commonMain`, platform-specific in `androidMain`/`iosMain` |
| VII. Testing Standards | ✅ PASS | Unit tests for filter logic, integration tests for WorkManager |

### Post-Phase 1 Check (PASSED)

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Mobile-First | ✅ PASS | Data model supports both Android & iOS; NSE design accounts for iOS specifics |
| II. Pattern-Based | ✅ PASS | V5NotificationPayload follows existing NotificationData pattern; V5NotificationFilter mirrors NotificationFilter |
| III. Accessibility | ✅ PASS | Filter UI design follows existing NotificationSettingsScreen patterns |
| IV. CI/CD | ✅ PASS | No blocking changes; conventional commits enforced |
| V. Code Gen | ✅ N/A | No OpenAPI changes |
| VI. Multiplatform | ✅ PASS | Common filter logic in commonMain; platform-specific in androidMain/iosMain |
| VII. Testing | ✅ PASS | Test files defined for filter logic and WorkManager integration |


## Project Structure

### Documentation (this feature)

```text
specs/dev/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
└── tasks.md             # Phase 2 output (NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
composeApp/src/
├── commonMain/kotlin/me/calebjones/spacelaunchnow/
│   ├── data/
│   │   ├── model/
│   │   │   ├── NotificationData.kt          # UPDATE: Add V5 fields
│   │   │   ├── NotificationState.kt         # UPDATE: Add V5 filter categories
│   │   │   └── V5NotificationPayload.kt     # NEW: V5-specific payload model
│   │   ├── repository/
│   │   │   ├── NotificationRepositoryImpl.kt # UPDATE: V5 topic management
│   │   │   └── SubscriptionProcessor.kt     # UPDATE: V5 topic calculation
│   │   └── storage/
│   │       └── NotificationStateStorage.kt  # UPDATE: V5 preference persistence
│   └── ui/settings/
│       └── NotificationSettingsScreen.kt    # UPDATE: V5 filter UI
│
├── androidMain/kotlin/me/calebjones/spacelaunchnow/
│   ├── services/
│   │   └── SpaceLaunchFirebaseMessagingService.kt  # UPDATE: V5 handling
│   ├── workers/
│   │   └── NotificationWorker.kt            # UPDATE: V5 payload parsing
│   └── data/notifications/
│       ├── NotificationDisplayHelper.kt     # UPDATE: V5 notification construction
│       └── V5MigrationHelper.kt             # NEW: V4→V5 migration
│
├── iosMain/kotlin/me/calebjones/spacelaunchnow/
│   └── data/notifications/
│       └── IosPushMessaging.kt              # UPDATE: V5 topic management

iosApp/
├── iosApp/
│   ├── AppDelegate.swift                    # UPDATE: V5 topic subscription
│   └── NotificationData.swift               # UPDATE: V5 payload model (Swift)
├── NotificationServiceExtension/            # NEW: NSE target
│   ├── NotificationService.swift            # NEW: V5 filtering extension
│   └── Info.plist                           # NEW: NSE configuration
└── iosApp.xcodeproj/
    └── project.pbxproj                      # UPDATE: Add NSE target

composeApp/src/
├── commonTest/
│   └── NotificationFilterV5Test.kt          # NEW: V5 filter unit tests
└── androidTest/
    └── NotificationWorkerV5Test.kt          # NEW: V5 worker tests
```

**Structure Decision**: Using existing KMP structure with platform-specific implementations. iOS requires new Notification Service Extension target for `mutable-content` interception.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| iOS Notification Service Extension | Required for `mutable-content` interception on iOS | APNs with mutable-content requires NSE to modify/filter before display; no alternative exists |
| App Groups (iOS) | Required for NSE to access user preferences | NSE runs in separate process, cannot access main app's UserDefaults without App Groups |
