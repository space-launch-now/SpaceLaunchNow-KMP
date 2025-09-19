# Firebase Cloud Messaging (FCM) with Topic-Based Subscriptions — KMP Plan

This document outlines how to add push notifications using Firebase Cloud Messaging (FCM) with
topic-based subscriptions to this Kotlin Multiplatform (KMP) app.

Targets in this repo: Android, iOS, Desktop/JVM.

---

## Architecture Overview

- Common API (shared): Define `expect` interface `PushMessaging` with:
    - `suspend fun subscribeToTopic(topic: String): Result<Unit>`
    - `suspend fun unsubscribeFromTopic(topic: String): Result<Unit>`
    - `suspend fun getToken(): Result<String>`
    - Optional later: `val messages: Flow<PushMessage>` to stream messages/events
- Android `actual`: Use Firebase Messaging SDK (`firebase-messaging-ktx`).
- iOS `actual`: Use a small Swift bridge (SPM: FirebaseMessaging) exposing Objective‑C compatible
  APIs that Kotlin/Native can call.
- Desktop/JVM: Provide a no‑op implementation.

Topic naming: must match `[A-Za-z0-9-_.~%]`, up to 900 chars. Avoid spaces. Examples:
`launches_all`, `provider_spacex`, `region.us-east`.

---

## Android Integration

### 1) Gradle setup

- Add Google Services plugin to the Android application module (`composeApp`).
- Add Firebase BOM + Messaging dependency to `androidMain`.

Example (composeApp/build.gradle.kts):

```kotlin
plugins {
    // ... existing plugins ...
    id("com.google.gms.google-services")
}

kotlin {
    // ...
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
                implementation("com.google.firebase:firebase-messaging-ktx")
            }
        }
    }
}

android {
    // ... existing android block ...
}
```

Notes:

- Keep using BOM to manage Firebase versions.
- You can move versions to `libs.versions.toml` if desired.

### 2) Firebase config

- Download `google-services.json` from the Firebase console and place it in `composeApp/` (Android
  app module root).
- Ensure `applicationId` (`me.calebjones.spacelaunchnow.kmp`) matches the Firebase Android app
  package.

### 3) Manifest and permissions

- Add Android 13+ notification permission:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

- Register a `FirebaseMessagingService` subclass (to receive messages and token updates):

```xml
<application>
    <!-- ... -->
    <service
        android:name=".push.AppFirebaseMessagingService"
        android:exported="false">
        <intent-filter>
            <action android:name="com.google.firebase.MESSAGING_EVENT" />
        </intent-filter>
    </service>
</application>
```

### 4) Runtime permission (Android 13+)

- At first run or in a dedicated screen, request `POST_NOTIFICATIONS`. Only after granted (or if <
  13), proceed to subscribe to topics.

### 5) Implementation guidance

- `AppFirebaseMessagingService` (androidMain):
    - Override `onNewToken(token: String)` to store/report token.
    - Override `onMessageReceived` to handle notification and data messages.
- `PushMessaging` Android `actual`:
    - `FirebaseMessaging.getInstance().subscribeToTopic(topic)` / `unsubscribeFromTopic(topic)`.
    - `getToken()` via `FirebaseMessaging.getInstance().token`.
- Notifications:
    - Create a default notification channel on app start for Android 8+.
- DI (Koin):
    - Provide `PushMessaging` implementation and any `NotificationHelper` from an Android-specific
      Koin module.

---

## iOS Integration

### 1) Dependencies and capabilities

- Add Firebase via Swift Package Manager (Xcode → Project → Package Dependencies):
    - Repository: https://github.com/firebase/firebase-ios-sdk
    - Product: `FirebaseMessaging` (also brings `FirebaseCore`).
- Add `GoogleService-Info.plist` to the iOS target and ensure it’s included in the app bundle.
- Enable capabilities:
    - Push Notifications
    - Background Modes → Remote notifications
- Configure APNs in Firebase (upload APNs key/cert) for proper delivery.

### 2) App initialization (Swift)

- Configure Firebase early (e.g., in `iOSApp.swift` or an AppDelegate):

```swift
import FirebaseCore
import FirebaseMessaging
import UserNotifications

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        UNUserNotificationCenter.current().delegate = NotificationDelegate.shared
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, _ in
            DispatchQueue.main.async {
                UIApplication.shared.registerForRemoteNotifications()
            }
        }
        Messaging.messaging().delegate = NotificationDelegate.shared
    }
    var body: some Scene { /* existing */ }
}
```

- In `NotificationDelegate` (singleton):
    - Conform to `UNUserNotificationCenterDelegate` and `MessagingDelegate`.
    - Forward APNs device token to FCM via `Messaging.messaging().apnsToken = deviceToken`.

### 3) Topic subscription bridge

Create a Swift bridge visible to Obj‑C:

```swift
@objcMembers
class FCMBridge: NSObject {
    func subscribe(toTopic topic: String, completion: @escaping (Error?) -> Void) {
        Messaging.messaging().subscribe(toTopic: topic) { error in completion(error) }
    }
    func unsubscribe(fromTopic topic: String, completion: @escaping (Error?) -> Void) {
        Messaging.messaging().unsubscribe(fromTopic: topic) { error in completion(error) }
    }
    func getToken(completion: @escaping (String?, Error?) -> Void) {
        Messaging.messaging().token { token, error in completion(token, error) }
    }
}
```

### 4) KMP `actual` for iOS

- In `iosMain`, implement `actual class PushMessaging` that calls into `FCMBridge` using
  Kotlin/Native interop.
- Optionally, expose a callback/Flow to shared code by bridging from the Swift delegate to Kotlin
  later.

Notes:

- iOS push notifications require a physical device (not simulator). Ensure APNs is correctly
  configured in Firebase.

---

## Shared API (commonMain)

Define `expect` interface and a no-op Desktop implementation.

```kotlin
// commonMain
expect interface PushMessaging {
    suspend fun subscribeToTopic(topic: String): Result<Unit>
    suspend fun unsubscribeFromTopic(topic: String): Result<Unit>
    suspend fun getToken(): Result<String>
}

// desktopMain (no-op)
actual class PushMessagingImpl : PushMessaging {
    override suspend fun subscribeToTopic(topic: String) = Result.success(Unit)
    override suspend fun unsubscribeFromTopic(topic: String) = Result.success(Unit)
    override suspend fun getToken() = Result.failure(UnsupportedOperationException("No token on desktop"))
}
```

DI wiring (Koin): provide `PushMessaging` in platform modules and bind to the shared code that needs
it.

---

## Testing

- Android:
    - Use Firebase Console to send a message to a topic or use HTTP v1 API (
      `projects/*/messages:send`).
    - Ensure `POST_NOTIFICATIONS` is granted on Android 13+.
- iOS:
    - Test on a physical device.
    - Confirm APNs config in Firebase. Check device token and FCM token logs. Send topic message via
      console or HTTP v1.

HTTP v1 example (topic):

```json
POST https://fcm.googleapis.com/v1/projects/your-project-id/messages:send
Authorization: Bearer <OAuth2 Access Token>
{
  "message": {
    "topic": "launches_all",
    "notification": {"title": "Next launch", "body": "T-1 hour"},
    "data": {"type": "launch", "id": "1234"}
  }
}
```

---

## Step-by-step Checklist

1) Android build setup

- Add Google Services plugin to `composeApp`.
- Add Firebase BOM + messaging dependency to `androidMain`.
- Place `google-services.json` in `composeApp/`.

2) Android app code

- Create `AppFirebaseMessagingService` and register in Manifest.
- Add notification channel creation.
- Implement Android `actual` of `PushMessaging`.
- Add runtime permission request for Android 13+.

3) Shared code

- Define `expect` `PushMessaging` and inject via Koin.
- Call `subscribeToTopic` at appropriate UX point (e.g., settings toggle or onboarding).

4) iOS setup

- Add SPM dependency `FirebaseMessaging`.
- Add `GoogleService-Info.plist` to target.
- Enable Push Notifications + Background Modes (Remote notifications).
- Configure APNs in Firebase.

5) iOS app code

- Configure Firebase on launch; set notification center and messaging delegates.
- Implement `NotificationDelegate` bridging APNs/FCM tokens.
- Add `FCMBridge` class.
- Implement iOS `actual` of `PushMessaging` that calls `FCMBridge`.

6) QA

- Subscribe the app to a test topic (e.g., `test_topic`).
- Send test notifications from Firebase Console and via HTTP v1.
- Verify foreground/background behavior and deep links (if any).

---

## Open Questions / Decisions

- What initial topics should the app subscribe to (e.g., `launches_all`, provider-specific)?
- Do we need data-only messages for in-app updates vs. notification payloads for user-visible
  alerts?
- Should subscriptions be user-driven (settings UI) or automatic based on app state/preferences?
- Will tokens be sent to a backend for server-side targeting, or will topics suffice?

---

## References

- Firebase Cloud Messaging for Android
- Firebase Cloud Messaging for iOS
- HTTP v1 API for FCM messages
- Apple Push Notification service (APNs)
