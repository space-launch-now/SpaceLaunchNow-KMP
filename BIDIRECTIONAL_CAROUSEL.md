# Bidirectional Launch Carousel

## Overview

The launch carousel on the home screen now supports bidirectional scrolling, allowing users to:
- Scroll **left** to see recent **previous** launches
- Scroll **right** to see **upcoming** launches

This prevents the confusing situation where a "SUCCESS" launch appears in the first card of "upcoming launches".

## Implementation Details

### API Extensions

Added `previous` parameter support to the launch API extension functions:
- `LaunchesApi.getLaunchMiniList()` - Now supports `previous: Boolean?` parameter
- `LaunchesApi.getLaunchList()` - Now supports `previous: Boolean?` parameter

### Repository Layer

**LaunchRepository Interface:**
- Added `getPreviousLaunchesNormal(limit: Int): Result<PaginatedLaunchNormalList>`

**LaunchRepositoryImpl:**
- Implements `getPreviousLaunchesNormal()` to fetch previous launches with ordering `-net` (most recent first)

### ViewModel Layer

**HomeViewModel:**
- Added `previousLaunches: StateFlow<List<LaunchNormal>>` - List of previous launches
- Added `combinedLaunches: StateFlow<List<LaunchNormal>>` - Combined list of previous + upcoming launches
- Added `upcomingStartIndex: StateFlow<Int>` - Index where upcoming launches start in the combined list
- Updated `loadUpcomingLaunches()` to fetch both upcoming and previous launches in parallel
- Combines the launches with previous launches reversed (chronologically oldest first)

### UI Layer

**LaunchListView:**
- Uses `combinedLaunches` instead of just `upcomingLaunches`
- Automatically scrolls to `upcomingStartIndex` when data loads, positioning the view at the first upcoming launch
- Users can scroll left to see previous launches
- Users can scroll right to see more upcoming launches

## User Experience

1. **Initial View**: When the home screen loads, the carousel automatically positions at the first upcoming launch
2. **Scroll Left**: Users can swipe/scroll left to see recent previous launches (5 launches by default)
3. **Scroll Right**: Users can continue scrolling right to see more upcoming launches
4. **Visual Continuity**: The transition between previous and upcoming launches is seamless

## Configuration

The number of previous launches loaded can be adjusted in `HomeViewModel.loadUpcomingLaunches()`:
```kotlin
val previousDeferred = async { launchRepository.getPreviousLaunchesNormal(limit = 5) }
```

Change the `limit` parameter to load more or fewer previous launches.
