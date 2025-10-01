package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationSettings(
    val enableNotifications: Boolean = true,
    val notifyDailySummary: Boolean = false,
    val notifyBeforeLaunch: Boolean = true,
    val notifyMinutesBefore: Int = 30,
    val subscribedTopics: Set<String> = emptySet(),
    val subscribedAgencies: Set<Int> = emptySet(),
    val subscribedLocations: Set<Int> = emptySet(),
    val useStrictMatching: Boolean = false, // false = inclusive (OR), true = strict (AND)
    val followAllLaunches: Boolean = true,
    val hideTbdLaunches: Boolean = false,
    val keepLaunchesFor24Hours: Boolean = true,
    val eventNotifications: Boolean = true,
    val netstampChanged: Boolean = true,
    val webcastOnly: Boolean = false,
    val twentyFourHour: Boolean = true,
    val oneHour: Boolean = false,
    val tenMinutes: Boolean = true,
    val oneMinute: Boolean = false,
    val inFlight: Boolean = true,
    val success: Boolean = true
)

@Serializable
data class PushMessage(
    val title: String,
    val body: String,
    val data: Map<String, String> = emptyMap()
)

@Serializable
data class NotificationTopic(
    val id: String,
    val name: String,
    val description: String? = null
) {
    companion object {
        val LAUNCHES_ALL = NotificationTopic("launches", "Launches")
        val EVENTS = NotificationTopic("events", "Events")
    }
}

@Serializable
data class NotificationAgency(
    val id: Int,
    val name: String,
    val abbreviation: String? = null
)

@Serializable
data class NotificationLocation(
    val id: Int,
    val name: String,
    val countryCode: String? = null
)