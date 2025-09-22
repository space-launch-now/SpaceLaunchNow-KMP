package me.calebjones.spacelaunchnow.data.notifications

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.calebjones.spacelaunchnow.data.model.PushMessage

actual class PushMessaging actual constructor() {

    actual val messages: Flow<PushMessage> = flowOf()

    actual suspend fun subscribeToTopic(topic: String): Result<Unit> {
        // iOS implementation - TODO: implement with Firebase iOS SDK
        return Result.success(Unit)
    }

    actual suspend fun unsubscribeFromTopic(topic: String): Result<Unit> {
        // iOS implementation - TODO: implement with Firebase iOS SDK  
        return Result.success(Unit)
    }

    actual suspend fun getToken(): Result<String> {
        // iOS implementation - TODO: implement with Firebase iOS SDK
        return Result.failure(UnsupportedOperationException("Token not implemented on iOS yet"))
    }
}