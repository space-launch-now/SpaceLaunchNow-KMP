# Research: Fix Slow Warm Start on Low-RAM Android Devices

**Feature**: 009-fix-slow-warm-start-low-ram | **Date**: 2026-03-24

## Research Tasks Completed

### 1. Coil 3 Memory Cache Configuration

**Decision**: Use dynamic memory cache sizing based on device RAM class

**Rationale**:
- Coil 3 default uses 25% of available heap for memory cache
- On 3GB devices with ~512MB app heap, this is ~128MB competing with app memory
- Android's `ActivityManager.isLowRamDevice()` reliably identifies 3-4GB devices
- Reducing to 10% on low-RAM provides ~51MB cache - still effective for scrolling performance

**Alternatives Considered**:
- Fixed cache size (50MB): Rejected - doesn't scale with device capabilities
- Disable memory cache entirely: Rejected - severe performance regression
- Use `MemoryInfo.lowMemory` threshold: Rejected - reactive not proactive

**Implementation Reference**:
```kotlin
// Coil 3 ImageLoader.Builder API
ImageLoader.Builder(context)
    .memoryCache {
        MemoryCache.Builder()
            .maxSizePercent(context, percent)  // 0.0 - 1.0
            .build()
    }
    .diskCache {
        DiskCache.Builder()
            .directory(cacheDir)
            .maxSizeBytes(sizeBytes)
            .build()
    }
```

**Source**: [Coil 3 Documentation](https://coil-kt.github.io/coil/image_loaders/)

---

### 2. Android Low-RAM Device Detection

**Decision**: Use `ActivityManager.isLowRamDevice()` as primary detection

**Rationale**:
- API available since Android 4.4 (API 19)
- System-level determination considers manufacturer RAM configuration
- Returns true for devices with ≤1GB RAM by default, but OEMs can increase threshold
- Samsung, Xiaomi, and other OEMs properly flag their 3GB devices

**Alternatives Considered**:
- Runtime.maxMemory() threshold: Rejected - varies by Dalvik/ART configuration
- Total RAM from MemoryInfo: Considered as fallback; total RAM ≤4GB as secondary check
- Device model allowlist: Rejected - unmaintainable

**Combined Approach**:
```kotlin
actual fun isLowRamDevice(): Boolean {
    val activityManager = context.getSystemService<ActivityManager>()
    // Primary check: system flag
    if (activityManager?.isLowRamDevice == true) return true
    
    // Secondary check: total RAM ≤4GB (4GB = 4 * 1024 * 1024 * 1024)
    val memInfo = ActivityManager.MemoryInfo()
    activityManager?.getMemoryInfo(memInfo)
    return memInfo.totalMem <= 4L * 1024 * 1024 * 1024
}
```

**Source**: [Android MemoryInfo API](https://developer.android.com/reference/android/app/ActivityManager.MemoryInfo)

---

### 3. CircularProgressIndicator Performance Impact

**Decision**: Replace with shimmer + static icon pattern in image loading slots

**Rationale**:
- CircularProgressIndicator triggers continuous recomposition (~60 times/sec)
- Each animation frame invalidates the composable tree
- Multiple indicators compound the problem (N images × 60 fps = N×60 recompositions/sec)
- Shimmer uses a shared animation that paints efficiently without recomposition

**Measurements** (from profiling existing fixes):
- Before: 12.90% slow warm start on 3-4GB devices
- After (partial fix): Improved warm start metrics (pending full measurement)

**Shimmer Library** (com.valentinilk.shimmer:compose-shimmer:1.3.3):
- Uses `graphicsLayer` modifier - GPU-accelerated
- Single animation state shared across all shimmer instances
- No recomposition triggered per frame

**Source**: [Compose Shimmer GitHub](https://github.com/valentinilk/compose-shimmer)

---

### 4. Blocking Initialization in Application.onCreate

**Decision**: Use safe defaults with async preference reads

**Rationale**:
- `runBlocking` on main thread delays Activity launch
- Datadog severity and sample rate have reasonable defaults
- Billing sync is not user-blocking - can happen in background

**Current Blocking Pattern** (MainApplication.kt):
```kotlin
// Lines 115-118 - blocks main thread
val consoleSeverity = runBlocking { loggingPrefs.getConsoleSeverity().first() }
val sampleRate = runBlocking { debugPrefs.debugSettingsFlow.first().datadogSampleRate }
```

**Proposed Non-Blocking Pattern**:
```kotlin
// Use defaults immediately
val consoleSeverity = Severity.Warn  // Safe default
val sampleRate = 75f  // Default from DebugSettings

// Update configuration asynchronously if different
applicationScope.launch(Dispatchers.IO) {
    val actualSeverity = loggingPrefs.getConsoleSeverity().first()
    val actualRate = debugPrefs.debugSettingsFlow.first().datadogSampleRate
    if (actualSeverity != consoleSeverity || actualRate != sampleRate) {
        // Reconfigure Datadog with actual values
    }
}
```

**Alternative Considered**:
- Move all Datadog init to first Activity: Rejected - would miss Application-level crashes

---

### 5. Deferred Billing Initialization

**Decision**: Move billing sync trigger to MainActivity.onResume

**Rationale**:
- RevenueCat SDK initialization is fast, but sync network call can delay
- Sync is not needed before UI is visible
- First Activity.onResume is reliable trigger point

**Current Pattern** (MainApplication.kt):
```kotlin
// In onCreate scope.launch block
billingManager.initialize(appUserId = null)  // Fast
// ... followed by sync operations that can delay
```

**Proposed Pattern**:
```kotlin
// MainApplication.onCreate - initialize only
billingManager.initialize(appUserId = null)

// MainActivity.onResume - sync on first resume
private var hasPerformedInitialSync = false
override fun onResume() {
    super.onResume()
    if (!hasPerformedInitialSync) {
        hasPerformedInitialSync = true
        lifecycleScope.launch {
            billingManager.syncPurchases()
        }
    }
}
```

---

### 6. Multiplatform Considerations

**Decision**: Use expect/actual for platform-specific memory detection

**Rationale**:
- `isLowRamDevice()` is Android-specific API
- Desktop always has sufficient RAM - return false
- iOS has different memory characteristics - return false (iOS manages memory differently)

**Implementation**:
```kotlin
// commonMain
expect fun isLowRamDevice(): Boolean

// androidMain
actual fun isLowRamDevice(): Boolean { /* ActivityManager check */ }

// desktopMain
actual fun isLowRamDevice(): Boolean = false

// iosMain  
actual fun isLowRamDevice(): Boolean = false
```

---

## Resolved Technical Context

| Question | Resolution |
|----------|------------|
| What cache size for low-RAM? | 10% of heap (~51MB on 512MB heap) |
| How to detect low-RAM? | ActivityManager.isLowRamDevice() + totalMem ≤4GB |
| What replaces CircularProgressIndicator? | Shimmer + static icon pattern |
| How to handle blocking init? | Safe defaults + async update |
| Where to move billing sync? | MainActivity.onResume with first-resume flag |

## Dependencies Verified

| Dependency | Version | Status |
|------------|---------|--------|
| coil-compose | 3.3.0 | ✅ Available in libs.versions.toml |
| compose-shimmer | 1.3.3 | ✅ Available in libs.versions.toml |
| Koin | Current | ✅ Already configured |

## Next Steps

1. Create `ImageLoaderModule.kt` with Koin module
2. Create `MemoryUtil.kt` with expect/actual pattern
3. Update `MainApplication.kt` to remove runBlocking
4. Update `MainActivity.kt` with deferred sync
5. Update remaining 7 files with shimmer pattern
