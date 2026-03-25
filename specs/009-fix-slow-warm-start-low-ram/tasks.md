# Tasks: Fix Slow Warm Start on Low-RAM Android Devices

**Feature**: 009-fix-slow-warm-start-low-ram  
**Input**: Design documents from `/specs/009-fix-slow-warm-start-low-ram/`  
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, quickstart.md ✅

**Organization**: Tasks are organized by priority phases from the implementation plan.

## Format: `[ID] [P?] [Phase] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Phase]**: Which implementation phase this task belongs to (SETUP, P1-High, P2-Med, P3-Low)
- Include exact file paths in descriptions

## Progress Summary

| Phase | Description | Target Impact | Status |
|-------|-------------|---------------|--------|
| Setup | Infrastructure & expect/actual | Foundation | ✅ Complete |
| P1-High | Custom Coil ImageLoader | ~40% improvement | ✅ Complete |
| P2-Med | Deferred Initialization | ~20% improvement | ✅ Complete |
| P3-Low | Complete Shimmer Rollout | ~15% improvement | ✅ Complete |
| Polish | Tests + Documentation | Validation | ⬜ Not Started |

## ✅ Already Completed (DO NOT INCLUDE IN TASKS)

These files have already been updated with shimmer pattern:
- ✅ `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/home/components/LaunchListView.kt`
- ✅ `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/home/components/EventsView.kt`
- ✅ `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/home/components/FeaturedLaunchRowCard.kt`
- ✅ `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/home/components/LatestUpdatesView.kt`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create foundational utilities for device memory detection across platforms

- [X] T001 Create `expect fun isLowRamDevice(): Boolean` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/MemoryUtil.kt`
- [X] T002 [P] Implement Android `actual` with ActivityManager.isLowRamDevice() and totalMem check in `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/util/MemoryUtil.android.kt`
- [X] T003 [P] Implement Desktop `actual` returning false in `composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/util/MemoryUtil.desktop.kt`
- [X] T004 [P] Implement iOS `actual` returning false in `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/util/MemoryUtil.ios.kt`

**Checkpoint**: MemoryUtil expect/actual pattern complete - can detect low-RAM devices on Android

---

## Phase 2: Custom Coil ImageLoader (Priority: P1-High) 🎯 MVP

**Goal**: Reduce memory cache pressure on low-RAM devices from ~25% to 10% of heap

**Impact**: ~40% of slow warm start improvement (memory cache is largest contributor)

**Independent Test**: Run app on 3GB device, verify memory cache uses ~80MB not ~200MB

### Implementation for P1-High

- [X] T005 [P1-High] Create `ImageLoaderModule.kt` with Koin module providing custom ImageLoader in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/di/ImageLoaderModule.kt`
  - Low-RAM devices: 10% memory cache, 50MB disk cache
  - Normal devices: 20% memory cache, 100MB disk cache
  - Configure crossfade and disk cache directory
- [X] T006 [P1-High] Register `imageLoaderModule` in Koin configuration in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/di/AppModule.kt` (koinConfig)
- [X] T007 [P1-High] Configure Coil singleton to use Koin-provided ImageLoader in `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainApplication.kt`
  - Added `SingletonImageLoader.setSafe` call after Koin starts
  - Get ImageLoader from Koin: `koin.koin.getOrNull<ImageLoader>()`

**Checkpoint**: Custom ImageLoader active - low-RAM devices use reduced cache sizes

---

## Phase 3: Deferred Initialization (Priority: P2-Med)

**Goal**: Remove blocking `runBlocking` calls from Application.onCreate to reduce startup latency

**Impact**: ~20% of slow warm start improvement (removes main thread blocking)

**Independent Test**: Run app cold start, verify no ANR warnings in logcat during startup

### Implementation for P2-Med

- [X] T008 [P2-Med] Replace `runBlocking` for consoleSeverity with safe default (Severity.Warn) in `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainApplication.kt`
- [X] T009 [P2-Med] Replace `runBlocking` for sampleRate with safe default (75f) in `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainApplication.kt`
- [X] T010 [P2-Med] Add background coroutine to read actual preferences and reconfigure Datadog if needed in `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainApplication.kt`
- [X] T011 [P2-Med] SKIPPED - Billing already uses GlobalScope.launch (non-blocking), no change needed

**Checkpoint**: Application.onCreate no longer blocks - startup is non-blocking

---

## Phase 4: Complete Shimmer Rollout - Starship Section (Priority: P3-Low)

**Goal**: Replace CircularProgressIndicator with shimmer pattern in Starship components

**Impact**: ~5% improvement (Starship is not home screen, less frequently accessed)

**Independent Test**: Navigate to Starship section, verify shimmer loading instead of spinner

### Implementation for P3-Low (Starship)

- [X] T012 [P] [P3-Low] SKIPPED - VehicleGrids.kt uses CircularProgressIndicator for pagination (not image loading)
- [X] T013 [P] [P3-Low] Replace CircularProgressIndicator with shimmer in `VehicleGridCards.kt` (2 occurrences) - DONE
- [X] T014 [P] [P3-Low] Replace CircularProgressIndicator with shimmer in `VehicleConfigCards.kt` (2 occurrences) - DONE

**Checkpoint**: All Starship components use shimmer pattern

---

## Phase 5: Complete Shimmer Rollout - Agency Section (Priority: P3-Low)

**Goal**: Replace CircularProgressIndicator with shimmer pattern in Agency screens

**Impact**: ~5% improvement (Agency screens less frequently accessed than home)

**Independent Test**: Navigate to Agencies section, verify shimmer loading instead of spinner

### Implementation for P3-Low (Agencies)

- [X] T015 [P] [P3-Low] Replace CircularProgressIndicator with shimmer in `AgencyDetailView.kt` (image loading portion) - DONE
  - Note: Kept CircularProgressIndicator for full-page loading state (data loading, not image)
- [X] T016 [P] [P3-Low] SKIPPED - AgencyListView.kt uses CircularProgressIndicator for data loading (not image loading)

**Checkpoint**: All Agency components use shimmer pattern

---

## Phase 6: Complete Shimmer Rollout - Remaining Components (Priority: P3-Low)

**Goal**: Replace CircularProgressIndicator with shimmer in Space Station and shared components

**Impact**: ~5% improvement (remaining edge cases)

**Independent Test**: Navigate to Space Station section and detail views, verify shimmer loading

### Implementation for P3-Low (Remaining)

- [X] T017 [P] [P3-Low] Replace CircularProgressIndicator with shimmer in `ExpeditionInfoCard.kt` (2 occurrences) - DONE
- [X] T018 [P] [P3-Low] Replace CircularProgressIndicator with shimmer in `SharedDetailScaffold.kt` (1 occurrence) - DONE

**Checkpoint**: All remaining components use shimmer pattern - CircularProgressIndicator fully removed from image loading

---

## Phase 7: Polish & Validation

**Purpose**: Testing, documentation, and validation of performance improvements

- [ ] T019 [P] Create unit test for isLowRamDevice() in `composeApp/src/androidTest/kotlin/me/calebjones/spacelaunchnow/util/MemoryUtilTest.kt`
  - Test low-RAM detection logic
  - Mock ActivityManager for deterministic testing
- [ ] T020 [P] Create unit test for ImageLoader configuration in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/di/ImageLoaderModuleTest.kt`
  - Verify cache sizes for low/normal RAM configurations
- [X] T021 [P] Update feature documentation with implementation notes in `specs/009-fix-slow-warm-start-low-ram/README.md` - DONE
- [ ] T022 Manual verification on 3GB RAM device (Samsung A32 or similar)
  - Confirm slow warm start rate improved
  - Check memory cache size in profiler (~80MB not ~200MB)
- [ ] T023 Manual verification on 4GB RAM device (Pixel 4a or similar)
  - Confirm no regression on normal devices
  - Check memory cache uses standard sizing

**Checkpoint**: Feature complete and validated - ready for PR

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1: Setup ─────────────┐
                            ▼
Phase 2: P1-High (ImageLoader) ───┐
                                  │
Phase 3: P2-Med (Deferred Init) ──┤── Can run in parallel after Setup
                                  │
Phase 4-6: P3-Low (Shimmer) ──────┘
                                  │
                                  ▼
Phase 7: Polish & Validation
```

### Critical Path (MVP)

**Minimum Viable Performance Fix** (Phases 1-2 only):
1. T001-T004: Setup (MemoryUtil expect/actual)
2. T005-T007: Custom ImageLoader (largest impact)

This alone should achieve ~40% improvement. P2-Med and P3-Low are incremental.

### Parallel Opportunities

**Phase 1 Parallelization**:
```
T001 (expect) ──► T002 [P] (androidMain)
              ├► T003 [P] (desktopMain)
              └► T004 [P] (iosMain)
```

**Phase 4-6 Parallelization** (All P3-Low shimmer tasks can run in parallel):
```
T012 [P] (VehicleGrids.kt)
T013 [P] (VehicleGridCards.kt)
T014 [P] (VehicleConfigCards.kt)
T015 [P] (AgencyDetailView.kt)
T016 [P] (AgencyListView.kt)
T017 [P] (ExpeditionInfoCard.kt)
T018 [P] (SharedDetailScaffold.kt)
```

---

## Shimmer Pattern Reference

Use this pattern when replacing CircularProgressIndicator (from already-fixed files):

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
            modifier = Modifier.size(72.dp),  // Adjust size as needed
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
    }
}
```

**Required Import**: `import com.valentinilk.shimmer.shimmer`

---

## Success Criteria

| Metric | Current | Target | Measurement |
|--------|---------|--------|-------------|
| Slow warm start (3-4GB devices) | 12.90% | <5% | Firebase Perf Monitoring |
| Slow warm start (overall) | 2.22% | <2% | Firebase Perf Monitoring |
| Memory cache size (low-RAM) | ~200MB | ~80MB | Android Profiler |

---

## Commit Message Format

Following conventional commits for CI/CD:

- Phase 1: `feat(perf): add MemoryUtil expect/actual for low-RAM detection`
- Phase 2: `perf(coil): configure device-aware memory cache sizing`
- Phase 3: `perf(startup): defer blocking initialization to background`
- Phase 4-6: `perf(ui): replace CircularProgressIndicator with shimmer in [component]`
- Phase 7: `test(perf): add unit tests for memory detection and cache config`
