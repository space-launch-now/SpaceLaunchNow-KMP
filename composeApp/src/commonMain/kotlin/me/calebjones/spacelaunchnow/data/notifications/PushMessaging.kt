// Usage: Before handling/displaying any notification in your FCM/Push handler, always check the user's notificationEnabled preference and suppress if false.
package me.calebjones.spacelaunchnow.data.notifications

import kotlinx.coroutines.flow.Flow
import me.calebjones.spacelaunchnow.data.model.PushMessage

expect class PushMessaging() {
    suspend fun subscribeToTopic(topic: String): Result<Unit>
    suspend fun unsubscribeFromTopic(topic: String): Result<Unit>
    suspend fun getToken(): Result<String>
    val messages: Flow<PushMessage>
}