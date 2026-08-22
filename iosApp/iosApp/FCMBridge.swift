import Foundation
import FirebaseMessaging
import ComposeApp

/// Bridge between Swift Firebase SDK and Kotlin Multiplatform code
/// Provides FCM functionality to Kotlin/Native via Objective-C compatible APIs
///
/// Communication with Kotlin:
/// - Checks IosPushMessagingBridge.pendingOperation / lastRequestedTopic / currentRequestId
///   to see what Kotlin needs
/// - Calls IosPushMessagingBridge.provideToken/provideSubscribeResult/etc. with the result,
///   echoing the request id so Kotlin can discard a late result for an older request
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

    /// Request id (`IosPushMessagingBridge.currentRequestId`) of the operation
    /// currently executing against Firebase, or 0 when idle.
    ///
    /// `processPendingKotlinRequests()` is re-entered from the APNs and FCM token
    /// callbacks. Without this guard an in-flight request would be issued a second
    /// time, and the second completion would resolve whatever request Kotlin had
    /// queued next -- confirming a topic that was never actually sent.
    private var inFlightRequestId: Int64 = 0

    /// Check for the pending Kotlin request and fulfill it.
    /// Called when Kotlin posts "KotlinFCMRequestPending", and again whenever the
    /// APNs or FCM token becomes available. Every path reports back to Kotlin --
    /// success or failure -- so the Kotlin caller never waits on a result that
    /// will not come.
    @objc public func processPendingKotlinRequests() {
        let bridge = IosPushMessagingBridge.shared
        let requestId = bridge.currentRequestId
        let operation = bridge.pendingOperation
        print("🔍 FCMBridge: Pending operation: \(operation) (request \(requestId))")

        switch operation {
        case .getToken, .subscribe, .unsubscribe:
            break
        default:
            // No pending operation (NONE case)
            print("✨ FCMBridge: No pending operation (NONE)")
            return
        }

        guard requestId != inFlightRequestId else {
            print("⏳ FCMBridge: Request \(requestId) is already in flight — not re-issuing")
            return
        }
        inFlightRequestId = requestId

        switch operation {
        case .getToken:
            print("📞 FCMBridge: Processing GET_TOKEN request \(requestId) from Kotlin")
            // getToken() answers from the cache when it can, and otherwise asks Firebase.
            getToken { token, error in
                self.finish(requestId)
                if let token = token {
                    bridge.provideToken(requestId: requestId, token: token, errorMessage: nil)
                } else {
                    bridge.provideToken(
                        requestId: requestId,
                        token: nil,
                        errorMessage: error?.localizedDescription ?? "Unknown error"
                    )
                }
            }

        case .subscribe:
            guard let topic = bridge.lastRequestedTopic else {
                finish(requestId)
                bridge.provideSubscribeResult(requestId: requestId, errorMessage: "No topic attached to subscribe request")
                return
            }
            print("📞 FCMBridge: Processing SUBSCRIBE request \(requestId) for topic: \(topic)")
            subscribeToTopic(topic) { error in
                self.finish(requestId)
                bridge.provideSubscribeResult(requestId: requestId, errorMessage: error?.localizedDescription)
            }

        case .unsubscribe:
            guard let topic = bridge.lastRequestedTopic else {
                finish(requestId)
                bridge.provideUnsubscribeResult(requestId: requestId, errorMessage: "No topic attached to unsubscribe request")
                return
            }
            print("📞 FCMBridge: Processing UNSUBSCRIBE request \(requestId) for topic: \(topic)")
            unsubscribeFromTopic(topic) { error in
                self.finish(requestId)
                bridge.provideUnsubscribeResult(requestId: requestId, errorMessage: error?.localizedDescription)
            }

        default:
            break
        }
    }

    /// Mark `requestId` as no longer executing. A late completion for an older
    /// request must not clear a newer one's in-flight marker.
    private func finish(_ requestId: Int64) {
        if inFlightRequestId == requestId {
            inFlightRequestId = 0
        }
    }
}
