# Contracts: Fix Slow Warm Start on Low-RAM Android Devices

This feature is a **performance optimization** that does not introduce new API contracts or interfaces.

## No New APIs

This feature modifies:
- Internal Coil ImageLoader configuration
- Application initialization timing
- UI loading state presentation

## Internal Interfaces

### MemoryUtil (expect/actual)

```kotlin
// commonMain
expect fun isLowRamDevice(): Boolean

// Returns:
// - Android: true if ActivityManager.isLowRamDevice() or totalMem ≤ 4GB
// - Desktop: false (always sufficient memory)
// - iOS: false (iOS manages memory differently)
```

### ImageLoader Configuration

Not a contract, but a Koin module providing:

```kotlin
single<ImageLoader> {
    // Returns Coil ImageLoader with device-appropriate cache sizes
    // - Low-RAM: 10% memory, 50MB disk
    // - Normal: 20% memory, 100MB disk
}
```

## Existing Contracts Unchanged

- LaunchLibrary API 2.4.0 - no changes
- RevenueCat Billing - no changes  
- Firebase Performance - no changes (only measurement)
