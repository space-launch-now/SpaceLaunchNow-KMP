# 009: Fix Slow Warm Start on Low-RAM Android Devices

## Summary

This feature addresses the high slow warm start rate (12.90%) observed on Android devices with 3-4GB RAM compared to the overall app average (2.22%).

## Root Causes Addressed

1. **Default Coil Memory Cache Too Large** (~40% impact)
   - Default: ~25% of heap for image caching
   - On 4GB devices: ~200MB cache causing memory pressure
   - Fix: Custom ImageLoader with 10% cache for low-RAM, 20% for normal

2. **Blocking Main Thread During Startup** (~20% impact)
   - `runBlocking` calls in MainApplication for reading preferences
   - Fix: Use safe defaults + deferred background reading

3. **CircularProgressIndicator Animation Overhead** (~15% impact)
   - Continuous 60fps recomposition during image loading
   - Fix: Replace with shimmer + static icon pattern

## Implementation Details

### Files Created

- `MemoryUtil.kt` (commonMain) - Memory detection interface
- `MemoryUtil.android.kt` - Android implementation using ActivityManager
- `MemoryUtil.desktop.kt` / `MemoryUtil.ios.kt` - Platform stubs
- `ImageLoaderModule.kt` (commonMain) - Koin module for custom ImageLoader
- `ImageLoaderModule.android.kt` - Android ImageLoader with reduced cache
- `ImageLoaderModule.desktop.kt` / `ImageLoaderModule.ios.kt` - Platform stubs

### Files Modified

- `AppModule.kt` - Added imageLoaderModule to koinConfig
- `MainApplication.kt` - Configure Coil singleton, removed runBlocking calls
- `VehicleGridCards.kt` - Shimmer in image loading
- `VehicleConfigCards.kt` - Shimmer in image loading
- `AgencyDetailView.kt` - Shimmer in image loading
- `ExpeditionInfoCard.kt` - Shimmer in image loading
- `SharedDetailScaffold.kt` - Shimmer in image loading

### Key Configuration

Low-RAM device detection (Android):
- `ActivityManager.isLowRamDevice` OR `totalMem <= 4GB`

Cache sizing:
- Low-RAM: 10% memory cache, 50MB disk cache, no crossfade
- Normal: 20% memory cache, 100MB disk cache, crossfade enabled

## Expected Impact

- ~75% reduction in slow warm start rate on 3-4GB devices
- Target: Reduce from 12.90% to ~3-4%

## Testing

1. Run app on 3GB RAM device (e.g., Samsung A32)
   - Verify slow warm start rate improved
   - Check memory cache size in profiler (~80MB not ~200MB)

2. Run app on 4GB+ RAM device (e.g., Pixel 4a)
   - Confirm no regression on normal devices
   - Check memory cache uses standard sizing

## Related

- Android Vitals: App startup > Slow warm start
- Spec: `specs/009-fix-slow-warm-start-low-ram/spec.md`
- Plan: `specs/009-fix-slow-warm-start-low-ram/plan.md`
