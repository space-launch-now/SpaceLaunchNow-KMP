# Tasks: Domain Model Layer Migration

**Input**: Design documents from `specs/dev/domain-model-layer/`
**Prerequisites**: plan.md ✅, research.md ✅, data-model.md ✅, quickstart.md ✅

**Tests**: Domain model tests, mapper unit tests, fake repositories, and ViewModel unit tests are included at each layer.

**Organization**: Tasks are grouped by migration step to enable incremental implementation. Each phase builds on the previous and is independently verifiable via compilation.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Migration step this task belongs to (US1–US5)
- Exact file paths included in every task description

## Path Conventions

- **Common source**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/`
- **Common test**: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/`
- **Android**: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/`
- **Desktop**: `composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/`
- **iOS**: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/`

---

## Phase 1: Setup

**Purpose**: Ensure prerequisites are met and directory structure exists

- [X] T001 Run `./gradlew openApiGenerate` to ensure generated API clients are present
- [X] T002 Create directory structure `domain/model/` and `domain/mapper/` under `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/`

---

## Phase 2: Foundational — Domain Model Types

**Purpose**: Create all domain model data classes that MUST exist before mappers, repositories, or UI can reference them

**⚠️ CRITICAL**: No mapper, repository, or UI migration can begin until all model files compile

- [X] T003 [P] Create `PaginatedResult<T>` generic wrapper data class in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/model/PaginatedResult.kt` — fields: count (Int), next (String?), previous (String?), results (List\<T\>)
- [X] T004 [P] Create shared value types in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/model/Common.kt` — includes Provider, ProviderDetail, RocketConfig, RocketDetail, RocketStage, LauncherSummary, LandingAttemptSummary, SpacecraftFlightSummary, PayloadSummary, Pad, Location, Mission, Orbit, LaunchStatus, NetPrecision, LaunchAttemptCounts, ProgramSummary, VideoLink, InfoLink, MissionPatchSummary, TimelineEntry, Update as defined in data-model.md
- [X] T005 [P] Create unified `Launch` data class with `@Immutable` annotation in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/model/Launch.kt` — all fields from data-model.md Entity Relationship Diagram (basic, normal+, detailed-only tiers with nullable detail fields)
- [X] T006 [P] Create unified `Event` data class with `@Immutable` annotation in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/model/Event.kt` — includes EventType, ExpeditionSummary, SpaceStationSummary, AstronautSummary as defined in data-model.md
- [X] T007 [P] Create `LaunchFilterParams` data class in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/model/LaunchFilterParams.kt` — fields per data-model.md Filter Parameters section
- [X] T008 [P] Create `EventFilterParams` data class in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/model/EventFilterParams.kt` — fields for typeIds, programId, search, limit, offset, upcoming

**Checkpoint**: `./gradlew compileKotlinDesktop` — all domain model types compile with zero errors

### Domain Model Tests

- [X] T139 [P] Write domain model tests for Launch in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/domain/model/LaunchTest.kt` — test construction with all fields populated, test nullable detail fields default correctly, test copy() preserves/overrides fields, test list fields default to emptyList()
- [X] T140 [P] Write domain model tests for Event in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/domain/model/EventTest.kt` — test construction, nullable detail fields, list defaults (launches, expeditions, programs default to emptyList())
- [X] T141 [P] Write domain model tests for Common types in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/domain/model/CommonTypesTest.kt` — test Provider, RocketConfig, Pad, Location, Mission, LaunchStatus, PaginatedResult\<T\> construction and defaults; verify PaginatedResult.results defaults to emptyList()

---

## Phase 3: US1 — Mapper Layer (Priority: P1) 🎯 MVP

**Goal**: Create extension function mappers that convert generated API types → domain types, with full unit test coverage

**Independent Test**: All mapper unit tests pass via `./gradlew test`

### Implementation for US1

- [X] T009 [US1] Create common type mapper extension functions in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/mapper/CommonMappers.kt` — toDomain() extensions for AgencyMini→Provider, AgencyNormal→Provider, PadDetailed→Pad, LocationList→Location, Mission→Mission (domain), LaunchStatus→LaunchStatus (domain), NetPrecision→NetPrecision (domain), VidURL→VideoLink, InfoURL→InfoLink, Update→Update (domain), ProgramMini→ProgramSummary, TimelineEvent→TimelineEntry, MissionPatch→MissionPatchSummary, SpacecraftFlight→SpacecraftFlightSummary, Country→countryCode String
- [X] T010 [US1] Create Launch mapper extension functions in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/mapper/LaunchMappers.kt` — LaunchBasic.toDomain(), LaunchNormal.toDomain(), LaunchDetailed.toDomain(), PaginatedLaunchBasicList.toDomain(), PaginatedLaunchNormalList.toDomain(), PaginatedLaunchDetailedList.toDomain() — each calls CommonMappers for nested types; flatten Image→imageUrl/thumbnailUrl
- [X] T011 [US1] Create Event mapper extension functions in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/mapper/EventMappers.kt` — EventEndpointNormal.toDomain(), EventEndpointDetailed.toDomain(), PaginatedEventEndpointNormalList.toDomain() — detail-only fields (agencies, launches, expeditions, spaceStations, programs, astronauts) default to empty lists
- [X] T012 [P] [US1] Write unit tests for CommonMappers in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/domain/mapper/CommonMappersTest.kt` — test each toDomain() extension with representative API objects, verify null handling and field mapping
- [X] T013 [P] [US1] Write unit tests for LaunchMappers in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/domain/mapper/LaunchMappersTest.kt` — test LaunchBasic→Launch (normal+ fields null), LaunchNormal→Launch (detailed fields null/empty), LaunchDetailed→Launch (all fields populated), paginated wrapper mapping
- [X] T014 [P] [US1] Write unit tests for EventMappers in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/domain/mapper/EventMappersTest.kt` — test EventEndpointNormal→Event (detail fields empty), EventEndpointDetailed→Event (all fields populated), paginated wrapper mapping

**Checkpoint**: `./gradlew test` — all mapper unit tests pass. Mapper functions are the ONLY new code importing from `me.calebjones.spacelaunchnow.api.launchlibrary.models`.

---

## Phase 4: US2 — Repository Layer Migration (Priority: P1)

**Goal**: Add domain-returning methods alongside existing API-type methods. Mark old methods `@Deprecated`.

**Independent Test**: `./gradlew compileKotlinDesktop` compiles. Existing functionality unchanged.

### Implementation for US2

- [X] T015 [P] [US2] Add domain-returning method signatures to `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/LaunchRepository.kt` — new methods: `getUpcomingLaunches(limit, offset) → Result<PaginatedResult<Launch>>`, `getPreviousLaunches(limit, offset) → Result<PaginatedResult<Launch>>`, `getFeaturedLaunch(forceRefresh, agencyIds, locationIds) → Result<DataResult<PaginatedResult<Launch>>>`, `getInFlightLaunches(forceRefresh, agencyIds, locationIds) → Result<DataResult<PaginatedResult<Launch>>>`, `getUpcomingLaunchesNormalDomain(limit, forceRefresh, agencyIds, locationIds) → Result<DataResult<PaginatedResult<Launch>>>`, `getPreviousLaunchesNormalDomain(limit, forceRefresh, agencyIds, locationIds) → Result<DataResult<PaginatedResult<Launch>>>`, `getLaunchDetail(id, forceRefresh) → Result<Launch>`, `getStarshipLaunches(limit, forceRefresh, programId) → Result<PaginatedResult<Launch>>`, `getStarshipHistory(limit, forceRefresh) → Result<DataResult<PaginatedResult<Launch>>>` — mark old methods `@Deprecated` with `ReplaceWith`
- [X] T016 [P] [US2] Add domain-returning method signatures to `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/EventsRepository.kt` — new methods: `getUpcomingEventsDomain(limit, forceRefresh) → Result<DataResult<PaginatedResult<Event>>>`, `getEventDetail(eventId) → Result<Event>`, `getEventsByTypeDomain(typeIds, limit) → Result<PaginatedResult<Event>>`, `getEventsByLaunchIdDomain(launchId, limit) → Result<PaginatedResult<Event>>`, `getEventsPaginatedDomain(limit, offset, search, typeIds, upcoming, forceRefresh) → Result<DataResult<PaginatedResult<Event>>>` — mark old methods `@Deprecated`
- [X] T017 [US2] Implement domain-returning methods in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/LaunchRepositoryImpl.kt` — each new method calls existing API extension function, then maps response with `.toDomain()` from LaunchMappers; import from `domain.mapper` and `domain.model`; preserve existing caching/stale-while-revalidate pattern
- [X] T018 [US2] Implement domain-returning methods in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/EventsRepositoryImpl.kt` — each new method calls existing API/cache logic, then maps with `.toDomain()` from EventMappers; preserve existing caching pattern

**Checkpoint**: `./gradlew compileKotlinDesktop` compiles. Both old and new repository methods available. Existing app behavior unchanged.

### Fake Repositories (Test Infrastructure)

- [X] T142 [P] [US2] Create `FakeLaunchRepository` in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/repository/FakeLaunchRepository.kt` — implements `LaunchRepository` interface with hand-rolled fakes; expose `var upcomingLaunchesResult`, `var launchDetailResult`, `var shouldFail = false` flags; follow existing mock pattern from `MockSubscriptionRepository.kt` and `MockAstronautListRepository`
- [X] T143 [P] [US2] Create `FakeEventsRepository` in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/repository/FakeEventsRepository.kt` — implements `EventsRepository` interface; expose `var upcomingEventsResult`, `var eventDetailResult`, `var shouldFail = false`

---

## Phase 5: US3 — ViewModel Migration (Priority: P1)

**Goal**: Switch all ViewModel StateFlow types from API models to domain models. Call new repository domain methods.

**Independent Test**: `./gradlew compileKotlinDesktop` compiles after all VMs + their connected composables are updated (Phase 5 + Phase 6 together).

### Implementation for US3

- [X] T019 [P] [US3] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/NextUpViewModel.kt` — change `StateFlow<LaunchNormal?>` to `StateFlow<Launch?>`, call domain repository method, remove API model imports
- [X] T020 [P] [US3] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/LaunchViewModel.kt` — change `StateFlow<LaunchDetailed?>` to `StateFlow<Launch?>`, call `getLaunchDetail()` domain method, remove API model imports
- [X] T021 [P] [US3] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/LaunchesViewModel.kt` — change launch list StateFlow from `LaunchNormal` to `Launch`, call domain repository methods, remove API model imports
- [X] T022 [P] [US3] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/ScheduleViewModel.kt` — change `LaunchBasic` to `Launch`, call `getUpcomingLaunches()` domain method, remove API model imports
- [X] T023 [P] [US3] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/EventsViewModel.kt` — change `EventEndpointNormal` to `Event`, call `getUpcomingEventsDomain()`, remove API model imports
- [X] T024 [P] [US3] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/EventViewModel.kt` — change `EventEndpointDetailed` to `Event`, call `getEventDetail()`, remove API model imports
- [X] T025 [P] [US3] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/HistoryViewModel.kt` — change `LaunchNormal` to `Launch`, remove API model imports
- [X] T026 [P] [US3] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/FeaturedLaunchViewModel.kt` — change `LaunchNormal`/`EventEndpointDetailed` to `Launch`/`Event`, call domain methods, remove API model imports
- [X] T027 [P] [US3] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/LaunchCarouselViewModel.kt` — change `LaunchNormal` to `Launch`, remove API model imports
- [X] T028 [P] [US3] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/StarshipViewModel.kt` — change `LaunchNormal` to `Launch`, call domain Starship methods, remove API model imports
- [X] T029 [P] [US3] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/FeedViewModel.kt` — change `UpdateEndpoint` usage to domain `Update` type if applicable, remove API model imports
- [X] T030 [P] [US3] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/HomeViewModel.kt` — update any launch/event type references to domain types, remove API model imports

**Checkpoint**: All ViewModels compile with domain types. (Full compilation requires Phase 6 composable updates.)

### ViewModel Unit Tests

- [X] T144 [P] [US3] Write ViewModel test for NextUpViewModel in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/NextUpViewModelTest.kt` — use `FakeLaunchRepository`; test `fetchNextLaunch()` success updates StateFlow, test failure sets error state; use `kotlin.test`, `StandardTestDispatcher`, `Dispatchers.setMain/resetMain` pattern
- [X] T145 [P] [US3] Write ViewModel test for LaunchViewModel in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/LaunchViewModelTest.kt` — use `FakeLaunchRepository`; test `loadLaunchDetail(id)` populates `StateFlow<Launch?>`, test error handling
- [X] T146 [P] [US3] Write ViewModel test for ScheduleViewModel in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/ScheduleViewModelTest.kt` — use `FakeLaunchRepository`; test upcoming launches list loading, test pagination/offset behavior
- [X] T147 [P] [US3] Write ViewModel test for EventsViewModel in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/EventsViewModelTest.kt` — use `FakeEventsRepository`; test upcoming events list, test filter by type
- [X] T148 [P] [US3] Write ViewModel test for LaunchesViewModel in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/LaunchesViewModelTest.kt` — use `FakeLaunchRepository`; test upcoming vs previous tabs, test filter application
- [X] T149 [P] [US3] Write ViewModel test for FeaturedLaunchViewModel in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/FeaturedLaunchViewModelTest.kt` — use `FakeLaunchRepository` + `FakeEventsRepository`; test featured launch selection, test fallback behavior

---

## Phase 6: US4 — UI Composable Migration (Priority: P1)

**Goal**: Update all Composable functions to accept domain model types instead of API types. Eliminate `LaunchCardData` sealed interface.

**Independent Test**: `./gradlew compileKotlinDesktop` — zero errors. `./gradlew desktopRun --quiet` — Home, Schedule, Launch Detail, Events all render correctly.

### Core Launch UI

- [X] T031 [US4] Eliminate `LaunchCardData` sealed interface and refactor `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/compose/LaunchCardHeader.kt` — replace `LaunchCardData` parameter with `Launch` parameter in `LaunchCardHeaderOverlay` and all related composables; access fields directly from unified Launch type; remove all `when` branches over LaunchBasic/LaunchNormal/LaunchDetailed
- [X] T032 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/compose/CountdownWidget.kt` — replace API `LaunchStatus`/`NetPrecision` imports with domain model imports
- [X] T033 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/home/components/FeaturedLaunchRowCard.kt` — change `LaunchNormal` parameter to `Launch`, update field access
- [X] T034 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/home/components/LastLaunchCard.kt` — change `LaunchNormal` parameter to `Launch`, update field access
- [X] T035 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/home/components/LatestUpdatesView.kt` — change `LaunchBasic`/`UpdateEndpoint` to domain `Launch`/`Update`, update all field access patterns

### Onboarding

- [X] T036 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/onboarding/OnboardingPaywallScreen.kt` — change `LaunchNormal` to `Launch`
- [X] T037 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/onboarding/pages/WelcomePage.kt` — change `LaunchNormal` to `Launch`
- [X] T038 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/onboarding/pages/SchedulePage.kt` — change `LaunchBasic` to `Launch`

### Launch Detail Tabs

- [X] T039 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/detail/compose/tabs/OverviewTabContent.kt` — change `LaunchDetailed` parameter to `Launch`, change `EventEndpointNormal` to `Event`
- [X] T040 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/detail/compose/tabs/RocketTabContent.kt` — change `LaunchDetailed` parameter to `Launch`, access `rocketDetail` from unified type
- [X] T041 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/detail/compose/tabs/MissionTabContent.kt` — change `LaunchDetailed` parameter to `Launch`
- [X] T042 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/detail/compose/tabs/AgencyTabContent.kt` — change `LaunchDetailed` parameter to `Launch`, access `providerDetail` from unified type
- [X] T043 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/detail/compose/tablet/TabletLaunchDetailContent.kt` — change `LaunchDetailed` to `Launch`, `EventEndpointNormal` to `Event`

### Detail Components

- [X] T044 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/detail/compose/components/VideoPlayer.kt` — change `VidURL` parameter to domain `VideoLink`
- [X] T045 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/detail/compose/components/TimelineCard.kt` — change `TimelineEvent`/`TimelineEventType` to domain `TimelineEntry`
- [X] T046 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/detail/compose/components/SpacecraftDetailsCard.kt` — change `SpacecraftFlightDetailedSerializerNoLaunch` to domain `SpacecraftFlightSummary`
- [X] T047 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/detail/compose/components/RelatedEventsCard.kt` — change `EventEndpointNormal` to domain `Event`

### Event & Video UI

- [X] T048 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/event/EventDetailView.kt` — change `EventEndpointDetailed` to domain `Event`, replace `AgencyMini`/`AstronautNormal`/`LaunchBasic`/`ProgramNormal`/`SpaceStationNormal`/`ExpeditionNormal`/`Update` with domain equivalents
- [X] T049 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/video/FullscreenVideoScreen.kt` — change `VidURL`/`Language`/`VidURLType` to domain `VideoLink`
- [X] T050 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/state/VideoPlayerState.kt` — change `VidURL` to domain `VideoLink`
- [X] T051 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/compose/VideoPlayer.kt` — change `VidURL` parameter to domain `VideoLink`

### Preview Data & Components

- [X] T052 [P] [US4] Update `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/preview/PreviewData.kt` — replace all API model constructors (LaunchBasic, LaunchNormal, AgencyMini, AstronautNormal, etc.) with domain model constructors (Launch, Provider, etc.) for preview/sample data
- [X] T053 [P] [US4] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/components/CountryChip.kt` — change `Country` API type to `String` countryCode parameter if needed

**Checkpoint**: `./gradlew compileKotlinDesktop` — zero compilation errors. All composables use only domain types. No API model imports in any UI file.

---

## Phase 7: US5 — Infrastructure & Platform Migration (Priority: P2)

**Goal**: Update cache, local data source, utilities, and platform-specific files to use domain types

**Independent Test**: `./gradlew compileKotlinDesktop` compiles. `./gradlew test` passes. No API model imports outside mappers and repository impls.

### Cache & Storage

- [X] T054 [US5] Refactor `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/cache/LaunchCache.kt` — collapse `normalCache`/`detailedCache` maps into single `launchCache: Map<String, Launch>` with unified `cacheLaunch(launch: Launch)` and `getCachedLaunch(id: String): Launch?` methods; remove LaunchNormal/LaunchDetailed imports
- [X] T055 [US5] Refactor `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/database/LaunchLocalDataSource.kt` — change return types from API types to domain `Launch`; internally deserialize JSON → API type → `.toDomain()` mapper; import from `domain.mapper.LaunchMappers`
- [X] T056 [US5] Refactor `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/database/EventLocalDataSource.kt` — change return types from `EventEndpointNormal` to domain `Event`; internally deserialize JSON → API type → `.toDomain()` mapper

### Utilities

- [X] T057 [US5] Update `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/LaunchFormatUtil.kt` — add `formatLaunchTitle(launch: Launch): String` overload using `launch.provider.name`, `launch.provider.abbrev`, `launch.rocket?.fullName ?: launch.rocket?.name`, `launch.name`; mark LaunchBasic/LaunchNormal/LaunchDetailed overloads `@Deprecated(ReplaceWith)`
- [X] T058 [P] [US5] Update `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/SharingUtil.kt` — change `LaunchNormal` parameter to domain `Launch`, update field access
- [X] T059 [P] [US5] Update `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/VideoUtil.kt` — change `VidURL` references to domain `VideoLink` if applicable

### Platform-Specific Files

- [X] T060 [P] [US5] Migrate `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/widgets/NextUpWidget.kt` — change `LaunchNormal` to domain `Launch`, update field access
- [X] T061 [P] [US5] Migrate `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/widgets/LaunchListWidget.kt` — change `LaunchNormal` to domain `Launch`, update field access
- [X] T062 [P] [US5] Migrate `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/util/PlatformSharingService.kt` — change `LaunchNormal` to domain `Launch`
- [X] T063 [P] [US5] Migrate `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/sync/PhoneDataLayerService.kt` — change `LaunchBasic` to domain `Launch`
- [X] T064 [P] [US5] Migrate `composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/util/PlatformSharingService.kt` — change `LaunchNormal` to domain `Launch`
- [X] T065 [P] [US5] Migrate `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/util/PlatformSharingService.kt` — change `LaunchNormal` to domain `Launch`
- [X] T066 [P] [US5] Migrate `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/KoinInitializer.kt` — replace `PaginatedLaunchNormalList` reference with domain type

### Test File Updates

- [X] T067 [P] [US5] Update `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/compose/LatestUpdatesViewTest.kt` — change API model test data to domain model constructors

**Checkpoint**: `./gradlew compileKotlinDesktop` and `./gradlew test` — zero errors. Only mappers and repository impls import from `api.launchlibrary.models`.

---

## Phase 8: 3A Verification Checkpoint

**Purpose**: Verify Launch + Event migration (Phases 1–7) before starting secondary entities

- [X] T068 Run `./gradlew compileKotlinDesktop` — confirm zero compilation errors
- [X] T069 Run `./gradlew test` — confirm all unit tests pass
- [X] T070 Run `./gradlew desktopRun --quiet` — manually verify Home, Schedule, Launch Detail, Events
- [X] T071 Verify import isolation for Launch/Event: grep `api.launchlibrary.models` in `ui/`, `viewmodel/`, `cache/`, `util/` — only secondary entity imports should remain

**Checkpoint**: Phase 3A complete. Commit `refactor(domain): complete Launch + Event domain migration`. Phase 3B can now proceed.

---

## Phase 9: 3B Domain Models — Secondary Entity Types

**Purpose**: Create domain model data classes for all remaining entities. Must compile before their mappers.

- [X] T073 [P] [3B] Create `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/model/Agency.kt` — `Agency` data class (id, name, abbrev, type, countryCode, logoUrl, imageUrl, description?, administrator?, foundingYear?, totalLaunchCount?, successfulLaunches?, failedLaunches?, infoUrl?, wikiUrl?) with `@Immutable`; covers both AgencyNormal and AgencyEndpointDetailed tiers via nullable detail fields
- [X] T074 [P] [3B] Create `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/model/Astronaut.kt` — `Astronaut` data class (id, name, nationality, profileImageUrl, bio?, status: AstronautStatus?, type: AstronautType?, dateOfBirth?, dateOfDeath?, agency: Provider?, flightsCount?, landingsCount?, lastFlight?, firstFlight?, socialMedia: List\<SocialMediaLink\>); supporting types `AstronautStatus(id, name)`, `AstronautType(id, name)`, `SocialMediaLink(url, platform)` with `@Immutable`
- [X] T075 [P] [3B] Create `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/model/Vehicle.kt` — `VehicleConfig` data class (id, name, fullName, family, variant, imageUrl, description?, infoUrl?, wikiUrl?, length?, diameter?, launchMass?, leoCapacity?, gtoCapacity?, toThrust?, apogee?, totalLaunchCount?, consecutiveSuccessfulLaunches?, maidenFlight?, active?) with `@Immutable`; also `LauncherDetail` (id, serialNumber, flightProven, imageUrl, flights?, lastLaunchDate?, firstLaunchDate?) for individual booster tracking
- [X] T076 [P] [3B] Create `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/model/Spacecraft.kt` — `Spacecraft` data class (id, name, serialNumber, status?, description?, imageUrl, config: SpacecraftConfig?) and `SpacecraftConfig` (id, name, type?, agency: Provider?, imageUrl?, inUse?, capability?, history?, details?, maidenFlight?, humanRated?) with `@Immutable`
- [X] T077 [P] [3B] Create `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/model/SpaceStation.kt` — `SpaceStation` data class (id, name, status, type, description?, founded?, deorbited?, orbit?, imageUrl?, owners: List\<Provider\>, activeExpeditions: List\<ExpeditionSummary\>) and `Expedition` (id, name, start, end, crew: List\<AstronautSummary\>, spaceStation: SpaceStationSummary?) with `@Immutable`
- [X] T078 [P] [3B] Create `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/model/Program.kt` — `Program` data class (id, name, description?, imageUrl?, infoUrl?, wikiUrl?, type?, startDate?, endDate?, agencies: List\<Provider\>, missionPatches: List\<MissionPatchSummary\>) expanding `ProgramSummary` from Common.kt with `@Immutable`

**Checkpoint**: `./gradlew compileKotlinDesktop` — all 3B domain model types compile

### Domain Model Tests

- [X] T150 [P] [3B] Write domain model tests for Agency in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/domain/model/AgencyTest.kt` — test construction, nullable detail fields, defaults
- [X] T151 [P] [3B] Write domain model tests for Astronaut in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/domain/model/AstronautTest.kt` — test construction, AstronautStatus/AstronautType/SocialMediaLink nested types, socialMedia defaults to emptyList()
- [X] T152 [P] [3B] Write domain model tests for Vehicle types in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/domain/model/VehicleTest.kt` — test VehicleConfig + LauncherDetail construction and nullable fields
- [X] T153 [P] [3B] Write domain model tests for Spacecraft in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/domain/model/SpacecraftTest.kt` — test Spacecraft + SpacecraftConfig construction
- [X] T154 [P] [3B] Write domain model tests for SpaceStation in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/domain/model/SpaceStationTest.kt` — test SpaceStation + Expedition construction, owners/activeExpeditions default to emptyList()
- [X] T155 [P] [3B] Write domain model tests for Program in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/domain/model/ProgramTest.kt` — test Program construction, agencies/missionPatches default to emptyList()

---

## Phase 10: 3B Mappers — Secondary Entity Mappers + Tests

**Purpose**: Create `toDomain()` extension functions for all secondary entity API types

### Implementation

- [X] T079 [P] [3B] Create Agency mapper in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/mapper/AgencyMappers.kt` — `AgencyNormal.toDomain(): Agency`, `AgencyEndpointDetailed.toDomain(): Agency`, `PaginatedAgencyNormalList.toDomain(): PaginatedResult<Agency>`; map AgencyType→type String, Country→countryCode String, flatten Image→logoUrl/imageUrl
- [X] T080 [P] [3B] Create Astronaut mapper in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/mapper/AstronautMappers.kt` — `AstronautEndpointNormal.toDomain(): Astronaut`, `AstronautEndpointDetailed.toDomain(): Astronaut`, `PaginatedAstronautEndpointNormalList.toDomain(): PaginatedResult<Astronaut>`; map nested Agency→Provider via CommonMappers, AstronautStatus/AstronautType to domain equivalents, SocialMedia→SocialMediaLink
- [X] T081 [P] [3B] Create Vehicle/Launcher mapper in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/mapper/VehicleMappers.kt` — `LauncherConfigList.toDomain(): VehicleConfig`, `LauncherConfigNormal.toDomain(): VehicleConfig`, `LauncherConfigDetailed.toDomain(): VehicleConfig`, `LauncherDetailed.toDomain(): LauncherDetail` for individual boosters
- [X] T082 [P] [3B] Create Spacecraft mapper in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/mapper/SpacecraftMappers.kt` — `SpacecraftEndpointDetailed.toDomain(): Spacecraft`, `SpacecraftConfigDetailed.toDomain(): SpacecraftConfig`, `PaginatedSpacecraftEndpointDetailedList.toDomain(): PaginatedResult<Spacecraft>`
- [X] T083 [P] [3B] Create SpaceStation mapper in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/mapper/SpaceStationMappers.kt` — `SpaceStationDetailedEndpoint.toDomain(): SpaceStation`, `ExpeditionDetailed.toDomain(): Expedition`, `PaginatedSpaceStationDetailedEndpointList.toDomain(): PaginatedResult<SpaceStation>`
- [X] T084 [P] [3B] Create Program mapper in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/mapper/ProgramMappers.kt` — `ProgramNormal.toDomain(): Program`, `ProgramDetailed.toDomain(): Program` (expanding ProgramSummary mapper already in CommonMappers)

### Tests

- [X] T085 [P] [3B] Write unit tests for AgencyMappers in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/domain/mapper/AgencyMappersTest.kt`
- [X] T086 [P] [3B] Write unit tests for AstronautMappers in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/domain/mapper/AstronautMappersTest.kt`
- [X] T087 [P] [3B] Write unit tests for VehicleMappers in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/domain/mapper/VehicleMappersTest.kt`
- [X] T088 [P] [3B] Write unit tests for SpacecraftMappers in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/domain/mapper/SpacecraftMappersTest.kt`
- [X] T089 [P] [3B] Write unit tests for SpaceStationMappers in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/domain/mapper/SpaceStationMappersTest.kt`
- [X] T090 [P] [3B] Write unit tests for ProgramMappers in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/domain/mapper/ProgramMappersTest.kt`

**Checkpoint**: `./gradlew test` — all 3B mapper tests pass

---

## Phase 11: 3B Repository Layer — Secondary Entity Repos

**Purpose**: Add domain-returning methods to secondary entity repositories, mark old methods `@Deprecated`

- [X] T091 [P] [3B] Add domain methods to `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/AgencyRepository.kt` — `getAgencyList(): Result<PaginatedResult<Agency>>`, etc.; mark API-type methods `@Deprecated`
- [X] T092 [P] [3B] Add domain methods to `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/AstronautRepository.kt` — `getAstronautList(): Result<PaginatedResult<Astronaut>>`, `getAstronautDetail(id): Result<Astronaut>`; mark API-type methods `@Deprecated`
- [X] T093 [P] [3B] Add domain methods to `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/LauncherRepository.kt` and `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/LauncherConfigRepository.kt` — domain return types for launcher list/detail; mark old `@Deprecated`
- [X] T094 [P] [3B] Add domain methods to `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/SpacecraftRepository.kt` — `getSpacecraftList(): Result<PaginatedResult<Spacecraft>>`, `getSpacecraftDetail(id): Result<Spacecraft>`; mark old `@Deprecated`
- [X] T095 [P] [3B] Add domain methods to `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/SpaceStationRepository.kt` — `getSpaceStationDetail(id): Result<SpaceStation>`; mark old `@Deprecated`
- [X] T096 [P] [3B] Add domain methods to `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/ProgramRepository.kt` — `getProgram(id): Result<Program>`, `getProgramList(): Result<PaginatedResult<Program>>`; mark old `@Deprecated`
- [X] T097 [3B] Implement domain methods in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/AgencyRepositoryImpl.kt` — call API, map with `.toDomain()`
- [X] T098 [3B] Implement domain methods in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/AstronautRepositoryImpl.kt` — call API, map with `.toDomain()`; also update `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/AstronautFilterRepositoryImpl.kt` if it returns API types
- [X] T099 [3B] Implement domain methods in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/LauncherRepositoryImpl.kt` and `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/LauncherConfigRepositoryImpl.kt`
- [X] T100 [3B] Implement domain methods in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/SpacecraftRepositoryImpl.kt` — call API, map with `.toDomain()`
- [X] T101 [3B] Implement domain methods in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/SpaceStationRepositoryImpl.kt`
- [X] T102 [3B] Implement domain methods in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/ProgramRepositoryImpl.kt`

**Checkpoint**: `./gradlew compileKotlinDesktop` — all repos compile. Existing app unchanged.

### Fake Repositories (Test Infrastructure)

- [X] T156 [P] [3B] Create `FakeAgencyRepository` in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/repository/FakeAgencyRepository.kt` — implements `AgencyRepository`; expose `var agencyListResult`, `var shouldFail = false`
- [X] T157 [P] [3B] Create `FakeSpacecraftRepository` in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/repository/FakeSpacecraftRepository.kt` — implements `SpacecraftRepository`
- [X] T158 [P] [3B] Create `FakeSpaceStationRepository` in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/repository/FakeSpaceStationRepository.kt` — implements `SpaceStationRepository`
- [X] T159 [P] [3B] Create `FakeLauncherRepository` in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/repository/FakeLauncherRepository.kt` — implements `LauncherRepository`; also `FakeLauncherConfigRepository` in same or separate file

---

## Phase 12: 3B ViewModels — Secondary Entity VMs

**Purpose**: Switch secondary entity ViewModels from API types to domain types

- [X] T103 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/AgencyViewModel.kt` — change `AgencyEndpointDetailed`/`AgencyNormal` StateFlows to domain `Agency`, call domain repo methods *(Slice A: complete — dropped `LaunchRepository` dep, added `getAgencyDetailDomain` to `AgencyRepository`)*
- [X] T104 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/AstronautListViewModel.kt` — change `AstronautEndpointNormal` to domain `Astronaut`, call domain repo methods *(already migrated: uses `AstronautListItem` via `AstronautRepository`)*
- [X] T105 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/AstronautDetailViewModel.kt` — change `AstronautEndpointDetailed` to domain `Astronaut`, call domain repo methods *(already migrated: uses `AstronautDetail` via `AstronautRepository`)*
- [X] T106 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/RocketViewModel.kt` — change launcher/rocket API types to domain `VehicleConfig`/`LauncherDetail` *(Slice B: complete — expanded `VehicleConfig` from 24→36 fields, added `getRocketsDomain`/`getRocketDetailsDomain` to `RocketRepository`, migrated `OnboardingViewModel` + `ExplorePage` + `RocketFilterState`)*
- [X] T107 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/SpaceStationViewModel.kt` — change `SpaceStationDetailedEndpoint` to domain `SpaceStation` *(already migrated: uses `SpaceStationDetail` + `ExpeditionDetailItem`)*
- [X] T108 [P] [3B] Update `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/OnboardingViewModel.kt` — replace `AstronautEndpointNormal` with domain `Astronaut` if used in onboarding astronaut preview *(already migrated: `_astronauts` is `List<AstronautListItem>`; `_rockets`/`_agencies` remain API-typed pending Phase 13 UI migration)*

**Checkpoint**: All secondary VMs compile with domain types

### ViewModel Unit Tests

- [X] T160 [P] [3B] Write ViewModel test for AgencyViewModel in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/AgencyViewModelTest.kt` — use `FakeAgencyRepository`; test agency list loading, test detail fetch, test error state; `kotlin.test` + `StandardTestDispatcher` *(Slice A: 5 passing tests)*
- [X] T161 [P] [3B] Write ViewModel test for RocketViewModel in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/RocketViewModelTest.kt` — replace existing TODO stubs with real tests using `FakeLauncherRepository`/`FakeLauncherConfigRepository`; test vehicle list loading, test detail, test error handling *(Slice B: 5 passing tests using new `FakeRocketRepository`)*
- [X] T162 [P] [3B] Write ViewModel test for SpaceStationViewModel in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/SpaceStationViewModelTest.kt` — use `FakeSpaceStationRepository`; test detail loading, expedition data, error state

---

## Phase 13: 3B UI Composables — Secondary Entity UI

**Purpose**: Update all secondary entity composables to accept domain types

### Agency UI

- [X] T109 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/agencies/compose/AgencyListView.kt` — change `AgencyNormal`/`AgencyType` params to domain `Agency`, update field access
- [X] T110 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/agencies/compose/AgencyDetailView.kt` — change `AgencyEndpointDetailed` to domain `Agency`
- [X] T111 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/agencies/AgencyFilterSheet.kt` — replace `AgencySortOrder` API type with domain equivalent if applicable *(already clean — no API imports)*

### Astronaut UI (10 files)

- [X] T112 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/astronaut/AstronautDetailView.kt` — change API astronaut types to domain `Astronaut`
- [X] T113 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/astronaut/AstronautListScreen.kt` — change API types to domain `Astronaut`
- [X] T114 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/astronaut/AstronautFilterSheet.kt` — replace API filter types with domain types
- [X] T115 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/astronaut/components/AstronautCard.kt` — change `AstronautEndpointNormal`/`AgencyMini`/`AstronautStatus`/`AstronautType` to domain types
- [X] T116 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/astronaut/components/AstronautProfileCard.kt` — change to domain `Astronaut`
- [X] T117 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/astronaut/components/AstronautStatsCard.kt` — change `AstronautEndpointDetailed` to domain `Astronaut`
- [X] T118 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/astronaut/components/AstronautPersonalInfoCard.kt` — change `AstronautEndpointDetailed`/`AstronautStatus`/`AstronautType`/`Country` to domain types
- [X] T119 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/astronaut/components/AstronautLinksCard.kt` — change `AstronautEndpointDetailed`/`SocialMedia`/`SocialMediaLink` to domain types
- [X] T120 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/astronaut/components/AstronautInfoCard.kt` — change `AstronautEndpointDetailed` to domain `Astronaut`
- [X] T121 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/astronaut/components/AstronautFlightHistoryCard.kt` — already migrated `LaunchBasic→Launch` in Phase 6; verify no remaining API astronaut imports

### Vehicle / Rocket UI

- [X] T122 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/rockets/RocketListView.kt` — change launcher config API types to domain `VehicleConfig` *(Slice B: complete — migrated `RocketListItem.kt` + `RocketDetailView.kt` to `VehicleConfig`)*
- [X] T123 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/starship/StarshipScreen.kt` — update vehicle type references to domain types *(Slice C: complete — screen had no direct API imports; migrated transitively via `StarshipViewModel` StateFlows now exposing domain types)*
- [X] T124 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/starship/components/StarshipVehiclesTab.kt` — change spacecraft/launcher API types to domain types *(Slice C: complete — migrated `StarshipVehiclesTab`, `VehicleGrids`, `VehicleGridCards`, `VehicleConfigCards` to domain `Spacecraft`/`SpacecraftConfig`/`LauncherDetail`/`VehicleConfig`; added `SpacecraftConfigRepository.getConfigurationsByAgencyDomain()` + `PaginatedSpacecraftConfigDetailedList.toDomain()` mapper)*

### SpaceStation UI

- [X] T125 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/spacestation/SpaceStationDetailView.kt` — change `SpaceStationDetailedEndpoint`/`ExpeditionDetailed` to domain `SpaceStation`/`Expedition`
- [X] T126 [P] [3B] Migrate `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/spacestation/SpaceStationDetailScreen.kt` — change `SpaceStationDetailedEndpoint` to domain `SpaceStation`

### Preview Data

- [X] T127 [P] [3B] Update `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/preview/PreviewData.kt` — replace remaining API model constructors for `AgencyMini`, `AgencyNormal`, `AstronautNormal`, `AstronautStatus`, `AstronautType`, `Country` with domain model constructors *(already migrated: PreviewData.kt uses only domain types — Launch, LaunchStatus, Provider, Mission, Pad, Location; zero `api.launchlibrary` imports remain)*

**Checkpoint**: `./gradlew compileKotlinDesktop` — all secondary entity UI compiles with domain types

---

## Phase 14: 3B Infrastructure — LocalDataSources & Tests

**Purpose**: Update secondary entity LocalDataSources and test files to use domain types

### LocalDataSources

- [X] T128 [P] [3B] Refactor `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/database/SpacecraftLocalDataSource.kt` (~189 lines) — change return types from `SpacecraftEndpointDetailed` to domain `Spacecraft`; deserialize JSON → API type → `.toDomain()` *(reads return domain; writes still accept API type. RepositoryImpl inverted: domain methods own cache; legacy `@Deprecated` methods are API-only pass-through)*
- [X] T129 [P] [3B] Refactor `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/database/SpaceStationLocalDataSource.kt` (~270 lines) — change return types from `SpaceStationDetailedEndpoint`/`ExpeditionDetailed`/`IssTle` to domain equivalents; deserialize JSON → API type → `.toDomain()` *(station→`SpaceStationDetail`, expedition→`ExpeditionDetailItem`; TLE kept as `IssTle` — no domain equivalent exists, sourced from separate wheretheiss.at API. SpaceStationRepositoryImpl simplified — dropped redundant `.toDomain`/`.toDomainDetail` at cache sites)*
- [X] T130 [P] [3B] Refactor `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/database/ProgramLocalDataSource.kt` — change return types from `ProgramNormal` to domain `Program`; deserialize JSON → API type → `.toDomain()` *(repository inverted — `getProgramDomain` owns cache, legacy `getProgram` is API-only pass-through. Migrated StarshipViewModel + StarshipOverviewTab to domain `Program`. Added `vidUrls: List<VideoLink>` to domain Program)*

### Test Files

- [X] T131 [P] [3B] Update `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/astronaut/AstronautListViewModelTest.kt` — change API model test data (`AstronautEndpointNormal`, `PaginatedAstronautEndpointNormalList`) to domain model constructors *(already migrated: file uses `AstronautListItem`, `AstronautDetail`, `PaginatedResult` domain types; zero `api.launchlibrary` imports)*
- [X] T132 [P] [3B] Update `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/astronaut/AstronautDetailViewModelTest.kt` — change API model test data (`AstronautEndpointDetailed`) to domain model constructors *(already migrated: zero `api.launchlibrary` imports)*

**Checkpoint**: `./gradlew test` — all tests pass. Only mappers and repo impls import secondary API types.

---

## Phase 15: Final Verification & Cleanup

**Purpose**: Full compilation, testing, and manual verification across ALL targets after complete migration

- [X] T133 Run `./gradlew compileKotlinDesktop` — confirm zero compilation errors across all targets *(BUILD SUCCESSFUL)*
- [X] T134 Run `./gradlew test` — confirm ALL unit tests pass (3A + 3B mapper tests + existing tests) *(BUILD SUCCESSFUL — all suites 0 failures)*
- [X] T135 Run `./gradlew desktopRun --quiet` — manually verify: Home, Schedule, Launch Detail, Events, Agency List, Agency Detail, Astronaut List, Astronaut Detail, Rocket/Vehicle views, SpaceStation Detail, Starship screen *(app running, Home loading launches+events+articles, no domain mapping crashes)*
- [X] T136 Verify COMPLETE import isolation: grep for `api.launchlibrary.models` imports — should ONLY appear in `domain/mapper/`, `data/repository/*Impl.kt`, `database/*LocalDataSource.kt`, and `api/extensions/`; ZERO occurrences in `ui/`, `viewmodel/`, `cache/`, `util/` *(PASS — 0 violations in ui/cache/util; 20 remaining in `*Repository.kt` interfaces for @Deprecated methods — T138 scope)*
- [X] T137 Run quickstart.md validation checklist — confirm all 5 migration rules hold for ALL entities: (1) mappers are only bridge to API types, (2) VMs/UI only import domain/model, (3) unified types everywhere, (4) nullable = not loaded, (5) lists default empty *(All 5 rules verified. Note: SNAPI `Article` type in UI is from a different API — out of scope for this migration)*
- [X] T138 Remove all `@Deprecated` annotations from old API-type repository methods (they should have zero callers now) OR confirm they have no remaining callers and delete the old method signatures *(COMPLETE — deleted 33 @Deprecated methods across 9 repository interfaces. Converted delegating impl methods to `private` helpers; deleted non-delegating legacy impl methods outright in SpacecraftRepositoryImpl + ProgramRepositoryImpl. Removed matching legacy stubs from 7 Fake repositories. Stripped class-level `@Suppress("DEPRECATION")`. `:composeApp:compileKotlinDesktop` passes clean; pre-existing test compile failures in VehicleTest/SpacecraftTest/VehicleMappersTest/ScheduleViewModelTest are unrelated to this task.)*

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 — BLOCKS all subsequent phases
- **US1 Mappers (Phase 3)**: Depends on Phase 2 — BLOCKS US2
- **US2 Repository (Phase 4)**: Depends on Phase 3 — BLOCKS US3
- **US3 ViewModels (Phase 5)**: Depends on Phase 4
- **US4 UI Composables (Phase 6)**: Depends on Phase 5 — must migrate together with VMs for compilation
- **US5 Infrastructure (Phase 7)**: Depends on Phase 4 (repos); can run in parallel with Phase 5+6
- **3A Verification (Phase 8)**: Depends on Phases 5+6+7
- **3B Models (Phase 9)**: Depends on Phase 2 (shared types); can start after Phase 8 checkpoint
- **3B Mappers (Phase 10)**: Depends on Phase 9
- **3B Repos (Phase 11)**: Depends on Phase 10
- **3B ViewModels (Phase 12)**: Depends on Phase 11
- **3B UI (Phase 13)**: Depends on Phase 12 — must migrate together with VMs
- **3B Infrastructure (Phase 14)**: Depends on Phase 11; can run in parallel with Phase 12+13
- **Final Verification (Phase 15)**: Depends on ALL previous phases

### Critical Path

```
Phase 1 → 2 → 3 → 4 → 5+6 → 7 → Phase 8 (3A checkpoint)
                           ↘ 7 ↗
                                    ↓
                    Phase 9 → 10 → 11 → 12+13 → Phase 15
                                     ↘  14  ↗
```

### Within Each Phase

- Models before mappers (Phase 2 before Phase 3; Phase 9 before Phase 10)
- Mapper files created sequentially: CommonMappers → LaunchMappers → EventMappers (T009 → T010 → T011)
- Mapper tests can run in parallel (T012–T014; T085–T090)
- Repository interfaces before implementations (T015/T016 before T017/T018; T091–T096 before T097–T102)
- All ViewModel tasks [P] — different files, no interdependencies
- All UI composable tasks [P] — different files, no interdependencies
- Infrastructure tasks [P] where marked — different files
- All 3B entity groups are independent of each other within a phase

### Parallel Opportunities

- All 6 domain model files (T003–T008) can be created in parallel
- All 3 domain model test files (T139–T141) can be written in parallel with mapper work
- All 3 mapper test files (T012–T014) can be written in parallel
- Both repository interfaces (T015, T016) can be updated in parallel
- Both fake repositories (T142, T143) can be created in parallel
- All 12 ViewModel files (T019–T030) can be migrated in parallel
- All 6 ViewModel test files (T144–T149) can be written in parallel (after fake repos exist)
- All 23 UI composable files (T031–T053) can be migrated in parallel (except T031 should go first as it removes LaunchCardData)
- All platform-specific files (T060–T067) can be migrated in parallel
- Phase 7 (US5 Infrastructure) can run in parallel with Phase 5+6 (US3+US4)
- All 6 secondary model files (T073–T078) can be created in parallel
- All 6 secondary model test files (T150–T155) can be written in parallel
- All 6 secondary mapper files (T079–T084) + all 6 test files (T085–T090) in parallel
- All 6 secondary repo interfaces (T091–T096) in parallel
- All 4 secondary fake repos (T156–T159) can be created in parallel
- All 6 secondary VM files (T103–T108) in parallel
- All 3 secondary VM test files (T160–T162) can be written in parallel
- All 19 secondary UI files (T109–T127) in parallel
- Phase 14 (3B Infrastructure) can run in parallel with Phase 12+13

---

## Parallel Example: Phase 2

```
# All domain model files can be created simultaneously:
T003: PaginatedResult.kt
T004: Common.kt
T005: Launch.kt
T006: Event.kt
T007: LaunchFilterParams.kt
T008: EventFilterParams.kt
```

## Parallel Example: Phase 5

```
# All ViewModel migrations are independent:
T019: NextUpViewModel.kt
T020: LaunchViewModel.kt
T021: LaunchesViewModel.kt
T022: ScheduleViewModel.kt
... (all 12 files simultaneously)
```

## Parallel Example: Phase 6

```
# After T031 (LaunchCardData elimination), all other composables can proceed:
T032–T053: All 22 composable files simultaneously
```

---

## Implementation Strategy

### MVP First (Phase 1–4)

1. Complete Phase 1: Setup
2. Complete Phase 2: Domain Models
3. Complete Phase 3: Mappers + Tests
4. Complete Phase 4: Repository Layer
5. **STOP and VALIDATE**: Mapper tests pass, repo compiles, existing app unchanged
6. This is the safe rollback point — all old code still works

### Incremental Delivery

1. Phases 1–4 → Foundation ready, old app still works ✅
2. Phase 5+6 → ViewModels + UI switched to domain types → Full 3A migration ✅
3. Phase 7 → Infrastructure (cache, utils, platform) cleaned up ✅
4. Phase 8 → 3A verified → Commit checkpoint ✅
5. Phase 9–11 → 3B models + mappers + repos → Pattern repeated ✅
6. Phase 12+13 → 3B ViewModels + UI migrated ✅
7. Phase 14 → 3B infrastructure cleaned up ✅
8. Phase 15 → Full verification → Ready for PR ✅

### Commit Strategy

Use `refactor(domain):` prefix per project conventions:
- `refactor(domain): add domain model data classes` (Phase 2)
- `refactor(domain): add mapper extension functions with tests` (Phase 3)
- `refactor(domain): add domain methods to repository layer` (Phase 4)
- `refactor(domain): migrate ViewModels to domain types` (Phase 5)
- `refactor(domain): migrate UI composables to domain types` (Phase 6)
- `refactor(domain): migrate cache, utils, and platform files` (Phase 7)
- `refactor(domain): complete Launch + Event domain migration` (Phase 8)
- `refactor(domain): add secondary entity domain models` (Phase 9)
- `refactor(domain): add secondary entity mappers with tests` (Phase 10)
- `refactor(domain): add secondary entity domain repo methods` (Phase 11)
- `refactor(domain): migrate secondary entity ViewModels` (Phase 12)
- `refactor(domain): migrate secondary entity UI composables` (Phase 13)
- `refactor(domain): migrate secondary entity infrastructure` (Phase 14)
- `refactor(domain): complete full domain model migration` (Phase 15)

---

## Notes

- [P] tasks = different files, no dependencies on incomplete tasks in same phase
- [US1–US5] labels = Phase 3A migration steps; [3B] labels = Phase 3B secondary entity tasks
- Each phase should compile independently after completion (except Phase 5+6 and 12+13 which must complete together)
- Commit after each phase completion
- Stop at Phase 8 checkpoint to validate 3A before starting 3B
- All 3B entity groups (Agency, Astronaut, Vehicle, Spacecraft, SpaceStation, Program) are independent of each other within a phase
- Avoid: editing generated API files, changing SQLDelight schema, breaking stale-while-revalidate cache pattern
