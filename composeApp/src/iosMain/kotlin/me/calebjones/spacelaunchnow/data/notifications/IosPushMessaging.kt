package me.calebjones.spacelaunchnow.data.notifications

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import me.calebjones.spacelaunchnow.data.model.PushMessage
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of PushMessaging using Firebase Cloud Messaging
 * 
 * Communicates with Swift FCMBridge via IosPushMessagingBridge.
 * The bridge uses a simple request/callback pattern:
 * 1. Kotlin requests an operation (token, subscribe, unsubscribe)
 * 2. Swift FCMBridge checks the pendingOperation property
 * 3. Swift performs the operation and calls provideToken/provideSubscribeResult/etc.
 * 4. Kotlin resumes the suspended coroutine with the result
 * 
 * Swift components:
 * - FCMBridge.swift: Provides Firebase Messaging functionality
 * - AppDelegate.swift: Initializes Firebase, handles notifications and tokens
 * - iOSApp.swift: Registers AppDelegate with @UIApplicationDelegateAdaptor
 */

actual class PushMessaging actual constructor() {
    private val log = SpaceLogger.getLogger("IosPushMessaging")

    private val _messages = MutableSharedFlow<PushMessage>()
    actual val messages: Flow<PushMessage> = _messages.asSharedFlow()

    actual suspend fun subscribeToTopic(topic: String): Result<Unit> = suspendCoroutine { continuation ->
        log.i { "Subscribing to topic: $topic" }
        IosPushMessagingBridge.requestSubscribe(topic) { result ->
            continuation.resume(result)
        }
    }

    actual suspend fun unsubscribeFromTopic(topic: String): Result<Unit> = suspendCoroutine { continuation ->
        log.i { "Unsubscribing from topic: $topic" }
        IosPushMessagingBridge.requestUnsubscribe(topic) { result ->
            continuation.resume(result)
        }
    }

    actual suspend fun getToken(): Result<String> = suspendCoroutine { continuation ->
        log.i { "Getting FCM token" }
        
        IosPushMessagingBridge.requestToken { result ->
            result.onSuccess { token ->
                log.i { "SUCCESS - Got FCM token: ${token.take(20)}..." }
            }.onFailure { error ->
                log.e { "ERROR - Failed to get FCM token: ${error.message}" }
            }
            continuation.resume(result)
        }
    }
}