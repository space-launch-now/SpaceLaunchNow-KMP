# Tasks: Phase 3 — Introduce Domain Models in KMP

Decouple KMP's ViewModel and UI layers from any specific API response types by introducing a domain model layer. The repository layer becomes the mapping boundary. **Phased**: Launches + Events first (3A), then all remaining entities (3B).

**Depends on**: Nothing — this work is independent and can be done in parallel with Phase 2.

**Key files**:
- `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/` — Repository interfaces + implementations
- `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/` — ViewModels
- `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/LaunchFormatUtil.kt` — Formatting utilities
- `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/database/LaunchLocalDataSource.kt` — SQLDelight cache
- `composeApp/src/commonMain/sqldelight/me/calebjones/spacelaunchnow/database/Launch.sq` — SQLDelight schema

---

## Phase 3A: Core Domain Models — Launches + Events

### 3A-1 — Create domain model package

Create all files in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/model/`

**Shared primitives** — create `PaginatedResult.kt`:
- [ ] `data class PaginatedResult<T>(val count: Int, val next: String?, val previous: String?, val results: List<T>)`

**Supporting value types** — create `common.kt` (or split per concept):
- [ ] `data class LaunchStatus(val id: Int, val name: String, val abbrev: String, val description: String)`
- [ ] `data class Provider(val id: Int, val name: String, val type: String?, val countryCode: String?, val logoUrl: String?)`
- [ ] `data class RocketConfig(val id: Int, val fullName: String, val family: String?, val variant: String?)`
- [ ] `data class Pad(val id: Int, val name: String, val latitude: Double?, val longitude: Double?)`
- [ ] `data class Location(val id: Int, val name: String, val countryCode: String?)`
- [ ] `data class Orbit(val name: String?, val abbrev: String?)`
- [ ] `data class Mission(val name: String?, val description: String?, val type: String?, val orbit: Orbit?)`
- [ ] `data class ProgramSummary(val id: Int, val name: String, val imageUrl: String?)`
- [ ] `data class MissionPatch(val name: String?, val imageUrl: String?)`
- [ ] `data class Update(val id: Int, val profileImage: String?, val comment: String?, val createdBy: String?, val createdOn: kotlinx.datetime.Instant?)`

**Launch models** — create `Launch.kt`:
- [ ] `data class Launch(val id: String, val name: String, val net: kotlinx.datetime.Instant?, val windowStart: kotlinx.datetime.Instant?, val windowEnd: kotlinx.datetime.Instant?, val status: LaunchStatus, val provider: Provider, val rocket: RocketConfig, val pad: Pad, val location: Location, val mission: Mission?, val imageUrl: String?, val thumbnailUrl: String?, val isCrewed: Boolean, val webcastLive: Boolean, val programs: List<ProgramSummary>, val vidUrls: List<String>, val missionPatches: List<MissionPatch>, val holdreason: String?, val failreason: String?, val hashtag: String?)`
- [ ] `data class LaunchDetail(val launch: Launch, val infoUrls: List<String>, val updates: List<Update>, val rocketStages: List<RocketStage>, val spacecraftFlights: List<SpacecraftFlightSummary>, val crew: List<CrewMemberSummary>, val landingAttempts: List<LandingAttemptSummary>)`
- [ ] Supporting detail types: `data class RocketStage(val id: Int, val type: String?, val reused: Boolean?, val launcherFlight: Int?, val turnaroundFlightTime: Int?, val launcher: LauncherSummary?, val landingAttempt: LandingAttemptSummary?)`
- [ ] `data class LauncherSummary(val id: Int, val serialNumber: String?, val flightProven: Boolean, val imageUrl: String?)`
- [ ] `data class LandingAttemptSummary(val id: Int, val outcome: String?, val description: String?, val location: String?, val type: String?)`
- [ ] `data class SpacecraftFlightSummary(val id: Int, val serialNumber: String?, val spacecraftName: String?, val destination: String?, val missionEnd: kotlinx.datetime.Instant?)`
- [ ] `data class CrewMemberSummary(val id: Int, val name: String, val role: String?, val nationality: String?, val profileImageUrl: String?)`

**Event models** — create `Event.kt`:
- [ ] `data class EventType(val id: Int, val name: String)`
- [ ] `data class Event(val id: Int, val name: String, val type: EventType, val description: String?, val date: kotlinx.datetime.Instant?, val location: String?, val newsUrl: String?, val videoUrl: String?, val imageUrl: String?, val featureImageUrl: String?, val launches: List<Launch>, val programs: List<ProgramSummary>, val updates: List<Update>)`
- [ ] `data class EventDetail(val event: Event, val spaceStations: List<SpaceStationSummary>)`
- [ ] `data class SpaceStationSummary(val id: Int, val name: String, val imageUrl: String?)`

### 3A-2 — Create mapper interfaces and LL implementations

Create files in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/mapper/`

**Interfaces** — create `LaunchMapper.kt`:
- [ ] `interface LaunchMapper { fun mapBasic(launch: LaunchBasic): Launch; fun mapNormal(launch: LaunchNormal): Launch; fun mapDetailed(launch: LaunchDetailed): LaunchDetail }`

**Interfaces** — create `EventMapper.kt`:
- [ ] `interface EventMapper { fun mapEvent(event: me.calebjones.spacelaunchnow.api.launchlibrary.models.Event): Event; fun mapEventDetailed(event: EventDetailed): EventDetail }`

**LL implementation** — create `LLLaunchMapper.kt`:
- [ ] `class LLLaunchMapper : LaunchMapper` — implement all three mapping methods
  - Map `LaunchBasic.id.toString()` → `Launch.id`
  - Map nested `lsp` → `Provider`
  - Map nested `rocket.configuration` → `RocketConfig`
  - Map nested `pad` → `Pad`, `pad.location` → `Location`
  - Map nested `mission` → `Mission` (with orbit)
  - Map `status` → `LaunchStatus`
  - Map `program` list → `List<ProgramSummary>`
  - For `LaunchDetailed`, map `rocket.launcherstage[]` → `List<RocketStage>`, `spacecraft_stage` → `SpacecraftFlightSummary`, `crew` → `List<CrewMemberSummary>`, `updates` → `List<Update>`

**LL implementation** — create `LLEventMapper.kt`:
- [ ] `class LLEventMapper(private val launchMapper: LaunchMapper) : EventMapper` — implement both mapping methods
  - Map `type` → `EventType`
  - Map `launches[]` → `List<Launch>` using injected `LaunchMapper.mapNormal()`
  - Map `updates[]` → `List<Update>`
  - Map `spacestations[]` → `List<SpaceStationSummary>` for detail

### 3A-3 — Update repository interfaces

- [ ] In `LaunchRepository` interface: add new methods returning domain types alongside existing methods (keep old methods `@Deprecated` during transition):
  - `suspend fun getUpcomingLaunches(limit: Int, offset: Int = 0): Result<PaginatedResult<Launch>>`
  - `suspend fun getPreviousLaunches(limit: Int, offset: Int = 0): Result<PaginatedResult<Launch>>`
  - `suspend fun getLaunchDetail(id: String): Result<LaunchDetail>`
  - `suspend fun getLaunchesFiltered(params: LaunchFilterParams): Result<PaginatedResult<Launch>>`
  - `data class LaunchFilterParams(val statusIds: List<Int> = emptyList(), val providerIds: List<Int> = emptyList(), val locationIds: List<Int> = emptyList(), val rocketConfigIds: List<Int> = emptyList(), val programIds: List<Int> = emptyList(), val orbitIds: List<Int> = emptyList(), val includeSuborbital: Boolean? = null, val search: String? = null, val limit: Int = 20, val offset: Int = 0, val ordering: String? = null)` — define in `domain/model/LaunchFilterParams.kt`
- [ ] In `EventRepository` interface: add domain-typed methods:
  - `suspend fun getUpcomingEvents(limit: Int, offset: Int = 0): Result<PaginatedResult<Event>>`
  - `suspend fun getPreviousEvents(limit: Int, offset: Int = 0): Result<PaginatedResult<Event>>`
  - `suspend fun getEventDetail(id: Int): Result<EventDetail>`
  - `suspend fun getEventsFiltered(params: EventFilterParams): Result<PaginatedResult<Event>>`
  - `data class EventFilterParams(val typeIds: List<Int> = emptyList(), val programIds: List<Int> = emptyList(), val agencyIds: List<Int> = emptyList(), val search: String? = null, val limit: Int = 20, val offset: Int = 0)` — define in `domain/model/EventFilterParams.kt`

### 3A-4 — Update repository implementations

- [ ] In `LaunchRepositoryImpl`:
  - Inject `LaunchMapper` (via constructor parameter)
  - Implement the new domain-typed methods: call existing extension functions, map result through `launchMapper`
  - Example: `override suspend fun getUpcomingLaunches(limit: Int, offset: Int): Result<PaginatedResult<Launch>> = try { val response = launchesApi.getLaunchMiniList(limit = limit, offset = offset, upcoming = true); val body = response.body(); Result.success(PaginatedResult(body.count ?: 0, body.next, body.previous, body.results?.map { launchMapper.mapBasic(it) } ?: emptyList())) } catch (e: ResponseException) { Result.failure(e) }`

- [ ] In `EventRepositoryImpl`:
  - Inject `EventMapper`
  - Implement domain-typed methods the same way

- [ ] Register `LLLaunchMapper` and `LLEventMapper` in `AppModule.kt`:
  - `single<LaunchMapper> { LLLaunchMapper() }`
  - `single<EventMapper> { LLEventMapper(get()) }`
  - Add mapper types to `LaunchRepositoryImpl` and `EventRepositoryImpl` constructors in Koin bindings

### 3A-5 — Update ViewModels to use domain types

- [ ] `NextUpViewModel`: change `StateFlow<LaunchNormal?>` → `StateFlow<Launch?>`, call the new repository method
- [ ] `LaunchViewModel`: change `StateFlow<LaunchDetailed?>` → `StateFlow<LaunchDetail?>`, call `getLaunchDetail()`
- [ ] `ScheduleViewModel`:
  - Change upcoming/previous launch `StateFlow` from `List<LaunchBasic>` / `List<LaunchNormal>` → `List<Launch>`
  - Update `LaunchFilterParams` usage (was likely raw API params, now uses domain `LaunchFilterParams`)
- [ ] `NewsEventsViewModel` / `EventsViewModel`:
  - Change events `StateFlow` from generated event types → `List<Event>` / `PaginatedResult<Event>`
  - Update filter state to use domain `EventFilterParams`
- [ ] Any other ViewModel that directly holds generated API types — scan and update

### 3A-6 — Update UI Composables

- [ ] `LaunchCardHeader.kt`: replace `LaunchCardData` sealed interface (or any class holding generated types) with domain `Launch`; update all `when` branches
- [ ] `CountdownWidget.kt`: replace `LaunchStatus` from generated package → domain `LaunchStatus`
- [ ] `LaunchFormatUtil.kt`:
  - Add `formatLaunchTitle(launch: Launch): String` overload using `launch.provider.name` and `launch.rocket.fullName`
  - Add `formatLaunchTitle(launch: LaunchDetail): String` delegating to the `Launch` overload
  - Mark old generated-type overloads as `@Deprecated(message = "Use domain Launch type", replaceWith = ReplaceWith("formatLaunchTitle(launch)"))`
- [ ] Search for all `import me.calebjones.spacelaunchnow.api.launchlibrary.models.*` usages in composables — replace with domain model imports where affected by changes above
- [ ] Check `LaunchDetailScreen`, `LaunchListView`, `EventDetailScreen`, `EventListView`, `ScheduleScreen` — update property access to match domain model field names

### 3A-7 — Update local cache (LaunchLocalDataSource)

> **Recommendation**: Keep storing the raw API JSON blob in SQLDelight and map to domain models on read (zero DB migration needed for 3A). Revisit storage schema in Phase 4 when switching API backend.

- [ ] In `LaunchLocalDataSource.kt`: ensure the JSON deserialization path produces generated types that are then passed through `LaunchMapper.mapBasic()` / `mapNormal()` before being returned to callers
- [ ] Update all `LaunchLocalDataSource` call sites that currently return generated types to return domain `Launch` instead
- [ ] If any `StateFlow` or `Flow` in the local data source exposes generated types, wrap with `.map { launchMapper.mapBasic(it) }`

---

## Phase 3B: Secondary Domain Models

Apply the same 7-step pattern (domain models → mappers → repository interface → repository impl → ViewModel → UI → cache) for each remaining entity. Each can be done independently.

### 3B-1 — Agency domain models

- [ ] Create `domain/model/Agency.kt`: `data class Agency(...)`, `data class AgencyDetail(...)`
- [ ] Create `domain/mapper/AgencyMapper.kt` interface + `LLAgencyMapper.kt` implementation
- [ ] Update `AgencyRepository` interface + `AgencyRepositoryImpl` + Koin registration
- [ ] Update `AgencyViewModel`, `AgencyListView`, `AgencyDetailView` to use domain types

### 3B-2 — Astronaut domain models

- [ ] Create `domain/model/Astronaut.kt`: `data class Astronaut(...)`, `data class AstronautDetail(...)`
- [ ] Create `domain/mapper/AstronautMapper.kt` interface + `LLAstronautMapper.kt` implementation
- [ ] Update `AstronautRepository` interface + `AstronautRepositoryImpl` + Koin registration
- [ ] Update `AstronautViewModel`, astronaut UI composables

### 3B-3 — Vehicle / RocketConfig domain models

- [ ] Create `domain/model/Vehicle.kt`: `data class VehicleConfig(...)`, `data class VehicleConfigDetail(...)`
- [ ] Create `domain/mapper/VehicleMapper.kt` interface + `LLVehicleMapper.kt` implementation
- [ ] Update `LauncherConfigRepository` interface + implementation + Koin registration
- [ ] Update `VehicleViewModel`, `VehicleListView`, `VehicleDetailView`

### 3B-4 — Launcher (booster) domain models

- [ ] Create `domain/model/Launcher.kt`: `data class Launcher(...)`, `data class LauncherDetail(...)`
- [ ] Create `domain/mapper/LauncherMapper.kt` + `LLLauncherMapper.kt`
- [ ] Update `LauncherRepository` interface + implementation
- [ ] Update `LauncherViewModel`, `LauncherDetailView`

### 3B-5 — Spacecraft domain models

- [ ] Create `domain/model/Spacecraft.kt`: `data class Spacecraft(...)`, `data class SpacecraftDetail(...)`, `data class SpacecraftConfig(...)`, `data class SpacecraftConfigDetail(...)`
- [ ] Create `domain/mapper/SpacecraftMapper.kt` + `LLSpacecraftMapper.kt`
- [ ] Update `SpacecraftRepository` interface + implementation
- [ ] Update `SpacecraftViewModel`, spacecraft UI composables

### 3B-6 — Space Station + Expedition domain models

- [ ] Create `domain/model/SpaceStation.kt`: `data class SpaceStation(...)`, `data class Expedition(...)`, `data class ExpeditionDetail(...)`
- [ ] Create `domain/mapper/SpaceStationMapper.kt` + `LLSpaceStationMapper.kt`
- [ ] Update `SpaceStationRepository` interface + implementation
- [ ] Update `SpaceStationViewModel`, space station UI composables

### 3B-7 — Program domain models

- [ ] Create `domain/model/Program.kt`: `data class Program(...)`, `data class ProgramDetail(...)` (expands `ProgramSummary` from 3A)
- [ ] Create `domain/mapper/ProgramMapper.kt` + `LLProgramMapper.kt`
- [ ] Update `ProgramRepository` interface + implementation
- [ ] Update `ProgramViewModel`, `ProgramDetailView`

### 3B-8 — Config type domain models (LaunchStatus, Orbit, MissionType, etc.)

- [ ] Verify all config types already covered by shared value types created in 3A-1
- [ ] If any ViewModel or composable still uses raw generated config list types (e.g., `List<me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchStatus>`), replace with domain types
- [ ] Check filter UI screens (`StatusFilterSheet`, `OrbitFilterSheet`, etc.) for generated type usage

---

## Phase 3C: Verification

- [ ] `./gradlew compileKotlinDesktop` — zero compile errors across all three targets
- [ ] `./gradlew test` — all unit tests pass
  - Update any test that instantiates generated API types directly to use either domain types or mappers
- [ ] Desktop: run app (`./gradlew desktopRun`) and verify:
  - Home screen loads upcoming launches with correct title formatting
  - Schedule page loads and filters work (status, location, etc.)
  - Launch detail screen shows all sections (mission, crew, rocket stages, updates)
  - Events tab shows upcoming and previous events
  - Event detail shows launches, updates, space stations
- [ ] Android: `./gradlew installDebug` and verify same screens on device/emulator
- [ ] Confirm no composable directly imports from `me.calebjones.spacelaunchnow.api.launchlibrary.models` (except mappers and repository implementations)
- [ ] `LaunchFormatUtil` tests pass with new domain `Launch` type overloads
- [ ] `@Deprecated` annotations on old methods show warnings in IDE — confirm no non-mapper usage remains
