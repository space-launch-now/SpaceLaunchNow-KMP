package me.calebjones.spacelaunchnow.data.notifications

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import me.calebjones.spacelaunchnow.data.model.PushMessage
import me.calebjones.spacelaunchnow.util.logging.PushDiagnostics
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger

/**
 * iOS implementation of PushMessaging using Firebase Cloud Messaging.
 *
 * Every call goes through [IosPushMessagingBridge], which serializes requests to
 * the Swift FCMBridge and guarantees each one completes -- with Firebase's real
 * outcome, or a timeout failure -- so callers such as the V6 subscription
 * reconciler can always record a result and retry on their next pass.
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

    actual suspend fun subscribeToTopic(topic: String): Result<Unit> {
        log.i { "Subscribing to topic: $topic" }
        return IosPushMessagingBridge.subscribe(topic)
    }

    actual suspend fun unsubscribeFromTopic(topic: String): Result<Unit> {
        log.i { "Unsubscribing from topic: $topic" }
        return IosPushMessagingBridge.unsubscribe(topic)
    }

    actual suspend fun getToken(): Result<String> {
        log.i { "Getting FCM token" }
        return IosPushMessagingBridge.getToken()
            .onSuccess { token ->
                PushDiagnostics.recordTokenSuccess(token)
                log.i { "SUCCESS - Got FCM token (len=${token.length}, …${token.takeLast(6)})" }
            }
            .onFailure { error ->
                PushDiagnostics.recordTokenUnavailable(error.message ?: "exception")
                log.e { "ERROR - Failed to get FCM token: ${error.message}" }
            }
    }
}
