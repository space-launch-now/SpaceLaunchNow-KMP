package me.calebjones.spacelaunchnow.data.notifications

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.tasks.await
import me.calebjones.spacelaunchnow.data.model.PushMessage
import me.calebjones.spacelaunchnow.util.logging.logger

actual class PushMessaging actual constructor() {

    private val log = logger()

    private val _messages = MutableSharedFlow<PushMessage>()
    actual val messages: Flow<PushMessage> = _messages.asSharedFlow()

    actual suspend fun subscribeToTopic(topic: String): Result<Unit> {
        log.d { "Subscribing to FCM topic: $topic" }
        return try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
            log.d { "SUCCESS - Subscribed to FCM topic: $topic" }
            Result.success(Unit)
        } catch (e: Exception) {
            log.e { "ERROR - Failed to subscribe to topic $topic: ${e.message}" }
            Result.failure(e)
        }
    }

    actual suspend fun unsubscribeFromTopic(topic: String): Result<Unit> {
        log.d { "Unsubscribing from FCM topic: $topic" }
        return try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
            log.d { "SUCCESS - Unsubscribed from FCM topic: $topic" }
            Result.success(Unit)
        } catch (e: Exception) {
            log.e { "ERROR - Failed to unsubscribe from topic $topic: ${e.message}" }
            Result.failure(e)
        }
    }

    actual suspend fun getToken(): Result<String> {
        log.d { "Getting FCM token" }
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            log.d { "SUCCESS - Got FCM token: ${token.take(20)}..." }
            Result.success(token)
        } catch (e: Exception) {
            log.e { "ERROR - Failed to get FCM token: ${e.message}" }
            Result.failure(e)
        }
    }

    fun onMessageReceived(message: PushMessage) {
        log.d { "Received notif FCM message: $message" }
        _messages.tryEmit(message)
    }
}