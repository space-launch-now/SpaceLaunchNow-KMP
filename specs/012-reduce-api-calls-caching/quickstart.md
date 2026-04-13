# Quickstart: Reduce API Calls Per Session & Improve Caching

**Feature**: `012-reduce-api-calls-caching`

## Prerequisites

- Java 21 (JetBrains JDK)
- `.env` file with `API_KEY`
- Run `./gradlew openApiGenerate` if fresh clone

## Implementation Order

The 5 phases are designed so Phases 1-3 are independent (any order). Phase 4 depends on Phase 2. Phase 5 is fully independent.

**Recommended order**: Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5

## Phase 1: Clean Up Preload

**Files**: `PreloadViewModel.kt`

1. Open `PreloadViewModel.kt`
2. In `buildTier1Tasks()`:
   - Remove all 7 current tasks
   - Replace with 4 new Tier 1 tasks:
     ```kotlin
     PreloadTask("Featured launch", PreloadTier.CRITICAL) {
         launchRepository.getFeaturedLaunch(agencyIds = agencyIds, locationIds = locationIds)
     }
     PreloadTask("Upcoming normal launches", PreloadTier.CRITICAL) {
         launchRepository.getUpcomingLaunchesNormal(limit = 8, agencyIds = agencyIds, locationIds = locationIds)
     }
     PreloadTask("Articles", PreloadTier.CRITICAL) {
         articlesRepository.getArticles()
     }
     PreloadTask("Upcoming events", PreloadTier.CRITICAL) {
         eventsRepository.getUpcomingEvents()
     }
     ```
3. In `buildTier2Tasks()`:
   - Remove stats preload tasks (3 tasks: 24h/week/month)
   - Remove ISS station details task
   - Remove: astronauts, rockets, agencies tasks
   - Keep: previous normal launches, latest updates, history launches, all filter data

4. Verify: `./gradlew compileKotlinDesktop`

## Phase 2: Add Caching to Uncached Methods

**Files**: `Launch.sq`, `StatsLocalDataSource.kt` (new), `LaunchLocalDataSource.kt`, `LaunchRepositoryImpl.kt`, `LaunchRepository.kt`, `AppModule.kt`, `CacheCleanupService.kt`

1. Create `Stats.sq` in SQLDelight:
   ```sql
   CREATE TABLE StatsCache (
       stat_key TEXT NOT NULL PRIMARY KEY,
       count INTEGER NOT NULL,
       cached_at INTEGER NOT NULL,
       expires_at INTEGER NOT NULL
   );
   CREATE INDEX idx_stats_expires ON StatsCache(expires_at);
   ```

2. Create `StatsLocalDataSource.kt` following `ArticleLocalDataSource` as reference

3. Add `getStatsCount()` to `LaunchRepository` interface

4. Implement `getStatsCount()` in `LaunchRepositoryImpl` with stale-while-revalidate (see `contracts/launch-cache-contract.md`)

5. Add cache to `getInFlightLaunches()` in `LaunchRepositoryImpl` (see contract)

6. Register `StatsLocalDataSource` in `AppModule.kt`

7. Add `statsLocalDataSource.deleteExpiredStats()` to `CacheCleanupService`

8. Update `StatsViewModel` to use `getStatsCount()` instead of `getUpcomingLaunchesList()`

9. Verify: `./gradlew compileKotlinDesktop`

## Phase 3: Defer Below-Fold Sections

**Files**: `HomeScreen.kt`, `ResponsiveHomeContent.kt`

1. In `HomeScreen.kt` top-level `LaunchedEffect(Unit)`:
   - REMOVE: `statsViewModel.loadAllStats()`
   - Keep: featured, upcoming, feed, events loads

2. In `ResponsiveHomeContent.kt`:
   - Inside `item(key = "quick_stats")` composable, add:
     ```kotlin
     LaunchedEffect(Unit) { statsViewModel.loadAllStats() }
     ```
   - Inside `item(key = "history_section")` composable, add:
     ```kotlin
     LaunchedEffect(currentDay, currentMonth) { historyViewModel.loadHistoryLaunches() }
     ```
   - Split `launchesViewModel.loadLaunches()` into separate upcoming/previous calls, defer previous to its item block

3. Verify: Run desktop app, scroll Home page, confirm below-fold sections load on scroll

## Phase 4: Consolidate Launch Queries (Optional)

**Files**: `HomeDataCoordinator.kt` (new), `FeaturedLaunchViewModel.kt`, `LaunchesViewModel.kt`, `HomeScreen.kt`, `AppModule.kt`

1. Create `HomeDataCoordinator.kt` (see `contracts/home-coordinator-contract.md`)
2. Register as `single { HomeDataCoordinator(get()) }` in `AppModule.kt`
3. Modify `FeaturedLaunchViewModel` to consume from coordinator
4. Modify `LaunchesViewModel` to consume from coordinator
5. Update `HomeScreen.kt` LaunchedEffect to call `homeDataCoordinator.loadHomeData()`
6. Verify: desktop app loads Home with all sections from single API call

## Phase 5: HTTP-Level Caching (Optional)

**Files**: `NetworkModule.kt`

1. In `NetworkModule.kt`, add to HttpClient:
   ```kotlin
   install(HttpCache)
   ```
2. Verify LL API returns `Cache-Control` headers (test with curl)
3. If API doesn't return cache headers, remove and close this phase

## Verification Checklist

- [ ] Returning user Home: ≤5 API calls on mount
- [ ] First-time preload: ≤4 Tier 1 tasks
- [ ] Below-fold: 0 API calls until scrolled
- [ ] Stats section: serves from cache within 10 min
- [ ] In-flight section: serves from cache within 5 min
- [ ] Debug short cache TTL: all new caches respect 1-min override
- [ ] CacheCleanupService: cleans StatsCache entries
- [ ] All platforms build: `./gradlew compileKotlinDesktop`, Android, iOS
- [ ] No visual regressions on Home screen
