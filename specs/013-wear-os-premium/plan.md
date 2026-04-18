# Implementation Plan: Wear OS Premium Experience

**Branch**: `013-wear-os-premium` | **Date**: 2026-04-15 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/013-wear-os-premium/spec.md`

## Summary

Add a phased Wear OS experience (complications → tiles → companion app) as a premium feature in a new `wearApp` Gradle module. Gate all Wear surfaces behind a `WEAR_OS` `PremiumFeature` entitlement managed via RevenueCat on the phone and synced to the watch via the Wearable DataLayer API. Data follows a three-tier fallback: direct API (watch WiFi/LTE) → phone DataLayer sync → local DataStore cache. Uses Wear Compose Material3 1.5.0 for the companion app UI, Tiles 1.6.0 with Material3TileService for glanceable tiles, and Watchface Complications 1.3.0 for countdown complications.

## Technical Context

**Language/Version**: Kotlin 2.3.20, JDK 21, Compose Plugin 1.10.2  
**Primary Dependencies**: Wear Compose Material3 1.5.0 (foundation/navigation/material3/ui-tooling), Wear Tiles 1.6.0 + ProtoLayout 1.4.0, Watchface Complications Data Source 1.3.0, play-services-wearable 21.0.0, Koin 4.1.1, Ktor 3.3.1, kotlinx-serialization 1.9.0, RevenueCat KMP 1.9.0+14.3.0 (phone-side only)  
**Storage**: DataStore 1.1.7 (watch-side entitlement + launch cache), SQLDelight 2.1.0 (shared models from composeApp)  
**Testing**: JUnit4, Turbine (flow testing), MockK (mocking), Robolectric (complication/tile unit tests)  
**Target Platform**: Wear OS 4+ (API 30+, compileSdk 36), paired with Android phone app (minSdk 26)  
**Project Type**: Mobile — separate `wearApp` module alongside existing `composeApp`  
**Performance Goals**: 60fps rotary scroll in companion app, <500ms cached content load, complication refresh ≤15 min  
**Constraints**: <1% battery drain/day from background sync, <10s entitlement sync latency, offline-capable via three-tier fallback  
**Scale/Scope**: ~27.8K active users (phone), estimated 5-10% Wear OS adoption (~1.4-2.8K watch users), 3 watch surfaces (complication, tile, app with 3 screens)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*
*POST-PHASE 1 RE-CHECK (2026-04-15): All principles still PASS. Principle VI confirmed — wearApp is a standalone Android module with no commonMain modifications that could break iOS/Desktop. DataLayer models defined in wearApp/data/model/, not in shared code. Shared API models accessed via composeApp dependency. No gate violations.*

| # | Principle | Status | Notes |
|---|-----------|--------|-------|
| I | Mobile-First UX | ✅ PASS | Wear OS is an extension of the Android phone experience; phone app remains primary. Watch surfaces follow Wear OS design guidelines (round display, rotary input, glanceable). |
| II | Pattern-Based Consistency (Stale-While-Revalidate) | ✅ PASS | WatchLaunchRepository implements the same stale-while-revalidate pattern: show cached data immediately, refresh in background. Three-tier fallback (API → DataLayer → cache) extends this pattern to watch. |
| III | Accessibility | ✅ PASS | Wear Compose Material3 has built-in accessibility (TalkBack, content descriptions). Complications use standard complication types with built-in a11y. Tiles use ProtoLayout semantics. |
| IV | CI/CD & Conventional Commits | ✅ PASS | All commits use conventional format. wearApp module will be added to CI build matrix. Branch follows `###-feature-name` convention. |
| V | Code Generation | ✅ PASS | Shared API models from openApiGenerate used on watch. No separate code generation needed for wearApp. |
| VI | Multiplatform Architecture ("All Targets MUST Build") | ⚠️ CONDITIONAL | wearApp is a **new Android-only module** — it does not break existing composeApp targets (Android/iOS/Desktop). However, shared code used by wearApp must remain in commonMain or androidMain. **Gate condition**: wearApp must not introduce changes to commonMain that break iOS/Desktop compilation. |
| VII | Testing Standards | ✅ PASS | Unit tests for repository/viewmodel logic, Robolectric tests for complications/tiles, integration tests for DataLayer sync. |
| VIII | Compose Best Practices | ✅ PASS | Uses Wear Compose Material3 with dual previews (round/square annotations), StateFlow for reactive state, separate Composable files. |

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
wearApp/                              # New Wear OS application module
├── build.gradle.kts                  # com.android.application, Wear OS deps
├── src/
│   └── main/
│       ├── AndroidManifest.xml       # Wear OS metadata, standalone=true
│       ├── kotlin/me/calebjones/spacelaunchnow/wear/
│       │   ├── WearApplication.kt              # Koin setup for watch
│       │   ├── di/
│       │   │   └── WearModule.kt               # Koin module (repos, VMs)
│       │   ├── complication/
│       │   │   └── NextLaunchComplicationService.kt  # ComplicationDataSourceService
│       │   ├── tile/
│       │   │   └── NextLaunchTileService.kt    # Material3TileService
│       │   ├── ui/
│       │   │   ├── WearApp.kt                  # AppScaffold + navigation
│       │   │   ├── launch/
│       │   │   │   ├── LaunchListScreen.kt     # TransformingLazyColumn list
│       │   │   │   └── LaunchDetailScreen.kt   # Detail with Open on Phone
│       │   │   ├── premium/
│       │   │   │   └── PremiumGateScreen.kt    # Subscribe prompt
│       │   │   ├── settings/
│       │   │   │   └── SettingsScreen.kt       # UTC toggle, about
│       │   │   └── theme/
│       │   │       └── WearTheme.kt            # MaterialTheme + dynamic colors
│       │   ├── data/
│       │   │   ├── WatchLaunchRepository.kt    # Three-tier data fetching
│       │   │   ├── EntitlementSyncManager.kt   # DataLayer entitlement sync
│       │   │   └── DataLayerListenerService.kt # WearableListenerService
│       │   └── viewmodel/
│       │       ├── LaunchListViewModel.kt
│       │       └── LaunchDetailViewModel.kt
│       └── res/
│           ├── drawable/                       # Complication/tile icons
│           └── values/
│               └── strings.xml
└── src/test/                                   # Unit + Robolectric tests

composeApp/                           # Existing phone app (modifications)
├── src/androidMain/kotlin/.../
│   ├── sync/
│   │   └── PhoneDataLayerService.kt  # NEW: sends data to watch
│   └── ...
└── src/commonMain/kotlin/.../
    └── data/model/
        └── SubscriptionState.kt      # MODIFIED: add WEAR_OS to PremiumFeature
```

**Structure Decision**: Separate `wearApp` module (Android-only) alongside existing `composeApp` (KMP). The wearApp depends on shared models from composeApp's androidMain/commonMain via project dependency or shared artifact. This keeps the Wear OS code isolated and ensures Constitution Principle VI compliance — no changes to commonMain that could break iOS/Desktop targets.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| New `wearApp` module (Principle VI conditional) | Wear OS requires a separate `com.android.application` with Wear manifest metadata, cannot be a library module inside composeApp | A source set inside composeApp would couple Wear OS deps into KMP builds, breaking iOS/Desktop |
| DataLayer sync service (new inter-device communication) | RevenueCat has no Wear OS SDK — entitlement must be relayed from phone to watch | Polling the API from the watch every time would drain battery and fail offline; one-time DataLayer push is efficient |
