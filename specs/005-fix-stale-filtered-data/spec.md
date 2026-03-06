# Feature Spec: Fix Stale Filtered Data on Cold Start

## Problem Statement

When a user sets launch filter preferences (agency, location) and then kills/restarts the app, the homepage displays ALL cached launches instead of only those matching the user's active filters. This same issue affects Android and iOS widgets, which never apply user filter preferences.

## User Stories

### US-1: Homepage respects filters on cold start
**As a** user with active launch filters,  
**I want** the homepage to display only my filtered launches when I reopen the app,  
**So that** I see consistent, personalized content regardless of whether the app was recently open or cold-started.

### US-2: Widgets respect user filter preferences
**As a** user with active launch filters,  
**I want** my home screen widgets to display only launches matching my filter preferences,  
**So that** widget content is consistent with what I see in the app.

## Current Behavior (Bug)

1. **Homepage cold start**: On app restart, the repository loads cached launches from SQLDelight. If the fresh cache has data, it returns it immediately **without** applying user filters (agency/location). Only after the cache expires or on stale cache path does `filterLaunchesByPreferences()` run.

2. **Widgets (Android)**: `NextUpWidget` and `LaunchListWidget` call `getUpcomingLaunchesNormal(limit=N)` without passing any filter parameters. The repository returns unfiltered cached data.

3. **Widgets (iOS)**: Similar issue via `WidgetKitBridge` — filters not applied to widget data fetch.

## Expected Behavior

1. **Homepage**: On every data load (fresh cache, stale cache, or network), user filter preferences are applied before returning data to the UI.

2. **Widgets**: Widget data fetches should read user filter preferences from `NotificationStateStorage` via `LaunchFilterService` and pass them to repository calls.

## Functional Requirements

### FR-1: Consistent filter application in repository
The `LaunchRepositoryImpl` must apply user filter preferences on all code paths when returning cached data, not only on the stale cache path.

### FR-2: Widget filter integration (Android)
`NextUpWidget` and `LaunchListWidget` must read `NotificationState` and pass `agencyIds`/`locationIds` to their repository calls.

### FR-3: Widget filter integration (iOS)
The iOS widget bridge must pass filter parameters when fetching launch data.

### FR-4: Filter change triggers widget refresh
When the user changes filter preferences, widgets should be refreshed to reflect the new filters.

## Non-Functional Requirements

### NFR-1: Performance
Filter application should not add noticeable latency. In-memory filtering of cached results is acceptable for current data volumes (< 100 launches).

### NFR-2: Backward compatibility
No changes to the public `LaunchRepository` interface signatures. Filtering is handled internally or via existing optional parameters.

## Affected Components

- `LaunchRepositoryImpl` (commonMain) — cache retrieval logic
- `NextUpWidget` (androidMain) — widget data fetch
- `LaunchListWidget` (androidMain) — widget data fetch
- `WidgetUpdater` (androidMain) — widget update coordination
- `PlatformWidgetUpdater` (iosMain) — iOS widget update
- `WidgetKitBridge` (iosMain) — iOS widget data bridge
- `LaunchFilterService` (commonMain) — filter parameter extraction

## Out of Scope

- Database-level filtering (adding SQL WHERE clauses for agency/location)
- Composite caching per filter configuration
- New widget types or widget UI changes

## Acceptance Criteria

1. User sets agency filter → kills app → reopens → homepage shows only launches from that agency
2. User sets location filter → widget shows only launches from that location
3. User changes filters → widgets update within normal refresh cycle
4. User with "Follow All" enabled → sees all launches (no filtering applied)
5. No regression in API fetch filtering behavior
