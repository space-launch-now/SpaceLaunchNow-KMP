# Data Model: Fix Slow Warm Start on Low-RAM Android Devices

**Feature**: 009-fix-slow-warm-start-low-ram | **Date**: 2026-03-24

## Overview

This feature is a **performance optimization** that does not introduce new data models. The changes affect:
- Configuration of existing Coil ImageLoader
- Initialization timing of existing components
- UI patterns for image loading states

## Configuration Model

### ImageLoader Configuration

Not a traditional data model, but a configuration pattern:

```kotlin
/**
 * Coil ImageLoader configuration optimized for device memory class.
 * 
 * @property memoryCachePercent Percentage of heap for memory cache (0.10 - 0.25)
 * @property diskCacheSizeBytes Maximum disk cache size in bytes
 * @property crossfadeEnabled Whether to animate image transitions
 */
data class ImageLoaderConfig(
    val memoryCachePercent: Double,
    val diskCacheSizeBytes: Long,
    val crossfadeEnabled: Boolean = true
)

// Predefined configurations
object ImageLoaderConfigs {
    val LOW_RAM = ImageLoaderConfig(
        memoryCachePercent = 0.10,  // 10% of heap
        diskCacheSizeBytes = 50L * 1024 * 1024  // 50MB
    )
    
    val NORMAL = ImageLoaderConfig(
        memoryCachePercent = 0.20,  // 20% of heap
        diskCacheSizeBytes = 100L * 1024 * 1024  // 100MB
    )
}
```

## State Changes

### Existing Models Unchanged

The following existing models remain unchanged:
- `LaunchNormal`, `LaunchBasic`, `LaunchDetailed` - launch data
- `Event`, `EventNormal` - event data
- `AgencyNormal`, `AgencyDetailed` - agency data
- `SubscriptionState` - billing state

### No New Database Entities

No SQLite or DataStore schema changes required.

## UI State Patterns

### Image Loading State

The loading state presentation changes from animated to static:

**Before (CircularProgressIndicator)**:
```kotlin
// Triggers ~60 recompositions/second per indicator
loading = {
    CircularProgressIndicator(modifier = Modifier.size(24.dp))
}
```

**After (Shimmer + Icon)**:
```kotlin
// Single shared animation, no recomposition per frame
loading = {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .shimmer()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = appropriateIcon,
            contentDescription = null,
            modifier = Modifier.size(appropriateSize),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
    }
}
```

## Validation Rules

| Configuration | Constraint | Rationale |
|---------------|------------|-----------|
| memoryCachePercent | 0.05 - 0.30 | Below 5% causes excessive reloads; above 30% risks OOM |
| diskCacheSizeBytes | 10MB - 500MB | Disk space is less constrained than memory |
| totalMem threshold | ≤4GB | Devices with >4GB RAM don't need optimization |

## Related Models (Unchanged)

- `DebugPreferences` - contains Datadog sample rate (read timing changes only)
- `LoggingPreferences` - contains console severity (read timing changes only)
- `BillingManager` - initialization unchanged, sync timing moves to onResume
