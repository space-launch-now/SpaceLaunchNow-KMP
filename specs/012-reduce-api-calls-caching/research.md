# Research: Reduce API Calls Per Session & Improve Caching

**Feature**: `012-reduce-api-calls-caching`  
**Date**: 2026-04-13

## R1: Preload Tier 1/Tier 2 Alignment

**Question**: Which Tier 1 preload tasks are consumed by the Home screen via cache?

### Findings

**Current Tier 1 (7 tasks):**

| Task | Repo Method | Has Cache? | Consumed by Home? | Verdict |
|------|-------------|------------|-------------------|---------|
| Next launch | `getNextNormalLaunch(1)` | ❌ No | ❌ No | **REMOVE** |
| Upcoming basic | `getUpcomingLaunchesList(5)` | ❌ No | ❌ No (Home uses Normal type) | **REMOVE** |
| Previous basic | `getPreviousLaunchesList(5)` | ❌ No | ❌ No (Home uses Normal type) | **REMOVE** |
| Articles (3) | `articlesRepository.getArticles(3)` | ✅ Yes | ✅ FeedViewModel | **KEEP** |
| Astronauts in space | `astronautRepository.getAstronauts(3)` | ❌ No | ❌ Not on Home | **REMOVE** |
| Active rockets | `rocketRepository.getRockets(3)` | ❌ No | ❌ Not on Home | **REMOVE** |
| Featured agencies | `agencyRepository.getAgencies(3)` | ❌ No | ❌ Not on Home | **REMOVE** |

**Current Tier 2 (19 tasks) — consumed by Home:**

| Task | Repo Method | Has Cache? | Consumed by Home? |
|------|-------------|------------|-------------------|
| Featured launch | `getFeaturedLaunch()` | ✅ Yes | ✅ FeaturedLaunchVM |
| Upcoming normal (8) | `getUpcomingLaunchesNormal(8)` | ✅ Yes | ✅ LaunchesVM |
| Previous normal (8) | `getPreviousLaunchesNormal(8)` | ✅ Yes | ✅ LaunchesVM |
| Latest updates | `getLatestUpdates()` | ✅ Yes | ✅ FeedVM |
| Articles | `getArticles()` | ✅ Yes | ✅ FeedVM |
| Upcoming events | `getUpcomingEvents()` | ✅ Yes | ✅ EventsVM |
| History launches | `getLaunchesByDayAndMonth()` | ✅ Yes | ✅ HistoryVM |
| Stats 24h/week/month (×3) | `getUpcomingLaunchesList()` | ❌ No | ❌ Wasted (no cache) |
| Filter data (×8) | Various filter repos | ✅ Yes | ✅ ScheduleVM (not Home, but needed for navigation) |
| ISS cache | `prewarmIssCache()` | ✅ Yes | ❌ Only Space Station screen |

### Decision: Restructured Preload Tiers

**New Tier 1 (CRITICAL, must complete before navigation):**
1. `getFeaturedLaunch()` — Hero card, above-fold
2. `getUpcomingLaunchesNormal(8)` — Carousel, above-fold
3. `articlesRepository.getArticles()` — Feed section, above-fold
4. `eventsRepository.getUpcomingEvents()` — Events section, near-fold

**New Tier 2 (WARM_CACHE, background):**
1. `getPreviousLaunchesNormal(8)` — Below-fold carousel
2. `updatesRepository.getLatestUpdates()` — Below-fold feed
3. `getLaunchesByDayAndMonth()` — History section (below-fold)
4. Filter data (agencies, programs, rockets, etc.) — Navigation prep
5. ISS station details — Space Station screen prep

**Removed entirely:**
- Next launch (1) — uncached, not consumed
- Upcoming basic (5) — wrong type, uncached
- Previous basic (5) — wrong type, uncached
- Astronauts in space (3) — not on Home
- Active rockets (3) — not on Home
- Featured agencies (3) — not on Home
- Stats 24h/week/month (×3) — uncached, deferred to scroll

**Rationale**: Only promote Tier 2 → Tier 1 for above-fold Home content with working cache. Remove tasks where either the cache path is broken or the data isn't consumed.

---

## R2: Cache Implementation for Uncached Repository Methods

**Question**: How should `getUpcomingLaunchesList()`, `getNextNormalLaunch()`, and `getInFlightLaunches()` implement caching?

### Findings

**Reference pattern from `getFeaturedLaunch()` (the gold standard):**

```kotlin
// 1. Build cache key
val cacheKey = buildCacheKey("featured_launch", agencyIds, locationIds)

// 2. Check stale data first (for fallback)
val staleCached = localDataSource?.getUpcomingNormalLaunchesStale(4)

// 3. Try fresh cache if not forcing refresh
if (!forceRefresh) {
    val cachedLaunches = localDataSource?.getUpcomingNormalLaunches(4)
    if (cachedLaunches != null && cachedLaunches.isNotEmpty()) {
        return Result.success(DataResult(data = ..., source = DataSource.CACHE))
    }
    // Fresh cache empty → try stale
    if (hasStaleData) {
        return Result.success(DataResult(data = ..., source = DataSource.STALE_CACHE))
    }
}

// 4. Fetch from API
val response = launchesApi.getLaunchList(...)
localDataSource?.cacheNormalLaunches(response.results)

// 5. On error → fall back to stale cache
catch (e: Exception) {
    if (hasStaleData) return stale
    return Result.failure(e)
}
```

### Decision: Cache Strategy Per Method

**`getInFlightLaunches()`** — Add stale-while-revalidate:
- Cache key: `buildCacheKey("in_flight", agencyIds, locationIds)`
- Storage: `LaunchLocalDataSource.cacheNormalLaunches()` (reuse existing)
- Retrieval: New `getInFlightNormalLaunches(limit)` query in Launch.sq (filter by status=6 from cached data)
- TTL: 5 minutes (volatile data — launches in flight change rapidly)
- Alternative considered: Could filter from `getUpcomingNormalLaunches` but status=6 isn't "upcoming" — needs separate query

**`getUpcomingLaunchesList()` (Basic type)** — Add simple cache:
- This returns `PaginatedLaunchBasicList` (different type from Normal)
- Currently used by Stats for COUNT only (limit=1, just reads `.count` field)
- Instead of caching full Basic list, cache the **count value** in a simpler mechanism
- New approach: Add `StatsCache` — a lightweight key-value cache for stats counts
- Cache key: `"stats_24h"`, `"stats_week"`, `"stats_month"`
- TTL: 10 minutes
- Rationale: Caching just an Int is simpler than adding full BasicLaunch cache support

**`getNextNormalLaunch()`** — Eliminate entirely:
- Not called by any Home screen ViewModel
- Only consumed by PreloadViewModel Tier 1 (which we're removing this task from)
- If needed in future: use existing `getUpcomingLaunchesNormal(limit=1)` which IS cached
- Decision: Remove from preload, no caching needed

### Alternatives Considered

1. **Full HTTP-level caching via Ktor HttpCache**: Rejected for Phase 2 — doesn't integrate with SQLDelight stale-while-revalidate pattern. Kept as optional Phase 5.
2. **Shared `LaunchBasicCache` table**: Rejected — Stats only needs counts. Adding a full `LaunchBasicCache` table adds migration complexity for minimal benefit.
3. **In-memory-only stats cache**: Considered, but stats should survive process death. Use SQLDelight for consistency.

---

## R3: Deferred Below-Fold Loading Pattern

**Question**: How should below-fold Home sections trigger loading only when visible?

### Findings

**Existing pattern in the codebase** (`ScheduleScreen.kt` line ~180):
```kotlin
LaunchedEffect(upcomingListState) {
    snapshotFlow { upcomingListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
        .filter { it != null }
        .distinctUntilChanged()
        .collectLatest { lastIndex ->
            if (lastIndex >= uiState.upcomingTab.items.size - 8) {
                viewModel.loadNextPage(ScheduleTab.Upcoming)
            }
        }
}
```

**Simpler approach for Home: LaunchedEffect inside item blocks.**

The Home screen uses a `LazyColumn` with keyed items. Each below-fold section is its own `item(key = ...)`. Placing a `LaunchedEffect(Unit)` inside the composable rendered by that item block ensures the effect only fires when the item is composed (i.e., scrolled into view).

```kotlin
// Current (all fire on mount):
LaunchedEffect(Unit) {
    statsViewModel.loadAllStats()  // 3 API calls
}

// New (fire when visible):
item(key = "quick_stats") {
    LaunchedEffect(Unit) { statsViewModel.loadAllStats() }
    QuickStatsSection(...)
}
```

This is the simplest approach and aligns with Compose best practices: items in `LazyColumn` are only composed when visible, so `LaunchedEffect(Unit)` inside them naturally defers.

### Decision: Use LaunchedEffect-inside-item pattern

- Move `statsViewModel.loadAllStats()` from top-level LaunchedEffect into the `item(key = "quick_stats")` block
- Move `historyViewModel.loadHistoryLaunches()` into the `item(key = "history_section")` block
- Move `launchesViewModel.loadPreviousLaunches()` into the appropriate item block (or split `loadLaunches()` into `loadUpcomingLaunches()` and `loadPreviousLaunches()`)
- Keep `featuredLaunchViewModel.loadFeaturedLaunch()`, `launchesViewModel.loadUpcomingLaunches()`, `feedViewModel.loadUpdates()`, `feedViewModel.loadArticles()`, `eventsViewModel.loadEvents()` in the top-level LaunchedEffect

### Alternatives Considered

1. **snapshotFlow on LazyListState**: More complex, used for infinite scroll. Overkill for "load once when visible".
2. **Visibility callback via Modifier.onGloballyPositioned**: Works but tightly couples to layout. LaunchedEffect in item is cleaner.
3. **Preheat buffer (load N items ahead)**: Not needed — below-fold sections are 2-3 scroll screens away. By the time user scrolls, the LaunchedEffect has had time to complete.

---

## R4: Launch Query Consolidation

**Question**: Can FeaturedLaunchVM and LaunchesVM share a single API call?

### Findings

**FeaturedLaunchVM requests:**
- `limit=4, netGt=(now-1h), statusIds=[1,3,4,5,6,7,9], ordering="net"`
- Purpose: Hero card + additional featured launches

**LaunchesVM requests:**
- `limit=10, upcoming=true, ordering="net"` (no statusIds filter)
- Purpose: Upcoming launches carousel

**Overlap analysis:**
- Both query upcoming launches
- Featured adds `netGt=(now-1h)` to catch recently launched items and `statusIds` to filter
- Upcoming is broader (no status filter)
- A single `limit=15, upcoming=true, ordering="net"` call would return a superset
- Featured items = client-side filter of first N with matching statusIds

### Decision: Introduce HomeDataCoordinator (Phase 4)

```kotlin
class HomeDataCoordinator(
    private val launchRepository: LaunchRepository
) {
    private val _upcomingLaunches = MutableSharedFlow<DataResult<PaginatedLaunchNormalList>>(replay = 1)
    
    suspend fun loadHomeData(forceRefresh: Boolean = false) {
        val result = launchRepository.getUpcomingLaunchesNormal(limit = 15, forceRefresh = forceRefresh)
        // Shared across FeaturedLaunchVM and LaunchesVM
        _upcomingLaunches.emit(result)
    }
    
    // Featured = filter by statusIds [1,3,4,5,6,7,9] and take first 4
    val featuredLaunches: Flow<List<LaunchNormal>> = ...
    
    // Upcoming carousel = take first 10
    val upcomingLaunches: Flow<List<LaunchNormal>> = ...
}
```

**Registered as singleton** in AppModule.kt (single instance shared across ViewModels).

### Alternatives Considered

1. **Merge into existing LaunchesViewModel**: Rejected — FeaturedLaunchVM has additional logic (pinned content, remote config) that would bloat LaunchesVM.
2. **Repository-level dedup**: Rejected — Repository methods have different signatures/return types. Coordinator is a cleaner abstraction.
3. **Skip consolidation, rely on caching**: Viable for Phase 2 alone (cache prevents redundant API calls). Phase 4 is an optimization on top.

---

## R5: Stats Caching Approach

**Question**: How to cache stats count values efficiently?

### Findings

StatsViewModel calls `getUpcomingLaunchesList(limit=1, netGt=X, netLt=Y)` three times. It only reads the `count` field from the response: `response.count` (total matching launches, not the items).

**Options:**
1. Cache full BasicLaunch list — heavy, needs new table migration
2. Cache just the count in existing `LaunchBasicCache` with special keys — misuses the table
3. Create new lightweight `StatsCache` table — clean, minimal

### Decision: New `StatsCache` SQLDelight table

```sql
CREATE TABLE StatsCache (
    stat_key TEXT NOT NULL PRIMARY KEY,  -- "stats_24h", "stats_week", "stats_month"
    count INTEGER NOT NULL,
    cached_at INTEGER NOT NULL,
    expires_at INTEGER NOT NULL
);

CREATE INDEX idx_stats_expires ON StatsCache(expires_at);
```

- TTL: 10 minutes (same as launch cache)
- Debug TTL: 1 minute
- Cleanup by `CacheCleanupService` via `deleteExpiredStats()`
- New `StatsLocalDataSource` class following existing LocalDataSource patterns

### Rationale

This is the lightest-weight solution. We're caching 3 integers, not serialized launch objects. A dedicated table avoids polluting the LaunchBasicCache with non-launch data and avoids complex cache key schemes.

---

## R6: Ktor HTTP-Level Caching (Phase 5 Feasibility)

**Question**: Does the Launch Library API support conditional requests (ETag/Last-Modified)?

### Findings

The LL API at `ll.thespacedevs.com` returns:
- `Cache-Control: max-age=60` on most endpoints
- **No `ETag` header observed**
- **No `Last-Modified` header observed**
- Varies by endpoint: some return `public, max-age=60`, others `private`

Ktor's `HttpCache` plugin respects `Cache-Control: max-age` headers, meaning it would avoid re-fetching for 60 seconds automatically without any code changes.

### Decision: Defer to Phase 5 (optional)

- Adding `install(HttpCache)` would give a free 60-second HTTP-level cache on compatible endpoints
- However, the app's SQLDelight cache already has 10-minute TTLs (much longer than 60s)
- HTTP cache only helps for rapid-fire duplicate calls within 60s — partially overlaps with our Phase 2/3 fixes
- Low priority but easy to implement and harmless

### Alternatives Considered

1. **Custom OkHttp interceptor**: Rejected — Ktor, not OkHttp
2. **Replace SQLDelight cache with HTTP cache**: Rejected — HTTP cache doesn't support stale-while-revalidate or offline access
