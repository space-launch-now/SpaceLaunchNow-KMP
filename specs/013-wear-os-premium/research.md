# Research: Wear OS Premium Experience

**Date**: 2026-04-15 | **Status**: Complete

## R-001: Wear Compose UI Framework Selection

**Decision**: Use Wear Compose Material3 1.5.0 (stable) — NOT the older Wear Compose Material line.

**Rationale**: Material3 (compose-material3:1.5.0) is the first stable release of M3 Expressive for Wear OS and supersedes the older compose-material library. It provides dynamic color theming (matches watch face), TransformingLazyColumn with scaling/morphing animations, EdgeButton, AppScaffold/ScreenScaffold coordination, and automatic large-screen adaptation. The two libraries are mutually exclusive and must not be mixed.

**Alternatives Considered**:
- Wear Compose Material 1.6.1 (legacy line) — Rejected: Google explicitly recommends M3 as the replacement. Legacy Material will receive maintenance-only updates.
- Horologist Compose Layout — Rejected: Horologist is a Google GitHub project (not AndroidX), adds transitive complexity, and much of its value (scaffolding, tile rendering) is now built into Wear Compose M3 1.5.0 and Tiles 1.6.0.

**Dependencies**:
```
androidx.wear.compose:compose-foundation:1.5.0
androidx.wear.compose:compose-material3:1.5.0
androidx.wear.compose:compose-navigation:1.5.0
androidx.wear.compose:compose-ui-tooling:1.5.0
```

## R-002: Tile Implementation Approach

**Decision**: Use `Material3TileService` from Tiles 1.6.0 — NOT Horologist TileRenderer.

**Rationale**: Tiles 1.6.0 introduces `Material3TileService` which replaces the verbose two-method pattern (onTileRequest + onTileResourcesRequest) with a single `suspend fun` that returns a `ProtoLayoutScope` result. Resource collection is now automatic via `ProtoLayoutScope`. This is a cleaner Kotlin-first API that eliminates the need for Horologist's TileRenderer abstraction. The spec's FR-004 mentions Horologist, but this research supersedes that — Material3TileService is the recommended path.

**Alternatives Considered**:
- Horologist TileRenderer — Rejected: Extra dependency, Horologist is not part of AndroidX, and Tiles 1.6.0 Material3TileService provides equivalent functionality natively.
- Legacy TileService (onTileRequest/onTileResourcesRequest) — Rejected: Verbose, callback-based, not Kotlin-idiomatic.

**Dependencies**:
```
androidx.wear.tiles:tiles:1.6.0
androidx.wear.protolayout:protolayout:1.4.0
androidx.wear.protolayout:protolayout-material3:1.4.0
androidx.wear.protolayout:protolayout-expression:1.4.0
```

**Note**: Requires compileSdk ≥ 35 (project already uses 36).

## R-003: Complication Data Source Pattern

**Decision**: Use `ComplicationDataSourceService` from Watchface Complications 1.3.0.

**Rationale**: The watchface APIs are deprecated in favor of Watch Face Format, but the **complication data source APIs remain fully supported and are NOT deprecated**. `ComplicationDataSourceService` provides the standard way to supply data to watch face complications. Supports SHORT_TEXT, LONG_TEXT, and RANGED_VALUE types which map to our use case (countdown text + progress).

**Alternatives Considered**:
- Watch Face Format complications — Not applicable: WFF is for watch face developers, not data source providers. We are a data source, not a watch face.
- Tiles-only (skip complications) — Rejected: Complications are the most-seen Wear surface (on watch face) and the lowest effort to implement. Highest ROI.

**Dependencies**:
```
androidx.wear.watchface:watchface-complications-data-source:1.3.0
androidx.wear.watchface:watchface-complications-data-source-ktx:1.3.0
```

## R-004: Entitlement Sync Strategy (Phone → Watch)

**Decision**: Use Wearable DataLayer API (`DataClient`) for entitlement sync. No RevenueCat SDK on watch.

**Rationale**: RevenueCat does not offer a Wear OS SDK. The phone app already has RevenueCat KMP 1.9.0+14.3.0 fully integrated. The most reliable pattern is:
1. Phone detects entitlement change (RevenueCat listener) → writes to DataLayer `DataItem`
2. Watch receives via `WearableListenerService.onDataChanged()` → caches to DataStore
3. Watch checks local DataStore cache for entitlement state (works offline)

This avoids billing complexity on the watch and leverages the existing RevenueCat integration.

**Alternatives Considered**:
- RevenueCat on watch (direct API check) — Rejected: No Wear OS SDK exists. Direct REST API calls would require token management and wouldn't work offline.
- MessageClient for entitlement — Rejected: Messages are fire-and-forget, not persistent. DataClient stores the last-synced value even if the watch wasn't connected when the sync was sent.
- Shared Google Play billing — Rejected: Adds complexity, RevenueCat already abstracts billing.

**Dependencies**:
```
com.google.android.gms:play-services-wearable:21.0.0
```

## R-005: Data Fetching Three-Tier Fallback

**Decision**: Direct API → DataLayer sync → Local DataStore cache.

**Rationale**: Modern Wear OS 4+ watches often have WiFi and sometimes LTE. Direct API access is the fastest and most current data source. When the watch lacks internet, DataLayer sync from the phone provides near-real-time data. When both are unavailable, the local cache (refreshed by WorkManager every 30 min) ensures the watch always has something to display. This follows Constitution Principle II (Stale-While-Revalidate).

**Implementation Pattern**:
```kotlin
class WatchLaunchRepository(
    private val apiClient: LaunchesApi,       // Direct API (Ktor)
    private val dataLayerClient: DataClient,  // Phone sync
    private val dataStore: DataStore<...>,    // Local cache
) {
    suspend fun getUpcomingLaunches(): List<LaunchBasic> {
        // 1. Try direct API
        // 2. Fall back to DataLayer
        // 3. Fall back to local cache
        // Always update cache on successful fetch
    }
}
```

**Alternatives Considered**:
- Phone-only sync (no direct API) — Rejected: Introduces unnecessary phone dependency for watches with WiFi/LTE. Also slower.
- Direct API only — Rejected: Fails completely when watch has no internet (common in Bluetooth-only mode).

## R-006: wearApp Module Architecture

**Decision**: Separate `wearApp` Gradle module with `com.android.application` plugin.

**Rationale**: Wear OS apps require a separate Android application module with Wear-specific manifest entries (`<uses-feature android:name="android.hardware.type.watch"/>`, standalone mode). This cannot be a library module inside composeApp's KMP structure. The wearApp depends on shared data models from composeApp via a project dependency or shared library.

**Shared Code Strategy**: 
- API models (generated from OpenAPI) are in composeApp's generated sources. The wearApp will either:
  - (A) Depend on composeApp as an Android project dependency (access androidMain classes), OR
  - (B) Extract shared models into a separate `:shared` module  
- **Decision**: Start with option (A) for simplicity. Extract `:shared` module only if build times become problematic or circular dependencies arise.

**Alternatives Considered**:
- Source set inside composeApp — Rejected: Would couple Wear dependencies into KMP, potentially breaking iOS/Desktop builds (Constitution Principle VI).
- Separate repository — Rejected: Overkill for a module that shares the same deployment pipeline.

## R-007: play-services-wearable Version

**Decision**: Use play-services-wearable 21.0.0.

**Rationale**: This is the current stable release of the Wearable DataLayer API. It provides `DataClient`, `MessageClient`, `ChannelClient`, and `NodeClient` for phone-watch communication. Fully compatible with compileSdk 36 and Wear OS 4+.

## R-008: Companion App Navigation Pattern

**Decision**: Use `SwipeDismissableNavHost` from Wear Compose Navigation 1.5.0.

**Rationale**: This is the standard Wear OS navigation pattern that supports swipe-to-dismiss gestures for back navigation. The M3 1.5.0 release includes improved predictive back support for API 36+ and proper focus management for rotary input after swiping back.

**Screen Graph**:
```
LaunchList (start) ──tap──> LaunchDetail ──"Open on Phone"──> RemoteActivityHelper
      │                         │
      └──swipe back──<──────────┘
PremiumGate (shown if !entitled)
Settings (accessible from list)
```

## R-009: Background Refresh Strategy

**Decision**: WorkManager with periodic 30-minute refresh, respecting battery constraints.

**Rationale**: WorkManager 2.11.0 is already in the project deps. A `PeriodicWorkRequest` with `Constraints.Builder().setRequiresBatteryNotLow(true).build()` ensures background refresh without excessive battery drain. The work updates the DataStore cache so that complications and tiles always have relatively fresh data.

**Alternatives Considered**:
- AlarmManager — Rejected: Not lifecycle-aware, doesn't respect Doze mode properly on Wear OS.
- ComplicationDataSourceUpdateRequester with scheduled updates — Only works for complications, not tiles/app cache.

## R-010: Spec Correction — FR-004 Horologist Reference

**Decision**: Update FR-004 implementation to use `Material3TileService` (Tiles 1.6.0) instead of Horologist TileRenderer.

**Rationale**: The spec was written before research confirmed that Tiles 1.6.0 provides native Material3 tile support. Horologist is no longer needed for tile rendering. This simplifies dependencies and aligns with the AndroidX-only approach.
