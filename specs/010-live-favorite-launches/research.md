# Research: Live Favorite Launches Card

**Feature**: 010-live-favorite-launches  
**Date**: 2026-04-01

## Research Questions

### 1. API Support for In-Flight Status Filtering

**Decision**: Use existing API `status__ids` parameter with value `6` (In Flight)

**Rationale**: 
- Launch Library API 2.4.0 supports `status__ids` parameter for filtering by launch status
- Status ID 6 = "In Flight" as confirmed in `StatusColorUtil.kt`
- The existing `getLaunchList()` extension function already supports `statusIds` parameter

**Alternatives Considered**:
- Client-side filtering: Rejected - would require fetching all launches and filtering locally, inefficient
- Separate "in flight" endpoint: Not available in API

**Evidence**:
```kotlin
// From StatusColorUtil.kt
6 -> Color(0xFF1976D2) // In Flight - Blue 500
6 -> "In Flight"

// From ll_2.4.0.json schema
"status__ids" filter parameter available on /launches endpoints
```

### 2. Compose Animation for LIVE Indicator

**Decision**: Use Compose `infiniteTransition` with `animateFloat` for pulsing glow effect

**Rationale**:
- Cross-platform (works on Android, iOS, Desktop via Compose Multiplatform)
- Battery-efficient when using proper animation specs
- Native Compose API, no additional dependencies

**Alternatives Considered**:
- Lottie animations: Rejected - adds dependency, KMP support varies
- Custom Canvas drawing: Rejected - overkill for simple pulse effect
- GIF/video: Rejected - not scalable, accessibility concerns

**Implementation Pattern**:
```kotlin
val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
val alpha by infiniteTransition.animateFloat(
    initialValue = 0.5f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
        animation = tween(1000, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_alpha"
)
```

### 3. User Filter Integration

**Decision**: Reuse existing `LaunchFilterService.getFilterParams()` for consistent filtering

**Rationale**:
- Ensures LIVE card respects same filters as rest of home page
- No code duplication
- Already handles followAllLaunches, subscribedAgencies, subscribedLocations

**Evidence**:
```kotlin
// From HomeViewModel.kt
val currentFilters = notificationStateStorage.stateFlow.first()
val filterParams = launchFilterService.getFilterParams(currentFilters)
```

### 4. Caching Strategy for In-Flight Launches

**Decision**: Short TTL cache (5 minutes) with stale-while-revalidate pattern

**Rationale**:
- In-flight status changes quickly (launch → success/failure in minutes)
- Still want offline support for poor connectivity
- Follows constitutional Stale-While-Revalidate requirement

**Implementation**:
- Cache key: `"in_flight_launches"` (no filter parameters needed - status_id is fixed)
- TTL: 5 minutes fresh, 30 minutes stale
- Background refresh when showing stale data

### 5. Home Page Layout Integration

**Decision**: Insert LIVE card above featured launch in home page LazyColumn

**Rationale**:
- Most visible position
- Non-intrusive when no in-flight launches
- Follows existing home page component pattern

**Layout Structure**:
```
HomeScreen
├── [IF in-flight launch exists] LiveLaunchCard
├── FeaturedLaunchCard (NextUpView)
├── UpcomingLaunchesCarousel
├── UpdatesFeed
└── ...
```

### 6. Status Transition Handling

**Decision**: Rely on API polling (5-minute interval) for status updates

**Rationale**:
- Push notifications already handle status changes (inFlight, success, failure)
- API will return empty results when no in-flight launches
- UI gracefully hides LIVE card when data is empty

**No WebSocket needed**: 
- Polling interval aligns with typical launch phases
- Push notifications supplement for real-time awareness

## Resolved Unknowns

| Unknown | Resolution |
|---------|------------|
| API parameter for in-flight filtering | `status__ids=6` via getLaunchList extension |
| Animation approach for LIVE badge | Compose infiniteTransition with pulsing alpha |
| Filter integration | Reuse LaunchFilterService.getFilterParams() |
| Cache strategy | 5-minute TTL, stale-while-revalidate |
| Layout position | Above featured launch in home LazyColumn |
| Status change handling | API polling + existing push notifications |

## Dependencies Verified

| Dependency | Version | Purpose | Status |
|------------|---------|---------|--------|
| Compose Material3 | 1.7.x | Card, colors, animation | ✅ Exists |
| Ktor Client | 3.x | API calls | ✅ Exists |
| Koin | 3.5.x | DI for repository | ✅ Exists |
| SQLDelight | 2.x | Local caching | ✅ Exists |
| LaunchesApiExtensions | N/A | statusIds parameter | ✅ Already implemented |
