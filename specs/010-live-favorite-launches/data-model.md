# Data Model: Live Favorite Launches Card

**Feature**: 010-live-favorite-launches  
**Date**: 2026-04-01

## Entities

### 1. InFlightLaunchState (New)

ViewState wrapper for in-flight launch data in HomeViewModel.

```kotlin
// Uses existing ViewState<T> pattern from ViewState.kt
// Type alias for clarity:
typealias InFlightLaunchState = ViewState<LaunchNormal?>

// Properties inherited from ViewState<T>:
data class ViewState<T>(
    val data: T,                           // The in-flight launch (null if none)
    val isLoading: Boolean = false,        // Loading indicator
    val isUserInitiated: Boolean = false,  // User pulled to refresh
    val error: String? = null,             // Error message
    val dataSource: DataSource? = null,    // CACHE, STALE_CACHE, NETWORK
    val cacheTimestamp: Long? = null       // When data was cached
)
```

**Notes**:
- No new data class needed - reuses existing `ViewState<T>` and `LaunchNormal`
- `LaunchNormal` already has `status.id` field for checking in-flight status

### 2. LaunchNormal (Existing)

Existing entity from OpenAPI generation. Key fields for this feature:

```kotlin
// Already exists in api.launchlibrary.models.LaunchNormal
data class LaunchNormal(
    val id: String,                        // UUID
    val name: String,                      // Launch name
    val status: LaunchStatus?,             // Contains status.id (6 = In Flight)
    val net: Instant?,                     // Net Expected Time
    val image: Image?,                     // Launch image
    val launchServiceProvider: AgencyNormal?, // LSP for filtering
    val pad: Pad?,                         // Location for filtering
    val mission: Mission?,                 // Mission details
    // ... other fields
)
```

### 3. LaunchStatus (Existing)

Status entity from API:

```kotlin
data class LaunchStatus(
    val id: Int,          // 6 = In Flight
    val name: String,     // "In Flight"
    val abbrev: String    // Abbreviation
)
```

**Status ID Reference**:
| ID | Name | Used For |
|----|------|----------|
| 1 | GO | Upcoming launch ready |
| 2 | TBD | To Be Determined |
| 3 | Success | Launch succeeded |
| 4 | Failure | Launch failed |
| 5 | Hold | Launch on hold |
| **6** | **In Flight** | **Currently flying** |
| 7 | Partial Failure | Partial success |
| 8 | TBC | To Be Confirmed |
| 9 | Deployed | Payload deployed |

## Relationships

```
HomeViewModel
    ├── featuredLaunchState: ViewState<LaunchNormal?>     (existing)
    ├── inFlightLaunchState: ViewState<LaunchNormal?>     (NEW)
    ├── upcomingLaunchesState: ViewState<List<LaunchNormal>>
    └── ...

LaunchRepository
    ├── getFeaturedLaunch()                               (existing)
    ├── getInFlightLaunches()                             (NEW)
    └── ...

LaunchNormal
    └── status: LaunchStatus?
        └── id: Int (6 = In Flight)
```

## State Transitions

### In-Flight Launch State Machine

```
[No Data] ─── loadHomeScreenData() ───→ [Loading]
                                            │
                    ┌───────────────────────┴───────────────────────┐
                    │                                               │
                    ▼                                               ▼
            [In Flight Found]                              [No In Flight]
            (show LIVE card)                              (hide LIVE card)
                    │                                               │
                    │                                               │
                    └───── status changes (3,4,7) ──────────────────┘
                                via API refresh
```

### Launch Status Transitions

```
GO (1) ──→ In Flight (6) ──→ Success (3)
  │                    └──→ Failure (4)
  │                    └──→ Partial Failure (7)
  └──→ Hold (5) ──→ GO (1)
```

## Validation Rules

1. **Status Filtering**: Only show launches where `status.id == 6`
2. **User Filters**: Apply `LaunchFilterService.getFilterParams()` (agency/location)
3. **Empty State**: If no in-flight launches match filters, hide LIVE section entirely
4. **Cache Duration**: Fresh for 5 minutes, stale acceptable for 30 minutes

## Database Schema (SQLDelight)

No new tables needed. Uses existing launch caching with status-based filtering:

```kotlin
// Existing table already stores status_id
// Query modification needed in LaunchLocalDataSource:

fun getInFlightLaunches(): List<LaunchNormal>? {
    // SELECT * FROM cached_normal_launches WHERE status_id = 6
    return getCachedLaunchesByStatusId(6)
}
```

## API Parameters

For fetching in-flight launches:

```kotlin
launchesApi.getLaunchList(
    statusIds = listOf(6),              // In Flight only
    lspId = filterParams.agencyIds,     // User's agency filter
    locationIds = filterParams.locationIds, // User's location filter
    limit = 5,                          // Max in-flight launches
    ordering = "net"                    // Order by launch time
)
```
