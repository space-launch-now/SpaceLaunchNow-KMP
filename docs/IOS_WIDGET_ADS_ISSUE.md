# iOS Widget Extension - Ads Dependency Issue

## Problem

The LaunchWidgetExtension target fails to compile with errors:
```
Depends on Google Mobile Ads library for Android and iOS
```

This occurs in files like:
- `AdInitializer.ios.kt`
- Other ad-related files in `iosMain/ui/ads/`

## Root Cause

The widget extension target is trying to compile/link against the ComposeApp framework which includes BasicAds (Google Mobile Ads) dependencies. However:

1. **Widget extensions run in a separate process** and have different entitlements
2. **Widget extensions shouldn't include heavy dependencies** like ad SDKs
3. **The ComposeApp framework includes all iosMain code**, including ads

## Solutions

### Option 1: Exclude ComposeApp Framework from Widget (Recommended)

Widgets typically don't need the full app framework. They should only access shared data models.

**Steps:**
1. Open `iosApp.xcodeproj` in Xcode
2. Select the `LaunchWidgetExtension` target
3. Go to **Build Phases** → **Link Binary With Libraries**
4. Remove `ComposeApp.framework` if it's linked
5. Only link necessary frameworks (WidgetKit, SwiftUI, etc.)

**Note:** If the widget needs shared data models, create a separate lightweight shared framework without UI/ads code.

### Option 2: Create a Separate Framework for Widgets

Create a new KMP framework target specifically for widgets:

1. In `build.gradle.kts`, add a new iOS framework target:
```kotlin
listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64()
).forEach { iosTarget ->
    // Main app framework
    iosTarget.binaries.framework {
        baseName = "ComposeApp"
        isStatic = true
    }
    
    // Widget framework (no ads, no UI)
    iosTarget.binaries.framework("widget") {
        baseName = "WidgetShared"
        isStatic = true
        // Export only data models, no ads
        export(projects.shared.data)
    }
}
```

2. Link `WidgetShared.framework` to the widget extension instead

### Option 3: Make Ads Optional (Workaround)

If you must keep the current structure, you could make ads code conditional, but this is not recommended:

1. Add a compiler flag to detect widget builds
2. Wrap all ads code in conditional compilation
3. Provide no-op implementations for widgets

This approach is complex and error-prone.

## Recommended Approach

**Use Option 1** - widgets should be lightweight and shouldn't need the full app framework. They should only:
- Read launch data from shared storage (UserDefaults/AppGroup)
- Display static UI
- Open the app when tapped

The widget should NOT:
- Show ads (not allowed in widgets anyway)
- Make network requests (widgets have limited background time)
- Use heavy frameworks

## Implementation

1. **Remove ComposeApp framework** from LaunchWidgetExtension target in Xcode
2. **Create shared data models** in a separate module if needed
3. **Use App Groups** to share data between app and widget:
   ```swift
   let sharedDefaults = UserDefaults(suiteName: "group.me.calebjones.spacelaunchnow")
   ```
4. **Widget reads cached data**, doesn't need KMP framework at all

## References

- [Apple: Creating a Widget Extension](https://developer.apple.com/documentation/widgetkit/creating-a-widget-extension)
- [Apple: Sharing Data Between App and Widget](https://developer.apple.com/documentation/widgetkit/making-a-configurable-widget)
