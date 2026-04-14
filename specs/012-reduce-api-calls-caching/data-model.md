# Data Model: Reduce API Calls Per Session & Improve Caching

**Feature**: `012-reduce-api-calls-caching`  
**Date**: 2026-04-13

## New Entities

### StatsCache (SQLDelight Table)

**Purpose**: Lightweight cache for stats count values (24h/week/month launch counts), avoiding full launch list serialization.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `stat_key` | TEXT | PRIMARY KEY, NOT NULL | Cache identifier: `"stats_24h"`, `"stats_week"`, `"stats_month"` |
| `count` | INTEGER | NOT NULL | Total matching launch count from API `response.count` |
| `cached_at` | INTEGER | NOT NULL | Epoch milliseconds when cached |
| `expires_at` | INTEGER | NOT NULL | Epoch milliseconds when cache expires |

**Indexes**: `idx_stats_expires ON StatsCache(expires_at)` — for cleanup queries.

**TTL**: 10 minutes (production), 1 minute (debug via `AppPreferences.isDebugShortCacheTtlEnabled()`).

**SQL Schema**:
```sql
CREATE TABLE StatsCache (
    stat_key TEXT NOT NULL PRIMARY KEY,
    count INTEGER NOT NULL,
    cached_at INTEGER NOT NULL,
    expires_at INTEGER NOT NULL
);

CREATE INDEX idx_stats_expires ON StatsCache(expires_at);

-- Queries
getStatCount:
SELECT count FROM StatsCache WHERE stat_key = ? AND expires_at > ?;

getStatCountStale:
SELECT count, cached_at FROM StatsCache WHERE stat_key = ?;

upsertStat:
INSERT OR REPLACE INTO StatsCache(stat_key, count, cached_at, expires_at) VALUES (?, ?, ?, ?);

deleteExpiredStats:
DELETE FROM StatsCache WHERE expires_at < ?;

deleteAllStats:
DELETE FROM StatsCache;
```

---

### HomeDataCoordinator (Kotlin Class — Phase 4)

**Purpose**: Shared data holder for consolidated launch query, serving both FeaturedLaunchViewModel and LaunchesViewModel from a single API call.

| Property | Type | Description |
|----------|------|-------------|
| `_homeUpcomingLaunches` | `MutableSharedFlow<DataResult<PaginatedLaunchNormalList>>` | Single upstream data source, replay=1 |
| `featuredLaunches` | `Flow<List<LaunchNormal>>` | Derived: filter by statusIds [1,3,4,5,6,7,9], take 4 |
| `upcomingLaunches` | `Flow<List<LaunchNormal>>` | Derived: take 10 |
| `isLoading` | `StateFlow<Boolean>` | Loading state |

**Lifecycle**: Singleton, registered in `AppModule.kt`. Survives ViewModel recreation on config changes.

---

## Modified Entities

### PreloadTask (Existing — PreloadViewModel.kt)

**Current tiers restructured**:

| Tier | Current Count | New Count | Net Change |
|------|--------------|-----------|------------|
| Tier 1 (CRITICAL) | 7 tasks | 4 tasks | -3 |
| Tier 2 (WARM_CACHE) | 19 tasks | 13 tasks | -6 |
| **Total** | **26 tasks** | **17 tasks** | **-9** |

**Tier 1 Changes**:
- REMOVED: Next launch, Upcoming basic, Previous basic, Astronauts, Rockets, Agencies (6 tasks)
- ADDED (from Tier 2): Featured launch, Upcoming normal launches (2 tasks)
- KEPT: Articles for onboarding (1 task)
- NEW: Upcoming events (moved from Tier 2)

**Tier 2 Changes**:
- REMOVED: Stats 24h/week/month (3 tasks — deferred to scroll), ISS station details (1 task — not Home)
- REMAINING: Previous normal, Latest updates, History launches, 8 filter tasks = 11 tasks

### LaunchLocalDataSource (Existing)

**New methods** (following existing patterns):

| Method | Signature | Purpose |
|--------|-----------|---------|
| `getInFlightNormalLaunches` | `(limit: Int) -> List<LaunchNormal>` | Fresh cache: in-flight launches (status=6) |
| `getInFlightNormalLaunchesStale` | `(limit: Int) -> List<LaunchNormal>` | Stale cache: in-flight launches |
| `cacheInFlightLaunches` | `(launches: List<LaunchNormal>) -> Unit` | Store in-flight launches with 5-min TTL |

**Note**: In-flight launches are stored in `LaunchNormalCache` table (existing) with a distinguishing query filter. No new table needed — the `status` column already exists in the cache table and can be queried.

### LaunchRepositoryImpl (Existing)

**Modified methods**:

| Method | Change |
|--------|--------|
| `getInFlightLaunches()` | Add stale-while-revalidate cache (currently API-only) |
| `getUpcomingLaunchesList()` | No cache change for full response, but add stats count caching |

**New internal method**:

| Method | Signature | Purpose |
|--------|-----------|---------|
| `getStatsCount` | `(key: String, netGt: Instant, netLt: Instant, forceRefresh: Boolean) -> Result<DataResult<Int>>` | Cache-first stats count retrieval |

### CacheCleanupService (Existing)

**Added cleanup call**: `statsLocalDataSource.deleteExpiredStats()` in the periodic cleanup cycle.

---

## Relationships

```
HomeScreen
├── FeaturedLaunchViewModel ─────┐
│   └── uses LaunchRepository    │
├── LaunchesViewModel ───────────┤ Phase 4: Both share HomeDataCoordinator
│   └── uses LaunchRepository    │
├── StatsViewModel               │
│   └── uses LaunchRepository ───┘ Phase 2: Uses StatsCache via new getStatsCount()
│       └── reads StatsCache
├── FeedViewModel
│   └── uses ArticlesRepository (cached ✅)
│   └── uses UpdatesRepository (cached ✅)
├── EventsViewModel
│   └── uses EventsRepository (cached ✅)
└── HistoryViewModel
    └── uses LaunchRepository (cached ✅)

PreloadViewModel
├── Tier 1: getFeaturedLaunch → LaunchNormalCache
├── Tier 1: getUpcomingLaunchesNormal → LaunchNormalCache
├── Tier 1: getArticles → ArticleCache
├── Tier 1: getUpcomingEvents → EventCache
├── Tier 2: getPreviousLaunchesNormal → LaunchNormalCache
├── Tier 2: getLatestUpdates → UpdateCache
├── Tier 2: getLaunchesByDayAndMonth → LaunchNormalCache
└── Tier 2: filter data × 8 → FilterOptionsLocalDataSource

CacheCleanupService (every 6 hours)
├── LaunchLocalDataSource.deleteExpiredLaunches()
├── EventLocalDataSource.deleteExpiredEvents()
├── ArticleLocalDataSource.deleteExpiredArticles()
├── UpdateLocalDataSource.deleteExpiredUpdates()
├── ProgramLocalDataSource.deleteExpiredPrograms()
├── SpacecraftLocalDataSource.deleteExpiredSpacecraft()
├── FilterOptionsLocalDataSource.deleteExpiredAgencies()
└── StatsLocalDataSource.deleteExpiredStats()  ← NEW
```

---

## State Transitions

### Stats Cache Lifecycle

```
EMPTY → [API Call] → FRESH (10 min TTL)
FRESH → [TTL expires] → STALE
STALE → [ViewModel requests] → Return STALE immediately + trigger background refresh → FRESH
FRESH/STALE → [CacheCleanupService runs] → EMPTY (if past expires_at)
FRESH/STALE → [Debug TTL enabled] → Behaves as 1-min TTL
```

### Home Screen Section Loading States

```
App Launch (returning user):
  Above-fold sections:
    CACHE → [LaunchedEffect(Unit)] → Check cache → HIT → Render from cache → Background refresh
    EMPTY → [LaunchedEffect(Unit)] → Check cache → MISS → API call → Render
  
  Below-fold sections (Phase 3):
    DEFERRED → [User scrolls to item] → [LaunchedEffect(Unit) inside item] → Check cache → HIT/MISS → Load
```
