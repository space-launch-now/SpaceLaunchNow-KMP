# Widget Theming - Complete Implementation Guide

## Overview

Widget appearance customization is a **premium-only feature** that allows users to personalize their home screen widgets with custom theme sources, background transparency, and corner radius settings. This document covers the complete implementation across Android and iOS platforms.

## Features

### ✅ Implemented
- **Theme Source Selection** (Android only) - Follow App Theme, Follow System, or Dynamic Colors (Material You)
- **Background Transparency** - Adjustable alpha (0-100%)
- **Corner Radius** - Adjustable corners (0-48dp for Android, 0-40dp for iOS)
- **Manual Apply System** - User-controlled widget updates
- **Premium Access Check** - DataStore-cached subscription status
- **Cross-Platform Support** - Platform-specific implementations for Android and iOS

---

## Architecture

### Data Storage (WidgetPreferences)

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/preferences/WidgetPreferences.kt`

Stores widget appearance settings in DataStore:

```kotlin
// Theme source (Android only)
- WIDGET_THEME_SOURCE: "FOLLOW_APP_THEME" | "FOLLOW_SYSTEM" | "DYNAMIC_COLORS"

// Appearance settings (all platforms)
- WIDGET_BACKGROUND_ALPHA: 0.0 (transparent) to 1.0 (opaque) - Default: 0.95
- WIDGET_CORNER_RADIUS: 0dp to 48dp - Default: 16dp

// Premium access (cached)
- WIDGET_ACCESS_GRANTED: Timestamp when access was granted
```

**Key Methods:**
- `updateWidgetThemeSource(WidgetThemeSource)`
- `updateWidgetBackgroundAlpha(Float)`
- `updateWidgetCornerRadius(Int)`
- `updateWidgetAccessGranted(Boolean)`
- `resetToDefaults()`

### Premium Access

**Problem**: Widgets run in a separate process and cannot access RevenueCat SDK directly.

**Solution**: Cache subscription status in DataStore when checked in the main app.

**Implementation** (`SubscriptionRepositoryImpl.kt`):
```kotlin
override suspend fun hasFeature(feature: PremiumFeature): Boolean {
    val hasPremiumEntitlement = revenueCatManager.hasEntitlement(...)
    
    // Cache widget access status for widget process
    if (feature == PremiumFeature.ADVANCED_WIDGETS) {
        widgetPreferences.updateWidgetAccessGranted(hasPremiumEntitlement)
        
        // Auto-update widgets when access changes
        if (hasPremiumEntitlement) {
            updateWidgetsAfterAccessChange("access granted")
        }
    }
    
    return hasPremiumEntitlement
}
```

---

## Android Implementation

### Theme Source Modes

#### 1. FOLLOW_APP_THEME (Default)
- Uses app's custom color scheme
- Respects **app's theme preference** (not system theme)
- If app is set to Dark mode, widgets are dark regardless of system theme

#### 2. FOLLOW_SYSTEM
- Uses Material 3 baseline colors
- Adapts to **system light/dark setting**
- Standard Material Design look

#### 3. DYNAMIC_COLORS (Material You)
- Extracts colors from wallpaper
- Adapts to system light/dark setting
- Requires Android 12+ (API 31+)
- Falls back to FOLLOW_APP_THEME on older devices

### ColorProviders Implementation

**File**: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/ui/theme/WidgetColorScheme.kt`

Material 3 color schemes for widgets with alpha support:

```kotlin
// Creates ColorProviders with transparency baked in
fun getColorProvidersWithAlpha(
    themeSource: WidgetThemeSource,
    backgroundAlpha: Float,
    customColorScheme: ColorScheme? = null
): ColorProviders {
    // Applies alpha at ColorScheme level, not at usage site
    // Ensures proper Material 3 semantic color integration
}
```

### Widget Update Flow

```
User adjusts slider in settings
    ↓
ViewModel updates StateFlow (e.g., widgetBackgroundAlpha)
    ↓
checkForUnappliedChanges() compares current vs last applied
    ↓
hasUnappliedWidgetChanges = true
    ↓
"Apply to Widgets" button becomes enabled
    ↓
User taps "Apply to Widgets"
    ↓
applyWidgetChanges() called
    ↓
Sequential DataStore writes (with logging):
  1. updateWidgetThemeSource()
  2. updateWidgetBackgroundAlpha()
  3. updateWidgetCornerRadius()
    ↓
Update tracking: lastApplied values = current values
    ↓
widgetApplyTrigger counter incremented (e.g., 1 → 2)
    ↓
LaunchedEffect detects trigger change
    ↓
delay(500ms) - Ensures cross-process DataStore sync
    ↓
WidgetUpdater.updateAllWidgets()
    ↓
Widgets re-render with new settings
```

### Manual Apply System

**Why Manual?**
- Prevents spam updates while dragging sliders
- Widget updates are expensive (battery/performance)
- Gives users control over when widgets refresh
- Clear visual feedback when changes are pending

**UI States:**

**No Changes (Applied):**
```
┌───────────────────────────────────────┐
│ Widget Appearance Changes             │
│                                       │
│ Your widgets are up to date.          │
│                                       │
│ [ Already Applied ] (disabled/gray)   │
└───────────────────────────────────────┘
```

**Changes Pending:**
```
┌───────────────────────────────────────┐
│ Widget Appearance Changes             │
│                                       │
│ You have unapplied widget changes.    │
│ Tap below to update your widgets.     │
│                                       │
│ [ Apply to Widgets ] (enabled/blue)   │
└───────────────────────────────────────┘
```

### Platform Restrictions

#### 1. Widgets Must Be On Home Screen
- Updates are silent no-ops if no widgets exist
- Added check in `WidgetUpdater` to warn if no widgets found

#### 2. Main Thread Requirement
- Glance widget updates MUST run on Main/UI thread
- Using `withContext(Dispatchers.Main)` in update calls

#### 3. DataStore Race Conditions
- Multiple settings written sequentially
- 500ms delay after writes for cross-process sync
- Comprehensive logging tracks write completion

---

## iOS Implementation

### Features Supported

✅ **Background Transparency** (0-100%)
✅ **Corner Radius** (0-40dp in 4dp steps)
❌ **Theme Source** - Intentionally skipped (Material 3 doesn't exist on iOS)

### Data Flow

```
User adjusts slider in app
    ↓
WidgetPreferences.widgetBackgroundAlphaFlow (Kotlin)
    ↓
KoinHelper.getWidgetBackgroundAlpha() (Kotlin → Swift bridge)
    ↓
LaunchProvider.fetchLaunches() (Swift)
    ↓
LaunchEntry.backgroundAlpha & .cornerRadius (Swift)
    ↓
NextUpWidgetView / LaunchListWidgetView (SwiftUI)
    ↓
Applied via ZStack + .opacity() + .cornerRadius()
```

### SwiftUI Implementation

**Files Modified:**
- `iosApp/LaunchWidget/LaunchData.swift` - Added appearance properties to `LaunchEntry`
- `iosApp/LaunchWidget/NextUpWidget.swift` - Applied background layer with opacity
- `iosApp/LaunchWidget/LaunchListWidget.swift` - Same pattern as NextUpWidget

**Background Layer Pattern:**
```swift
var body: some View {
    ZStack {
        // Background layer with user-configured opacity
        Color(white: 0.15)
            .opacity(entry.backgroundAlpha)
        
        // Content layer
        VStack {
            // Widget content here
        }
    }
    .cornerRadius(entry.cornerRadius)
}
```

**Why ZStack?**
- iOS 17+ `.containerBackground()` has limitations with opacity
- ZStack gives precise control over layering
- Works consistently across all widget sizes

### Kotlin-Swift Bridge

**File**: `composeApp/src/iosMain/kotlin/.../KoinInitializer.kt`

```kotlin
suspend fun getWidgetBackgroundAlpha(): Float {
    return widgetPreferences.widgetBackgroundAlphaFlow.first()
}

suspend fun getWidgetCornerRadius(): Float {
    return widgetPreferences.widgetCornerRadiusFlow.first().toFloat()
}
```

---

## UI Implementation

### Theme Customization Screen

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/ThemeCustomizationScreen.kt`

**Layout:**

```
┌─────────────────────────────────────────────┐
│ Theme Customization                  [←]    │
├─────────────────────────────────────────────┤
│                                             │
│ Widget Appearance (Premium)                │
│ Customize the look of your home screen      │
│ widgets                                     │
│                                             │
│ Widget Theme (Android only)                 │
│ Choose where widget colors come from        │
│ ┌─────────────────────────────────────────┐│
│ │ 🎨  Follow App Theme              [✓]  ││
│ │     Use your custom app theme colors    ││
│ └─────────────────────────────────────────┘│
│ ┌─────────────────────────────────────────┐│
│ │ ⚙️  Follow System Theme                ││
│ │     Auto light/dark based on system     ││
│ └─────────────────────────────────────────┘│
│ ┌─────────────────────────────────────────┐│
│ │ 🌈  Dynamic Colors                      ││
│ │     Match wallpaper (Android 12+)       ││
│ └─────────────────────────────────────────┘│
│                                             │
│ Background Transparency              95%   │
│ 0% = Fully transparent, 100% = Opaque      │
│ [────────────────────●──────]              │
│                                             │
│ Corner Radius                          16dp│
│ Rounded corners (0-48dp)                   │
│ [───────────●───────────────]              │
│                                             │
│ ┌─────────────────────────────────────────┐│
│ │ Widget Appearance Changes               ││
│ │                                         ││
│ │ You have unapplied widget changes.      ││
│ │                                         ││
│ │ [    Apply to Widgets    ]              ││
│ └─────────────────────────────────────────┘│
│                                             │
│ [     Reset to Defaults     ]              │
│                                             │
└─────────────────────────────────────────────┘
```

### Platform-Specific UI

```kotlin
// Show theme source only on Android
if (getPlatform().name == "Android") {
    item { WidgetThemeSourceSelector(...) }
}

// Background alpha and corner radius on all platforms
item { BackgroundTransparencySlider(...) }
item { CornerRadiusSlider(...) }
```

---

## Troubleshooting

### Issue: Widgets Not Updating After Applying

**Symptoms:**
- User taps "Apply to Widgets"
- Logs show successful writes
- Widgets don't change

**Root Cause:**
- DataStore writes are async
- Widget process reads before writes complete

**Solution:**
1. Ensure sequential DataStore writes with logging
2. Use counter-based trigger (not timestamp)
3. Add 500ms delay for cross-process sync
4. Check widgets are actually on home screen

### Issue: "Apply" Only Works Once Per App Launch

**Root Cause:**
- Timestamp-based trigger can produce duplicate values
- StateFlow conflation skips duplicate values

**Solution:**
- Changed from timestamp to counter: `_widgetApplyTrigger = MutableStateFlow(0)`
- Increment counter on each apply: `_widgetApplyTrigger.value++`
- Counter always produces new value, guaranteed to trigger LaunchedEffect

### Issue: Widgets Show Old Values

**Root Cause:**
- Widget process reads from DataStore before app process writes complete
- 300ms delay was insufficient for multiple writes

**Solution:**
- Increased delay from 300ms → 500ms
- Added comprehensive logging to track write completion
- Trigger only fires after ALL writes confirmed complete

---

## Testing Checklist

### Android
- [ ] Install widget on home screen
- [ ] Change transparency slider
- [ ] "Apply to Widgets" button appears and is enabled
- [ ] Tap "Apply to Widgets"
- [ ] Widget updates within 1 second
- [ ] Button returns to disabled state
- [ ] Change theme source (Follow App/System/Dynamic)
- [ ] Apply changes
- [ ] Widget colors change appropriately
- [ ] Test on Android 12+ for Dynamic Colors
- [ ] Test on Android 11- for Dynamic Colors fallback

### iOS
- [ ] Install widget on home screen
- [ ] Change transparency slider
- [ ] "Apply to Widgets" button appears (no theme source picker)
- [ ] Tap "Apply to Widgets"
- [ ] Widget updates with new transparency
- [ ] Change corner radius
- [ ] Apply changes
- [ ] Widget corners update

### Premium Access
- [ ] Non-premium user sees locked widget content
- [ ] Premium user sees actual launch data
- [ ] Purchase premium in-app
- [ ] Widgets auto-update to show content
- [ ] Restart app
- [ ] Widgets still show premium content (DataStore cache working)

---

## Key Files

### Common (Kotlin Multiplatform)
- `data/preferences/WidgetPreferences.kt` - Settings storage
- `ui/settings/ThemeCustomizationScreen.kt` - Main UI
- `ui/settings/ThemeCustomizationViewModel.kt` - Business logic
- `data/repository/SubscriptionRepositoryImpl.kt` - Premium access caching

### Android
- `ui/theme/WidgetColorScheme.kt` - Material 3 color schemes
- `widgets/WidgetUpdater.kt` - Update utility
- `ui/settings/AndroidWidgetUpdateSideEffect.kt` - Update trigger
- `widgets/NextUpWidget.kt` - Next launch widget
- `widgets/LaunchListWidget.kt` - Launch list widget
- `ui/theme/WidgetTheme.kt` - Widget appearance loading

### iOS
- `iosApp/LaunchWidget/LaunchData.swift` - Widget data model
- `iosApp/LaunchWidget/NextUpWidget.swift` - Next launch widget
- `iosApp/LaunchWidget/LaunchListWidget.swift` - Launch list widget
- `composeApp/src/iosMain/kotlin/.../KoinInitializer.kt` - Kotlin-Swift bridge

---

## Commit Message Format

When committing widget theming changes, use conventional commits:

```bash
# Features
feat(widgets): add background transparency control
feat(widgets): implement dynamic colors support
feat(widgets): add iOS widget theming support

# Fixes
fix(widgets): resolve apply button race condition
fix(widgets): prevent duplicate updates on rapid clicks
fix(widgets): ensure DataStore writes complete before update

# Chores
chore(widgets): consolidate widget theming documentation
chore(widgets): add comprehensive logging for debugging
```

---

## Future Enhancements

### Potential Features
- [ ] Live preview in settings (show widget appearance before applying)
- [ ] Multiple widget themes (save/load presets)
- [ ] Per-widget customization (different settings for each widget)
- [ ] Auto-update on wallpaper change (Android 12+ Material You)
- [ ] Custom color picker (beyond Material 3 palette)
- [ ] iOS light/dark mode forcing (separate from transparency)

### Known Limitations
- iOS doesn't support Material 3 dynamic colors
- Widget updates require 500ms delay (platform limitation)
- Separate process prevents direct RevenueCat access
- Widgets must be on home screen to update (Android platform restriction)

---

## References

- [Material 3 Design System](https://m3.material.io/)
- [Jetpack Glance Documentation](https://developer.android.com/jetpack/compose/glance)
- [WidgetKit (iOS)](https://developer.android.com/reference/androidx/glance/appwidget/package-summary)
- [DataStore Documentation](https://developer.android.com/topic/libraries/architecture/datastore)
