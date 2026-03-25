# Quickstart: Fix Slow Warm Start on Low-RAM Android Devices

**Feature**: 009-fix-slow-warm-start-low-ram | **Date**: 2026-03-24

## Prerequisites

- Android Studio with Kotlin 2.0.21+
- Existing project dependencies (Coil 3.3.0, compose-shimmer 1.3.3)
- Familiarity with Koin DI pattern used in project

## Implementation Guide

### Step 1: Create MemoryUtil (Expect/Actual)

#### commonMain: MemoryUtil.kt
```kotlin
// composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/MemoryUtil.kt
package me.calebjones.spacelaunchnow.util

/**
 * Determines if the current device has constrained memory (3-4GB RAM).
 * Used to adjust cache sizes and loading behavior for better warm start performance.
 */
expect fun isLowRamDevice(): Boolean
```

#### androidMain: MemoryUtil.android.kt
```kotlin
// composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/util/MemoryUtil.android.kt
package me.calebjones.spacelaunchnow.util

import android.app.ActivityManager
import android.content.Context
import androidx.core.content.getSystemService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun isLowRamDevice(): Boolean {
    return MemoryUtilImpl.isLowRamDevice()
}

/**
 * Android implementation using ActivityManager APIs.
 */
internal object MemoryUtilImpl : KoinComponent {
    private val context: Context by inject()
    
    fun isLowRamDevice(): Boolean {
        val activityManager = context.getSystemService<ActivityManager>() ?: return false
        
        // Primary check: System-level low RAM flag
        if (activityManager.isLowRamDevice) return true
        
        // Secondary check: Total RAM ≤ 4GB
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        val fourGigabytes = 4L * 1024 * 1024 * 1024
        return memInfo.totalMem <= fourGigabytes
    }
}
```

#### desktopMain: MemoryUtil.desktop.kt
```kotlin
// composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/util/MemoryUtil.desktop.kt
package me.calebjones.spacelaunchnow.util

/**
 * Desktop always has sufficient memory - return false.
 */
actual fun isLowRamDevice(): Boolean = false
```

#### iosMain: MemoryUtil.ios.kt
```kotlin
// composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/util/MemoryUtil.ios.kt
package me.calebjones.spacelaunchnow.util

/**
 * iOS manages memory differently - return false.
 * iOS will terminate apps under memory pressure rather than allowing degraded performance.
 */
actual fun isLowRamDevice(): Boolean = false
```

---

### Step 2: Create Custom ImageLoader Module

```kotlin
// composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/di/ImageLoaderModule.kt
package me.calebjones.spacelaunchnow.di

import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import me.calebjones.spacelaunchnow.util.isLowRamDevice
import okio.Path.Companion.toOkioPath
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Koin module providing a custom Coil ImageLoader optimized for device memory class.
 * 
 * Configuration:
 * - Low-RAM devices (≤4GB): 10% memory cache, 50MB disk cache
 * - Normal devices: 20% memory cache, 100MB disk cache
 */
val imageLoaderModule = module {
    single<ImageLoader> {
        val context = get<android.content.Context>()
        val isLowRam = isLowRamDevice()
        
        val memoryCachePercent = if (isLowRam) 0.10 else 0.20
        val diskCacheSize = if (isLowRam) {
            50L * 1024 * 1024  // 50MB
        } else {
            100L * 1024 * 1024  // 100MB
        }
        
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, memoryCachePercent)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizeBytes(diskCacheSize)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
```

---

### Step 3: Register ImageLoader in Koin Configuration

Update `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/di/KoinConfig.kt`:

```kotlin
// Add import
import me.calebjones.spacelaunchnow.di.imageLoaderModule

// Update koinConfig to include imageLoaderModule
val koinConfig = koinConfiguration {
    modules(
        // ... existing modules ...
        networkModule,
        appModule,
        apiModule,
        imageLoaderModule,  // ADD THIS LINE
        // ... other modules ...
    )
}
```

---

### Step 4: Provide ImageLoader to Coil

In `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainActivity.kt`, ensure Coil uses the custom ImageLoader:

```kotlin
import coil3.compose.setSingletonImageLoaderFactory
import org.koin.android.ext.android.get

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure Coil to use our custom ImageLoader from Koin
        setSingletonImageLoaderFactory { context ->
            get<ImageLoader>()
        }
        
        // ... rest of onCreate
    }
}
```

---

### Step 5: Remove Blocking Initialization

Update `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainApplication.kt`:

**Before** (lines ~113-130):
```kotlin
// REMOVE THIS BLOCKING PATTERN
val consoleSeverity = runBlocking {
    loggingPrefs.getConsoleSeverity().first()
}
val sampleRate = runBlocking {
    debugPrefs.debugSettingsFlow.first().datadogSampleRate
}
```

**After**:
```kotlin
// Use safe defaults, update asynchronously
val defaultSeverity = co.touchlab.kermit.Severity.Warn
val defaultSampleRate = 75f

// Check if we should initialize Datadog with defaults
if (defaultSeverity <= co.touchlab.kermit.Severity.Debug || BuildConfig.IS_DEBUG) {
    log.d { "Initializing Datadog with default settings..." }
    initializeDatadog(
        context = this,
        sampleRate = defaultSampleRate,
        debugPreferences = debugPrefs
    )
    log.d { "✅ Datadog initialized with defaults" }
    
    // Update configuration asynchronously if user has different settings
    kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
        try {
            val actualSeverity = loggingPrefs.getConsoleSeverity().first()
            val actualRate = debugPrefs.debugSettingsFlow.first().datadogSampleRate
            // Reconfigure if needed (Datadog supports runtime config updates)
            if (actualRate != defaultSampleRate) {
                log.d { "Updating Datadog sample rate to ${actualRate.toInt()}%" }
                // Apply new configuration
            }
        } catch (e: Exception) {
            log.w(e) { "Failed to update Datadog configuration" }
        }
    }
}
```

---

### Step 6: Defer Billing Sync to onResume

Update `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainActivity.kt`:

```kotlin
class MainActivity : ComponentActivity() {
    
    private var hasPerformedInitialSync = false
    
    override fun onResume() {
        super.onResume()
        
        // Perform initial billing sync on first resume (not in Application.onCreate)
        if (!hasPerformedInitialSync) {
            hasPerformedInitialSync = true
            lifecycleScope.launch {
                try {
                    val billingManager = get<BillingManager>()
                    billingManager.syncPurchases()
                    log.d { "✅ Initial billing sync completed" }
                } catch (e: Exception) {
                    log.w(e) { "Initial billing sync failed" }
                }
            }
        }
    }
}
```

---

### Step 7: Replace CircularProgressIndicator with Shimmer Pattern

For each file with image loading, replace CircularProgressIndicator with shimmer pattern.

#### Example: VehicleGridCards.kt

**Before**:
```kotlin
SubcomposeAsyncImage(
    model = imageUrl,
    contentDescription = "Vehicle image",
    modifier = Modifier.fillMaxSize(),
    contentScale = ContentScale.Crop,
    loading = {
        CircularProgressIndicator(modifier = Modifier.size(24.dp))
    }
)
```

**After**:
```kotlin
import com.valentinilk.shimmer.shimmer
import me.calebjones.spacelaunchnow.ui.icons.CustomIcons
import me.calebjones.spacelaunchnow.ui.icons.RocketLaunch

SubcomposeAsyncImage(
    model = imageUrl,
    contentDescription = "Vehicle image",
    modifier = Modifier.fillMaxSize(),
    contentScale = ContentScale.Crop,
    loading = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shimmer()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = CustomIcons.RocketLaunch,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
    }
)
```

#### Files to Update

| File | Icon to Use | Size |
|------|-------------|------|
| VehicleGrids.kt | CustomIcons.RocketLaunch | 32.dp |
| VehicleGridCards.kt | CustomIcons.RocketLaunch | 24.dp |
| VehicleConfigCards.kt | CustomIcons.RocketLaunch | 24.dp |
| AgencyDetailView.kt | Icons.Default.Business | 48.dp |
| AgencyListView.kt | Icons.Default.Business | 24.dp |
| ExpeditionInfoCard.kt | Icons.Default.Person | 24.dp |
| SharedDetailScaffold.kt | CustomIcons.RocketLaunch | 72.dp |

---

## Verification

### Build Check
```bash
./gradlew compileKotlinDesktop --quiet
```

### Unit Test
```kotlin
// Test MemoryUtil on Android
@Test
fun `isLowRamDevice returns expected value`() {
    // Mock ActivityManager
    val result = isLowRamDevice()
    // Assert based on test device RAM
}
```

### Manual Testing

1. **Low-RAM Device Test** (3-4GB device):
   - Install debug APK
   - Open app, navigate to home
   - Press home, wait 10 seconds
   - Reopen app from recents
   - Observe: Images should reload smoothly without spinner animation

2. **Normal Device Test** (>4GB device):
   - Same steps as above
   - Observe: Faster image cache hits, minimal reloading

3. **Profiler Verification**:
   - Android Studio Profiler → Memory
   - Verify Coil cache size matches expected:
     - Low-RAM: ~50MB
     - Normal: ~100MB

---

## Troubleshooting

### Issue: "Unresolved reference: isLowRamDevice"
**Cause**: Missing expect/actual declarations
**Fix**: Ensure all three platform implementations exist (android, desktop, ios)

### Issue: Images not loading
**Cause**: ImageLoader not registered with Coil
**Fix**: Verify `setSingletonImageLoaderFactory` is called in MainActivity.onCreate

### Issue: Shimmer not animating
**Cause**: Missing shimmer dependency or import
**Fix**: Verify `com.valentinilk.shimmer:compose-shimmer` in build.gradle.kts

### Issue: Build error in desktopMain
**Cause**: Android Context used in commonMain ImageLoaderModule
**Fix**: Use expect/actual for ImageLoader factory too, or conditionally provide
