# Contracts: Wear OS Premium Experience

**Date**: 2026-04-15 | **Status**: Complete

These are **interface contracts** — the public API surface each component exposes. Implementation details are deferred to task execution.

## WatchLaunchRepository

```kotlin
/**
 * Three-tier data fetching for watch launch data.
 * Priority: Direct API → DataLayer phone sync → Local DataStore cache.
 */
interface WatchLaunchRepository {

    /** Flow of cached launches, emits on every cache update. */
    val launches: Flow<List<CachedLaunch>>

    /** Flow of the data source for the current cache. */
    val dataSource: Flow<DataSource>

    /**
     * Fetch upcoming launches using the three-tier fallback.
     * Updates the local cache on success.
     * @param limit Max launches to fetch (default 20).
     * @return Result with the fetched launches or the failure reason.
     */
    suspend fun refreshLaunches(limit: Int = 20): Result<List<CachedLaunch>>

    /**
     * Get a single launch by ID from the local cache.
     * @param launchId UUID string of the launch.
     * @return The cached launch or null if not found.
     */
    suspend fun getLaunchById(launchId: String): CachedLaunch?

    /**
     * Get the first upcoming launch (for complications/tiles).
     * @return The next launch or null if cache is empty.
     */
    suspend fun getNextLaunch(): CachedLaunch?
}
```

## EntitlementSyncManager

```kotlin
/**
 * Manages premium entitlement state on the watch.
 * Receives sync from phone via DataLayer, caches locally.
 */
interface EntitlementSyncManager {

    /** Flow of the current entitlement state. */
    val entitlementState: Flow<WearEntitlementState>

    /**
     * Check if WEAR_OS premium is currently active.
     * Uses cached state — does not trigger a sync.
     */
    suspend fun isWearOsPremium(): Boolean

    /**
     * Process an incoming entitlement update from the phone.
     * Called by DataLayerListenerService.onDataChanged().
     * @param active Whether the entitlement is active.
     * @param expiresAt Optional expiry timestamp.
     */
    suspend fun onEntitlementReceived(active: Boolean, expiresAt: Instant?)

    /**
     * Request a fresh entitlement sync from the phone.
     * Sends a message via MessageClient asking the phone to push current state.
     */
    suspend fun requestSync()
}
```

## PhoneDataLayerService (phone-side)

```kotlin
/**
 * Phone-side service that pushes data to the watch via DataLayer.
 * Registered in composeApp's androidMain.
 */
interface PhoneDataLayerSync {

    /**
     * Push the current launch list and entitlement state to the watch.
     * Called on: app foreground, subscription change, periodic WorkManager.
     */
    suspend fun syncToWatch()

    /**
     * Push only the entitlement state to the watch.
     * Called immediately on RevenueCat listener callback.
     */
    suspend fun syncEntitlementToWatch(active: Boolean, expiresAt: Instant?)
}
```

## NextLaunchComplicationService

```kotlin
/**
 * ComplicationDataSourceService providing launch countdown data.
 *
 * Supported types:
 * - SHORT_TEXT: "T-2h 15m" countdown
 * - LONG_TEXT: "Falcon 9 — T-2h 15m" (vehicle + countdown)
 * - RANGED_VALUE: Progress from T-24h to T-0
 *
 * Behavior:
 * - Premium users: Live countdown data from WatchLaunchCache
 * - Free users: "Subscribe" placeholder text
 * - No data: "No launches" placeholder
 *
 * Update frequency: System-managed via ComplicationDataSourceUpdateRequester
 * (minimum ~5 minutes between updates, actual timing varies by watch face).
 */
// Extends: ComplicationDataSourceService
// Key methods:
//   onComplicationRequest(complicationRequest: ComplicationRequest): ComplicationData
//   getPreviewData(type: ComplicationType): ComplicationData
```

## NextLaunchTileService

```kotlin
/**
 * Material3TileService displaying next launch information.
 *
 * Layout:
 * - Header: Agency icon + agency abbreviation
 * - Title: Mission name
 * - Body: Vehicle name, countdown timer, location
 *
 * Behavior:
 * - Premium users: Full launch info from WatchLaunchCache
 * - Free users: "Upgrade on phone" with deep link
 * - No data: "No upcoming launches"
 *
 * Refresh: System-managed tile refresh + triggered on DataLayer update.
 */
// Extends: Material3TileService
// Key method:
//   suspend fun tile(requestParams: TileRequest): ProtoLayoutScope.() -> Unit
```

## WearApp Navigation

```kotlin
/**
 * Sealed class defining watch app navigation destinations.
 */
sealed class WearScreen {
    /** Scrollable list of upcoming launches. Start destination for premium users. */
    data object LaunchList : WearScreen()

    /** Detailed view of a single launch with "Open on Phone" action. */
    data class LaunchDetail(val launchId: String) : WearScreen()

    /** Premium gate shown to free-tier users. Start destination for free users. */
    data object PremiumGate : WearScreen()

    /** Settings screen (UTC toggle, about, version). */
    data object Settings : WearScreen()
}
```

## LaunchListViewModel

```kotlin
/**
 * ViewModel for the watch launch list screen.
 */
// interface contract:
//   val uiState: StateFlow<LaunchListUiState>
//   fun refresh()

/**
 * UI state for launch list.
 */
data class LaunchListUiState(
    val launches: List<CachedLaunch> = emptyList(),
    val isLoading: Boolean = false,
    val dataSource: DataSource = DataSource.STALE_CACHE,
    val lastUpdated: Instant? = null,
    val error: String? = null,
    val isPremium: Boolean = false
)
```

## LaunchDetailViewModel

```kotlin
/**
 * ViewModel for the watch launch detail screen.
 */
// interface contract:
//   val uiState: StateFlow<LaunchDetailUiState>
//   fun openOnPhone(launchId: String)

data class LaunchDetailUiState(
    val launch: CachedLaunch? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val formattedTitle: String = "",
    val countdown: String = ""
)
```

## DataLayerListenerService (watch-side)

```kotlin
/**
 * WearableListenerService on the watch that receives data from the phone.
 * Registered in wearApp's AndroidManifest.
 *
 * Listens for:
 * - DataItem changes at path "/spacelaunchnow/sync" → updates cache + entitlement
 * - Messages at path "/spacelaunchnow/request-sync" → triggers phone sync
 */
// Extends: WearableListenerService
// Key method:
//   onDataChanged(dataEvents: DataEventBuffer)
```

## WorkManager Contracts

```kotlin
/**
 * Periodic background refresh worker for watch data cache.
 *
 * Schedule: Every 30 minutes
 * Constraints: RequiresBatteryNotLow, RequiresNetworkConnectivity (optional)
 * Action: Calls WatchLaunchRepository.refreshLaunches()
 *         Then triggers ComplicationDataSourceUpdateRequester
 *         Then triggers TileUpdateRequester
 */
// Class: WatchDataRefreshWorker extends CoroutineWorker
```
