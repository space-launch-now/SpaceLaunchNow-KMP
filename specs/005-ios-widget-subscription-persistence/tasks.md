# Tasks: iOS Widget Subscription Persistence

**Feature**: 005-ios-widget-subscription-persistence  
**Date**: 2026-03-05  
**Input**: Design documents from `/specs/005-ios-widget-subscription-persistence/`  
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, quickstart.md ✅

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Exact file paths included in descriptions

## User Stories (from spec.md)

| Story | Priority | Description |
|-------|----------|-------------|
| US1 | P1 | Enhanced Cache Infrastructure (FR-1, FR-3) |
| US2 | P2 | Fail-Safe Widget Verification (FR-2, FR-5) |
| US3 | P3 | Subscription Expiry Tracking (FR-4) |

---

## Phase 1: Setup

**Purpose**: No additional setup needed - project structure already exists

*This feature builds on existing widget infrastructure. No new project setup required.*

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core data structures that all user stories depend on

**⚠️ CRITICAL**: These tasks must complete before user story implementation

- [X] T001 Create `WidgetAccessCache.kt` data class in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/widgets/WidgetAccessCache.kt`
- [X] T002 Add `subscriptionExpiryMs: Long? = null` field to `LocalSubscriptionData` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/subscription/LocalSubscriptionStorage.kt`
- [X] T003 [P] Add `syncWidgetAccessCache(cache: WidgetAccessCache)` method signature to `WidgetAccessSharer` expect class in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/widgets/WidgetAccessSharer.kt`

**Checkpoint**: Foundation ready - core data structures defined

---

## Phase 3: User Story 1 - Enhanced Cache Infrastructure (Priority: P1) 🎯 MVP

**Goal**: Write comprehensive subscription state to App Group storage so widgets can determine access without main app

**Independent Test**: After implementation, check NSUserDefaults for `widget_subscription_expiry`, `widget_was_ever_premium`, `widget_last_verified` keys

### Implementation for User Story 1

- [X] T004 [US1] Implement `syncWidgetAccessCache()` in iOS actual class `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/widgets/WidgetAccessSharer.ios.kt`
- [X] T005 [US1] Add `wasEverPremium: Boolean` tracking to `SimpleSubscriptionRepository` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/SimpleSubscriptionRepository.kt`
- [X] T006 [US1] Update state.onEach in `SimpleSubscriptionRepository` to build and sync `WidgetAccessCache` with all fields
- [X] T007 [P] [US1] Add no-op implementation of `syncWidgetAccessCache()` to desktop actual class `composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/widgets/WidgetAccessSharer.desktop.kt`
- [X] T008 [P] [US1] Add no-op implementation of `syncWidgetAccessCache()` to Android actual class `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/widgets/WidgetAccessSharer.android.kt`

**Checkpoint**: Enhanced cache is being written to NSUserDefaults with expiry, wasEverPremium, and lastVerified fields

---

## Phase 4: User Story 2 - Fail-Safe Widget Verification (Priority: P2)

**Goal**: Widget reads enhanced cache and uses fail-safe logic that defaults to unlocked for users who were ever premium

**Independent Test**: Force close app after subscribing → widget should remain unlocked

### Implementation for User Story 2

- [X] T009 [US2] Create `WidgetAccessState.swift` with fail-safe `shouldShowUnlocked` logic in `iosApp/LaunchWidget/WidgetAccessState.swift`
- [X] T010 [US2] Update `fetchLaunches()` in `LaunchData.swift` to use `WidgetAccessState.readFromCache().shouldShowUnlocked` instead of direct NSUserDefaults read in `iosApp/LaunchWidget/LaunchData.swift`
- [X] T011 [US2] Remove Koin initialization from widget provider in `LaunchData.swift` when checking access (keep for launch fetching)
- [X] T012 [US2] Add logging for access state decisions in `WidgetAccessState.swift` for debugging

**Checkpoint**: Widget uses fail-safe access check that prevents false locks for paid users

---

## Phase 5: User Story 3 - Subscription Expiry Tracking (Priority: P3)

**Goal**: Extract and propagate subscription expiry date from RevenueCat through the data flow

**Independent Test**: Subscribe, check NSUserDefaults `widget_subscription_expiry` contains future timestamp; let expire, widget locks

### Implementation for User Story 3

- [X] T013 [US3] Extract subscription expiry from `EntitlementInfo.expirationDateMillis` in `IosBillingManager.updatePurchaseState()` in `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/data/billing/IosBillingManager.kt`
- [X] T014 [US3] Add `subscriptionExpiryMs: Long? = null` field to `PurchaseState` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/PurchaseState.kt`
- [X] T015 [US3] Propagate expiry through `SubscriptionSyncer` when building `LocalSubscriptionData` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/subscription/SubscriptionSyncer.kt`
- [X] T016 [US3] Update `WidgetAccessCache` construction in `SimpleSubscriptionRepository` to include expiry from local storage

**Checkpoint**: Full expiry tracking from RevenueCat → LocalStorage → WidgetAccessCache → NSUserDefaults → Swift widget

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Testing, documentation, and cleanup

- [X] T017 [P] Update `IOS_WIDGET_PREMIUM_GATING.md` documentation with new cache fields in `docs/premium/IOS_WIDGET_PREMIUM_GATING.md`
- [ ] T018 Manual test: Premium user force closes app → widget stays unlocked
- [ ] T019 Manual test: Premium subscription expires → widget locks after expiry date
- [ ] T020 Manual test: Lifetime user → widget always unlocked (no expiry)
- [ ] T021 Manual test: New free user → widget shows locked state
- [ ] T022 Manual test: Device restart → widget stays unlocked for premium users
- [ ] T023 Code review for edge cases in fail-safe logic
- [ ] T024 Run quickstart.md validation checklist

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1 (Setup): None - skipped (no setup needed)
    │
    ▼
Phase 2 (Foundational): T001, T002, T003
    │
    ▼ (all foundational tasks must complete)
    │
    ├──────────────────┬──────────────────┐
    ▼                  ▼                  ▼
Phase 3 (US1)     Phase 4 (US2)     Phase 5 (US3)
T004-T008         T009-T012         T013-T016
    │                  │                  │
    └──────────────────┴──────────────────┘
                       │
                       ▼
              Phase 6 (Polish)
              T017-T024
```

### User Story Dependencies

- **User Story 1 (P1)**: Depends on Foundational (T001-T003) - Can start after T001 completes
- **User Story 2 (P2)**: Depends on Foundational (T001) - Needs `WidgetAccessCache` data class definition
- **User Story 3 (P3)**: Depends on Foundational (T002) - Needs expiry field in `LocalSubscriptionData`

### Within Each User Story

1. T001 (WidgetAccessCache) must complete before T004-T006, T009
2. T002 (expiry field) must complete before T013-T016
3. T004 must complete before T005-T006 (iOS actual needed for sync calls)
4. T009 must complete before T010-T012 (WidgetAccessState needed for LaunchData)

### Parallel Opportunities

**Foundational Phase:**
- T001, T002 can run in parallel (different files)
- T003 depends on T001 (needs WidgetAccessCache type)

**User Story Implementation:**
- T007, T008 can run in parallel (no-op implementations)
- US1, US2, US3 can proceed in parallel once dependencies met
- T017 can run in parallel with manual testing (T018-T022)

---

## Parallel Example: Multiple User Stories

```bash
# Developer 1: US1 (Kotlin cache writing)
T004 → T005 → T006

# Developer 2: US2 (Swift cache reading) - can start after T001
T009 → T010 → T011 → T012

# Developer 3: US3 (Expiry data flow) - can start after T002
T013 → T014 → T015 → T016
```

---

## Implementation Strategy

### MVP Scope (Minimum Viable Fix)

**Phases 2 + 3 (Foundational + US1)** deliver the core fix:
- Enhanced cache with `wasEverPremium` flag
- Paid users will remain unlocked after force close

### Full Solution

**All Phases (2-6)** provide complete protection:
- Fail-safe logic in widget (US2)
- Expiry-based validation (US3)
- Comprehensive testing

### Suggested Order

1. **Start with**: T001 (WidgetAccessCache) - unblocks most work
2. **Then parallel**: T002 + T004 (expiry field + iOS cache writing)
3. **Then**: T005-T006 (repository integration)
4. **Then**: T009-T012 (Swift widget updates)
5. **Finally**: T013-T016 (expiry data flow) + T017-T024 (polish)

---

## Task Summary

| Phase | Task Count | Parallel Tasks | Estimated Time |
|-------|------------|----------------|----------------|
| Foundational | 3 | 2 | 30 min |
| US1 (Cache Writing) | 5 | 2 | 1.5 hours |
| US2 (Fail-Safe Logic) | 4 | 0 | 1.5 hours |
| US3 (Expiry Tracking) | 4 | 0 | 1 hour |
| Polish | 8 | 1 | 1.5 hours |
| **Total** | **24** | **5** | **~6 hours** |
