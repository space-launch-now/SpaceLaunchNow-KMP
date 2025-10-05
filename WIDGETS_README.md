# Android Widgets Implementation

This document describes the Android widgets implementation for Space Launch Now KMP.

## Overview

Two Android home screen widgets have been implemented using Jetpack Glance:

1. **Next Up Widget** - Displays the next upcoming rocket launch with countdown
2. **Launch List Widget** - Shows a list of the next 5 upcoming rocket launches

## Files Created

### Widget Components

- `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/widgets/NextUpWidget.kt`
    - Main widget implementation for the next launch
    - Shows launch name, agency, location, countdown, and status
    - Tap to open main app

- `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/widgets/LaunchListWidget.kt`
    - List widget showing multiple upcoming launches
    - Compact view with key information for each launch
    - Scrollable list supporting up to 5 launches

### Worker

- `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/workers/WidgetUpdateWorker.kt`
    - WorkManager worker that updates widgets every 15 minutes
    - Requires network connectivity to fetch fresh data
    - Automatically scheduled on app startup

### XML Resources

- `composeApp/src/androidMain/res/xml/next_up_widget_info.xml`
    - Widget provider configuration for Next Up widget
    - Min size: 180dp x 110dp (3x2 cells)
    - Updates every 15 minutes

- `composeApp/src/androidMain/res/xml/launch_list_widget_info.xml`
    - Widget provider configuration for Launch List widget
    - Min size: 250dp x 180dp (4x3 cells)
    - Updates every 15 minutes

- `composeApp/src/androidMain/res/values/strings.xml`
    - Added widget description strings

### Manifest Updates

- `composeApp/src/androidMain/AndroidManifest.xml`
    - Registered `NextUpWidgetReceiver`
    - Registered `LaunchListWidgetReceiver`
    - Both receivers listen for `APPWIDGET_UPDATE` action

### Application Updates

- `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainApplication.kt`
    - Added widget update scheduling on app startup
    - Periodic work request configured for 15-minute intervals

## Features

### Next Up Widget

- ✅ Shows the next upcoming launch
- ✅ Real-time countdown timer
- ✅ Launch name and agency
- ✅ Launch location
- ✅ Status indicator (Go/TBD/etc.)
- ✅ Rocket emoji visual element
- ✅ Tap to open app
- ✅ Graceful handling when no launches available

### Launch List Widget

- ✅ Shows up to 5 upcoming launches
- ✅ Compact countdown for each launch
- ✅ Agency and location information
- ✅ Status abbreviations
- ✅ Scrollable list view
- ✅ Launch count in header
- ✅ Tap to open app
- ✅ Empty state handling

## Dependencies

The following dependencies were added to support widgets:

```gradle
// Glance for Android Widgets
implementation(libs.androidx.glance)
implementation(libs.androidx.glance.material3)
```

Versions in `gradle/libs.versions.toml`:

```toml
[versions]
androidx-glance = "1.1.1"

[libraries]
androidx-glance = { group = "androidx.glance", name = "glance-appwidget", version.ref = "androidx-glance" }
androidx-glance-material3 = { group = "androidx.glance", name = "glance-material3", version.ref = "androidx-glance" }
```

## Architecture

### Data Flow

1. Widget updates are triggered by:
    - System update broadcasts (every 15 minutes as configured)
    - WorkManager periodic updates (every 15 minutes)
    - Manual user refresh (long-press widget)

2. Widget fetches data:
    - Uses Koin DI to inject `LaunchRepository`
    - Fetches data on IO dispatcher
    - Handles errors gracefully with fallback UI

3. Widget renders:
    - Uses Jetpack Glance composable functions
    - Applies Material 3 theming via `GlanceTheme`
    - Adapts to system dark/light mode

### Update Strategy

- **Automatic Updates**: Every 15 minutes via WorkManager
- **Network Requirement**: Updates only run when network is available
- **Battery Optimization**: Uses `ExistingPeriodicWorkPolicy.KEEP` to avoid duplicate work
- **Error Handling**: Failed updates are retried with exponential backoff

## Usage

### Adding Widgets to Home Screen

1. Long-press on home screen
2. Tap "Widgets"
3. Find "Space Launch Now" or "SLN - KMP"
4. Choose either:
    - **Next Launch** - Compact single launch view
    - **Upcoming Launches** - List of launches

### Widget Sizes

Both widgets are resizable:

- **Next Up Widget**:
    - Minimum: 3x2 grid cells
    - Recommended: 3x2 or 4x2

- **Launch List Widget**:
    - Minimum: 4x3 grid cells
    - Recommended: 4x4 or 4x5 for optimal viewing

## Theming

Widgets automatically adapt to:

- System dark/light mode
- Material You dynamic colors (Android 12+)
- Custom app theme colors via `GlanceTheme`

## Known Limitations

1. **Update Frequency**: Android limits widget updates to 30-minute intervals via system, but
   WorkManager supplements with more frequent updates
2. **Data Freshness**: Widgets show cached data when offline
3. **Interaction**: Widgets are read-only; tapping opens the main app
4. **Size Constraints**: Very small widget sizes may truncate content

## Future Enhancements

Potential improvements:

- [ ] Widget configuration activity for customizing displayed launches
- [ ] Filter widgets by agency or location
- [ ] Add launch image/mission patch to widgets
- [ ] Interactive elements (favorite, share, etc.)
- [ ] Live countdown updates (requires AlarmManager for sub-minute updates)
- [ ] Widget themes/color customization
- [ ] Notification integration from widgets

## Testing

To test widgets:

1. **Install App**: Build and install the debug APK
2. **Add Widget**: Long-press home screen and add widget
3. **Verify Data**: Check that launch data appears
4. **Test Updates**: Wait 15 minutes or use WorkManager test tools
5. **Test Tap**: Tap widget to verify app launches
6. **Test Offline**: Toggle airplane mode to verify graceful degradation

### Manual Widget Update

To force an immediate widget update for testing:

```kotlin
// In Android Studio's Device File Explorer or via adb shell
adb shell am broadcast -a android.appwidget.action.APPWIDGET_UPDATE
```

## Troubleshooting

### Widget Not Appearing

- Check that app is installed correctly
- Verify `AndroidManifest.xml` has widget receivers registered
- Check Android logs for errors: `adb logcat | grep Widget`

### Widget Not Updating

- Verify network connectivity
- Check WorkManager status in Android Studio
- Ensure battery optimization is not restricting app
- Check logs for update errors

### Widget Shows "No launches"

- Verify API is accessible
- Check repository logs for fetch errors
- Ensure Koin DI is properly initialized

### Koin NoDefinitionFoundException

**Issue**:
`org.koin.core.error.NoDefinitionFoundException: No definition found for type 'NotificationRepository'`

**Root Causes**:

1. The Koin module definition was checking `BuildConfig.IS_DEBUG` during module initialization (at
   class load time)
2. The `nativeConfig()` was trying to set `androidContext()` before the Application instance was
   available

**Solutions Applied**:

**1. Remove BuildConfig check from module definition** (`AppModule.kt`):
```kotlin
// Before:
single<NotificationRepository> {
    if (BuildConfig.IS_DEBUG) {
        NotificationRepositoryImpl(...)
    } else {
        NotificationRepositoryImpl(...)
    }
}

// After:
single<NotificationRepository> {
    NotificationRepositoryImpl(
        pushMessaging = get(),
        notificationPreferences = get(),
        debugPreferences = getOrNull<DebugPreferences>()  // Always use getOrNull()
    )
}
```

**2. Fix Android module context initialization** (`AppModule.android.kt`):

```kotlin
// Remove duplicate androidContext/androidLogger from nativeConfig
actual fun nativeConfig(): KoinAppDeclaration = {
    // Android context is set in MainApplication.onCreate via androidContext()
    // Just return the androidModule here
    modules(androidModule)
}

// Keep androidContext() in module definitions
val androidModule = module {
    single { createDataStore(androidContext()) }  // This androidContext() is OK
    single(named("DebugDataStore")) { createDebugDataStore(androidContext()) }
    single(named("AppSettingsDataStore")) { createAppSettingsDataStore(androidContext()) }
}
```

**3. Ensure proper initialization order** (`MainApplication.kt`):

```kotlin
override fun onCreate() {
    super.onCreate()
    instance = this  // Set instance first
    
    initializeBuildConfig()  // Initialize BuildConfig
    
    startKoin {
        koinConfig()  // This calls nativeConfig()
        androidContext(this@MainApplication)  // Set context here
    }
}
```

**Why This Works**:

- `getOrNull()` returns `null` if definition doesn't exist (no crash)
- `NotificationRepositoryImpl` accepts nullable `DebugPreferences`
- Android context is set in `MainApplication` before modules that need it are initialized
- No conditional logic during module loading

**Widget-specific**: Widgets use `KoinJavaComponent.inject()` to access the global Koin instance.

## Performance Considerations

- Widgets use coroutines for async data fetching
- IO operations run on `Dispatchers.IO`
- Minimal memory footprint (~2-5 MB per widget)
- Battery-efficient update schedule
- Network requests only when necessary

## Accessibility

- Widgets use semantic content descriptions
- Text sizes follow Material Design guidelines
- High contrast mode support via `GlanceTheme`
- Screen reader compatible

---

**Last Updated**: 2025
**Jetpack Glance Version**: 1.1.1
**Minimum Android Version**: API 26 (Android 8.0)
