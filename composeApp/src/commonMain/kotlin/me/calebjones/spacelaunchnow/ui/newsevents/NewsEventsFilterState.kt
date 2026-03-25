package me.calebjones.spacelaunchnow.ui.newsevents

import kotlinx.serialization.Serializable

/**
 * Persisted filter state for the News & Events screen.
 * Serialized to DataStore for persistence across app sessions.
 */
@Serializable
data class NewsEventsFilterState(
    val selectedNewsSites: List<String> = emptyList(),
    val selectedEventTypeIds: List<Int> = emptyList()
)
