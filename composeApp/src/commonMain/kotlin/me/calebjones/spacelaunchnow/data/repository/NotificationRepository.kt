package me.calebjones.spacelaunchnow.data.repository

import kotlinx.coroutines.flow.StateFlow
import me.calebjones.spacelaunchnow.data.model.NotificationAgency
import me.calebjones.spacelaunchnow.data.model.NotificationLocation
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.notifications.v6.V6ReconcileResult

interface NotificationRepository {

    // Single source of truth - all UI observes this
    val state: StateFlow<NotificationState>

    // App initialization
    suspend fun initialize()

    // Simple state updates (instant UI feedback)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setFollowAllLaunches(enabled: Boolean)
    suspend fun setUseStrictMatching(enabled: Boolean)
    suspend fun setTopicEnabled(topic: NotificationTopic, enabled: Boolean)
    suspend fun setAgencyEnabled(agency: NotificationAgency, enabled: Boolean)
    suspend fun setAgencyEnabled(topicName: String, enabled: Boolean)
    suspend fun setLocationEnabled(location: NotificationLocation, enabled: Boolean)
    suspend fun setLocationEnabled(topicName: String, enabled: Boolean)

    // Data access
    suspend fun getAvailableAgencies(): List<NotificationAgency>
    suspend fun getAvailableLocations(): List<NotificationLocation>

    // V6 subscription reconciliation. Triggered by explicit Save, once per app
    // start (inside initialize()), on Android token refresh, and immediately on
    // the master kill switch.
    suspend fun reconcileSubscriptions(): V6ReconcileResult
    suspend fun forceResubscribe(): V6ReconcileResult

    // Permission handling
    suspend fun requestNotificationPermission(): Boolean
    suspend fun hasNotificationPermission(): Boolean
}