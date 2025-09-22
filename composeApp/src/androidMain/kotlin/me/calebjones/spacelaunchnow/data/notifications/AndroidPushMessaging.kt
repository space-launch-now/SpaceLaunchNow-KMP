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
        return try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun unsubscribeFromTopic(topic: String): Result<Unit> {
        return try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun getToken(): Result<String> {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun onMessageReceived(message: PushMessage) {
        _messages.tryEmit(message)
    }
}