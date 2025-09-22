package me.calebjones.spacelaunchnow.data.notifications

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.calebjones.spacelaunchnow.data.model.PushMessage

actual class PushMessaging actual constructor() {

    actual val messages: Flow<PushMessage> = flowOf()

    actual suspend fun subscribeToTopic(topic: String): Result<Unit> {
        // Desktop implementation - no-op
        return Result.success(Unit)
    }

    actual suspend fun unsubscribeFromTopic(topic: String): Result<Unit> {
        // Desktop implementation - no-op  
        return Result.success(Unit)
    }

    actual suspend fun getToken(): Result<String> {
        // Desktop implementation - no token available
        return Result.failure(UnsupportedOperationException("No token available on desktop"))
    }
}