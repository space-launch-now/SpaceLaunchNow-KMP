# iOS Notification Filtering - v4 System

## Overview

iOS will use the **same filtering logic** as Android through the shared `NotificationFilter` object in common code. This ensures consistent behavior across platforms.

## Implementation Plan

### 1. iOS Notification Delegate

When iOS receives a notification, it needs to:
1. Extract the data payload from `userInfo`
2. Convert to Kotlin Map
3. Call shared `NotificationFilter` 
4. Show/suppress notification based on result

### 2. Swift Implementation (Future)

```swift
import UserNotifications
import ComposeApp

class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    
    // Get notification preferences from shared storage
    private func getNotificationSettings() -> NotificationState {
        // TODO: Access shared NotificationState from DataStore
        // For now, return default
        return NotificationState.companion.DEFAULT
    }
    
    // Will be called when notification arrives while app is in foreground
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        let userInfo = notification.request.content.userInfo
        
        // Extract data payload (v4 format)
        guard let dataDict = extractDataDictionary(from: userInfo) else {
            print("⚠️ Failed to extract data dictionary from userInfo")
            completionHandler([])
            return
        }
        
        // Get user notification settings
        let settings = getNotificationSettings()
        
        // Apply shared filtering logic
        let shouldShow = NotificationFilter.shared.shouldShowFromMap(
            dataMap: dataDict,
            state: settings
        )
        
        if shouldShow {
            // Show notification
            print("✅ Showing notification")
            completionHandler([.banner, .sound, .badge])
        } else {
            // Suppress notification
            print("🔇 Suppressing notification based on user preferences")
            completionHandler([])
        }
    }
    
    // Will be called when user taps notification
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo
        
        // Extract launch data for deep linking
        if let launchId = userInfo["launch_id"] as? String {
            print("User tapped notification for launch: \(launchId)")
            // TODO: Navigate to launch detail screen
        }
        
        completionHandler()
    }
    
    // Helper to extract data dictionary from FCM userInfo
    private func extractDataDictionary(from userInfo: [AnyHashable: Any]) -> [String: String]? {
        // FCM on iOS puts custom data directly in userInfo
        var dataDict: [String: String] = [:]
        
        let expectedKeys = [
            "notification_type",
            "launch_id",
            "launch_uuid",
            "launch_name",
            "launch_image",
            "launch_net",
            "launch_location",
            "webcast",
            "agency_id",
            "location_id"
        ]
        
        for key in expectedKeys {
            if let value = userInfo[key] as? String {
                dataDict[key] = value
            }
        }
        
        // Check if we got required fields
        guard !dataDict.isEmpty,
              dataDict["notification_type"] != nil,
              dataDict["launch_id"] != nil else {
            return nil
        }
        
        return dataDict
    }
}
```

### 3. App Delegate Setup

```swift
import UIKit
import FirebaseCore
import FirebaseMessaging
import ComposeApp

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    var window: UIWindow?
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        
        // Initialize Firebase
        FirebaseApp.configure()
        
        // Set up notification delegate
        UNUserNotificationCenter.current().delegate = NotificationDelegate()
        
        // Request notification permissions
        requestNotificationPermissions()
        
        // Register for remote notifications
        application.registerForRemoteNotifications()
        
        return true
    }
    
    private func requestNotificationPermissions() {
        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .sound, .badge]
        ) { granted, error in
            if granted {
                print("✅ Notification permissions granted")
            } else {
                print("❌ Notification permissions denied")
            }
        }
    }
    
    // Called when APNs token is available
    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        // FCM will handle this
        Messaging.messaging().apnsToken = deviceToken
    }
}
```

### 4. Topic Subscription (Already Implemented)

The `IosPushMessaging` class will handle topic subscriptions:

```kotlin
// In IosPushMessaging.kt (TODO: implement)
actual class PushMessaging actual constructor() {
    
    actual suspend fun subscribeToTopic(topic: String): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            FCMBridge.shared.subscribeToTopic(topic) { error in
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                } else {
                    continuation.resume(Result.success(Unit))
                }
            }
        }
    }
}
```

## Key Benefits of Shared Filtering

### ✅ Write Once, Use Everywhere
- Filtering logic in `NotificationFilter` (common code)
- iOS and Android both use the same code
- No platform-specific filtering bugs

### ✅ Consistent Behavior
- Users get same filtering on iOS and Android
- Same rules apply everywhere
- Same logging format

### ✅ Easy to Test
- Can test filtering logic once
- Platform-agnostic unit tests
- Mock `NotificationState` for different scenarios

### ✅ Easy to Maintain
- One place to update filtering logic
- Changes automatically apply to both platforms
- Reduced code duplication

## Testing iOS Filtering

### 1. Unit Tests (Shared)

```kotlin
// In commonTest
class NotificationFilterTest {
    
    @Test
    fun `followAll ignores strict matching`() {
        val data = NotificationData(
            notificationType = "twentyFourHour",
            launchId = "1",
            launchUuid = "uuid",
            launchName = "Test",
            launchImage = null,
            launchNet = "2025-10-13",
            launchLocation = "KSC",
            webcast = "true",
            agencyId = "121", // SpaceX
            locationId = "27"  // KSC
        )
        
        val state = NotificationState(
            enableNotifications = true,
            followAllLaunches = true,
            useStrictMatching = true, // Should be ignored
            subscribedAgencies = emptySet(), // Should be ignored
            subscribedLocations = emptySet() // Should be ignored
        )
        
        val result = NotificationFilter.shouldShowNotification(data, state)
        assertTrue(result, "followAll should bypass all filtering")
    }
    
    @Test
    fun `strict matching requires both agency and location`() {
        val data = NotificationData(
            notificationType = "twentyFourHour",
            launchId = "1",
            launchUuid = "uuid",
            launchName = "Test",
            launchImage = null,
            launchNet = "2025-10-13",
            launchLocation = "KSC",
            webcast = "true",
            agencyId = "121", // SpaceX
            locationId = "27"  // KSC
        )
        
        // Only agency subscribed, not location
        val state = NotificationState(
            enableNotifications = true,
            followAllLaunches = false,
            useStrictMatching = true,
            subscribedAgencies = setOf("121"),
            subscribedLocations = setOf("11") // Different location
        )
        
        val result = NotificationFilter.shouldShowNotification(data, state)
        assertFalse(result, "Strict matching should require BOTH agency and location")
    }
}
```

### 2. iOS Integration Test

```swift
func testNotificationFilteringFromSwift() {
    // Simulate v4 notification data
    let dataDict: [String: String] = [
        "notification_type": "twentyFourHour",
        "launch_id": "999",
        "launch_uuid": "550e8400-e29b-41d4-a716-446655440000",
        "launch_name": "Test Launch",
        "launch_image": "",
        "launch_net": "October 13, 2025 14:30:00 UTC",
        "launch_location": "Kennedy Space Center",
        "webcast": "true",
        "agency_id": "121", // SpaceX
        "location_id": "27"  // KSC
    ]
    
    // Create test state
    let state = NotificationState(
        enableNotifications: true,
        followAllLaunches: false,
        useStrictMatching: false,
        subscribedAgencies: ["121"], // SpaceX
        subscribedLocations: ["27"]  // KSC
    )
    
    // Test filtering
    let shouldShow = NotificationFilter.shared.shouldShowFromMap(
        dataMap: dataDict,
        state: state
    )
    
    XCTAssertTrue(shouldShow, "Should show SpaceX launch from KSC")
}
```

## Migration Notes

### Current State
- iOS notifications not yet fully implemented
- `IosPushMessaging` returns success but doesn't actually subscribe
- No iOS notification handling yet

### When Implementing
1. ✅ Filtering logic already ready in common code
2. ⏳ Need to implement `IosPushMessaging` with FCMBridge
3. ⏳ Need to create `NotificationDelegate` in Swift
4. ⏳ Need to wire up delegate in AppDelegate
5. ⏳ Need to access `NotificationState` from Swift

### Storage Access from Swift

The `NotificationState` is stored in DataStore. To access from Swift:

```swift
// Option 1: Create Kotlin helper
// In common code:
class NotificationStateProvider(private val storage: NotificationStateStorage) {
    suspend fun getCurrentState(): NotificationState {
        return storage.getState()
    }
}

// In Swift:
let provider = NotificationStateProvider(storage: storage)
provider.getCurrentState { state, error in
    // Use state
}
```

```swift
// Option 2: Direct DataStore access (if exposed to iOS framework)
// TBD based on DataStore iOS compatibility
```

## Summary

The v4 notification system is **iOS-ready** because:

1. ✅ **Filtering logic is shared** - `NotificationFilter` is in common code
2. ✅ **Data model is shared** - `NotificationData` parses the same format
3. ✅ **State model is shared** - `NotificationState` is platform-agnostic
4. ✅ **Simple topics** - iOS only needs to subscribe to `k_prod_v4`
5. ⏳ **Implementation needed** - Swift notification delegate and FCM bridge

When iOS notifications are implemented, they'll automatically get all the filtering capabilities with minimal platform-specific code!
