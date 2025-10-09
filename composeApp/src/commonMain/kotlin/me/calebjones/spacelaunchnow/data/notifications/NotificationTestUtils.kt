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
                NotificationTopic.EVENTS,
                NotificationTopic.TWENTY_FOUR_HOUR,
                NotificationTopic.TEN_MINUTES,
                NotificationTopic.SUCCESS
            )
            defaultTopics.forEach { topic ->
                notificationRepository.setTopicEnabled(topic, true)
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
                NotificationTopic.EVENTS,
                NotificationTopic.TWENTY_FOUR_HOUR,
                NotificationTopic.TEN_MINUTES,
                NotificationTopic.SUCCESS
            )
            defaultTopics.forEach { topic ->
                notificationRepository.setTopicEnabled(topic, false)
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
        return notificationRepository.state.value.subscribedTopics
    }

    /**
     * Helper function to create test notification settings
     */
    fun createTestSettings() = me.calebjones.spacelaunchnow.data.model.NotificationState(
        enableNotifications = true,
        subscribedTopics = setOf(
            NotificationTopic.EVENTS.id,
            NotificationTopic.TWENTY_FOUR_HOUR.id
        )
    )
}