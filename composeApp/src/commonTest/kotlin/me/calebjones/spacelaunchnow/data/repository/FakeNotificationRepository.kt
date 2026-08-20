package me.calebjones.spacelaunchnow.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.calebjones.spacelaunchnow.data.model.NotificationAgency
import me.calebjones.spacelaunchnow.data.model.NotificationLocation
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.notifications.v6.V6ReconcileResult

/**
 * Hand-written fake following the MockSubscriptionRepository pattern: call
 * counters plus a configurable result, no mocking framework.
 */
class FakeNotificationRepository : NotificationRepository {

    private val _state = MutableStateFlow(NotificationState.DEFAULT)
    override val state: StateFlow<NotificationState> = _state.asStateFlow()

    var reconcileCalls = 0
    var forceResubscribeCalls = 0
    var nextReconcileResult = V6ReconcileResult(attempted = 3, failed = 0)

    override suspend fun initialize() {}

    override suspend fun setNotificationsEnabled(enabled: Boolean) {}

    override suspend fun setFollowAllLaunches(enabled: Boolean) {}

    override suspend fun setUseStrictMatching(enabled: Boolean) {}

    override suspend fun setTopicEnabled(topic: NotificationTopic, enabled: Boolean) {}

    override suspend fun setAgencyEnabled(agency: NotificationAgency, enabled: Boolean) {}

    override suspend fun setAgencyEnabled(topicName: String, enabled: Boolean) {}

    override suspend fun setLocationEnabled(location: NotificationLocation, enabled: Boolean) {}

    override suspend fun setLocationEnabled(topicName: String, enabled: Boolean) {}

    override suspend fun getAvailableAgencies(): List<NotificationAgency> = NotificationAgency.getAll()

    override suspend fun getAvailableLocations(): List<NotificationLocation> = NotificationLocation.getAll()

    override suspend fun requestNotificationPermission(): Boolean = true

    override suspend fun hasNotificationPermission(): Boolean = true

    override suspend fun reconcileSubscriptions(): V6ReconcileResult {
        reconcileCalls++
        return nextReconcileResult
    }

    override suspend fun forceResubscribe(): V6ReconcileResult {
        forceResubscribeCalls++
        return nextReconcileResult
    }
}
