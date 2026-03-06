# Data Model: Fix Stale Filtered Data on Cold Start

## Entities

No new entities are introduced. This fix modifies the **data flow** through existing entities.

## Existing Entities (Reference)

### LaunchNormal
- **Source**: Generated API model (`me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal`)
- **Filter-relevant fields**:
  - `launchServiceProvider?.id: Int?` — matched against `agencyIds` filter
  - `pad?.location?.id: Int?` — matched against `locationIds` filter

### NotificationState
- **Source**: `data/model/NotificationState.kt`
- **Filter-relevant fields**:
  - `followAllLaunches: Boolean` — when true, no filters applied
  - `subscribedAgencies: Set<String>` — agency IDs as strings
  - `subscribedLocations: Set<String>` — location IDs as strings

### FilterParams
- **Source**: `data/services/LaunchFilterService.kt`
- **Fields**:
  - `agencyIds: List<Int>?` — null means no agency filter
  - `locationIds: List<Int>?` — null means no location filter
  - `requiresFlexibleMerge: Boolean` — OR vs AND logic flag

## Data Flow Corrections

### Before (Bug)

```
getFeaturedLaunch(agencyIds=[1,2], locationIds=[3])
│
├─ Fresh cache hit?
│   └─ YES → Return ALL cached launches ❌ (ignores agencyIds/locationIds)
│
├─ Stale cache?
│   └─ YES → filterLaunchesByPreferences() → Return filtered ✅
│
└─ API fetch
    └─ Pass agencyIds/locationIds to API → Return filtered ✅
```

### After (Fix)

```
getFeaturedLaunch(agencyIds=[1,2], locationIds=[3])
│
├─ Fresh cache hit?
│   └─ YES → filterLaunchesByPreferences() → Return filtered ✅
│
├─ Stale cache?
│   └─ YES → filterLaunchesByPreferences() → Return filtered ✅
│
└─ API fetch
    └─ Pass agencyIds/locationIds to API → Return filtered ✅
```

### Widget Data Flow — Before (Bug)

```
NextUpWidget.fetchNextLaunch()
│
└─ launchRepository.getUpcomingLaunchesNormal(limit=1) ← NO FILTERS
    └─ Returns first cached launch regardless of user preferences ❌
```

### Widget Data Flow — After (Fix)

```
NextUpWidget.fetchNextLaunch()
│
├─ Read NotificationState from NotificationStateStorage
├─ Get agencyIds/locationIds from LaunchFilterService
└─ launchRepository.getUpcomingLaunchesNormal(limit=1, agencyIds=ids, locationIds=ids)
    └─ Returns first launch matching user filter preferences ✅
```

## State Transitions

No state transitions affected. `NotificationState` is read-only from the perspective of this fix.

## Validation Rules

1. When `followAllLaunches == true`: `agencyIds` and `locationIds` must be `null` (no filtering)
2. When filters produce zero results from cache: fall through to API call (existing behavior preserved)
3. `filterLaunchesByPreferences()` with `null` filter lists returns input unchanged (identity operation)
