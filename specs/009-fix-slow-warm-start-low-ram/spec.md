# Feature Specification: Fix Slow Warm Start on Low-RAM Android Devices

**ID**: 009-fix-slow-warm-start-low-ram  
**Status**: Planning Complete  
**Priority**: High  
**Target**: Android 3-4GB RAM devices (9% of installs)

## Problem Statement

Android devices with 3-4GB of RAM are experiencing a 12.90% slow warm start rate compared to the overall 2.22% rate. This affects approximately 9% of the user base and degrades the user experience for memory-constrained devices.

## Root Cause Analysis

### Primary Causes (Completed Analysis)

1. **CircularProgressIndicator Animation Overhead**
   - Multiple animated indicators cause continuous recomposition during warm start
   - Each animation frame (~60fps) triggers composable tree invalidation
   - GPU overdraw from alpha blending in circular progress animations

2. **Default Coil Memory Cache Size**
   - Coil default: 25% of heap (~200MB on 4GB, ~150MB on 3GB devices)
   - Large cache competes with app memory during warm start
   - Aggressive eviction when backgrounded → full image reload on resume

3. **Blocking Initialization in Application.onCreate**
   - `runBlocking` calls for DataStore preference reads
   - Billing manager sync happening synchronously in onCreate

### Secondary Causes

4. **Remaining CircularProgressIndicator in non-home screens**
   - Starship section, Agency screens, Space Station screens

## Success Criteria

| Metric | Current | Target |
|--------|---------|--------|
| Slow warm start rate (3-4GB devices) | 12.90% | <5% |
| Slow warm start rate (overall) | 2.22% | <2% |
| Memory cache on low-RAM | ~200MB | ~80MB |

## Scope

### In Scope

- Custom Coil ImageLoader configuration with device-aware cache sizing
- Deferred initialization for non-blocking startup
- Complete shimmer pattern rollout for remaining image loading slots
- Unit tests for memory detection and cache configuration

### Out of Scope

- iOS performance optimization (already performing well)
- Network layer changes
- API response caching changes
- Other performance metrics (cold start, frame rate)

## Changes Already Implemented

The following files have been updated with shimmer pattern:
- ✅ LaunchListView.kt
- ✅ EventsView.kt  
- ✅ FeaturedLaunchRowCard.kt
- ✅ LatestUpdatesView.kt

## Remaining Work

### Phase 1: Custom ImageLoader (High Priority)
- Create `MemoryUtil.kt` with expect/actual pattern
- Create `ImageLoaderModule.kt` with Koin configuration
- Register ImageLoader in Koin and provide to Coil

### Phase 2: Deferred Initialization (Medium Priority)
- Remove `runBlocking` from MainApplication.onCreate
- Move billing sync to MainActivity.onResume

### Phase 3: Complete Shimmer Rollout (Low Priority)
- VehicleGrids.kt (2 occurrences)
- VehicleGridCards.kt (2 occurrences)
- VehicleConfigCards.kt (2 occurrences)
- AgencyDetailView.kt (2 occurrences)
- AgencyListView.kt (1 occurrence)
- ExpeditionInfoCard.kt (2 occurrences)
- SharedDetailScaffold.kt (1 occurrence)

## Technical Approach

See [plan.md](plan.md) for detailed implementation plan.
See [quickstart.md](quickstart.md) for code examples.

## Testing Strategy

1. **Unit Tests**: Memory detection, cache configuration
2. **Benchmark Tests**: Warm start timing comparison
3. **Manual Tests**: 3GB and 4GB device verification

## Rollout Strategy

1. Internal testing with feature flag
2. Firebase Remote Config for gradual rollout (10% → 50% → 100%)
3. Monitor Firebase Performance metrics for 1 week
4. Full release after stable metrics confirmed

## References

- Firebase Performance Monitoring dashboard
- Existing shimmer implementation in LaunchListView.kt
- [Coil 3 Memory Management](https://coil-kt.github.io/coil/)
- [Android Low-RAM Devices](https://developer.android.com/topic/performance/memory)
