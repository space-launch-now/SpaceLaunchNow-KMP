# iOS Widget Premium Gating Implementation

## Overview
This document describes the implementation of premium feature gating for iOS Launch List widgets, mirroring the Android implementation.

## Implementation Date
**Created:** ${new Date().toISOString().split('T')[0]}

## Architecture

### 1. Entitlement Check (Kotlin → Swift Bridge)

**File:** `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/util/KoinInitializer.kt`

Added `hasWidgetAccess()` method to KoinHelper:

```kotlin
suspend fun hasWidgetAccess(): Boolean {
    return try {
        subscriptionRepository.hasFeature(PremiumFeature.ADVANCED_WIDGETS)
    } catch (e: Exception) {
        println("KoinHelper: Failed to check widget access: ${e.message}")
        false // Default to locked if check fails
    }
}
```

**Key Points:**
- Uses `SubscriptionRepository.hasFeature(PremiumFeature.ADVANCED_WIDGETS)`
- Returns `false` on error (secure by default)
- Exposed to Swift via Kotlin/Native bridge

### 2. Widget Data Model

**File:** `iosApp/LaunchWidget/LaunchData.swift`

Updated `LaunchEntry` struct:

```swift
struct LaunchEntry: TimelineEntry {
    let hasWidgetAccess: Bool  // NEW: Premium entitlement flag
    // ... other fields
}
```

**Key Points:**
- Carries entitlement state through widget pipeline
- Set to `false` when user lacks premium access
- Set to `true` when premium or data fetch fails with error

### 3. Widget Timeline Provider

**File:** `iosApp/LaunchWidget/LaunchData.swift`

Updated `fetchLaunches()` method:

```swift
private func fetchLaunches() async -> LaunchEntry {
    // Check widget access first
    let hasAccess = try await helper.hasWidgetAccess()
    
    if !hasAccess {
        return LaunchEntry(
            date: Date(),
            launches: [],
            hasWidgetAccess: false,  // Locked state
            isPlaceholder: false,
            errorMessage: nil
        )
    }
    
    // Only fetch launches if user has access
    // ... rest of fetch logic
}
```

**Key Points:**
- Checks entitlement BEFORE fetching data
- Returns locked entry immediately if no access
- Prevents unnecessary API calls for non-premium users
- Error states still show access (user may have network issues)

### 4. Widget UI

**File:** `iosApp/LaunchWidget/LaunchListWidget.swift`

Updated widget body logic:

```swift
var body: some View {
    if entry.isPlaceholder {
        placeholderView
    } else if !entry.hasWidgetAccess {
        lockedView  // NEW: Show paywall
    } else if let errorMessage = entry.errorMessage {
        errorView(message: errorMessage)
    } else if !entry.launches.isEmpty {
        launchListView
    } else {
        emptyView
    }
}
```

Added locked view:

```swift
private var lockedView: some View {
    VStack(spacing: 12) {
        Image(systemName: "lock.fill")
            .font(.system(size: 40))
            .foregroundStyle(.orange)
        
        Text("🚀 Premium Widget")
            .font(.headline)
            .fontWeight(.bold)
            .foregroundStyle(.primary)
        
        Text("Upgrade to Premium to unlock the Launch List widget")
            .font(.caption)
            .foregroundStyle(.secondary)
            .multilineTextAlignment(.center)
            .lineLimit(3)
        
        Divider()
            .padding(.vertical, 4)
        
        HStack(spacing: 4) {
            Image(systemName: "arrow.up.circle.fill")
                .font(.caption2)
            Text("Tap to upgrade")
                .font(.caption2)
                .fontWeight(.medium)
        }
        .foregroundStyle(.blue)
    }
    .padding()
    .widgetURL(URL(string: "spacelaunchnow://subscription"))
}
```

**Key Points:**
- Shows lock icon, title, description
- Uses `.widgetURL()` for deep linking
- Consistent with Android locked view design
- Clear call-to-action: "Tap to upgrade"

### 5. Deep Linking

**File:** `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/MainViewController.kt`

Added navigation state management:

```kotlin
// Shared state for navigation from iOS
private val navigationDestinationState = mutableStateOf<String?>(null)

// Public function for iOS to trigger navigation
fun setNavigationDestination(destination: String?) {
    navigationDestinationState.value = destination
}

fun MainViewController() = ComposeUIViewController { 
    // ... initialization
    
    val navigationDestination by navigationDestinationState
    
    SpaceLaunchNowApp(
        navigationDestination = navigationDestination,
        onNavigationDestinationConsumed = {
            navigationDestinationState.value = null
        }
    ) 
}
```

**File:** `iosApp/iosApp/iOSApp.swift`

Added URL scheme handler:

```swift
@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    handleDeepLink(url)
                }
        }
    }
    
    private func handleDeepLink(_ url: URL) {
        guard url.scheme == "spacelaunchnow" else { return }
        
        switch url.host {
        case "subscription":
            MainViewControllerKt.setNavigationDestination(destination: "subscription")
            print("Deep link: Navigating to subscription screen")
        default:
            print("Deep link: Unknown host: \(url.host ?? "nil")")
        }
    }
}
```

**File:** `iosApp/iosApp/Info.plist`

Added URL scheme registration:

```xml
<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleTypeRole</key>
        <string>Editor</string>
        <key>CFBundleURLName</key>
        <string>me.calebjones.spacelaunchnow</string>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>spacelaunchnow</string>
        </array>
    </dict>
</array>
```

**Key Points:**
- URL scheme: `spacelaunchnow://subscription`
- Deep links to SupportUs (subscription) screen
- Uses shared navigation state between Swift and Kotlin
- Handled in App.kt via `navigationDestination` parameter

## User Flow

### Premium User
1. User adds Launch List widget to home screen
2. Widget checks entitlements via `hasWidgetAccess()`
3. Returns `true` → Fetches launches from API
4. Displays launch list with countdown timers

### Non-Premium User
1. User adds Launch List widget to home screen
2. Widget checks entitlements via `hasWidgetAccess()`
3. Returns `false` → Skips API fetch
4. Displays locked view with premium paywall
5. User taps widget → Opens app
6. Deep link triggers navigation to SupportUs screen
7. User can purchase premium subscription

## Testing Checklist

- [ ] **Widget displays correctly for premium users**
  - Shows launch list
  - Countdown timers work
  - Tapping opens app to launch detail

- [ ] **Widget shows paywall for non-premium users**
  - Shows lock icon and "🚀 Premium Widget" title
  - Shows upgrade prompt
  - No API calls made

- [ ] **Deep linking works**
  - Tapping locked widget opens app
  - Navigates to SupportUs (subscription) screen
  - Navigation state clears after consumed

- [ ] **Entitlement transitions**
  - User upgrades → Widget refreshes to show launches (within 15 min)
  - User downgrades → Widget shows paywall (within 15 min)

- [ ] **Error handling**
  - Network errors show error view (not locked view)
  - Entitlement check errors default to locked

## Comparison with Android

| Aspect | Android | iOS |
|--------|---------|-----|
| **UI Framework** | Glance Composables | SwiftUI WidgetKit |
| **Entitlement Check** | `subscriptionRepository.hasFeature()` | Same via KoinHelper |
| **Locked View** | Glance Column + Text | SwiftUI VStack + Text |
| **Deep Linking** | Intent extra → MainActivity | URL scheme → iOSApp |
| **Navigation** | LaunchedEffect in App.kt | setNavigationDestination() |
| **API Prevention** | Check before provideGlance() | Check before fetchLaunches() |

## Implementation Status

✅ **Completed:**
- KoinHelper bridge for entitlement check
- LaunchEntry model updated with hasWidgetAccess
- Timeline provider checks entitlements before fetch
- Widget UI shows locked view for non-premium users
- Deep linking to subscription screen
- URL scheme registered in Info.plist

🔲 **Pending:**
- Device testing with actual premium/non-premium accounts
- Widget gallery configuration (hide for non-premium)
- Widget refresh testing after subscription change

## Related Files

### Kotlin
- `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/util/KoinInitializer.kt`
- `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/MainViewController.kt`
- `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/App.kt`
- `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/PremiumFeature.kt`

### Swift
- `iosApp/LaunchWidget/LaunchData.swift`
- `iosApp/LaunchWidget/LaunchListWidget.swift`
- `iosApp/iosApp/iOSApp.swift`
- `iosApp/iosApp/Info.plist`

### Android (Reference Implementation)
- `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/widgets/LaunchListWidget.kt`
- `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainActivity.kt`

## Notes

- Widget refresh frequency: 15 minutes (WidgetKit system policy)
- Entitlement changes may take up to 15 minutes to reflect in widget
- Deep link URL: `spacelaunchnow://subscription`
- Fallback behavior: Locked on error (secure by default)
- Android implementation serves as reference for feature parity

## Future Enhancements

- [ ] Add widget configuration to show/hide based on premium status
- [ ] Implement widget intent for instant refresh after subscription purchase
- [ ] Add analytics tracking for widget paywall taps
- [ ] Consider premium-only widgets (hide from gallery for non-premium)
