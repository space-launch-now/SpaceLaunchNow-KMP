# iOS Firebase Cloud Messaging (FCM) Setup Checklist

This document outlines the complete setup needed for Firebase Cloud Messaging on iOS for Space Launch Now KMP.

## Status: 🟡 In Progress

✅ **Completed:**
- GoogleService-Info.plist added to repository
- CI/CD workflow configured to decode and install GoogleService-Info.plist

⚠️ **TODO:**
- Add Firebase SDK via Swift Package Manager
- Configure APNs in Firebase Console
- Add Push Notification capability to Xcode project
- Implement Swift FCM bridge
- Update iOSApp.swift to initialize Firebase
- Implement Kotlin/Native interop for iOS push messaging

---

## 1. Add Firebase SDK Dependencies (Swift Package Manager)

### Option A: Via Xcode (Recommended for local development)
1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Select your project in the navigator
3. Select the "iosApp" target
4. Go to "Package Dependencies" tab
5. Click "+" to add a package
6. Enter URL: `https://github.com/firebase/firebase-ios-sdk`
7. Select version: **11.8.0** (or latest)
8. Add these libraries:
   - ✅ **FirebaseMessaging** (required for FCM)
   - ✅ **FirebaseCore** (automatically included)
   - ⚠️ Optional: **FirebaseAnalytics** (if you want analytics)

### Option B: Via project.pbxproj (For CI/CD)
Add to your Xcode project file under `XCRemoteSwiftPackageReference`:

```xml
/* Begin XCRemoteSwiftPackageReference section */
    Firebase = {
        isa = XCRemoteSwiftPackageReference;
        repositoryURL = "https://github.com/firebase/firebase-ios-sdk";
        requirement = {
            kind = upToNextMajorVersion;
            minimumVersion = 11.8.0;
        };
    };
/* End XCRemoteSwiftPackageReference section */
```

---

## 2. Configure APNs (Apple Push Notification Service)

### A. Generate APNs Authentication Key (Recommended)
1. Go to [Apple Developer Portal](https://developer.apple.com/account/resources/authkeys)
2. Click "+" to create a new key
3. Name it: "Space Launch Now APNs"
4. Enable: ✅ **Apple Push Notifications service (APNs)**
5. Download the `.p8` file (you can only download once!)
6. Note the **Key ID** (e.g., `ABC123XYZ`)

### B. Upload to Firebase Console
1. Open [Firebase Console](https://console.firebase.google.com/)
2. Select your project → ⚙️ Settings → Cloud Messaging
3. Scroll to "Apple app configuration"
4. Click "Upload" under "APNs Authentication Key"
5. Upload your `.p8` file
6. Enter **Key ID** and **Team ID** (from Apple Developer account)
7. Click "Upload"

---

## 3. Add Push Notification Capability to Xcode

### Via Xcode:
1. Open `iosApp/iosApp.xcodeproj`
2. Select the "iosApp" target
3. Go to "Signing & Capabilities" tab
4. Click "+ Capability"
5. Add: **Push Notifications**
6. Add: **Background Modes** (optional, for background notifications)
   - If added, enable: ✅ Remote notifications

### Verify:
- Your `iosApp.entitlements` file should now include:
```xml
<key>aps-environment</key>
<string>development</string>
```

---

## 4. Update Info.plist

Add Firebase required keys to `iosApp/iosApp/Info.plist`:

```xml
<!-- Firebase notification permissions -->
<key>UIBackgroundModes</key>
<array>
    <string>remote-notification</string>
</array>

<!-- Optional: For displaying notifications while app is in foreground -->
<key>FirebaseAppDelegateProxyEnabled</key>
<false/>
```

---

## 5. Create FCM Bridge Swift Files

### A. Create `FCMBridge.swift`

Create `iosApp/iosApp/FCMBridge.swift`:

```swift
import Foundation
import FirebaseMessaging

@objc public class FCMBridge: NSObject {
    
    @objc public static let shared = FCMBridge()
    
    private override init() {
        super.init()
    }
    
    @objc public func subscribeToTopic(
        topic: String,
        completion: @escaping (Error?) -> Void
    ) {
        Messaging.messaging().subscribe(toTopic: topic) { error in
            completion(error)
        }
    }
    
    @objc public func unsubscribeFromTopic(
        topic: String,
        completion: @escaping (Error?) -> Void
    ) {
        Messaging.messaging().unsubscribe(fromTopic: topic) { error in
            completion(error)
        }
    }
    
    @objc public func getToken(
        completion: @escaping (String?, Error?) -> Void
    ) {
        Messaging.messaging().token { token, error in
            completion(token, error)
        }
    }
}
```

### B. Create `AppDelegate.swift`

Create `iosApp/iosApp/AppDelegate.swift`:

```swift
import UIKit
import FirebaseCore
import FirebaseMessaging
import UserNotifications

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil
    ) -> Bool {
        
        // Initialize Firebase
        FirebaseApp.configure()
        
        // Set delegates
        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self
        
        // Request notification permissions
        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .badge, .sound]
        ) { granted, error in
            print("Notification permission granted: \(granted)")
            if let error = error {
                print("Error requesting notifications: \(error.localizedDescription)")
            }
        }
        
        application.registerForRemoteNotifications()
        
        return true
    }
    
    // MARK: - APNs Token
    
    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        print("APNs device token: \(deviceToken)")
        Messaging.messaging().apnsToken = deviceToken
    }
    
    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        print("Failed to register for remote notifications: \(error.localizedDescription)")
    }
    
    // MARK: - FCM Token
    
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        print("FCM token: \(fcmToken ?? "nil")")
        
        // Send token to your backend if needed
        let dataDict: [String: String] = ["token": fcmToken ?? ""]
        NotificationCenter.default.post(
            name: Notification.Name("FCMToken"),
            object: nil,
            userInfo: dataDict
        )
    }
    
    // MARK: - Foreground Notifications
    
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        // Show notification even when app is in foreground
        completionHandler([.banner, .badge, .sound])
    }
    
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo
        print("User tapped notification: \(userInfo)")
        
        // Handle notification tap here
        
        completionHandler()
    }
}
```

---

## 6. Update iOSApp.swift

Update `iosApp/iosApp/iOSApp.swift` to use the AppDelegate:

```swift
import SwiftUI

@main
struct iOSApp: App {
    // Register AppDelegate
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

---

## 7. Implement Kotlin/Native Interop (iosMain)

### A. Create iOS Push Messaging Implementation

Create `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/push/PushMessaging.kt`:

```kotlin
package me.calebjones.spacelaunchnow.push

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// External interface to Swift FCMBridge
@OptIn(ExperimentalForeignApi::class)
@ObjCInterface
external class FCMBridge : NSObject {
    @ObjCMethod
    fun subscribeToTopic(topic: String, completion: (NSError?) -> Unit)
    
    @ObjCMethod
    fun unsubscribeFromTopic(topic: String, completion: (NSError?) -> Unit)
    
    @ObjCMethod
    fun getToken(completion: (String?, NSError?) -> Unit)
    
    companion object {
        @ObjCMethod
        fun shared(): FCMBridge
    }
}

actual class PushMessaging {
    
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun subscribeToTopic(topic: String): Result<Unit> = suspendCoroutine { continuation ->
        dispatch_async(dispatch_get_main_queue()) {
            FCMBridge.shared().subscribeToTopic(topic) { error ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                } else {
                    continuation.resume(Result.success(Unit))
                }
            }
        }
    }
    
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun unsubscribeFromTopic(topic: String): Result<Unit> = suspendCoroutine { continuation ->
        dispatch_async(dispatch_get_main_queue()) {
            FCMBridge.shared().unsubscribeFromTopic(topic) { error ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                } else {
                    continuation.resume(Result.success(Unit))
                }
            }
        }
    }
    
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getToken(): Result<String> = suspendCoroutine { continuation ->
        dispatch_async(dispatch_get_main_queue()) {
            FCMBridge.shared().getToken { token, error ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                } else if (token != null) {
                    continuation.resume(Result.success(token))
                } else {
                    continuation.resume(Result.failure(Exception("Token is null")))
                }
            }
        }
    }
}
```

---

## 8. Add GoogleService-Info.plist to Xcode

The file is already in your repository and CI/CD workflow, but you need to **add it to Xcode project**:

1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Right-click on "iosApp" folder in navigator
3. Select "Add Files to 'iosApp'..."
4. Navigate to `iosApp/iosApp/GoogleService-Info.plist`
5. ✅ Ensure "Copy items if needed" is **unchecked** (file is already there)
6. ✅ Ensure "Add to targets: iosApp" is **checked**
7. Click "Add"

Verify: The file should now appear in your Xcode project navigator with the iosApp target checked.

---

## 9. Update CI/CD Secrets

You already have `FIREBASE_GOOGLE_SERVICE_INFO_PLIST` secret added ✅

Verify in GitHub:
- Repository → Settings → Secrets and variables → Actions
- Should see: `FIREBASE_GOOGLE_SERVICE_INFO_PLIST` (base64 encoded)

---

## 10. Test on Physical Device

**⚠️ iOS Push Notifications REQUIRE a physical device** (Simulator doesn't support APNs)

### Local Testing:
1. Connect your iPhone via USB
2. Build and run from Xcode
3. Grant notification permissions when prompted
4. Check Xcode console for:
   - `APNs device token: <data>`
   - `FCM token: <string>`

### Send Test Notification:
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project → Engage → Messaging
3. Click "Send your first message"
4. Enter notification title and text
5. Click "Send test message"
6. Enter your FCM token (from Xcode console logs)
7. Click "Test"
8. You should receive a notification on your device! 🎉

---

## 11. Topic Subscription Testing

In your app (iOS):
```kotlin
// Subscribe to a topic
val pushMessaging = PushMessaging()
val result = pushMessaging.subscribeToTopic("launches_all")

if (result.isSuccess) {
    println("✅ Subscribed to topic: launches_all")
} else {
    println("❌ Failed to subscribe: ${result.exceptionOrNull()}")
}
```

Send to topic from Firebase Console:
1. Messaging → New campaign → Firebase Notification
2. Enter title and text
3. Click "Next"
4. Under "Target" → Select "Topic"
5. Enter topic name: `launches_all`
6. Click "Review" → "Publish"
7. All subscribed devices receive notification! 🚀

---

## Troubleshooting

### No APNs Token
- ✅ Check you're testing on a physical device (not simulator)
- ✅ Verify "Push Notifications" capability is enabled in Xcode
- ✅ Check device is signed in to iCloud
- ✅ Check Console.app logs for APNs errors

### No FCM Token
- ✅ Verify `FirebaseApp.configure()` is called in AppDelegate
- ✅ Check `GoogleService-Info.plist` is added to Xcode project
- ✅ Verify APNs key is uploaded to Firebase Console
- ✅ Check Xcode logs for Firebase initialization errors

### Notifications Not Received
- ✅ Verify notification permissions granted
- ✅ Check FCM token is valid (from logs)
- ✅ Verify APNs certificate/key matches your app bundle ID
- ✅ Check Firebase Console message status (delivered/failed)
- ✅ Ensure app is in background (foreground requires `willPresent` implementation)

### CI/CD Build Fails
- ✅ Verify `FIREBASE_GOOGLE_SERVICE_INFO_PLIST` secret is set
- ✅ Check workflow step "Decode GoogleService-Info.plist" succeeds
- ✅ Verify file is created at `iosApp/iosApp/GoogleService-Info.plist`

---

## Next Steps

1. ✅ Complete Swift files (FCMBridge.swift, AppDelegate.swift)
2. ✅ Update iOSApp.swift to use AppDelegate
3. ✅ Add Firebase SPM package to Xcode project
4. ✅ Add GoogleService-Info.plist to Xcode project references
5. ✅ Configure APNs in Firebase Console
6. ✅ Add Push Notification capability in Xcode
7. ✅ Implement Kotlin/Native interop in iosMain
8. ✅ Test on physical device
9. ✅ Verify topic subscriptions work
10. ✅ Test end-to-end notification flow

---

## References

- [Firebase iOS Setup](https://firebase.google.com/docs/ios/setup)
- [Firebase Cloud Messaging iOS](https://firebase.google.com/docs/cloud-messaging/ios/client)
- [Apple Push Notification Service](https://developer.apple.com/documentation/usernotifications)
- [Kotlin/Native ObjC Interop](https://kotlinlang.org/docs/native-objc-interop.html)
- [Your FCM Implementation Plan](./push-messaging-fcm.md)
