# Tasks: Phase 4 — Migrate KMP to SLN-API

Swap KMP's API backend from Launch Library 2.4.0 to SLN-API. Because domain models (Phase 3) have already decoupled the UI/ViewModel from API types, this is purely a mapper + repository swap with no UI changes required.

**Depends on**:
- Phase 2 complete — SLN-API has all endpoints KMP needs (agencies, programs, locations, launchers, spacecraft, spacecraft-configurations, updates, expeditions, all filter params)
- Phase 3A minimum — domain models exist for Launches + Events (required for initial cutover)
- Phase 3B recommended before removing LL client entirely

**Key files**:
- `composeApp/build.gradle.kts` — OpenAPI generation tasks
- `composeApp/openapi-config.yaml` — LL API generation config (will be superseded)
- `specs/` — target dir for SLN-API spec
- `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/di/` — Koin modules
- `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/mapper/` — mappers from Phase 3
- `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/` — repositories

---

## Phase 4A: Generate SLN-API Client in KMP

### 4A-1 — Add SLN-API OpenAPI spec to KMP

- [ ] From `SpaceLaunchNow-API/openapi/sln-api.yaml` (after Phase 2 is complete), copy the final spec to `SpaceLaunchNow-KMP-Main/specs/sln-api.yaml`
- [ ] Confirm the spec has all required paths: `/v1/launches`, `/v1/events`, `/v1/astronauts`, `/v1/vehicles`, `/v1/agencies`, `/v1/programs`, `/v1/locations`, `/v1/launchers`, `/v1/spacecraft`, `/v1/spacecraft-configurations`, `/v1/updates`, `/v1/expeditions`, `/v1/config/{type}`
- [ ] Consider: add a script or CI step to fetch the latest spec from the running SLN-API `/openapi.yaml` endpoint rather than manually copying

### 4A-2 — Add second openApiGenerate task in build.gradle.kts

- [ ] In `composeApp/build.gradle.kts`, add a second `openApiGenerate` task configuration targeting `specs/sln-api.yaml`:
  ```kotlin
  openApiGenerate {
    // existing LL config stays here
  }
  
  tasks.register<GenerateTask>("openApiGenerateSln") {
    generatorName.set("jvm-ktor")
    inputSpec.set("$projectDir/../specs/sln-api.yaml")
    outputDir.set("$buildDir/generated/sln-api")
    packageName.set("me.calebjones.spacelaunchnow.api.sln")
    // same other options as existing task
  }
  ```
- [ ] Add `openApiGenerateSln` as a dependency of `compileKotlinCommon` (or equivalent)
- [ ] Create `composeApp/openapi-sln-config.yaml` (copy of `openapi-config.yaml`, update `packageName` to `me.calebjones.spacelaunchnow.api.sln`)
- [ ] Run `./gradlew openApiGenerateSln` — verify generated sources appear in `build/generated/sln-api/`
- [ ] Run `./gradlew compileKotlinDesktop` — verify generated SLN client compiles

### 4A-3 — Create SLN API Koin module

- [ ] Create `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/di/SlnApiModule.kt`:
  - Configure Ktor `HttpClient` for SLN-API base URL
  - Base URL: read from `EnvironmentManager.getEnv("SLN_API_BASE_URL", "https://api.spacelaunchnow.app")` (or local: `http://10.0.2.2:8080` for Android emulator)
  - Authentication: Bearer token via `setApiKey()` / `setApiKeyPrefix("Bearer")` if SLN-API requires it; or open if no auth
  - User agent: reuse `UserAgentUtil.getUserAgent()`
  - Register generated SLN API client instances (e.g., `single { LaunchesApi(get()) }` under a named qualifier to avoid clash with LL client)
- [ ] Add `SlnApiModule` to the Koin app startup in `MainApplication.kt` (Android) and `main.kt` (Desktop) — but only under a feature flag (see 4D-1)

---

## Phase 4B: Create SLN Mappers

All SLN mappers implement the same `*Mapper` interfaces created in Phase 3. The difference is the input type (SLN-API generated models vs LL generated models).

### 4B-1 — SLNLaunchMapper

- [ ] Create `domain/mapper/SLNLaunchMapper.kt` implementing `LaunchMapper`:
  - Map `me.calebjones.spacelaunchnow.api.sln.models.Launch` → domain `Launch`
  - Map `me.calebjones.spacelaunchnow.api.sln.models.LaunchDetail` → domain `LaunchDetail`
  - Note: SLN field names may differ from LL — refer to `docs/kmp-api-endpoints.md` and the SLN-API spec for exact response shapes
  - Pay special attention to: launch ID (SLN uses UUID string vs LL UUID object), date fields (confirm RFC3339 vs epoch), status structure, nested agency/provider shape

### 4B-2 — SLNEventMapper

- [ ] Create `domain/mapper/SLNEventMapper.kt` implementing `EventMapper`:
  - Map SLN event list + detail response types → domain `Event` / `EventDetail`
  - Inject `LaunchMapper` for nested launch objects within events

### 4B-3 — Remaining SLN mappers (after Phase 3B)

- [ ] `SLNAgencyMapper` → domain `Agency`/`AgencyDetail`
- [ ] `SLNAstronautMapper` → domain `Astronaut`/`AstronautDetail`
- [ ] `SLNVehicleMapper` → domain `VehicleConfig`/`VehicleConfigDetail`
- [ ] `SLNLauncherMapper` → domain `Launcher`/`LauncherDetail`
- [ ] `SLNSpacecraftMapper` → domain `Spacecraft`/`SpacecraftDetail`/`SpacecraftConfig`/`SpacecraftConfigDetail`
- [ ] `SLNSpaceStationMapper` → domain `SpaceStation`/`Expedition`/`ExpeditionDetail`
- [ ] `SLNProgramMapper` → domain `Program`/`ProgramDetail`

---

## Phase 4C: Create SLN Repository Implementations

Each SLN repository implementation follows this pattern:
- Inject the SLN-API client (named qualifier to avoid clash with LL client)
- Inject the corresponding SLN mapper
- Implement all methods from the repository interface
- Use extension functions (same pattern as `api/extensions/LaunchesApiExtensions.kt`) if SLN-API methods also have many params; otherwise call directly

### 4C-1 — SLNLaunchRepositoryImpl

- [ ] Create `data/repository/SLNLaunchRepositoryImpl.kt` implementing `LaunchRepository`:
  - `getUpcomingLaunches()` → calls SLN `/v1/launches?upcoming=true&ordering=net&limit=N`
  - `getPreviousLaunches()` → calls SLN `/v1/launches?upcoming=false&ordering=-net&limit=N`
  - `getLaunchDetail(id)` → calls SLN `/v1/launches/{id}`
  - `getLaunchesFiltered(params)` → calls SLN `/v1/launches` with filter params from `LaunchFilterParams`
  - Map all responses through `SLNLaunchMapper`

### 4C-2 — SLNEventRepositoryImpl

- [ ] Create `data/repository/SLNEventRepositoryImpl.kt` implementing `EventRepository`:
  - `getUpcomingEvents()` → `/v1/events?upcoming=true&ordering=date&limit=N`
  - `getPreviousEvents()` → `/v1/events?upcoming=false&ordering=-date&limit=N`
  - `getEventDetail(id)` → `/v1/events/{id}`
  - `getEventsFiltered(params)` → `/v1/events` with `EventFilterParams`
  - Map through `SLNEventMapper`

### 4C-3 — Remaining SLN repository implementations (after Phase 3B)

- [ ] `SLNAgencyRepositoryImpl` implementing `AgencyRepository`
- [ ] `SLNAstronautRepositoryImpl` implementing `AstronautRepository`
- [ ] `SLNLauncherConfigRepositoryImpl` implementing `LauncherConfigRepository`
- [ ] `SLNLauncherRepositoryImpl` implementing `LauncherRepository`
- [ ] `SLNSpacecraftRepositoryImpl` implementing `SpacecraftRepository`
- [ ] `SLNSpaceStationRepositoryImpl` implementing `SpaceStationRepository`
- [ ] `SLNProgramRepositoryImpl` implementing `ProgramRepository`

---

## Phase 4D: Feature Flag + Switchover

### 4D-1 — Add API backend toggle

- [ ] Add `SLN_API_ENABLED=false` to `.env` file
- [ ] In `EnvironmentManager` or a new `FeatureFlags.kt`, expose: `val slnApiEnabled: Boolean = EnvironmentManager.getEnv("SLN_API_ENABLED", "false").toBoolean()`
- [ ] In `AppModule.kt`, conditionally bind repository implementations:
  ```kotlin
  single<LaunchRepository> {
      if (FeatureFlags.slnApiEnabled) SLNLaunchRepositoryImpl(get(named("sln")), get())
      else LaunchRepositoryImpl(get(), get())
  }
  single<EventRepository> {
      if (FeatureFlags.slnApiEnabled) SLNEventRepositoryImpl(get(named("sln")), get())
      else EventRepositoryImpl(get(), get())
  }
  // repeat for each repository
  ```
- [ ] Only include `SlnApiModule` in Koin init when `slnApiEnabled` is true (avoid loading unused HTTP client)

### 4D-2 — Verify with SLN-API backend

- [ ] Set `SLN_API_ENABLED=true` in `.env` and ensure local SLN-API Docker stack is running (`docker compose up` in `SpaceLaunchNow-API/`)
- [ ] Run Android on emulator (`./gradlew installDebug`) — emulator base URL is `http://10.0.2.2:8080`
- [ ] Checklist for each screen:
  - [ ] Home / NextUp: launches load, countdown is correct, launch title formatted correctly
  - [ ] Schedule / upcoming: list loads, pagination works
  - [ ] Schedule / previous: list loads, dates correct
  - [ ] Schedule filters: status filter, location filter, rocket filter — all return correct filtered results
  - [ ] Launch detail: all sections present (mission, pad, rocket, crew if applicable, updates, video URLs)
  - [ ] Events / upcoming: events list loads
  - [ ] Events / previous: events list loads
  - [ ] Event detail: linked launches appear, updates appear
  - [ ] Agency list (if implemented)
  - [ ] Astronaut list (if implemented)
  - [ ] Notifications still work (Firebase — unaffected by API swap)
  - [ ] Android widget still works
- [ ] Run Desktop (`./gradlew desktopRun`) and verify same screens

### 4D-3 — Data parity verification

- [ ] Pick 10 random upcoming launches — compare SLN-API response vs LL API response for same launch ID (all key fields: name, net, status, provider, pad, mission, vid_urls, programs)
- [ ] Pick 5 previous launches — verify net date, status, mission details
- [ ] Pick 5 events — verify type, date, linked launches, updates

### 4D-4 — Set SLN-API as default

- [ ] Change default in `FeatureFlags.kt` / `.env` to `SLN_API_ENABLED=true`
- [ ] Run full regression test suite: `./gradlew test`
- [ ] Install on physical Android device and smoke test

---

## Phase 4E: Cleanup (after 4D-4 is stable for 1+ sprint)

- [ ] Delete `ll_2.4.0.json` from project root
- [ ] Delete `composeApp/openapi-config.yaml` (LL config) — keep only `openapi-sln-config.yaml`
- [ ] Remove the original `openApiGenerate` task from `composeApp/build.gradle.kts` (the LL one); rename `openApiGenerateSln` → `openApiGenerate` for consistency
- [ ] Delete all files in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/api/extensions/` (LL extension functions)
- [ ] Delete `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/di/ApiModule.kt` (LL Koin module)
- [ ] Delete all `LL*Mapper.kt` files in `domain/mapper/` (e.g., `LLLaunchMapper.kt`, `LLEventMapper.kt`, etc.)
- [ ] Delete all `*RepositoryImpl.kt` LL implementations (keep only `SLN*RepositoryImpl.kt`)
- [ ] Remove `if (FeatureFlags.slnApiEnabled)` branching from `AppModule.kt` — hard-wire SLN repositories
- [ ] Delete `FeatureFlags.kt` (or keep if used for other flags)
- [ ] Remove `@Deprecated` old repository interface methods (the generated-type-returning ones)
- [ ] Run `./gradlew compileKotlinDesktop` and `./gradlew test` — confirm zero errors

---

## Verification

- [ ] `./gradlew compileKotlinDesktop` passes with LL client fully removed
- [ ] `./gradlew test` — all tests pass
- [ ] `./gradlew installDebug` — Android debug build on device
- [ ] No import from `me.calebjones.spacelaunchnow.api.launchlibrary` anywhere outside `build/generated/` (grep confirms)
- [ ] App binary size: compare before/after — removing unused LL generated client should reduce by ~500 KB–2 MB depending on generated model count
- [ ] Conventional commit: `feat!: migrate KMP from Launch Library API to SLN-API`
