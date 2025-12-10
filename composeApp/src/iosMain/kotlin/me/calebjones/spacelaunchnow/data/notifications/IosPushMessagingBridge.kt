package me.calebjones.spacelaunchnow.data.notifications

import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNotificationName

/**
 * Bridge that coordinates FCM operations between Kotlin and Swift.
 * 
 * Simple request/callback pattern:
 * 1. IosPushMessaging stores a callback lambda
 * 2. Exposes property that Swift can check (lastRequestedTopic, needsToken, etc.)
 * 3. Kotlin posts NSNotification to trigger Swift FCMBridge
 * 4. Swift FCMBridge reads the property and performs the operation
 * 5. Swift calls the provide*() function with results
 * 6. Kotlin executes the stored callback
 * 
 * Communication uses NSNotificationCenter to avoid cinterop complexity.
 * Swift FCMBridge already listens for "KotlinFCMRequestPending" notifications.
 */
object IosPushMessagingBridge {
    private val log = SpaceLogger.getLogger("IosPushMessagingBridge")
    
    // Notification name that Swift FCMBridge listens for
    private val KOTLIN_FCM_REQUEST_NOTIFICATION: NSNotificationName = "KotlinFCMRequestPending"
    
    // Stored callbacks
    private var tokenCallback: ((Result<String>) -> Unit)? = null
    private var subscribeCallback: ((Result<Unit>) -> Unit)? = null
    private var unsubscribeCallback: ((Result<Unit>) -> Unit)? = null
    
    // Request state - Swift reads these
    var lastRequestedTopic: String? = null
        private set
    
    var pendingOperation: Operation = Operation.NONE
        private set
    
    enum class Operation {
        NONE,
        GET_TOKEN,
        SUBSCRIBE,
        UNSUBSCRIBE
    }
    
    /**
     * Post a notification to trigger Swift FCMBridge.processPendingKotlinRequests()
     * Swift's FCMBridge is already set up to listen for "KotlinFCMRequestPending" notifications.
     */
    private fun notifySwift() {
        log.d { "Posting NSNotification to trigger Swift FCMBridge" }
        platform.darwin.dispatch_async(platform.darwin.dispatch_get_main_queue()) {
            NSNotificationCenter.defaultCenter.postNotificationName(
                aName = KOTLIN_FCM_REQUEST_NOTIFICATION,
                `object` = null
            )
        }
    }
    
    // ===== Functions called by Kotlin (IosPushMessaging) =====
    
    fun requestToken(callback: (Result<String>) -> Unit) {
        log.i { "Requesting FCM token from Swift" }
        tokenCallback = callback
        pendingOperation = Operation.GET_TOKEN
        // Post notification to trigger Swift FCMBridge
        notifySwift()
    }
    
    fun requestSubscribe(topic: String, callback: (Result<Unit>) -> Unit) {
        log.i { "Requesting subscribe to topic: $topic" }
        lastRequestedTopic = topic
        subscribeCallback = callback
        pendingOperation = Operation.SUBSCRIBE
        // Post notification to trigger Swift FCMBridge
        notifySwift()
    }
    
    fun requestUnsubscribe(topic: String, callback: (Result<Unit>) -> Unit) {
        log.i { "Requesting unsubscribe from topic: $topic" }
        lastRequestedTopic = topic
        unsubscribeCallback = callback
        pendingOperation = Operation.UNSUBSCRIBE
        // Post notification to trigger Swift FCMBridge
        notifySwift()
    }
    
    // ===== Functions called by Swift (FCMBridge) =====
    
    /**
     * Swift calls this to provide the FCM token.
     */
    fun provideToken(token: String?, errorMessage: String?) {
        log.d { "Swift provided FCM token: ${token?.take(20)}..." }
        val callback = tokenCallback
        if (callback == null) {
            log.w { "Received token but no callback registered" }
            return
        }
        
        if (token != null) {
            // Success - clear the pending operation and callback
            callback(Result.success(token))
            tokenCallback = null
            pendingOperation = Operation.NONE
            log.d { "Token request successful, clearing pending operation" }
        } else {
            // Failure - check if this is likely an APNs timing issue
            val isApnsError = errorMessage?.contains("APNS") == true || 
                             errorMessage?.contains("apns") == true
            
            if (isApnsError) {
                // Keep operation pending for automatic retry when APNs arrives
                // Don't call callback yet - wait for retry result
                log.w { "Token request failed due to APNs - keeping operation pending for automatic retry" }
            } else {
                // Other error - report failure and clear
                log.e { "Token request failed with non-APNs error: $errorMessage" }
                callback(Result.failure(Exception(errorMessage ?: "Failed to get FCM token")))
                tokenCallback = null
                pendingOperation = Operation.NONE
            }
        }
    }
    
    /**
     * Swift calls this after attempting to subscribe to a topic.
     */
    fun provideSubscribeResult(errorMessage: String?) {
        log.d { "Swift provided subscribe result: ${if (errorMessage == null) "success" else "error: $errorMessage"}" }
        val callback = subscribeCallback
        if (callback == null) {
            log.w { "Received subscribe result but no callback registered" }
            return
        }
        
        if (errorMessage == null) {
            // Success - clear the pending operation and callback
            callback(Result.success(Unit))
            subscribeCallback = null
            lastRequestedTopic = null
            pendingOperation = Operation.NONE
            log.d { "Subscribe successful, clearing pending operation" }
        } else {
            // Failure - check if this is likely an APNs timing issue
            val isApnsError = errorMessage.contains("APNS") || errorMessage.contains("apns")
            
            if (isApnsError) {
                // Keep operation pending for automatic retry when APNs arrives
                log.w { "Subscribe failed due to APNs - keeping operation pending for automatic retry" }
            } else {
                // Other error - report failure and clear
                log.e { "Subscribe failed with non-APNs error: $errorMessage" }
                callback(Result.failure(Exception(errorMessage)))
                subscribeCallback = null
                lastRequestedTopic = null
                pendingOperation = Operation.NONE
            }
        }
    }
    
    /**
     * Swift calls this after attempting to unsubscribe from a topic.
     */
    fun provideUnsubscribeResult(errorMessage: String?) {
        log.d { "Swift provided unsubscribe result: ${if (errorMessage == null) "success" else "error: $errorMessage"}" }
        val callback = unsubscribeCallback
        if (callback == null) {
            log.w { "Received unsubscribe result but no callback registered" }
            return
        }
        
        if (errorMessage == null) {
            // Success - clear the pending operation and callback
            callback(Result.success(Unit))
            unsubscribeCallback = null
            lastRequestedTopic = null
            pendingOperation = Operation.NONE
            log.d { "Unsubscribe successful, clearing pending operation" }
        } else {
            // Failure - check if this is likely an APNs timing issue
            val isApnsError = errorMessage.contains("APNS") || errorMessage.contains("apns")
            
            if (isApnsError) {
                // Keep operation pending for automatic retry when APNs arrives
                log.w { "Unsubscribe failed due to APNs - keeping operation pending for automatic retry" }
            } else {
                // Other error - report failure and clear
                log.e { "Unsubscribe failed with non-APNs error: $errorMessage" }
                callback(Result.failure(Exception(errorMessage)))
                unsubscribeCallback = null
                lastRequestedTopic = null
                pendingOperation = Operation.NONE
            }
        }
    }
}
