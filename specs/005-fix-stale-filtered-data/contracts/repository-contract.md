# Repository Contract: Filtered Cache Behavior

## LaunchRepositoryImpl — Cache Filtering Contract

### `getUpcomingLaunchesNormal(limit, forceRefresh, agencyIds, locationIds)`

**Pre-conditions**: None

**Post-conditions**:
- When `forceRefresh == false` and fresh cache has data:
  - IF `agencyIds != null` OR `locationIds != null`: apply `filterLaunchesByPreferences()` before returning
  - IF filtered result is empty: fall through to stale cache, then API
  - Return `DataSource.CACHE` with filtered results
- When stale cache path (existing): `filterLaunchesByPreferences()` applied (already correct)
- When API path (existing): filters passed as API parameters (already correct)

**Invariant**: All three code paths (fresh cache, stale cache, API) return filtered results when filter parameters are provided.

### `getFeaturedLaunch(forceRefresh, agencyIds, locationIds)`

Same contract as `getUpcomingLaunchesNormal` with `limit=4`.

### `getPreviousLaunchesNormal(limit, forceRefresh, agencyIds, locationIds)`

Same contract as `getUpcomingLaunchesNormal` for previous launches.

---

## Widget Contract: Filter-Aware Data Fetch

### Android: `NextUpWidget.fetchNextLaunch()`

**Pre-conditions**: Koin is initialized, `NotificationStateStorage` and `LaunchFilterService` are available

**Post-conditions**:
- Reads current `NotificationState`
- Extracts `agencyIds` and `locationIds` via `LaunchFilterService`
- Calls `launchRepository.getUpcomingLaunchesNormal(limit=1, agencyIds=agencyIds, locationIds=locationIds)`
- Returns first launch matching user filter preferences, or `null`

### Android: `LaunchListWidget.fetchUpcomingLaunches()`

Same as `NextUpWidget.fetchNextLaunch()` but with `limit=10`, returns `List<LaunchNormal>`.

### iOS: `KoinHelper.fetchUpcomingLaunchesOrNull(limit)`

**Updated contract**:
- Reads current `NotificationState` from `NotificationStateStorage`
- Extracts filter params via `LaunchFilterService`
- Calls `launchRepository.getUpcomingLaunchesNormal(limit, forceRefresh=true, agencyIds, locationIds)`
- Returns filtered `PaginatedLaunchNormalList?`

**Note**: No changes needed to Swift `LaunchData.swift` since `KoinHelper` handles filtering internally.
