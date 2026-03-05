# Quickstart: Fix Stale Filtered Data on Cold Start

## Overview

This fix addresses 3 locations where user launch filter preferences are not applied:
1. Fresh cache return path in `LaunchRepositoryImpl` (affects homepage cold start)
2. Android widget data fetching (never passes filters)
3. iOS widget data fetching via `KoinHelper` (never passes filters)

## Fix 1: Repository Fresh Cache Filtering

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/LaunchRepositoryImpl.kt`

### Change in `getFeaturedLaunch()`

In the fresh cache hit section (~line 89-104), apply `filterLaunchesByPreferences()`:

```kotlin
// BEFORE (BUG):
val cachedLaunches = localDataSource?.getUpcomingNormalLaunches(4)
if (cachedLaunches != null && cachedLaunches.isNotEmpty()) {
    return Result.success(DataResult(
        data = PaginatedLaunchNormalList(count = cachedLaunches.size, ...),
        source = DataSource.CACHE, ...
    ))
}

// AFTER (FIX):
val cachedLaunches = localDataSource?.getUpcomingNormalLaunches(4)
if (cachedLaunches != null && cachedLaunches.isNotEmpty()) {
    val filteredCache = filterLaunchesByPreferences(cachedLaunches, agencyIds, locationIds)
    if (filteredCache.isNotEmpty()) {
        return Result.success(DataResult(
            data = PaginatedLaunchNormalList(count = filteredCache.size, ..., results = filteredCache),
            source = DataSource.CACHE, ...
        ))
    }
    // If filtered to empty, fall through to stale cache / API
}
```

### Same change in `getUpcomingLaunchesNormal()` (~line 261-275)
### Same change in `getPreviousLaunchesNormal()` (~line 450-465)

## Fix 2: Android Widget Filter Integration

**Files**: `NextUpWidget.kt`, `LaunchListWidget.kt`

```kotlin
// In fetchNextLaunch() / fetchUpcomingLaunches():
private suspend fun fetchNextLaunch(): LaunchNormal? {
    return withContext(Dispatchers.IO) {
        try {
            val launchRepository: LaunchRepository by koinInject(LaunchRepository::class.java)
            val notificationStateStorage: NotificationStateStorage by koinInject(NotificationStateStorage::class.java)
            val launchFilterService: LaunchFilterService by koinInject(LaunchFilterService::class.java)

            val state = notificationStateStorage.state.first()
            val agencyIds = launchFilterService.getAgencyIds(state)
            val locationIds = launchFilterService.getLocationIds(state)

            val result = launchRepository.getUpcomingLaunchesNormal(
                limit = 1,
                agencyIds = agencyIds,
                locationIds = locationIds
            )
            result.getOrNull()?.data?.results?.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
```

## Fix 3: iOS Widget Filter Integration

**File**: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/KoinInitializer.kt`

```kotlin
// In KoinHelper class, update fetchUpcomingLaunchesOrNull:
suspend fun fetchUpcomingLaunchesOrNull(limit: Int): PaginatedLaunchNormalList? {
    val notificationStateStorage = getKoin().get<NotificationStateStorage>()
    val launchFilterService = getKoin().get<LaunchFilterService>()

    val state = notificationStateStorage.state.first()
    val agencyIds = launchFilterService.getAgencyIds(state)
    val locationIds = launchFilterService.getLocationIds(state)

    val result = launchRepository.getUpcomingLaunchesNormal(
        limit,
        forceRefresh = true,
        agencyIds = agencyIds,
        locationIds = locationIds
    )
    return result.getOrNull()?.data
}
```

**No changes needed to Swift code** — `LaunchData.swift` calls `fetchUpcomingLaunchesOrNull(limit:)` which now internally handles filtering.

## Testing

### Manual Test Cases

1. **Homepage cold start**: Set agency filter → kill app → reopen → verify only filtered launches shown
2. **Widget filtering**: Set agency filter → add widget → verify widget shows only filtered launches
3. **Follow All**: Enable "Follow All" → verify all launches shown (no filtering)
4. **Filter change**: Change filters while app is open → verify widget updates on next refresh

### Automated Tests

- Unit test: `filterLaunchesByPreferences()` with various null/non-null filter combos
- Repository test: Mock `LaunchLocalDataSource` → verify fresh cache path applies filters
- Widget test: Verify `fetchNextLaunch()` reads from `NotificationStateStorage`

## Commit Message

```
fix(data): apply user filters to fresh cache and widget data

Fresh cache path in LaunchRepositoryImpl returned unfiltered launches,
causing homepage to show all launches on cold start instead of only
user-filtered ones. Android and iOS widgets also ignored filter prefs.

- Apply filterLaunchesByPreferences() to fresh cache path in
  getFeaturedLaunch, getUpcomingLaunchesNormal, getPreviousLaunchesNormal
- Inject LaunchFilterService into Android widgets (NextUpWidget,
  LaunchListWidget) to pass filter params to repository calls
- Update KoinHelper.fetchUpcomingLaunchesOrNull to read and apply
  user filter preferences for iOS widget
```
