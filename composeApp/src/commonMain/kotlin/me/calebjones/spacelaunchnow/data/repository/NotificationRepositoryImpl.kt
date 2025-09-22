package me.calebjones.spacelaunchnow.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.calebjones.spacelaunchnow.data.model.NotificationSettings
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.notifications.PushMessaging

class NotificationRepositoryImpl(
    private val pushMessaging: PushMessaging
) : NotificationRepository {

    private val _notificationSettings = MutableStateFlow(NotificationSettings())

    override suspend fun getNotificationSettings(): NotificationSettings {
        return _notificationSettings.value
    }

    override suspend fun updateNotificationSettings(settings: NotificationSettings) {
        _notificationSettings.value = settings
        // TODO: Persist settings to local storage
    }

    override suspend fun subscribeToTopic(topic: NotificationTopic): Result<Unit> {
        return pushMessaging.subscribeToTopic(topic.topicName).also { result ->
            if (result.isSuccess) {
                val currentSettings = _notificationSettings.value
                val updatedTopics = currentSettings.subscribedTopics + topic.topicName
                updateNotificationSettings(currentSettings.copy(subscribedTopics = updatedTopics))
            }
        }
    }

    override suspend fun unsubscribeFromTopic(topic: NotificationTopic): Result<Unit> {
        return pushMessaging.unsubscribeFromTopic(topic.topicName).also { result ->
            if (result.isSuccess) {
                val currentSettings = _notificationSettings.value
                val updatedTopics = currentSettings.subscribedTopics - topic.topicName
                updateNotificationSettings(currentSettings.copy(subscribedTopics = updatedTopics))
            }
        }
    }

    override suspend fun requestNotificationPermission(): Boolean {
        // Platform-specific implementation will handle permission request
        return true
    }

    override fun getNotificationSettingsFlow(): Flow<NotificationSettings> {
        return _notificationSettings.asStateFlow()
    }
}