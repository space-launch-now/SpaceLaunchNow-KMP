# Tasks: Onboarding Paywall

**Input**: Design documents from `/specs/006-onboarding-paywall/`  
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, quickstart.md ✅

**Tests**: Not explicitly requested in spec — test tasks omitted. Unit tests included only as a polish task.

**Organization**: Tasks grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Paths relative to `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add the DataStore preference and navigation route that all user stories depend on

- [X] T001 [P] Add `ONBOARDING_COMPLETED` preference key, `onboardingCompletedFlow`, and `setOnboardingCompleted()` to `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/storage/AppPreferences.kt`
- [X] T002 [P] Add `@Serializable data object Onboarding` route to `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/navigation/Screen.kt`

**Checkpoint**: New preference key and navigation route available for all subsequent phases.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: None — no additional foundational infrastructure needed beyond Phase 1. All dependencies are existing (`SubscriptionViewModel`, `AppIconBox`, DataStore, NavHost).

**⚠️ Phase 1 MUST be complete before Phase 3 can begin.**

---

## Phase 3: User Story 1 — New User Sees Full-Screen Onboarding (Priority: P1) 🎯 MVP

**Goal**: First-time users land on a visually stunning full-screen onboarding screen with welcome hero, premium perks, pricing cards, and a dismiss button that navigates to Home.

**Independent Test**: Fresh install → app opens to onboarding screen → "Continue" navigates to Home → back button does NOT return to onboarding → restart → goes directly to Home.

### Implementation for User Story 1

- [X] T003 [US1] Create `OnboardingContent` stateless composable (hero section with gradient background, app icon with glow, welcome heading/subtitle, 4 perk cards with icons, pricing section header, 3 pricing cards with Annual elevated, "Continue" button, error message area) in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/onboarding/OnboardingScreen.kt`
- [X] T004 [US1] Create `OnboardingScreen` stateful composable that injects `SubscriptionViewModel` via `koinInject()`, loads products via `viewModel.getProductByType()`, calls `appPreferences.setOnboardingCompleted(true)` on dismiss, and invokes `onComplete` callback in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/onboarding/OnboardingScreen.kt`
- [X] T005 [US1] Add dual previews (`OnboardingScreenPreview` with `SpaceLaunchNowPreviewTheme()` and `OnboardingScreenDarkPreview` with `SpaceLaunchNowPreviewTheme(isDark = true)`) in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/onboarding/OnboardingScreen.kt`
- [X] T006 [US1] Register `composableWithCompositionLocal<Onboarding>` route in `NavHost` with `OnboardingScreen(onComplete = { navController.navigate(Home) { popUpTo<Onboarding> { inclusive = true } } })` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/App.kt`
- [X] T007 [US1] Set conditional `startDestination` — read `appPreferences.onboardingCompletedFlow.collectAsState(initial = true)`, set `startDestination = if (onboardingCompleted) Home else Onboarding` on the `NavHost` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/App.kt`

**Checkpoint**: New users see the onboarding screen, can dismiss to Home, and never see it again. This is the MVP — deploy and validate here.

---

## Phase 4: User Story 2 — Existing/Returning User Skips Onboarding (Priority: P2)

**Goal**: Users who have already completed onboarding or have prior `BETA_WARNING_SHOWN` flag land directly on Home. `BetaWarningDialog` is fully removed.

**Independent Test**: Existing install with `BETA_WARNING_SHOWN=true` → update app → launches directly to Home (migration runs). Subscribed user → launches to Home. `BetaWarningDialog.kt` no longer exists.

### Implementation for User Story 2

- [X] T008 [US2] Add one-time `LaunchedEffect(Unit)` migration in `App.kt` — if `appPreferences.isBetaWarningShown()` is true, call `appPreferences.setOnboardingCompleted(true)` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/App.kt`
- [X] T009 [US2] Remove the `BetaWarningDialog()` composable call and its import from `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/App.kt`
- [X] T010 [US2] Delete file `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/compose/BetaWarningDialog.kt`

**Checkpoint**: Existing users skip onboarding, BetaWarningDialog fully removed from codebase.

---

## Phase 5: User Story 3 — User Can Purchase from Onboarding (Priority: P3)

**Goal**: Users can tap a pricing card on the onboarding screen to initiate a purchase flow directly, and on success are navigated to Home with their subscription active.

**Independent Test**: On onboarding screen → tap Annual pricing card → purchase flow triggers via `SubscriptionViewModel.purchaseProduct()` → on success, onboarding marked complete and navigated to Home → on failure, inline error shown, user stays on screen.

### Implementation for User Story 3

- [X] T011 [US3] Wire pricing card `onSubscribe` callback in `OnboardingScreen` to call `viewModel.purchaseProduct(productInfo)`, handle success (set onboarding complete + navigate Home) and failure (show error message) in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/onboarding/OnboardingScreen.kt`

**Checkpoint**: Full purchase flow works from onboarding. All three user stories are complete.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final validation, cleanup, and testing across all stories

- [ ] T012 [P] Verify compilation with `./gradlew compileKotlinDesktop`
- [ ] T013 [P] Run quickstart.md verification checklist (10 items) from `specs/006-onboarding-paywall/quickstart.md`
- [ ] T014 [P] Create unit tests for conditional routing logic and onboarding completion in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/onboarding/OnboardingScreenTest.kt`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **User Story 1 (Phase 3)**: Depends on Phase 1 completion — BLOCKED until T001 and T002 are done
- **User Story 2 (Phase 4)**: Depends on Phase 1 (T001 for migration setter) — can run in parallel with Phase 3
- **User Story 3 (Phase 5)**: Depends on Phase 3 (T003/T004 must exist to wire purchase callback)
- **Polish (Phase 6)**: Depends on all user stories being complete

### User Story Dependencies

- **US1 (Phase 3)**: Core screen — no dependency on other stories
- **US2 (Phase 4)**: Migration + cleanup — independent of US1 screen implementation, only needs Phase 1
- **US3 (Phase 5)**: Purchase wiring — depends on US1 (the screen must exist to add purchase flow)

### Within Each User Story

```
Phase 1: T001 ──┐
          T002 ──┤ (parallel — different files)
                 │
Phase 3:         ├── T003 (stateless composable) ── T004 (stateful wrapper) ── T005 (previews)
                 │                                                               │
                 │                                                    T006 (route) ── T007 (startDest)
                 │
Phase 4:         ├── T008 (migration) ── T009 (remove dialog call) ── T010 (delete file)
                 │
Phase 5:         └── T011 (purchase wiring — after T004)
                 
Phase 6: T012 ──┐
          T013 ──┤ (all parallel)
          T014 ──┘
```

### Parallel Opportunities

- **Phase 1**: T001 and T002 can run in parallel (different files)
- **Phase 3 + Phase 4**: US1 and US2 can proceed in parallel after Phase 1 (different files, independent concerns)
- **Phase 6**: All polish tasks can run in parallel

---

## Parallel Example: Phase 1

```bash
# Launch both setup tasks together:
Task T001: "Add ONBOARDING_COMPLETED to AppPreferences.kt"
Task T002: "Add Onboarding route to Screen.kt"
```

## Parallel Example: Phase 3 + Phase 4

```bash
# After Phase 1, can work on US1 and US2 in parallel:
# Developer A (US1): T003 → T004 → T005 → T006 → T007
# Developer B (US2): T008 → T009 → T010
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001 + T002)
2. Complete Phase 3: User Story 1 (T003–T007)
3. **STOP and VALIDATE**: Fresh install shows onboarding, dismiss works, doesn't re-appear
4. Deploy/demo if ready — existing users won't be affected yet (migration not wired)

### Incremental Delivery

1. Phase 1 → Setup ready
2. Phase 3 (US1) → New users see onboarding → **MVP!**
3. Phase 4 (US2) → Existing users safely migrated, BetaWarningDialog removed
4. Phase 5 (US3) → Purchase flow wired from onboarding screen
5. Phase 6 → Compile verification, tests, checklist validation

### Recommended Order (Single Developer)

1. T001 + T002 (Phase 1 — parallel, ~5 min)
2. T003 → T004 → T005 (Phase 3 — OnboardingScreen creation, bulk of work)
3. T006 → T007 (Phase 3 — App.kt wiring)
4. T008 → T009 → T010 (Phase 4 — migration + cleanup)
5. T011 (Phase 5 — purchase wiring)
6. T012 + T013 + T014 (Phase 6 — validation)

---

## Notes

- All source files are in `commonMain` — no platform-specific code needed
- Reuse patterns from `SupportUsScreen.kt` (PremiumPerkCard, PricingCard, AppIconBox) but define locally since originals are `private`
- `collectAsState(initial = true)` for `onboardingCompletedFlow` defaults to Home while DataStore loads — prevents flash for existing users
- Commit convention: `feat(ui): add onboarding paywall screen` for US1, `refactor(ui): remove BetaWarningDialog` for US2
- The onboarding screen must be visually stunning — gradient backgrounds, entrance animations, premium typography (see spec.md NFR: Visual Design Excellence)
