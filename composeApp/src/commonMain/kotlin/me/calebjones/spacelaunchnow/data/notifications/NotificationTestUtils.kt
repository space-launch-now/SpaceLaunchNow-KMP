package me.calebjones.spacelaunchnow.data.notifications

import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository

/**
 * Utility functions for testing and demonstrating notification functionality
 */
class NotificationTestUtils(
    private val notificationRepository: NotificationRepository
) {

    /**
     * Subscribe to all default notification topics
     */
    suspend fun subscribeToAllTopics(): Result<Unit> {
        return try {
            val defaultTopics = listOf(
                NotificationTopic.LAUNCHES_ALL,
                NotificationTopic.EVENTS
            )
            defaultTopics.forEach { topic ->
                notificationRepository.subscribeToTopic(topic)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Unsubscribe from all notification topics
     */
    suspend fun unsubscribeFromAllTopics(): Result<Unit> {
        return try {
            val defaultTopics = listOf(
                NotificationTopic.LAUNCHES_ALL,
                NotificationTopic.EVENTS
            )
            defaultTopics.forEach { topic ->
                notificationRepository.unsubscribeFromTopic(topic)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get the list of currently subscribed topics
     */
    suspend fun getSubscribedTopics(): Set<String> {
        return notificationRepository.getNotificationSettings().subscribedTopics
    }

    /**
     * Helper function to create test notification settings
     */
    fun createTestSettings() = me.calebjones.spacelaunchnow.data.model.NotificationSettings(
        enableNotifications = true,
        notifyDailySummary = true,
        notifyBeforeLaunch = true,
        notifyMinutesBefore = 30,
        subscribedTopics = setOf(
            NotificationTopic.LAUNCHES_ALL.id,
            NotificationTopic.EVENTS.id
        )
    )
}