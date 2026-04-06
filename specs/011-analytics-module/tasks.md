# Tasks: Analytics Module (Multi-Provider Plugin Architecture)

**Input**: Design documents from `/specs/011-analytics-module/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅, quickstart.md ✅

**Tests**: Included — the spec requires testing per Constitution Principle VII (Testing Standards).

**Organization**: Tasks are grouped by functional requirement (mapped to user stories) to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## User Story Mapping

| Story | Spec FR | Description | Priority |
|-------|---------|-------------|----------|
| US1 | FR-1 + FR-3 | Core Analytics Interface & Manager | P1 (MVP) |
| US2 | FR-2 | Event Taxonomy (sealed class hierarchy) | P1 (MVP) |
| US3 | FR-4a | Console Analytics Provider (debug/desktop) | P1 (MVP) |
| US4 | FR-4b | Firebase Analytics Provider (Android + iOS) | P2 |
| US5 | FR-5 | Navigation Screen Tracking | P2 |
| US6 | FR-7 | Consent Management (per-provider toggle) | P3 |
| US7 | FR-6 | Koin Integration & Full Wiring | P3 |

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Gradle dependency setup, package structure creation

- [X] T001 Add `gitlive-firebase-analytics` library entry to `gradle/libs.versions.toml` under `[libraries]` section using existing `gitlive-firebase` version ref
- [X] T002 Add `gitlive-firebase-analytics` implementation dependency to `commonMain` dependencies block in `composeApp/build.gradle.kts`
- [X] T003 Create analytics package directory structure under `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/core/`, `analytics/events/`, `analytics/navigation/`, `analytics/providers/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core interfaces that ALL providers and the manager depend on. MUST complete before any user story.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T004 Create `AnalyticsProvider` interface in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/core/AnalyticsProvider.kt` per Contract 1 (interface with `name`, `isEnabled`, `initialize`, `trackEvent`, `trackScreenView`, `setUserId`, `setUserProperty`, `flush`, `reset`)
- [X] T005 Create `AnalyticsManager` interface in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/core/AnalyticsManager.kt` per Contract 3 (interface with `track`, `trackScreenView`, `setUserId`, `setUserProperty`, `flush`, `reset`, `enableProvider`)

**Checkpoint**: Core interfaces defined — provider and manager implementations can now proceed.

---

## Phase 3: User Story 1 — Core Analytics Manager (Priority: P1) 🎯 MVP

**Goal**: Implement the `AnalyticsManagerImpl` fan-out dispatcher that receives events and dispatches to all enabled providers using SupervisorJob coroutine scope.

**Independent Test**: Verify that `track(event)` calls `trackEvent()` on all enabled providers, skips disabled providers, and survives a provider throwing an exception.

### Tests for User Story 1

- [X] T006 [P] [US1] Create `FakeAnalyticsProvider` test double in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/analytics/FakeAnalyticsProvider.kt` (records all calls, configurable `isEnabled`, can throw on demand)
- [X] T007 [P] [US1] Create `AnalyticsManagerImplTest` in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/analytics/AnalyticsManagerImplTest.kt` with test cases: dispatches to all enabled providers, skips disabled providers, survives provider exception, setUserId propagates, reset propagates, enableProvider toggles correct provider

### Implementation for User Story 1

- [X] T008 [US1] Implement `AnalyticsManagerImpl` class in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/core/AnalyticsManagerImpl.kt`

**Checkpoint**: AnalyticsManagerImpl can dispatch events to N providers. Tests validate fan-out, error isolation, and provider toggling.

---

## Phase 4: User Story 2 — Event Taxonomy (Priority: P1) 🎯 MVP

**Goal**: Define the full sealed class hierarchy of typed analytics events with `toParameters()` conversion.

**Independent Test**: Verify that each event type produces the correct `name` string and `toParameters()` map with expected keys/values.

### Tests for User Story 2

- [X] T009 [P] [US2] Create `AnalyticsEventTest` in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/analytics/AnalyticsEventTest.kt` with test cases: each event subclass produces correct `name` property, `toParameters()` returns expected key-value pairs, optional parameters excluded when null

### Implementation for User Story 2

- [X] T010 [US2] Create `AnalyticsEvent` sealed class in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/events/AnalyticsEvent.kt` per Contract 2 — includes all 30 event subclasses: `LaunchViewed`, `LaunchShared`, `LaunchReminderSet`, `LaunchFavorited`, `TabSelected`, `SearchPerformed`, `ArticleViewed`, `EventViewed`, `AgencyViewed`, `AstronautViewed`, `RocketViewed`, `SpaceStationViewed`, `ThirdPartyReferral`, `ContentShared`, `VideoWatchTime`, `VideoOpenedExternal`, `PaywallViewed`, `PurchaseStarted`, `PurchaseCompleted`, `PurchaseRestored`, `NotificationReceived`, `NotificationTapped`, `NotificationSettingChanged`, `AppOpened`, `OnboardingStep`, `ThemeChanged`, `FilterChanged`, `WidgetConfigured`, `WidgetTapped`, `ScreenViewed`

**Checkpoint**: Type-safe event taxonomy complete. AnalyticsManagerImpl + AnalyticsEvent form the core analytics pipeline.

---

## Phase 5: User Story 3 — Console Analytics Provider (Priority: P1) 🎯 MVP

**Goal**: Implement a `ConsoleAnalyticsProvider` that logs all analytics events to SpaceLogger. Serves as the debug provider on all platforms and as the primary provider on Desktop.

**Independent Test**: Verify that `trackEvent()` and `trackScreenView()` produce log output and that toggling `isEnabled` prevents logging.

### Tests for User Story 3

- [X] T011 [P] [US3] Create `ConsoleAnalyticsProviderTest` in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/analytics/ConsoleAnalyticsProviderTest.kt` with test cases: `trackEvent` does not throw, `trackScreenView` does not throw, `isEnabled = false` skips tracking, `name` returns `"console"`, `setUserId` stores and `reset` clears

### Implementation for User Story 3

- [X] T012 [US3] Implement `ConsoleAnalyticsProvider` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/providers/ConsoleAnalyticsProvider.kt`

**Checkpoint**: Console provider ready. Full analytics pipeline works end-to-end (Manager → Event → Console log). Desktop builds functional.

---

## Phase 6: User Story 4 — Firebase Analytics Provider (Priority: P2)

**Goal**: Implement `FirebaseAnalyticsProvider` for Android and iOS using GitLive Firebase KMP SDK (`dev.gitlive:firebase-analytics`). Desktop does NOT get a Firebase provider — it uses Console only.

**Independent Test**: Verify Android & iOS builds compile with the Firebase provider registered. Manually verify events appear in Firebase DebugView.

### Implementation for User Story 4

- [X] T013 [P] [US4] Implement `FirebaseAnalyticsProvider` for Android in `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/analytics/providers/FirebaseAnalyticsProvider.kt` — uses `dev.gitlive:firebase-analytics` KMP API: `Firebase.analytics.logEvent(event.name, event.toParameters())`, `Firebase.analytics.setUserId(userId)`, `Firebase.analytics.setUserProperty(key, value)`, `Firebase.analytics.resetAnalyticsData()` for reset. Wrap each call in try/catch with SpaceLogger error logging.
- [X] T014 [P] [US4] Implement `FirebaseAnalyticsProvider` for iOS in `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/analytics/providers/FirebaseAnalyticsProvider.kt` — same GitLive KMP API as Android (shared `dev.gitlive:firebase-analytics` API surface). Wrap each call in try/catch with SpaceLogger error logging.

**Checkpoint**: Firebase Analytics events sent on both Android and iOS. Console provider continues to work on Desktop.

---

## Phase 7: User Story 5 — Navigation Screen Tracking (Priority: P2)

**Goal**: Automatically track screen views when the user navigates between destinations. Uses `NavController.currentBackStackEntryAsState()` in a `LaunchedEffect` to detect route changes and maps routes to human-readable screen names.

**Independent Test**: Navigate through Home → Schedule → LaunchDetail → back and verify `trackScreenView()` was called with correct screen names for each transition.

### Implementation for User Story 5

- [X] T015 [P] [US5] Create `RouteScreenMapper` object in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/navigation/RouteScreenMapper.kt` — `fun mapRouteToScreenName(route: String): String` that maps route class simple names to human-readable names per the Route-to-Screen Name Mapping table in contracts (Home→"Home", Schedule→"Schedule", LaunchDetail→"Launch Detail", EventDetail→"Event Detail", etc.). Uses a `when` expression with string contains/startsWith matching on route class names. Returns the route string as fallback for unknown routes.
- [X] T016 [US5] Create `AnalyticsScreenTracker` composable in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/navigation/AnalyticsScreenTracker.kt` — `@Composable fun AnalyticsScreenTracker(navController: NavHostController, analyticsManager: AnalyticsManager)` that uses `navController.currentBackStackEntryAsState()`, keyed `LaunchedEffect` on the entry, extracts `destination.route`, calls `RouteScreenMapper.mapRouteToScreenName(route)`, and dispatches `analyticsManager.trackScreenView(screenName, route)`.
- [X] T017 [US5] Integrate `AnalyticsScreenTracker` into the main NavHost in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/App.kt`

**Checkpoint**: Screen views auto-tracked on every navigation transition. No manual tracking calls needed in individual screens.

---

## Phase 8: User Story 6 — Consent Management (Priority: P3)

**Goal**: Allow users to enable/disable individual analytics providers via stored preferences. The `AnalyticsManager` reads these preferences to initialize provider enabled states.

**Independent Test**: Toggle Firebase provider off in preferences → verify `track()` calls skip Firebase provider. Toggle back on → verify events resume.

### Implementation for User Story 6

- [X] T018 [US6] Create `AnalyticsPreferences` class in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/core/AnalyticsPreferences.kt` — wraps `DataStore<Preferences>` with keys `analytics_enabled` (global), `analytics_firebase_enabled`, `analytics_console_enabled`. Provides `Flow<Boolean>` getters via `isProviderEnabled(providerName: String)` and suspend `setProviderEnabled(providerName: String, enabled: Boolean)`. Uses named DataStore `"AppSettingsDataStore"`.
- [X] T019 [US6] Update `AnalyticsManagerImpl` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/core/AnalyticsManagerImpl.kt` — add optional `AnalyticsPreferences` constructor parameter. In an init block, launch a coroutine that observes preference changes for each provider and calls `enableProvider(name, enabled)` when preferences change. If preferences is null, all providers remain enabled (default behavior, preserves existing tests).

**Checkpoint**: Per-provider consent management works. Users can opt out of specific analytics platforms via stored preferences.

---

## Phase 9: User Story 7 — Koin Integration & Full Wiring (Priority: P3)

**Goal**: Create the `analyticsModule` Koin module, register platform-specific providers via platform modules, wire `AnalyticsManager` into the Koin graph, and add the module to the app's Koin configuration.

**Independent Test**: App launches successfully on all 3 platforms. Inject `AnalyticsManager` from Koin and call `track()` without exceptions. Desktop gets Console provider only; Android/iOS get Firebase + Console.

### Implementation for User Story 7

- [X] T020 [US7] Create `analyticsModule` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/di/AnalyticsModule.kt` — registers `ConsoleAnalyticsProvider` as `single<AnalyticsProvider>(named("console"))`, `AnalyticsPreferences` as `single`, and `AnalyticsManagerImpl` as `single<AnalyticsManager>` using `getAll<AnalyticsProvider>()` and optional `getOrNull<AnalyticsPreferences>()`
- [X] T021 [P] [US7] Register `FirebaseAnalyticsProvider` in Android platform module `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/di/AppModule.android.kt` — add `single<AnalyticsProvider>(named("firebase")) { FirebaseAnalyticsProvider() }` to `androidModule`
- [X] T022 [P] [US7] Register `FirebaseAnalyticsProvider` in iOS platform module `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/di/AppModule.ios.kt` — add `single<AnalyticsProvider>(named("firebase")) { FirebaseAnalyticsProvider() }` to `iosModule`
- [X] T023 [US7] Add `analyticsModule` to the `koinConfig` modules list in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/di/AppModule.kt` — modify `modules(networkModule, apiModule, appModule, debugModule, imageLoaderModule)` to include `analyticsModule`

**Checkpoint**: Full analytics pipeline wired end-to-end via Koin. All 3 platforms build and run. `AnalyticsManager` injectable anywhere.

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Documentation, validation, and cleanup

- [X] T024 [P] Verify all three platform builds compile successfully — run `./gradlew compileKotlinDesktop` (Desktop), `./gradlew compileKotlinAndroid` (Android), and confirm iOS framework exports
- [X] T025 [P] Run all unit tests — `./gradlew allTests` — verify `AnalyticsManagerImplTest`, `AnalyticsEventTest`, and `ConsoleAnalyticsProviderTest` pass
- [X] T026 [P] Verify existing Datadog RUM/logging still functions — confirm `DatadogConfig.kt`, `DatadogRUM`, `DatadogLogger` are unmodified and app initializes Datadog without errors
- [X] T027 Run quickstart.md validation — follow the quickstart guide end-to-end to verify usage examples work as documented

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1 (Setup) ─────────────► Phase 2 (Foundational) ─────────────► Phase 3+ (User Stories)
   T001-T003                        T004-T005                          T006+
   No deps                          Depends on Setup                   Depends on Foundational
```

### User Story Dependencies

```
                        ┌─ US1 (Manager) ──────┐
Phase 2 ───────────────►├─ US2 (Events) ───────┤──► US5 (Navigation) ──► US7 (Koin Wiring)
(Foundational)          └─ US3 (Console) ──────┘         │                      │
                                                          │                      │
                         US4 (Firebase) ─────────────────┘──► US6 (Consent) ────┘
                         (can start after Phase 2,
                          but needs US2 for events)
```

- **US1 (Manager)**: Can start after Phase 2 — no user story deps
- **US2 (Events)**: Can start after Phase 2 — no user story deps. Parallel with US1.
- **US3 (Console)**: Can start after Phase 2 — no user story deps. Parallel with US1 and US2.
- **US4 (Firebase)**: Depends on US2 (needs AnalyticsEvent types) and Phase 2 (needs AnalyticsProvider interface)
- **US5 (Navigation)**: Depends on US1 (needs AnalyticsManager) and US2 (needs ScreenViewed event)
- **US6 (Consent)**: Depends on US1 (modifies AnalyticsManagerImpl)
- **US7 (Koin Wiring)**: Depends on US1 + US3 + US4 + US6 (wires all components together)

### Within Each User Story

- Tests written FIRST and should FAIL before implementation
- Interface before implementation
- Core logic before integration
- Story complete before moving to next priority

### Parallel Opportunities

**Batch 1** (after Phase 2):
- T006, T007 (US1 tests) | T009 (US2 test) | T011 (US3 test) — all test files, independent

**Batch 2** (after Batch 1):
- T008 (US1 impl) | T010 (US2 impl) | T012 (US3 impl) — different files, no deps between them

**Batch 3** (after US2 complete):
- T013 (US4 Android) | T014 (US4 iOS) — different source sets, no deps

**Batch 4** (after US1 + US2 complete):
- T015, T016 (US5 nav tracker) | T018 (US6 preferences) — different files

---

## Parallel Example: MVP (US1 + US2 + US3)

```
# Batch 1 — Write all tests in parallel:
T006: FakeAnalyticsProvider in commonTest/analytics/FakeAnalyticsProvider.kt
T007: AnalyticsManagerImplTest in commonTest/analytics/AnalyticsManagerImplTest.kt
T009: AnalyticsEventTest in commonTest/analytics/AnalyticsEventTest.kt
T011: ConsoleAnalyticsProviderTest in commonTest/analytics/ConsoleAnalyticsProviderTest.kt

# Batch 2 — Implement all core classes in parallel:
T008: AnalyticsManagerImpl in commonMain/analytics/core/AnalyticsManagerImpl.kt
T010: AnalyticsEvent sealed class in commonMain/analytics/events/AnalyticsEvent.kt
T012: ConsoleAnalyticsProvider in commonMain/analytics/providers/ConsoleAnalyticsProvider.kt

# All tests should now PASS
```

---

## Implementation Strategy

### MVP First (US1 + US2 + US3)

1. Complete Phase 1: Setup (dependencies, package structure)
2. Complete Phase 2: Foundational (AnalyticsProvider + AnalyticsManager interfaces)
3. Complete Phases 3-5: US1 (Manager) + US2 (Events) + US3 (Console) — **these can run in parallel**
4. **STOP and VALIDATE**: Full analytics pipeline works with ConsoleAnalyticsProvider. Desktop builds. All tests pass.
5. This is a deployable MVP increment.

### Incremental Delivery

1. Setup + Foundational → Interfaces ready
2. US1 + US2 + US3 → Core pipeline works (MVP!)
3. US4 (Firebase) → Real analytics on Android/iOS
4. US5 (Navigation) → Auto screen tracking
5. US6 (Consent) → Per-provider opt-out
6. US7 (Koin Wiring) → Full production integration
7. Each phase can be deployed/reviewed independently.

---

## Notes

- [P] tasks = different files, no dependencies between them
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group using conventional commits (e.g., `feat(analytics): add AnalyticsProvider interface`)
- Existing `analytics/DatadogConfig.kt` and `util/logging/` remain completely untouched
- ConsoleAnalyticsProvider lives in commonMain (available on all platforms) — NOT in desktopMain
- Firebase provider uses GitLive KMP SDK (same API on Android + iOS), NOT Android-only Firebase SDK
- All provider calls dispatched on `Dispatchers.Default` via `SupervisorJob` — zero main thread impact
