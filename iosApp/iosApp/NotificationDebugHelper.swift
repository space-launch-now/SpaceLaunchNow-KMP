import Foundation
import UIKit
import UserNotifications
import FirebaseMessaging

/// Helper class to debug iOS notification issues
class NotificationDebugHelper {
    
    static let shared = NotificationDebugHelper()
    
    private init() {}
    
    /// Print comprehensive notification setup status
    func printNotificationStatus() {
        print("\n" + String(repeating: "=", count: 60))
        print("📊 NOTIFICATION DEBUG STATUS")
        print(String(repeating: "=", count: 60))
        
        // 1. Check notification authorization status
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            print("\n1️⃣ NOTIFICATION AUTHORIZATION:")
            print("   Authorization Status: \(self.authorizationStatusString(settings.authorizationStatus))")
            print("   Alert Setting: \(self.settingString(settings.alertSetting))")
            print("   Badge Setting: \(self.settingString(settings.badgeSetting))")
            print("   Sound Setting: \(self.settingString(settings.soundSetting))")
            print("   Lock Screen: \(self.settingString(settings.lockScreenSetting))")
            print("   Notification Center: \(self.settingString(settings.notificationCenterSetting))")
            
            if #available(iOS 15.0, *) {
                print("   Scheduled Delivery: \(self.settingString(settings.scheduledDeliverySetting))")
            }
        }
        
        // 2. Check APNs token
        print("\n2️⃣ APNS TOKEN:")
        if let apnsToken = Messaging.messaging().apnsToken {
            let tokenString = apnsToken.map { String(format: "%02.2hhx", $0) }.joined()
            print("   ✅ APNs Token: \(tokenString.prefix(20))...\(tokenString.suffix(20))")
        } else {
            print("   ❌ APNs Token: NOT SET")
            print("   ⚠️  Without APNs token, FCM cannot work!")
        }
        
        // 3. Check FCM token
        print("\n3️⃣ FCM TOKEN:")
        Messaging.messaging().token { token, error in
            if let token = token {
                print("   ✅ FCM Token: \(token.prefix(20))...\(token.suffix(20))")
                print("   📋 Full Token: \(token)")
            } else if let error = error {
                print("   ❌ FCM Token Error: \(error.localizedDescription)")
            } else {
                print("   ⚠️  FCM Token: NOT AVAILABLE YET")
            }
        }
        
        // 4. Check background modes
        print("\n4️⃣ BACKGROUND MODES:")
        if let backgroundModes = Bundle.main.object(forInfoDictionaryKey: "UIBackgroundModes") as? [String] {
            print("   Configured modes: \(backgroundModes)")
            let hasRemoteNotification = backgroundModes.contains("remote-notification")
            print("   remote-notification: \(hasRemoteNotification ? "✅" : "❌")")
        } else {
            print("   ❌ No background modes configured!")
        }
        
        // 4b. Check Background App Refresh status
        print("\n4️⃣b BACKGROUND APP REFRESH:")
        let status = UIApplication.shared.backgroundRefreshStatus
        switch status {
        case .available:
            print("   ✅ Available - App can receive notifications when killed")
        case .denied:
            print("   ❌ DENIED - User disabled Background App Refresh")
            print("   ⚠️  Notifications will NOT work when app is killed")
            print("   📱 Fix: Settings → General → Background App Refresh → ON")
        case .restricted:
            print("   ⚠️  RESTRICTED - System policy prevents background refresh")
            print("   ⚠️  May be due to Low Power Mode or device restrictions")
        @unknown default:
            print("   ❓ Unknown status")
        }
        
        // 5. Check Firebase configuration
        print("\n5️⃣ FIREBASE CONFIGURATION:")
        if let path = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist"),
           let dict = NSDictionary(contentsOfFile: path) as? [String: Any] {
            print("   ✅ GoogleService-Info.plist found")
            if let projectId = dict["PROJECT_ID"] as? String {
                print("   Project ID: \(projectId)")
            }
            if let bundleId = dict["BUNDLE_ID"] as? String {
                print("   Bundle ID: \(bundleId)")
            }
            if let gcmSenderId = dict["GCM_SENDER_ID"] as? String {
                print("   GCM Sender ID: \(gcmSenderId)")
            }
        } else {
            print("   ❌ GoogleService-Info.plist NOT FOUND")
        }
        
        // 6. Check app bundle ID
        print("\n6️⃣ APP BUNDLE IDENTIFIER:")
        if let bundleId = Bundle.main.bundleIdentifier {
            print("   Bundle ID: \(bundleId)")
        }
        
        // 7. Check topic subscriptions
        print("\n7️⃣ TOPIC SUBSCRIPTIONS:")
        print("   Note: Cannot query subscribed topics directly")
        print("   Check Firebase Console → Cloud Messaging → Topics")
        
        // 8. Check pending notifications
        UNUserNotificationCenter.current().getPendingNotificationRequests { requests in
            print("\n8️⃣ PENDING NOTIFICATIONS:")
            print("   Count: \(requests.count)")
            if requests.count > 0 {
                for request in requests.prefix(5) {
                    print("   - \(request.identifier): \(request.content.title)")
                }
            }
        }
        
        // 9. Check delivered notifications
        UNUserNotificationCenter.current().getDeliveredNotifications { notifications in
            print("\n9️⃣ DELIVERED NOTIFICATIONS:")
            print("   Count: \(notifications.count)")
            if notifications.count > 0 {
                for notification in notifications.prefix(5) {
                    print("   - \(notification.request.identifier): \(notification.request.content.title)")
                }
            }
        }
        
        print("\n" + String(repeating: "=", count: 60))
        print("📊 END NOTIFICATION DEBUG STATUS")
        print(String(repeating: "=", count: 60) + "\n")
    }
    
    /// Test notification by creating a local notification
    func sendTestLocalNotification() {
        print("\n🧪 Sending test local notification...")
        
        let content = UNMutableNotificationContent()
        content.title = "Test Notification"
        content.body = "This is a test notification to verify notifications are working"
        content.sound = .default
        content.badge = 1
        
        let request = UNNotificationRequest(
            identifier: "test-\(Date().timeIntervalSince1970)",
            content: content,
            trigger: nil // Show immediately
        )
        
        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("❌ Failed to send test notification: \(error.localizedDescription)")
            } else {
                print("✅ Test notification sent successfully")
            }
        }
    }
    
    /// Subscribe to debug topic
    func subscribeToDebugTopic() {
        print("\n🔧 Subscribing to k_debug_v4 topic...")
        Messaging.messaging().subscribe(toTopic: "k_debug_v4") { error in
            if let error = error {
                print("❌ Failed to subscribe: \(error.localizedDescription)")
            } else {
                print("✅ Successfully subscribed to k_debug_v4")
            }
        }
    }
    
    /// Unsubscribe from debug topic
    func unsubscribeFromDebugTopic() {
        print("\n🔧 Unsubscribing from k_debug_v4 topic...")
        Messaging.messaging().unsubscribe(fromTopic: "k_debug_v4") { error in
            if let error = error {
                print("❌ Failed to unsubscribe: \(error.localizedDescription)")
            } else {
                print("✅ Successfully unsubscribed from k_debug_v4")
            }
        }
    }
    
    // MARK: - Helper Methods
    
    private func authorizationStatusString(_ status: UNAuthorizationStatus) -> String {
        switch status {
        case .notDetermined: return "❓ Not Determined"
        case .denied: return "❌ DENIED"
        case .authorized: return "✅ Authorized"
        case .provisional: return "⚠️ Provisional"
        case .ephemeral: return "⏱️ Ephemeral"
        @unknown default: return "❓ Unknown"
        }
    }
    
    private func settingString(_ setting: UNNotificationSetting) -> String {
        switch setting {
        case .notSupported: return "Not Supported"
        case .disabled: return "❌ Disabled"
        case .enabled: return "✅ Enabled"
        @unknown default: return "Unknown"
        }
    }
}
