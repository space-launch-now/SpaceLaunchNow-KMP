import Foundation
import FirebaseMessaging

/// Bridge between Swift Firebase SDK and Kotlin Multiplatform code
/// Provides FCM functionality to Kotlin/Native via Objective-C compatible APIs
@objc public class FCMBridge: NSObject {
    
    @objc public static let shared = FCMBridge()
    
    private var currentToken: String?
    
    private override init() {
        super.init()
    }
    
    // MARK: - Token Management
    
    @objc public func setCurrentToken(_ token: String) {
        self.currentToken = token
    }
    
    @objc public func getToken(completion: @escaping (String?, Error?) -> Void) {
        if let token = currentToken {
            completion(token, nil)
            return
        }
        
        Messaging.messaging().token { token, error in
            if let token = token {
                self.currentToken = token
                completion(token, nil)
            } else {
                completion(nil, error)
            }
        }
    }
    
    // MARK: - Topic Subscriptions
    
    @objc public func subscribeToTopic(
        _ topic: String,
        completion: @escaping (Error?) -> Void
    ) {
        print("FCMBridge: Subscribing to topic: \(topic)")
        Messaging.messaging().subscribe(toTopic: topic) { error in
            if let error = error {
                print("FCMBridge: ERROR - Failed to subscribe to topic \(topic): \(error.localizedDescription)")
                completion(error)
            } else {
                print("FCMBridge: SUCCESS - Subscribed to topic: \(topic)")
                completion(nil)
            }
        }
    }
    
    @objc public func unsubscribeFromTopic(
        _ topic: String,
        completion: @escaping (Error?) -> Void
    ) {
        print("FCMBridge: Unsubscribing from topic: \(topic)")
        Messaging.messaging().unsubscribe(fromTopic: topic) { error in
            if let error = error {
                print("FCMBridge: ERROR - Failed to unsubscribe from topic \(topic): \(error.localizedDescription)")
                completion(error)
            } else {
                print("FCMBridge: SUCCESS - Unsubscribed from topic: \(topic)")
                completion(nil)
            }
        }
    }
    
    // MARK: - Client-Side Filtering (Simplified)
    
    /// Apply client-side filtering based on notification data
    /// This is a simplified version that checks basic conditions
    /// TODO: Integrate with shared Kotlin NotificationFilter once interop is complete
    @objc public func shouldShowNotification(data: [String: String]) -> Bool {
        print("=== FCMBridge: Evaluating notification filter ===")
        print("Data: \(data)")
        
        // For now, allow all notifications
        // TODO: Load user preferences and apply filtering logic
        // This should match the logic in NotificationFilter.kt from commonMain
        
        // Basic check: If notification_type is missing, suppress
        guard let notificationType = data["notification_type"] else {
            print("🔇 BLOCKED: Missing notification_type")
            return false
        }
        
        print("Notification type: \(notificationType)")
        
        // TODO: Implement full filtering logic:
        // 1. Check if notifications are globally enabled
        // 2. Check webcast-only filter
        // 3. Check notification type settings
        // 4. Check agency/location subscriptions
        
        // For now, allow all valid notifications
        print("✅ ALLOWED: Notification passed basic validation (full filtering TODO)")
        return true
    }
}
