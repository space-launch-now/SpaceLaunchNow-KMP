package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.data.model.NotificationSettings
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.model.NotificationAgency
import me.calebjones.spacelaunchnow.data.model.NotificationLocation
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun initializeNotifications()
    suspend fun getNotificationSettings(): NotificationSettings
    suspend fun updateNotificationSettings(settings: NotificationSettings)
    suspend fun subscribeToTopic(topic: NotificationTopic): Result<Unit>
    suspend fun unsubscribeFromTopic(topic: NotificationTopic): Result<Unit>
    suspend fun requestNotificationPermission(): Boolean
    fun getNotificationSettingsFlow(): Flow<NotificationSettings>

    // Agency and Location management
    suspend fun getAvailableAgencies(): List<NotificationAgency>
    suspend fun getAvailableLocations(): List<NotificationLocation>
    suspend fun subscribeToAgency(agency: NotificationAgency): Result<Unit>
    suspend fun unsubscribeFromAgency(agency: NotificationAgency): Result<Unit>
    suspend fun subscribeToLocation(location: NotificationLocation): Result<Unit>
    suspend fun unsubscribeFromLocation(location: NotificationLocation): Result<Unit>
    suspend fun updateTopicSubscriptions(): Result<Unit>
}