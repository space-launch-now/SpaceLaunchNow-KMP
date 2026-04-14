# Feature Specification: Reduce API Calls Per Session & Improve Caching

**Feature Branch**: `012-reduce-api-calls-caching`  
**Created**: 2026-04-13  
**Status**: Draft  
**Input**: User description: "Analyze API requests per session - they seem to be far higher than they need to be. Please make a plan to increase caching and reduction of startup API calls."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Returning User Sees Home Screen Instantly (Priority: P1)

A returning user opens the app and sees cached data in the Home screen within 1-2 seconds while fresh data loads in the background. The app should not make 11+ simultaneous API calls on Home screen mount. Above-fold content (featured launch, upcoming launches, news feed) should display from cache immediately.

**Why this priority**: This is the most common user session — returning users represent >90% of sessions. Reducing API calls here has the highest impact on perceived performance, battery usage, and API quota.

**Independent Test**: Open app as returning user with cached data from previous session. Verify Home screen sections appear instantly from cache, and network monitor shows ≤5 API calls on Home mount (not 11+).

**Acceptance Scenarios**:

1. **Given** a returning user with cached data (≤10 min old), **When** they open the app, **Then** Home screen sections display from cache with 0 API calls and background refresh triggers only for expired sections.
2. **Given** a returning user with stale cache (>10 min old), **When** they open the app, **Then** stale data displays immediately (DataSource.STALE_CACHE), and API calls refresh only visible above-fold sections (≤5 calls).
3. **Given** a returning user with no cache (cleared data), **When** they open the app, **Then** at most 5-7 API calls fire for above-fold content; below-fold sections defer until scrolled.

---

### User Story 2 - First-Time User Preload Only Caches Useful Data (Priority: P2)

A first-time user's preload screen fires only API calls whose results are actually consumed by the Home screen. Currently 6 of 7 Tier 1 preload tasks are wasted (data cached but never read by Home ViewModels, or using uncached repo methods).

**Why this priority**: Preload delays first-time user experience. Removing wasted calls speeds up the onboarding flow and reduces unnecessary API quota usage.

**Independent Test**: Fresh install → preload screen → measure Tier 1 API calls. Verify each preloaded result is consumed via cache on subsequent Home screen navigation.

**Acceptance Scenarios**:

1. **Given** a first-time user, **When** the preload screen runs, **Then** Tier 1 makes ≤4 critical API calls (featured launch, upcoming launches, articles, events) — not 7.
2. **Given** completed preload, **When** the user navigates to Home, **Then** all Home sections read from cache (0 additional API calls for preloaded data).
3. **Given** a low-RAM device, **When** preload runs, **Then** Tier 2 background tasks are skipped and Home screen gracefully falls back to on-demand loading.

---

### User Story 3 - Below-Fold Sections Load on Scroll (Priority: P2)

Stats (24h/week/month counts), History (launches on this day), and Previous Launches sections are below the fold on Home. They should not make API calls until the user scrolls them into view.

**Why this priority**: These sections fire 4-6 API calls that the user may never see if they navigate away from Home. Deferring saves API quota and reduces startup contention.

**Independent Test**: Open Home screen, observe network. Verify stats/history/previous sections make 0 calls until scrolled into viewport.

**Acceptance Scenarios**:

1. **Given** a user on the Home screen, **When** they do NOT scroll past the upcoming launches section, **Then** StatsViewModel, HistoryViewModel, and previous launches make 0 API calls.
2. **Given** a user scrolls to the stats section, **When** the stats item becomes visible, **Then** StatsViewModel.loadAllStats() triggers (3 calls) with cache support.

---

### User Story 4 - Consolidated Launch Queries (Priority: P3)

FeaturedLaunchViewModel and LaunchesViewModel both call the `launches/` endpoint with overlapping filters. A single upstream query should serve both, reducing 4 launch API calls to 1-2.

**Why this priority**: Architectural consolidation that prevents data duplication. Lower priority because caching (P1) already reduces most redundant calls — this is an optimization on top.

**Independent Test**: Network monitor shows ≤2 calls to `launches/` endpoint on Home load (1 upcoming+featured, 1 in-flight) instead of 4.

**Acceptance Scenarios**:

1. **Given** the Home screen loads, **When** FeaturedLaunchVM and LaunchesVM both need upcoming data, **Then** a single shared API call serves both ViewModels.
2. **Given** cached upcoming launch data, **When** both ViewModels access it, **Then** 0 API calls fire and both show consistent data.

---

### Edge Cases

- What happens when cache is corrupted (malformed JSON)? → Graceful fallback to API call, delete corrupted entry.
- What happens when user has no connectivity and no cache? → Show offline banner with empty state per section.
- What happens when cache TTL expires mid-session? → Stale-while-revalidate pattern shows stale data while refreshing.
- What happens when preload is interrupted (app killed during first-time setup)? → Preload resumes on next launch; Home uses on-demand loading for missing data.
- What happens when Stats time-range query returns different counts between cache and fresh? → Fresh data replaces cache immediately via StateFlow update.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Home screen MUST load above-fold content from cache when available (cache-first strategy).
- **FR-002**: Home screen MUST make ≤5 API calls on mount for a returning user with valid cache.
- **FR-003**: Below-fold Home sections (Stats, History, Previous Launches) MUST defer API calls until scrolled into view.
- **FR-004**: PreloadViewModel Tier 1 MUST only include tasks whose results are consumed by Home screen via cache.
- **FR-005**: `getUpcomingLaunchesList()`, `getNextNormalLaunch()`, and `getInFlightLaunches()` repository methods MUST implement cache-first with stale-while-revalidate pattern.
- **FR-006**: Stats time-range queries MUST be cacheable with TTL-based expiration (10 min).
- **FR-007**: All cache additions MUST respect existing `AppPreferences.isDebugShortCacheTtlEnabled()` debug override.
- **FR-008**: Cache additions MUST be cleaned by existing `CacheCleanupService` on its 6-hour interval.

### Key Entities

- **LaunchCache**: Extended to cover stats time-range results, next-launch, and in-flight launch data.
- **PreloadTask**: Modified to align Tier 1/Tier 2 boundaries with actual Home screen data consumption.
- **HomeDataCoordinator** (new, Phase 4): Shared data holder for consolidated launch queries serving multiple ViewModels.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Returning user Home screen mount triggers ≤5 API calls (down from ~11).
- **SC-002**: First-time user preload Tier 1 triggers ≤4 API calls (down from 7), all consumed by Home cache.
- **SC-003**: Below-fold sections trigger 0 API calls until scrolled into view.
- **SC-004**: `launches/` endpoint calls reduced from 7 to ≤3 on Home load.
- **SC-005**: All existing functionality remains intact — no visual regressions or missing data.
- **SC-006**: Cache hit rate for returning users >80% on first Home screen render.
