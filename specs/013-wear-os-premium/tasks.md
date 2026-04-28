# Tasks: Wear OS Premium Experience

**Input**: Design documents from `/specs/013-wear-os-premium/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅, quickstart.md ✅

**Tests**: Not explicitly requested — omitted. Add test phases per story if TDD is desired.

**Organization**: Tasks grouped by user story. US5 (Data Sync) and US4 (Entitlement Sync) are P1 infrastructure stories that block US1/US2/US3.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies on in-progress tasks)
- **[Story]**: Which user story (US1–US5) this task belongs to
- Include exact file paths in descriptions

## Path Conventions

- **Phone app**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/...`
- **Phone Android**: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/...`
- **Wear app**: `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/...`
- **Gradle config**: `gradle/libs.versions.toml`, `settings.gradle.kts`, `wearApp/build.gradle.kts`

---

## Phase 1: Setup (Module Skeleton)

**Purpose**: Create the wearApp Gradle module so it compiles and is recognized by the build system.

- [X] T001 Add Wear OS version entries and library aliases to `gradle/libs.versions.toml` per quickstart.md Section 1 (wear-compose 1.5.0, wear-tiles 1.6.0, wear-protolayout 1.4.0, wear-watchface 1.3.0, play-services-wearable 19.0.0)
- [X] T002 Add `include(":wearApp")` to `settings.gradle.kts`
- [X] T003 Create `wearApp/build.gradle.kts` with androidApplication + composeCompiler plugins, minSdk 30, compileSdk 36, and all Wear OS + shared dependencies per quickstart.md Section 3
- [X] T004 Create `wearApp/src/main/AndroidManifest.xml` with `<uses-feature android:name="android.hardware.type.watch"/>`, WearActivity, complication service, tile service, and DataLayer listener service declarations per quickstart.md Section 4
- [X] T005 [P] Create `wearApp/src/main/res/values/strings.xml` with app_name and common string resources
- [X] T006 [P] Add `play-services-wearable` implementation dependency to the android block of `composeApp/build.gradle.kts` for phone-side DataLayer communication
- [X] T007 Verify `./gradlew :wearApp:assembleDebug` compiles — fix any missing plugin aliases or dependency resolution errors

**Checkpoint**: wearApp module compiles as an empty Android app. Existing targets unaffected.

---

## Phase 2: Foundational (Shared Models + DI + App Shell)

**Purpose**: Data classes, enums, Koin module, theme, and app shell that ALL user stories depend on.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T008 Add `WEAR_OS` to `PremiumFeature` enum and map it to PREMIUM + LIFETIME tiers (not LEGACY) in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/SubscriptionState.kt`
- [X] T009 [P] Create `CachedLaunch` data class (id, name, net, statusAbbrev, statusName, lspName, lspAbbrev, rocketConfigName, missionName, missionDescription, padLocationName, imageUrl) in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/data/model/CachedLaunch.kt`
- [X] T010 [P] Create `SyncLaunch` data class with `@Serializable` annotation and `DataLayerSyncPayload` (launches, entitlementActive, syncTimestamp, phoneAppVersion) in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/data/model/DataLayerSyncPayload.kt`
- [X] T011 [P] Create `WearEntitlementState` data class and `SyncSource` enum (PHONE_SYNC, LOCAL_CACHE, DEFAULT) in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/data/model/WearEntitlementState.kt`
- [X] T012 [P] Create `WatchLaunchCache` data class and `DataSource` enum (DIRECT_API, PHONE_SYNC, STALE_CACHE) in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/data/model/WatchLaunchCache.kt`
- [X] T013 [P] Create `WearScreen` sealed class with LaunchList, LaunchDetail(launchId), PremiumGate, and Settings destinations in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/WearScreen.kt`
- [X] T014 [P] Create `WearTheme.kt` wrapping Wear Compose Material3 `MaterialTheme` with dynamic color theming in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/ui/theme/WearTheme.kt`
- [X] T015 Create `WearModule.kt` Koin module registering repository interfaces, viewmodels, EntitlementSyncManager, and DataStore instances in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/di/WearModule.kt`
- [X] T016 Create `WearApplication.kt` with `startKoin` loading WearModule and initializing WorkManager in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/WearApplication.kt`
- [X] T017 Create `WearActivity.kt` stub with `setContent { WearTheme { /* placeholder */ } }` in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/WearActivity.kt`

**Checkpoint**: Foundation ready — wearApp compiles with all data models, DI, and an empty activity shell. `./gradlew :wearApp:assembleDebug` passes.

---

## Phase 3: User Story 5 — Data Sync with Fallback (Priority: P1) 🎯 MVP Infrastructure

**Goal**: Watch fetches launch data via direct API (WiFi/LTE), falls back to phone DataLayer sync, then local DataStore cache. Background refresh every 30 minutes.

**Independent Test**: (1) Enable watch WiFi → verify direct API data appears, (2) disable watch WiFi with phone nearby → verify DataLayer sync delivers data, (3) disconnect both → verify cached data displays. (4) Wait 30 min → verify WorkManager refreshes cache.

### Implementation for User Story 5

- [X] T018 [US5] Create `WatchLaunchRepository` interface with `launches: Flow`, `dataSource: Flow`, `refreshLaunches()`, `getLaunchById()`, and `getNextLaunch()` in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/data/WatchLaunchRepository.kt`
- [X] T019 [US5] Implement `WatchLaunchRepositoryImpl` with three-tier fallback (direct Ktor API call → DataLayer DataClient read → DataStore cache) and cache-update-on-success in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/data/WatchLaunchRepositoryImpl.kt`
- [X] T020 [US5] Create `PhoneDataLayerSync` interface and `PhoneDataLayerService` implementation that writes `DataLayerSyncPayload` JSON to DataClient at path `/spacelaunchnow/sync` in `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/sync/PhoneDataLayerService.kt`
- [X] T021 [US5] Register `PhoneDataLayerListenerService` in `composeApp/src/androidMain/AndroidManifest.xml` listening for watch sync-request messages at path `/spacelaunchnow/request-sync`
- [X] T022 [US5] Create `DataLayerListenerService` extending `WearableListenerService` with `onDataChanged()` that parses `DataLayerSyncPayload`, updates `WatchLaunchCache` DataStore, and notifies repository in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/data/DataLayerListenerService.kt`
- [X] T023 [US5] Create `WatchDataRefreshWorker` extending `CoroutineWorker` with `Constraints(requiresBatteryNotLow=true)` that calls `WatchLaunchRepository.refreshLaunches()` in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/worker/WatchDataRefreshWorker.kt`
- [X] T024 [US5] Enqueue `WatchDataRefreshWorker` as `PeriodicWorkRequest` (30 min repeat) in `WearApplication.kt` startup

**Checkpoint**: Watch can fetch, receive, and cache launch data through all three tiers. Background refresh runs every 30 minutes.

---

## Phase 4: User Story 4 — Entitlement Sync Between Phone and Watch (Priority: P1)

**Goal**: When a user subscribes/unsubscribes on phone, the watch reflects the change within 10 seconds. Watch caches entitlement locally for offline access with 24-hour grace period.

**Independent Test**: Toggle premium in RevenueCat sandbox on phone → verify watch `WearEntitlementState` updates within 10s. Disconnect phone → verify cached state persists. Wait 24h+ with no sync → verify grace period expires.

### Implementation for User Story 4

- [X] T025 [P] [US4] Create `EntitlementSyncManager` interface with `entitlementState: Flow`, `isWearOsPremium()`, `onEntitlementReceived()`, and `requestSync()` in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/data/EntitlementSyncManager.kt`
- [X] T026 [US4] Implement `EntitlementSyncManagerImpl` with DataStore-backed cache, SyncSource tracking, and 24-hour grace period expiry logic in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/data/EntitlementSyncManagerImpl.kt`
- [X] T027 [US4] Add `syncEntitlementToWatch(active, expiresAt)` method to `PhoneDataLayerService` that writes entitlement DataItem at path `/spacelaunchnow/entitlement` in `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/sync/PhoneDataLayerService.kt`
- [X] T028 [US4] Hook RevenueCat `UpdatedCustomerInfoListener` to call `PhoneDataLayerService.syncEntitlementToWatch()` on every entitlement change in the existing subscription/billing setup code
- [X] T029 [US4] Extend `DataLayerListenerService.onDataChanged()` to parse entitlement data and call `EntitlementSyncManager.onEntitlementReceived()` in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/data/DataLayerListenerService.kt`

**Checkpoint**: Entitlement state flows from RevenueCat → phone DataLayer → watch DataStore. Premium gating infrastructure ready for all Wear surfaces.

---

## Phase 5: User Story 1 — Watch Complications Showing Next Launch (Priority: P1) 🎯 MVP Feature

**Goal**: Premium subscribers add a complication to their watch face showing a live countdown to the next launch (SHORT_TEXT: "T-2h 15m", LONG_TEXT: "Falcon 9 — T-2h 15m", RANGED_VALUE: progress bar T-24h→T-0). Free users see "Subscribe" placeholder.

**Independent Test**: Deploy to Wear OS emulator → add "Next Launch" complication to watch face → verify countdown accuracy against LL API → verify it advances to next launch at T-0 → verify free-tier shows "Subscribe".

### Implementation for User Story 1

- [X] T030 [US1] Create `NextLaunchComplicationService` extending `ComplicationDataSourceService` implementing `onComplicationRequest()` with SHORT_TEXT (countdown), LONG_TEXT (vehicle + countdown via `LaunchFormatUtil`), and RANGED_VALUE (progress T-24h to T-0) types in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/complication/NextLaunchComplicationService.kt`
- [X] T031 [US1] Add `getPreviewData()` returning sample complication data for the watch face picker, and premium gating that returns "Subscribe" `PlainComplicationText` for free users in `NextLaunchComplicationService.kt`
- [X] T032 [US1] Wire `ComplicationDataSourceUpdateRequester.requestUpdateAll()` into `WatchDataRefreshWorker.doWork()` and `DataLayerListenerService.onDataChanged()` to trigger complication refresh on new data

**Checkpoint**: Complication appears in watch face picker, shows live countdown for premium users, "Subscribe" for free users. Updates on data refresh.

---

## Phase 6: User Story 2 — Glanceable Launch Tile (Priority: P2)

**Goal**: Premium subscribers see a tile in their carousel with agency icon, mission name, vehicle, countdown timer, and location. Free users see "Upgrade on phone" with deep link.

**Independent Test**: Deploy to Wear OS emulator → add tile to carousel → verify layout shows correct launch data → verify free-tier shows upgrade prompt → verify tile refreshes on DataLayer sync.

### Implementation for User Story 2

- [X] T033 [US2] Create `NextLaunchTileService` extending `Material3TileService` with tile layout showing agency abbreviation, mission name, vehicle name (via `LaunchFormatUtil`), countdown, and pad location in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/tile/NextLaunchTileService.kt`
- [X] T034 [US2] Add premium gating to `NextLaunchTileService` — free users see "Upgrade on phone" text with deep link to phone subscription screen, empty cache shows "No upcoming launches" in `NextLaunchTileService.kt`
- [X] T035 [US2] Wire `TileService.getUpdater(context).requestUpdate(NextLaunchTileService::class.java)` into `WatchDataRefreshWorker.doWork()` and `DataLayerListenerService.onDataChanged()` to trigger tile refresh

**Checkpoint**: Tile appears in carousel, shows rich launch info for premium users, upgrade prompt for free users. Refreshes on data sync.

---

## Phase 7: User Story 3 — Companion App: Browse Upcoming Launches (Priority: P3)

**Goal**: Premium users browse a scrollable launch list, tap into detail with mission info and "Open on Phone", and access settings (UTC toggle). Free users see a premium gate screen.

**Independent Test**: Launch app on Wear emulator → verify list shows launches → tap into detail → verify all fields (title via LaunchFormatUtil, NET via DateTimeUtil, status, location) → tap "Open on Phone" → verify phone opens → verify rotary scroll works → verify free-tier redirects to PremiumGateScreen.

### Implementation for User Story 3

- [X] T036 [P] [US3] Create `LaunchListViewModel` with `StateFlow<LaunchListUiState>` collecting from `WatchLaunchRepository.launches` and `EntitlementSyncManager.entitlementState`, with `refresh()` action in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/viewmodel/LaunchListViewModel.kt`
- [X] T037 [P] [US3] Create `LaunchDetailViewModel` with `StateFlow<LaunchDetailUiState>`, launch-by-ID lookup, live countdown ticker, and `openOnPhone(launchId)` via `RemoteActivityHelper` in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/viewmodel/LaunchDetailViewModel.kt`
- [X] T038 [US3] Create `LaunchListScreen` Composable with `TransformingLazyColumn`, rotary scroll via `rotaryScrollable()`, launch cards showing formatted title (`LaunchFormatUtil`), countdown, status chip, and "Last updated" indicator in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/ui/launch/LaunchListScreen.kt`
- [X] T039 [US3] Create `LaunchDetailScreen` Composable with `ScreenScaffold`, mission name, NET datetime (via `DateTimeUtil` respecting UTC toggle), status, agency, location, live countdown, and "Open on Phone" `EdgeButton` in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/ui/launch/LaunchDetailScreen.kt`
- [X] T040 [P] [US3] Create `PremiumGateScreen` Composable with "Subscribe on your phone" prompt, icon, and deep link to phone paywall via `RemoteActivityHelper` in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/ui/premium/PremiumGateScreen.kt`
- [X] T041 [P] [US3] Create `SettingsScreen` Composable with UTC toggle (backed by `DateTimeUtil` setting) and app version display in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/ui/settings/SettingsScreen.kt`
- [X] T042 [US3] Create `WearApp.kt` Composable with `AppScaffold` + `SwipeDismissableNavHost`, entitlement-based start destination (LaunchList if premium, PremiumGate if free), and navigation graph in `wearApp/src/main/kotlin/me/calebjones/spacelaunchnow/wear/ui/WearApp.kt`
- [X] T043 [US3] Wire `WearActivity.kt` to render `WearApp()` inside `WearTheme`, and add dual `@WearPreviewDevices` + `@WearPreviewFontScales` preview annotations to all companion app Composables (LaunchListScreen, LaunchDetailScreen, PremiumGateScreen, SettingsScreen)

**Checkpoint**: Full companion app functional — list, detail, premium gate, settings, rotary input, "Open on Phone", entitlement-based routing. All Composables have Wear previews.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: CI integration, documentation, and final validation.

- [X] T044 [P] Add `:wearApp:assembleDebug` step to CI workflow in `.github/workflows/` PR build job
- [X] T045 [P] Update `README.md` with Wear OS module overview, build instructions, and emulator setup
- [X] T046 Verify `./gradlew compileKotlinDesktop` and `./gradlew :composeApp:assembleDebug` still pass — confirm no commonMain changes break iOS/Desktop (Constitution Principle VI)
- [X] T047 Run full end-to-end validation per `specs/013-wear-os-premium/quickstart.md` Section 7 verification steps (assembleDebug, desktop build, emulator deploy, complication picker, tile carousel)

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1: Setup ──────────────────> (no deps, start immediately)
Phase 2: Foundational ───────────> depends on Phase 1
Phase 3: US5 Data Sync ─────────> depends on Phase 2
Phase 4: US4 Entitlement Sync ──> depends on Phase 2 (can PARALLEL with Phase 3)
Phase 5: US1 Complications ─────> depends on Phase 3 + Phase 4
Phase 6: US2 Tiles ─────────────> depends on Phase 3 + Phase 4 (can PARALLEL with Phase 5)
Phase 7: US3 Companion App ─────> depends on Phase 3 + Phase 4 (can PARALLEL with Phase 5, 6)
Phase 8: Polish ────────────────> depends on all desired story phases
```

### User Story Dependencies

- **US5 (Data Sync)**: Depends only on Foundational. No dependency on other stories.
- **US4 (Entitlement Sync)**: Depends only on Foundational. Can run in **parallel** with US5 (different files). Extends `PhoneDataLayerService` created in US5, but `PhoneDataLayerService` is a single file that can be built incrementally.
- **US1 (Complications)**: Depends on US4 (entitlement gating) + US5 (launch data). Cannot start until both are done.
- **US2 (Tiles)**: Depends on US4 + US5. Can run in **parallel** with US1 (different service files).
- **US3 (Companion App)**: Depends on US4 + US5. Can run in **parallel** with US1 and US2 (different files entirely).

### Within Each User Story

```
Models (Phase 2) ──> Interfaces ──> Implementations ──> Wiring/Integration
```

### Parallel Opportunities

**Phase 2 (Foundational)** — 6 tasks run in parallel:
```
T009 CachedLaunch.kt ─────────────────┐
T010 DataLayerSyncPayload.kt ─────────┤
T011 WearEntitlementState.kt ──────────┤── all [P], different files
T012 WatchLaunchCache.kt ─────────────┤
T013 WearScreen.kt ───────────────────┤
T014 WearTheme.kt ────────────────────┘
```

**Phase 3+4 (US5+US4)** — stories run in parallel:
```
US5: T018→T019→T020→T021→T022→T023→T024
US4: T025→T026→T027→T028→T029
(US4 T025 is [P], can start same time as US5 T018)
```

**Phase 5+6+7 (US1+US2+US3)** — all three can run in parallel:
```
US1: T030→T031→T032
US2: T033→T034→T035
US3: T036+T037(parallel)→T038→T039, T040+T041(parallel)→T042→T043
```

---

## Implementation Strategy

### MVP First (Complications Only — US5 + US4 + US1)

1. Complete Phase 1: Setup (T001–T007)
2. Complete Phase 2: Foundational (T008–T017)
3. Complete Phase 3: US5 Data Sync (T018–T024)
4. Complete Phase 4: US4 Entitlement Sync (T025–T029)
5. Complete Phase 5: US1 Complications (T030–T032)
6. **STOP and VALIDATE**: Test complication on Wear OS emulator
7. Deploy to internal testing — MVP delivers value

### Incremental Delivery

1. Setup + Foundational → Module compiles, app shell launches
2. US5 + US4 → Data and entitlement infrastructure ready
3. **+ US1 Complications** → First visible feature on watch face (**MVP!**)
4. **+ US2 Tiles** → Richer glanceable experience in tile carousel
5. **+ US3 Companion App** → Full browsing experience on watch
6. Polish → CI, docs, final validation
7. Each increment is independently shippable and testable

### Parallel Team Strategy

With 2 developers after Phase 2:

```
Dev A: US5 (data sync) → US1 (complications) → US2 (tiles)
Dev B: US4 (entitlement) → US3 (companion app)
Both: Phase 8 (polish)
```

---

## Notes

- `LaunchFormatUtil.formatLaunchTitle()` MUST be used for all launch title display (FR-009)
- `DateTimeUtil` MUST be used for all datetime formatting to support UTC toggle (FR-010)
- Wear Compose Material3 1.5.0 only — do NOT use legacy compose-material (research R-001)
- `Material3TileService` from Tiles 1.6.0 — do NOT use Horologist TileRenderer (research R-002, R-010)
- DataLayer path constants: `/spacelaunchnow/sync` (launches), `/spacelaunchnow/entitlement` (premium state), `/spacelaunchnow/request-sync` (watch→phone request)
- All Wear Composables need `@WearPreviewDevices` and `@WearPreviewFontScales` preview annotations (copilot-instructions: dual previews)
- Commit after each task using conventional format: `feat(wear):` for new features, `chore(wear):` for setup
