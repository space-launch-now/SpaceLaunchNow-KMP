package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.data.model.NotificationSettings
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun getNotificationSettings(): NotificationSettings
    suspend fun updateNotificationSettings(settings: NotificationSettings)
    suspend fun subscribeToTopic(topic: NotificationTopic): Result<Unit>
    suspend fun unsubscribeFromTopic(topic: NotificationTopic): Result<Unit>
    suspend fun requestNotificationPermission(): Boolean
    fun getNotificationSettingsFlow(): Flow<NotificationSettings>
}