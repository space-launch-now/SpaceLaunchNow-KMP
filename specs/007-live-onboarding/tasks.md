# Tasks: Live Composable Onboarding

**Input**: Design documents from `/specs/007-live-onboarding/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, quickstart.md ✅

**Tests**: Not explicitly requested — skipped. All composables include **dual previews** (light + dark) per project convention.

**Organization**: Tasks grouped by user story. Page content stories (US-2 through US-5) are ordered before the carousel assembly (US-1) because the carousel composes all page composables. Page stories are parallelizable.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2)
- All paths relative to `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/`

---

## Phase 1: Setup

**Purpose**: Rename existing onboarding, add navigation route and DataStore key

- [X] T001 Rename `ui/onboarding/OnboardingScreen.kt` → `ui/onboarding/OnboardingPaywallScreen.kt` and update all references in `App.kt`
- [X] T002 [P] Add `@Serializable data object LiveOnboarding` route to `navigation/Screen.kt`
- [X] T003 [P] Add `LIVE_ONBOARDING_COMPLETED` boolean preferences key, `liveOnboardingCompletedFlow: Flow<Boolean>`, and `setLiveOnboardingCompleted(completed: Boolean)` setter to `data/storage/AppPreferences.kt`

**Checkpoint**: Navigation route exists, DataStore key ready, old screen renamed

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared composable building blocks used by all user stories

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T004 [P] Create `DeviceFrame` composable with `DeviceFrameStyle` sealed class (`Android`/`IPhone`/`Generic`), runtime platform detection via `getPlatform()`, dark bezel (`Color(0xFF1A1A1A)`), rounded corners, platform-specific camera cutout (Android pill) / Dynamic Island (iOS), status bar with live clock (`LaunchedEffect` + `Clock.System.now()` updated every 60s), inner content slot clipped to screen area, and dual previews in `ui/onboarding/DeviceFrame.kt`
- [X] T005 [P] Create `WavyProgressBar` composable with `Canvas` + `Path` drawing: straight horizontal track line (`Color.White` alpha 0.3f), sine-wave progress fill line (primary color), accepting `currentPage`, `pageCount`, and `pageOffsetFraction` parameters, amplitude ~6dp, frequency configurable, stroke width 3dp, and dual previews in `ui/onboarding/WavyProgressBar.kt`
- [X] T006 Create `OnboardingPage` composable with centered `DeviceFrame` (~50-60% screen height), `Modifier.graphicsLayer(scaleX, scaleY)` for scaling live content, bold title text below frame, lighter subtitle text below title, content lambda for device frame inner content, `Modifier.pointerInput(Unit) {}` to disable touch on scaled content, and dual previews in `ui/onboarding/OnboardingPage.kt`

**Checkpoint**: Foundation ready — DeviceFrame, WavyProgressBar, and OnboardingPage composables compile and render in previews

---

## Phase 3: User Story 2 — Launch Card Preview Page (Priority: P1)

**Goal**: Render a live `LaunchCardHeaderOverlay` inside a device frame on onboarding page 1

**Independent Test**: Preview renders in IDE showing a SpaceX Falcon 9 launch card inside a device frame

- [X] T007 [P] [US2] Create `LaunchCardPage` composable wrapping `LaunchCardHeaderOverlay` with `PreviewData.launchNormalSpaceX` mock data inside `OnboardingPage`, with title "Track Every Launch" and subtitle describing launch tracking, and dual previews in `ui/onboarding/pages/LaunchCardPage.kt`

**Checkpoint**: LaunchCardPage renders a live launch card inside a device frame in preview

---

## Phase 4: User Story 3 — Schedule Screen Preview Page (Priority: P1)

**Goal**: Render a static schedule screen preview inside a device frame on onboarding page 2

**Independent Test**: Preview renders in IDE showing a mock schedule list with Upcoming/Previous tabs inside a device frame

- [X] T008 [P] [US3] Create `SchedulePage` with `SchedulePreviewContent` composable (static `LazyColumn` of 3-4 mock launch items using `PreviewData` launches, non-interactive Upcoming/Previous tab headers) inside `OnboardingPage`, with title "Your Launch Schedule" and subtitle describing schedule browsing, and dual previews in `ui/onboarding/pages/SchedulePage.kt`

**Checkpoint**: SchedulePage renders a mock schedule list inside a device frame in preview

---

## Phase 5: User Story 4 — Notification Filters Preview Page (Priority: P1)

**Goal**: Render notification filter UI with mock data inside a device frame on onboarding page 3

**Independent Test**: Preview renders in IDE showing mock agency checkboxes, location items, and topic toggles inside a device frame

- [X] T009 [P] [US4] Create `NotificationFiltersPage` with `NotificationFiltersPreviewContent` composable (mock agency checkboxes: SpaceX ✓, NASA ✓, Blue Origin ☐, ULA ☐; mock location items: Florida ✓, Texas ☐, California ☐; mock topic toggles: 24h Before ✓, 10min Before ✓) inside `OnboardingPage`, with title "Customize Notifications" and subtitle describing filter customization, and dual previews in `ui/onboarding/pages/NotificationFiltersPage.kt`

**Checkpoint**: NotificationFiltersPage renders mock notification filter options inside a device frame in preview

---

## Phase 6: User Story 5 — Notification Permission Request Page (Priority: P1)

**Goal**: Present a notification permission request with "Enable Notifications" button on onboarding page 4

**Independent Test**: Preview renders in IDE showing a notification-themed page with enable/skip buttons; on Android, tapping "Enable Notifications" triggers `POST_NOTIFICATIONS` permission dialog

- [X] T010 [US5] Create `NotificationPermissionPage` with notification-themed icon/illustration (no device frame), prominent "Enable Notifications" button calling `requestPlatformNotificationPermission()` via `rememberCoroutineScope`, "Maybe Later" text button to skip, callback for permission result (granted/denied/skipped), and dual previews in `ui/onboarding/pages/NotificationPermissionPage.kt`

**Checkpoint**: NotificationPermissionPage renders correctly; on Android device, tapping enable triggers system permission dialog

---

## Phase 7: User Story 1 — Live Onboarding Carousel (Priority: P1) 🎯 MVP

**Goal**: Assemble the full 4-page carousel with navigation, progress indicator, and completion handling

**Independent Test**: Run app (desktop or Android) → carousel renders 4 pages with swipe/next navigation, wavy progress bar animates, skip completes flow, last page "Get Started" completes flow

- [X] T011 [US1] Create `LiveOnboardingScreen` composable with: `HorizontalPager` (4 pages routing to `LaunchCardPage`/`SchedulePage`/`NotificationFiltersPage`/`NotificationPermissionPage`), space gradient background (reuse `spaceGradient` from existing `OnboardingPaywallScreen`), "Skip" text button top-right, `WavyProgressBar` between page content and button, full-width "Next" button (last page shows "Get Started"), `appPreferences.setLiveOnboardingCompleted(true)` on completion/skip, `onComplete: () -> Unit` callback, and dual previews in `ui/onboarding/LiveOnboardingScreen.kt`

**Checkpoint**: Full carousel works end-to-end — 4 pages swipeable, skip works, next advances, wavy progress animates, completion persists to DataStore

---

## Phase 8: User Story 6 — Onboarding Flow Sequencing (Priority: P1)

**Goal**: Wire LiveOnboarding into the app navigation so new users see it before the paywall

**Independent Test**: Fresh install → LiveOnboarding appears first → completing it → OnboardingPaywall appears → completing it → Home. Returning users skip both.

- [X] T012 [US6] Update `startDestination` logic in `App.kt` to collect `liveOnboardingCompletedFlow` from `AppPreferences` and check `liveOnboardingCompleted == false` → route to `LiveOnboarding` before checking `onboardingPaywallShown`
- [X] T013 [US6] Add `composable<Screen.LiveOnboarding>` entry to the `NavHost` in `App.kt` that renders `LiveOnboardingScreen(onComplete = { navController.navigate(Screen.Onboarding) { popUpTo(Screen.LiveOnboarding) { inclusive = true } } })` — navigating to paywall (or Home if paywall already shown)

**Checkpoint**: Full navigation sequence works — LiveOnboarding → Paywall → Home for new users; returning users go straight to Home

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Final validation and cleanup

- [X] T014 [P] Verify no remaining references to `OnboardingScreen` (pre-rename name) exist — search all `.kt` files for stale imports or usages
- [X] T015 Run quickstart.md verification checklist: LiveOnboarding route in Screen.kt ✓, LIVE_ONBOARDING_COMPLETED in AppPreferences ✓, DeviceFrame renders with live clock ✓, 4 carousel pages render ✓, Page 4 triggers notification permission on Android ✓, Skip marks onboarding complete ✓, Navigation sequence correct ✓, Dual previews for all composables ✓, Rename complete ✓

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1: Setup ──────────────────────── No dependencies
    │
Phase 2: Foundational ──────────────── Depends on Phase 1 (T001 rename)
    │
    ├── Phase 3: US-2 Launch Card ───── Depends on Phase 2 (DeviceFrame, OnboardingPage)
    ├── Phase 4: US-3 Schedule ──────── Depends on Phase 2 (can run parallel with Phase 3)
    ├── Phase 5: US-4 Filters ──────── Depends on Phase 2 (can run parallel with Phase 3-4)
    └── Phase 6: US-5 Permission ────── Depends on Phase 2 (can run parallel with Phase 3-5)
         │
Phase 7: US-1 Carousel ─────────────── Depends on Phases 3-6 (all page composables)
    │
Phase 8: US-6 Flow Sequencing ──────── Depends on Phase 7 (LiveOnboardingScreen)
    │
Phase 9: Polish ─────────────────────── Depends on Phase 8
```

### User Story Dependencies

- **US-2 (Launch Card)**: Needs DeviceFrame + OnboardingPage from Phase 2 — no other story deps
- **US-3 (Schedule)**: Needs DeviceFrame + OnboardingPage from Phase 2 — no other story deps
- **US-4 (Filters)**: Needs DeviceFrame + OnboardingPage from Phase 2 — no other story deps
- **US-5 (Permission)**: Needs Phase 2 foundation — no other story deps
- **US-1 (Carousel)**: Needs ALL page composables (US-2 through US-5) — assembles carousel
- **US-6 (Sequencing)**: Needs US-1 carousel screen — wires into App.kt

### Within Each User Story

- Page composables (US-2 through US-5) are single-file, self-contained tasks
- US-1 (Carousel): Single task assembling all pages into `HorizontalPager`
- US-6 (Sequencing): T012 (startDestination) before T013 (NavHost entry)

### Parallel Opportunities

- **Phase 1**: T002 and T003 can run in parallel (different files)
- **Phase 2**: T004 and T005 can run in parallel (different files); T006 depends on T004
- **Phases 3-6**: ALL can run in parallel (T007, T008, T009, T010 are different files with no cross-deps)
- **Phase 9**: T014 and T015 can run in parallel

---

## Parallel Example: Page Content (Phases 3-6)

```bash
# All page composables can be created simultaneously (independent files):
T007: LaunchCardPage in ui/onboarding/pages/LaunchCardPage.kt
T008: SchedulePage in ui/onboarding/pages/SchedulePage.kt
T009: NotificationFiltersPage in ui/onboarding/pages/NotificationFiltersPage.kt
T010: NotificationPermissionPage in ui/onboarding/pages/NotificationPermissionPage.kt
```

---

## Implementation Strategy

### MVP First (US-1 Carousel Only)

1. Complete Phase 1: Setup (rename, route, DataStore key)
2. Complete Phase 2: Foundational (DeviceFrame, WavyProgressBar, OnboardingPage)
3. Complete Phases 3-6: All page composables (parallel)
4. Complete Phase 7: US-1 Carousel assembly
5. **STOP and VALIDATE**: Preview all 4 pages, test swipe/next/skip
6. Complete Phase 8: US-6 navigation wiring
7. Deploy/demo with full flow

### Incremental Delivery

1. Setup + Foundational → DeviceFrame and WavyProgressBar render in previews
2. Add page composables (parallel) → Each page renders independently in preview
3. Add Carousel (US-1) → Full carousel works end-to-end in isolation
4. Add Navigation (US-6) → Complete app flow works
5. Polish → Verify rename, run checklist

### Suggested MVP Scope

The carousel (US-1) with all 4 pages is the minimum viable feature. US-6 (flow sequencing) is required for the feature to be accessible in the real app. **All phases are needed for MVP.**

---

## Notes

- [P] tasks = different files, no dependencies on incomplete tasks
- [Story] labels map tasks to user stories for traceability
- All new composables require dual previews (light + dark) using `SpaceLaunchNowPreviewTheme(isDark = true/false)`
- Use `PreviewData` for ALL mock data — no API calls during onboarding
- DeviceFrame uses runtime `getPlatform()` detection — NOT expect/actual
- Live clock uses `LaunchedEffect` + `Clock.System.now()` — update every 60 seconds
- Scaled content inside DeviceFrame uses `Modifier.graphicsLayer(scaleX, scaleY)` with touch disabled
- Space gradient background reuses the pattern from existing `OnboardingPaywallScreen`
- Commit after each task using `feat(onboarding):` prefix per conventional commits
