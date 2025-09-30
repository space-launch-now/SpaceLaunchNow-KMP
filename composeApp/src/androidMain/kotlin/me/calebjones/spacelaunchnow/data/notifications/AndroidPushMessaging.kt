package me.calebjones.spacelaunchnow.data.notifications

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.messaging.FirebaseMessaging
import me.calebjones.spacelaunchnow.data.model.PushMessage

actual class PushMessaging actual constructor() {

    private val _messages = MutableSharedFlow<PushMessage>()
    actual val messages: Flow<PushMessage> = _messages.asSharedFlow()

    actual suspend fun subscribeToTopic(topic: String): Result<Unit> {
        println("AndroidPushMessaging: Subscribing to FCM topic: $topic")
        return try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
            println("AndroidPushMessaging: SUCCESS - Subscribed to FCM topic: $topic")
            Result.success(Unit)
        } catch (e: Exception) {
            println("AndroidPushMessaging: ERROR - Failed to subscribe to topic $topic: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    actual suspend fun unsubscribeFromTopic(topic: String): Result<Unit> {
        println("AndroidPushMessaging: Unsubscribing from FCM topic: $topic")
        return try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
            println("AndroidPushMessaging: SUCCESS - Unsubscribed from FCM topic: $topic")
            Result.success(Unit)
        } catch (e: Exception) {
            println("AndroidPushMessaging: ERROR - Failed to unsubscribe from topic $topic: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    actual suspend fun getToken(): Result<String> {
        println("AndroidPushMessaging: Getting FCM token")
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            println("AndroidPushMessaging: SUCCESS - Got FCM token: ${token.take(20)}...")
            Result.success(token)
        } catch (e: Exception) {
            println("AndroidPushMessaging: ERROR - Failed to get FCM token: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun onMessageReceived(message: PushMessage) {
        _messages.tryEmit(message)
    }
}