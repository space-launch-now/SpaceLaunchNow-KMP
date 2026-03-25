# Implementation Plan: Fix Slow Warm Start on Low-RAM Android Devices

**Branch**: `009-fix-slow-warm-start-low-ram` | **Date**: 2026-03-24 | **Spec**: [specs/009-fix-slow-warm-start-low-ram/spec.md](spec.md)
**Input**: Performance optimization for 3-4GB RAM Android devices

## Summary

Fix the slow warm start rate on Android devices with 3-4GB RAM (currently 12.90% vs 2.22% overall). Target these devices representing 9% of installs. Implement custom Coil ImageLoader configuration with reduced memory cache, defer billing initialization to onResume, and complete shimmer pattern rollout for remaining image loading slots.

## Technical Context

**Language/Version**: Kotlin 2.0.21, Java 21  
**Primary Dependencies**: Coil 3.3.0, compose-shimmer 1.3.3, Koin DI, RevenueCat KMP  
**Storage**: DataStore (preferences), KStore (subscription state)  
**Testing**: JUnit5 + kotlinx-coroutines-test, Android instrumentation tests  
**Target Platform**: Android 8.0+ (API 26), focus on 3-4GB RAM devices  
**Project Type**: Mobile (Kotlin Multiplatform - Android, iOS, Desktop)  
**Performance Goals**: Reduce slow warm start rate from 12.90% to <5% on 3-4GB devices  
**Constraints**: Memory cache must not exceed 15% of heap on low-RAM devices, startup blocking < 100ms  
**Scale/Scope**: 9% of user base (~4,500 users based on 50k active)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Pre-Phase 0 Check (PASSED)

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Mobile-First (Android & iOS Equal Priority) | ✅ PASS | Android-specific optimization; iOS already performs well |
| II. Pattern-Based Consistency | ✅ PASS | Uses established shimmer pattern from already-fixed files |
| III. Accessibility & User Experience | ✅ PASS | Shimmer provides visual feedback, icons remain for accessibility |
| IV. CI/CD & Conventional Commits | ✅ PASS | Feature branch, `perf:` or `fix:` commit types |
| V. Code Generation & API Management | ✅ N/A | No API changes required |
| VI. Multiplatform Architecture | ✅ PASS | Coil config in commonMain, platform-specific cache sizing in androidMain |
| VII. Testing Standards | ✅ PASS | Benchmark tests for warm start, unit tests for cache config |
| VIII. Jetpack Compose Best Practices | ✅ PASS | Shimmer replaces animated CircularProgressIndicator reducing recompositions |

## Root Cause Analysis (Completed)

### Primary Causes (HIGH PRIORITY - Home Screen Impact)

1. **CircularProgressIndicator Animation Overhead** (PARTIALLY FIXED)
   - Multiple circular progress animations spin simultaneously during warm start
   - Each animation causes continuous recomposition (~16ms frame budget exceeded)
   - GPU overdraw from alpha blending in animations
   - **Fixed in**: LaunchListView.kt, EventsView.kt, FeaturedLaunchRowCard.kt, LatestUpdatesView.kt

2. **Default Coil Memory Cache Too Large** (NOT FIXED)
   - Default: 25% of heap = ~200MB on 4GB device
   - On 3GB device: ~150MB cache competes with app memory
   - Aggressive eviction during background → full reload on warm start
   - **Solution**: Custom ImageLoader with 10-15% heap for low-RAM, 20% for normal

3. **Blocking Initialization in onCreate** (PARTIALLY ADDRESSED)
   - `runBlocking` calls in MainApplication for preferences read
   - Billing sync triggered immediately in onCreate
   - **Solution**: Defer non-critical init to onResume/background

### Secondary Causes (LOWER PRIORITY - Not Home Screen)

4. **Remaining CircularProgressIndicator Files** (NOT FIXED)
   - Starship section: VehicleGrids.kt, VehicleGridCards.kt, VehicleConfigCards.kt
   - Agency screens: AgencyDetailView.kt, AgencyListView.kt
   - Space Station: ExpeditionInfoCard.kt
   - Shared: SharedDetailScaffold.kt

## Project Structure

### Documentation (this feature)

```text
specs/009-fix-slow-warm-start-low-ram/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output (minimal - no new models)
├── quickstart.md        # Phase 1 output
└── tasks.md             # Phase 2 output
```

### Source Code Changes

```text
composeApp/src/
├── commonMain/kotlin/me/calebjones/spacelaunchnow/
│   ├── di/
│   │   └── ImageLoaderModule.kt              # NEW: Custom Coil ImageLoader config
│   ├── ui/
│   │   ├── starship/components/
│   │   │   ├── VehicleGrids.kt               # UPDATE: Replace CircularProgressIndicator
│   │   │   ├── VehicleGridCards.kt           # UPDATE: Replace CircularProgressIndicator
│   │   │   └── VehicleConfigCards.kt         # UPDATE: Replace CircularProgressIndicator
│   │   ├── agencies/compose/
│   │   │   ├── AgencyDetailView.kt           # UPDATE: Replace CircularProgressIndicator
│   │   │   └── AgencyListView.kt             # UPDATE: Replace CircularProgressIndicator
│   │   ├── spacestation/components/
│   │   │   └── ExpeditionInfoCard.kt         # UPDATE: Replace CircularProgressIndicator
│   │   └── compose/
│   │       └── SharedDetailScaffold.kt       # UPDATE: Replace CircularProgressIndicator
│
├── androidMain/kotlin/me/calebjones/spacelaunchnow/
│   ├── MainApplication.kt                    # UPDATE: Defer billing init, reduce runBlocking
│   ├── MainActivity.kt                       # UPDATE: Add deferred init in onResume
│   └── util/
│       └── MemoryUtil.kt                     # NEW: Low-RAM device detection
│
└── desktopMain/kotlin/me/calebjones/spacelaunchnow/
    └── util/
        └── MemoryUtil.kt                     # NEW: Stub for cross-platform expect/actual
```

## Implementation Phases

### Phase 1: Custom Coil ImageLoader (HIGH IMPACT)

**Goal**: Reduce memory cache pressure on low-RAM devices

**Changes**:
1. Create `expect fun isLowRamDevice(): Boolean` in commonMain
2. Implement Android `actual` using `ActivityManager.isLowRamDevice()`
3. Create `ImageLoaderModule.kt` with custom configuration:
   - Low-RAM devices: 10% heap memory cache, 50MB disk cache
   - Normal devices: 20% heap memory cache, 100MB disk cache
4. Register ImageLoader in Koin and provide to Coil

**Technical Approach**:
```kotlin
// commonMain - expect declaration
expect fun isLowRamDevice(): Boolean

// androidMain - actual implementation
actual fun isLowRamDevice(): Boolean {
    val activityManager = context.getSystemService<ActivityManager>()
    return activityManager?.isLowRamDevice == true
}

// ImageLoader configuration
val imageLoaderModule = module {
    single<ImageLoader> {
        val context = get<Context>()
        ImageLoader.Builder(context)
            .memoryCache {
                val maxPercent = if (isLowRamDevice()) 0.10 else 0.20
                MemoryCache.Builder()
                    .maxSizePercent(context, maxPercent)
                    .build()
            }
            .diskCache {
                val maxSize = if (isLowRamDevice()) 50L * 1024 * 1024 else 100L * 1024 * 1024
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(maxSize)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
```

### Phase 2: Deferred Initialization (MEDIUM IMPACT)

**Goal**: Remove blocking calls from Application.onCreate

**Changes**:
1. Move Datadog initialization check to background coroutine
2. Move billing sync trigger to MainActivity.onResume with "first resume" flag
3. Replace `runBlocking` for preferences with cached defaults + background update

**Current Blocking Code** (MainApplication.kt:115-118):
```kotlin
val consoleSeverity = runBlocking {
    loggingPrefs.getConsoleSeverity().first()
}
val sampleRate = runBlocking {
    debugPrefs.debugSettingsFlow.first().datadogSampleRate
}
```

**Solution**:
```kotlin
// Use defaults, update asynchronously
val consoleSeverity = co.touchlab.kermit.Severity.Warn  // Safe default
val sampleRate = 75f  // Default sample rate

// Update in background after init
applicationScope.launch {
    val actualSeverity = loggingPrefs.getConsoleSeverity().first()
    val actualRate = debugPrefs.debugSettingsFlow.first().datadogSampleRate
    // Reconfigure Datadog if needed
}
```

### Phase 3: Complete Shimmer Pattern Rollout (LOW IMPACT)

**Goal**: Replace remaining CircularProgressIndicator in image loading slots

**Files to Update** (7 files, ~14 occurrences):

| File | Occurrences | Priority |
|------|-------------|----------|
| VehicleGrids.kt | 2 | Low (Starship section) |
| VehicleGridCards.kt | 2 | Low (Starship section) |
| VehicleConfigCards.kt | 2 | Low (Starship section) |
| AgencyDetailView.kt | 2 | Low (Agency screens) |
| AgencyListView.kt | 1 | Low (Agency screens) |
| ExpeditionInfoCard.kt | 2 | Low (Space station) |
| SharedDetailScaffold.kt | 1 | Medium (Shared component) |

**Shimmer Pattern** (from already-fixed files):
```kotlin
loading = {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .shimmer()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = CustomIcons.RocketLaunch,  // Or appropriate icon
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
    }
}
```

## Success Metrics

| Metric | Current | Target | Measurement |
|--------|---------|--------|-------------|
| Slow warm start (3-4GB devices) | 12.90% | <5% | Firebase Perf Monitoring |
| Slow warm start (overall) | 2.22% | <2% | Firebase Perf Monitoring |
| Memory cache size (low-RAM) | ~200MB | ~80MB | Profiler |
| App startup time | TBD | <500ms | Firebase Perf |

## Testing Strategy

### Unit Tests
- `MemoryUtilTest.kt`: Verify isLowRamDevice detection
- `ImageLoaderConfigTest.kt`: Verify cache sizes for low/normal RAM

### Integration Tests
- `WarmStartBenchmark.kt`: Macrobenchmark for warm start timing
- `ImageCacheBehaviorTest.kt`: Verify cache eviction behavior

### Manual Testing
- Test on 3GB RAM device (e.g., Samsung A32)
- Test on 4GB RAM device (e.g., Pixel 4a)
- Compare warm start with/without changes

## Complexity Tracking

No constitution violations - all changes follow established patterns.

| Risk | Mitigation |
|------|-----------|
| Reduced cache causes more network requests | Monitor network usage post-release |
| Deferred init causes race conditions | Use proper coroutine synchronization |
| Shimmer animation causes its own overhead | Shimmer uses pre-baked animation, measured as low-overhead |

## Rollout Strategy

1. **Phase 1**: Implement behind feature flag for internal testing
2. **Phase 2**: Firebase Remote Config for gradual rollout (10% → 50% → 100%)
3. **Phase 3**: Monitor Firebase Performance metrics
4. **Phase 4**: Full release after 1 week of stable metrics

## References

- [Coil 3 Memory Management](https://coil-kt.github.io/coil/upgrading_to_coil3/)
- [Android Low-RAM Devices](https://developer.android.com/topic/performance/memory)
- [Firebase Performance Monitoring](https://firebase.google.com/docs/perf-mon)
- Existing shimmer pattern in LaunchListView.kt, EventsView.kt
