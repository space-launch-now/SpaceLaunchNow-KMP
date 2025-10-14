# iOS FCM Quick Start Guide

## TL;DR - What You Need To Do

### 1. Xcode Setup (5 minutes)
```bash
# Open Xcode
open iosApp/iosApp.xcodeproj

# Then in Xcode:
# 1. Add Firebase SPM package (https://github.com/firebase/firebase-ios-sdk v11.8.0)
#    - Select: FirebaseMessaging
# 2. Add GoogleService-Info.plist to project (already in folder, just add reference)
# 3. Add "Push Notifications" capability
# 4. Add "Background Modes" capability → Enable "Remote notifications"
```

### 2. Create Swift Files (10 minutes)
Create these files in `iosApp/iosApp/`:

**FCMBridge.swift** - Exposes FCM to Kotlin
```swift
import FirebaseMessaging

@objc public class FCMBridge: NSObject {
    @objc public static let shared = FCMBridge()
    
    @objc public func subscribeToTopic(topic: String, completion: @escaping (Error?) -> Void) {
        Messaging.messaging().subscribe(toTopic: topic, completion: completion)
    }
    
    @objc public func unsubscribeFromTopic(topic: String, completion: @escaping (Error?) -> Void) {
        Messaging.messaging().unsubscribe(fromTopic: topic, completion: completion)
    }
    
    @objc public func getToken(completion: @escaping (String?, Error?) -> Void) {
        Messaging.messaging().token(completion: completion)
    }
}
```

**AppDelegate.swift** - Initializes Firebase
```swift
import UIKit
import FirebaseCore
import FirebaseMessaging
import UserNotifications

class AppDelegate: NSObject, UIApplicationDelegate, MessagingDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        FirebaseApp.configure()
        Messaging.messaging().delegate = self
        application.registerForRemoteNotifications()
        return true
    }
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }
    
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        print("FCM token: \(fcmToken ?? "")")
    }
}
```

**Update iOSApp.swift**
```swift
import SwiftUI

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate  // ADD THIS
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

### 3. Firebase Console Setup (5 minutes)
1. Generate APNs Auth Key:
   - [Apple Developer](https://developer.apple.com/account/resources/authkeys) → Create Key
   - Enable "Apple Push Notifications service (APNs)"
   - Download `.p8` file (ONLY ONE CHANCE!)
   - Note Key ID

2. Upload to Firebase:
   - [Firebase Console](https://console.firebase.google.com/) → Settings → Cloud Messaging
   - Upload `.p8` file
   - Enter Key ID and Team ID

### 4. Test (Physical Device Required!)
```bash
# Build and run on iPhone
# Check logs for:
# "FCM token: ..."

# Then in Firebase Console:
# Messaging → Send test message → Enter FCM token
```

## Files Created
- ✅ `iosApp/iosApp/FCMBridge.swift`
- ✅ `iosApp/iosApp/AppDelegate.swift`
- ✅ Updated `iosApp/iosApp/iOSApp.swift`

## Xcode Changes
- ✅ Added Firebase iOS SDK via SPM
- ✅ Added GoogleService-Info.plist reference
- ✅ Added "Push Notifications" capability
- ✅ Added "Background Modes" → "Remote notifications"

## Firebase Console
- ✅ APNs Auth Key uploaded
- ✅ iOS app configured

## Ready to Deploy!
Your CI/CD workflow already handles `GoogleService-Info.plist` decoding ✅

---

## Full Documentation
See [IOS_FCM_SETUP.md](./IOS_FCM_SETUP.md) for complete step-by-step guide with troubleshooting.
