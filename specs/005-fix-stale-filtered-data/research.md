# Research: Fix Stale Filtered Data on Cold Start

## Research Tasks & Findings

### RT-1: Root Cause — Fresh Cache Path Skips Filtering

**Task**: Confirm and document the exact code paths where filtering is not applied.

**Decision**: The bug exists in two methods in `LaunchRepositoryImpl.kt`:

1. **`getFeaturedLaunch()`** (line ~89-104): When `!forceRefresh` and fresh cache has data, returns immediately without filtering:
   ```kotlin
   val cachedLaunches = localDataSource?.getUpcomingNormalLaunches(4)
   if (cachedLaunches != null && cachedLaunches.isNotEmpty()) {
       return Result.success(DataResult(..., results = cachedLaunches))
       // ❌ agencyIds/locationIds ignored on this path
   }
   ```

2. **`getUpcomingLaunchesNormal()`** (line ~261-275): Same pattern — fresh cache returned unfiltered:
   ```kotlin
   val cachedLaunches = localDataSource?.getUpcomingNormalLaunches(limit)
   if (cachedLaunches != null && cachedLaunches.isNotEmpty()) {
       return Result.success(DataResult(..., results = cachedLaunches))
       // ❌ agencyIds/locationIds ignored on this path
   }
   ```

3. **`getPreviousLaunchesNormal()`** (line ~450-465): Same pattern for previous launches.

**Rationale**: The stale cache path correctly uses `filterLaunchesByPreferences()`, but the developer likely assumed fresh cache was already filtered. However, the SQLDelight cache stores ALL launches fetched (including those from other callers like widgets), so fresh cache can contain unfiltered data.

**Alternatives Considered**:
- Database-level filtering (add WHERE clauses for agency/location) — **Rejected**: requires schema migration, overkill for <100 launches
- Separate cache tables per filter configuration — **Rejected**: complex, fragile when filters change

### RT-2: Widget Filter Integration (Android)

**Task**: Determine the best pattern for injecting filter awareness into Android Glance widgets.

**Decision**: Widgets should read `NotificationState` via `NotificationStateStorage` and convert to filter params via `LaunchFilterService`, then pass to repository calls.

**Rationale**: Widgets already use Koin injection for `LaunchRepository`. Both `NotificationStateStorage` and `LaunchFilterService` are registered in Koin's `AppModule`. The widgets can inject them the same way:
```kotlin
val notificationStateStorage: NotificationStateStorage by koinInject(NotificationStateStorage::class.java)
val launchFilterService: LaunchFilterService by koinInject(LaunchFilterService::class.java)
```

Then fetch and apply:
```kotlin
val state = notificationStateStorage.state.first()
val agencyIds = launchFilterService.getAgencyIds(state)
val locationIds = launchFilterService.getLocationIds(state)
val result = launchRepository.getUpcomingLaunchesNormal(limit = 10, agencyIds = agencyIds, locationIds = locationIds)
```

**Alternatives Considered**:
- Pass filters via Glance DataStore prefs (written by WidgetUpdater) — **Rejected**: adds complexity, filter data is already in NotificationStateStorage accessible at runtime
- Create a dedicated WidgetRepository — **Rejected**: unnecessary abstraction for passing 2 parameters

### RT-3: iOS Widget Filter Integration

**Task**: Determine how Swift WidgetKit extension can pass filter preferences to KMP code.

**Decision**: Extend `KoinHelper.fetchUpcomingLaunchesOrNull()` to accept optional filter parameters, and have the Swift widget read filter state via `KoinHelper`.

**Implementation approach**:

1. Add `getFilterParams()` method to `KoinHelper` that reads `NotificationState` and returns agency/location IDs
2. Add overloaded `fetchUpcomingLaunchesOrNull(limit:agencyIds:locationIds:)` that passes filters to repository
3. Swift widget calls `getFilterParams()` then `fetchUpcomingLaunchesOrNull(limit:agencyIds:locationIds:)`

**Rationale**: `KoinHelper` already has access to `LaunchRepository` via Koin. Adding `LaunchFilterService` and `NotificationStateStorage` injection follows the same pattern. This keeps the Swift code minimal — it just calls KMP methods.

**Alternatives Considered**:
- Store filter state in shared UserDefaults (App Group) — **Rejected**: duplicates state, requires synchronization
- Direct CoreData/SQLite access from Swift — **Rejected**: violates KMP architecture, duplicates logic

### RT-4: Filter Change Widget Refresh

**Task**: Determine if widget refresh on filter change is already handled or needs new work.

**Decision**: No new work needed. The existing `PlatformWidgetUpdater.updateAllWidgets()` is already called when the user changes notification/filter settings. Once widgets read filters at fetch time, any widget refresh will pick up the new filter state.

**Rationale**: `WidgetUpdater.updateAllWidgets()` triggers `provideGlance()` which calls `fetchNextLaunch()`/`fetchUpcomingLaunches()`. After the fix, these methods will read current filter state. iOS uses `WidgetKitBridge.requestReload()` which triggers `getTimeline()` in Swift, which calls `fetchLaunches()`.

**Alternatives Considered**: None needed — existing refresh mechanism is sufficient.

### RT-5: Cache Key Correctness

**Task**: Investigate whether the `buildCacheKey()` including filter params creates false cache misses.

**Decision**: Not a bug, but worth noting. `buildCacheKey()` creates different cache keys for different filter configurations (e.g., `upcoming_launches_agencies_1,2_locations_3`). This means cache timestamps are tracked per-filter-config, which is correct behavior. The actual cache data (SQLDelight) is shared across all configs — only the cache freshness timestamp varies per key.

**Rationale**: This design means:
- Cache data is shared (all launches in one table)
- Cache freshness is per-filter-config
- Fresh/stale determination is per-filter-config
- The fix (filtering fresh cache) aligns with this design — same data, filtered at read time
