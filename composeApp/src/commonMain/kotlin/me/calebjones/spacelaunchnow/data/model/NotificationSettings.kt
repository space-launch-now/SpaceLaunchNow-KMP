package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationSettings(
    val enableNotifications: Boolean = true,
    val notifyDailySummary: Boolean = false,
    val notifyBeforeLaunch: Boolean = true,
    val notifyMinutesBefore: Int = 30,
    val subscribedTopics: Set<String> = emptySet()
)

@Serializable
data class PushMessage(
    val title: String,
    val body: String,
    val data: Map<String, String> = emptyMap()
)

enum class NotificationTopic(val topicName: String) {
    LAUNCHES_ALL("launches_all"),
    LAUNCHES_SPACEX("launches_spacex"),
    LAUNCHES_NASA("launches_nasa"),
    DAILY_SUMMARY("daily_summary")
}