import Foundation
import FirebaseMessaging
import ComposeApp

/// Bridge between Swift Firebase SDK and Kotlin Multiplatform code
/// Provides FCM functionality to Kotlin/Native via Objective-C compatible APIs
///
/// Communication with Kotlin:
/// - Checks IosPushMessagingBridge.pendingOperation to see what Kotlin needs
/// - Calls IosPushMessagingBridge.provideToken/provideSubscribeResult/etc. with results
@objc public class FCMBridge: NSObject {
    
    @objc public static let shared = FCMBridge()
    
    private var currentToken: String?
    
    private override init() {
        super.init()
        
        print("🔧 FCMBridge: Initializing...")
        
        // Listen for notifications from Kotlin when it makes FCM requests
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleKotlinRequest),
            name: NSNotification.Name("KotlinFCMRequestPending"),
            object: nil
        )
        
        print("✅ FCMBridge: Initialized and listening for Kotlin requests")
    }
    
    @objc private func handleKotlinRequest() {
        print("📞 FCMBridge: Received notification of pending Kotlin request")
        processPendingKotlinRequests()
    }
    
    // MARK: - Token Management
    
    @objc public func setCurrentToken(_ token: String) {
        self.currentToken = token
        print("🔑 FCMBridge: Token set, processing any pending Kotlin requests")
        processPendingKotlinRequests()
    }
    
    /// Synchronous token getter - returns cached token or nil
    @objc public func getCurrentToken() -> String? {
        if let token = currentToken {
            print("🔑 FCMBridge: getCurrentToken() returning cached token: \(token.prefix(20))...")
        } else {
            print("⚠️ FCMBridge: getCurrentToken() returning nil - no token available yet")
        }
        return currentToken
    }
    
    @objc public func getToken(completion: @escaping (String?, Error?) -> Void) {
        print("🔑 FCMBridge: getToken() called")
        
        if let token = currentToken {
            print("✅ FCMBridge: Returning cached token: \(token.prefix(20))...")
            completion(token, nil)
            return
        }
        
        print("⏳ FCMBridge: No cached token, fetching from Firebase...")
        Messaging.messaging().token { token, error in
            if let token = token {
                print("✅ FCMBridge: Successfully fetched token from Firebase: \(token.prefix(20))...")
                self.currentToken = token
                completion(token, nil)
            } else {
                print("❌ FCMBridge: Failed to fetch token from Firebase: \(error?.localizedDescription ?? "unknown error")")
                completion(nil, error)
            }
        }
    }
    
    // MARK: - Topic Subscriptions
    
    @objc public func subscribeToTopic(
        _ topic: String,
        completion: @escaping (Error?) -> Void
    ) {
        print("📢 FCMBridge: subscribeToTopic() called for topic: \(topic)")
        Messaging.messaging().subscribe(toTopic: topic) { error in
            if let error = error {
                print("❌ FCMBridge: Failed to subscribe to topic '\(topic)': \(error.localizedDescription)")
                completion(error)
            } else {
                print("✅ FCMBridge: Successfully subscribed to topic: \(topic)")
                completion(nil)
            }
        }
    }
    
    @objc public func unsubscribeFromTopic(
        _ topic: String,
        completion: @escaping (Error?) -> Void
    ) {
        print("🔕 FCMBridge: unsubscribeFromTopic() called for topic: \(topic)")
        Messaging.messaging().unsubscribe(fromTopic: topic) { error in
            if let error = error {
                print("❌ FCMBridge: Failed to unsubscribe from topic '\(topic)': \(error.localizedDescription)")
                completion(error)
            } else {
                print("✅ FCMBridge: Successfully unsubscribed from topic: \(topic)")
                completion(nil)
            }
        }
    }
    
    // MARK: - Kotlin Bridge Integration
    
    /// Check for pending Kotlin requests and fulfill them
    /// Should be called periodically or when FCM token is available
    @objc public func processPendingKotlinRequests() {
        print("🔍 FCMBridge: Checking for pending Kotlin requests...")
        let bridge = IosPushMessagingBridge.shared
        print("🔍 FCMBridge: Current pending operation: \(bridge.pendingOperation)")
        
        switch bridge.pendingOperation {
        case .getToken:
            print("📞 FCMBridge: Processing GET_TOKEN request from Kotlin")
            if let token = currentToken {
                bridge.provideToken(token: token, errorMessage: nil)
            } else {
                // Fetch token asynchronously
                getToken { token, error in
                    if let token = token {
                        bridge.provideToken(token: token, errorMessage: nil)
                    } else {
                        bridge.provideToken(token: nil, errorMessage: error?.localizedDescription ?? "Unknown error")
                    }
                }
            }
            
        case .subscribe:
            if let topic = bridge.lastRequestedTopic {
                print("📞 FCMBridge: Processing SUBSCRIBE request for topic: \(topic)")
                subscribeToTopic(topic) { error in
                    bridge.provideSubscribeResult(errorMessage: error?.localizedDescription)
                }
            }
            
        case .unsubscribe:
            if let topic = bridge.lastRequestedTopic {
                print("📞 FCMBridge: Processing UNSUBSCRIBE request for topic: \(topic)")
                unsubscribeFromTopic(topic) { error in
                    bridge.provideUnsubscribeResult(errorMessage: error?.localizedDescription)
                }
            }
            
        default:
            // No pending operation (NONE case)
            print("✨ FCMBridge: No pending operation (NONE)")
            break
        }
        
        print("✅ FCMBridge: Finished processing pending requests")
    }
}
