package me.calebjones.spacelaunchnow.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.calebjones.spacelaunchnow.data.model.NotificationAgency
import me.calebjones.spacelaunchnow.data.model.NotificationLocation
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.notifications.PushMessaging
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.util.logging.logger

expect suspend fun requestPlatformNotificationPermission(): Boolean
expect suspend fun hasPlatformNotificationPermission(): Boolean
expect fun openPlatformNotificationSettings(): Boolean

class NotificationRepositoryImpl(
    private val pushMessaging: PushMessaging,
    private val storage: NotificationStateStorage,
    private val debugPreferences: DebugPreferences? = null
) : NotificationRepository {

    private val log = logger()

    // Repository scope for background work
    private val repositoryScope = CoroutineScope(SupervisorJob())

    // Mutex to protect state mutations — prevents concurrent writes from corrupting persistence
    private val stateMutex = Mutex()

    // Single source of truth - all UI observes this
    // Start with isLoading=true to prevent flash of default values before persistence loads
    private val _state = MutableStateFlow(NotificationState.DEFAULT.copy(isLoading = true))
    override val state: StateFlow<NotificationState> = _state.asStateFlow()

    // Background subscription processor
    private val subscriptionProcessor = SubscriptionProcessor(
        pushMessaging = pushMessaging,
        debugPreferences = debugPreferences,
        coroutineScope = repositoryScope,
        debounceMs = 300L,
        onSubscriptionUpdate = { actualTopics ->
            // Route through mutex to prevent clobbering concurrent state updates
            updateState { it.copy(subscribedTopics = actualTopics) }
        }
    )

    override suspend fun initialize() {
        log.d { "NotificationRepository initializing..." }

        try {
            // Load persisted state
            val persistedState = storage.getState()

            // Protect state assignment with mutex to prevent race with concurrent updateState calls
            stateMutex.withLock {
                _state.value = persistedState
            }

            log.i { "Loaded notification state - notificationsEnabled: ${persistedState.enableNotifications}" }
            log.v { "Topic settings: ${persistedState.topicSettings}" }

            log.i { "notification_state_loaded agency_count=${persistedState.subscribedAgencies.size} location_count=${persistedState.subscribedLocations.size} enable_notifications=${persistedState.enableNotifications} follow_all=${persistedState.followAllLaunches}" }

            // Start background subscription processing
            subscriptionProcessor.requestUpdate(persistedState)

            log.i { "NotificationRepository initialized successfully" }
        } catch (e: Exception) {
            log.e(e) { "Failed to initialize NotificationRepository" }
            // On failure, clear loading state so UI isn't stuck on spinner
            _state.value = NotificationState.DEFAULT
        }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        updateState { currentState ->
            currentState.copy(enableNotifications = enabled)
        }
    }

    override suspend fun setFollowAllLaunches(enabled: Boolean) {
        val agencies = getAvailableAgencies()
        val locations = getAvailableLocations()

        updateState { currentState ->
            currentState.withFollowAllLaunches(enabled, agencies, locations)
        }
    }

    override suspend fun setUseStrictMatching(enabled: Boolean) {
        updateState { currentState ->
            currentState.copy(useStrictMatching = enabled)
        }
    }

    override suspend fun setTopicEnabled(topic: NotificationTopic, enabled: Boolean) {
        updateState { currentState ->
            currentState.withTopicEnabled(topic, enabled)
        }
    }

    override suspend fun setAgencyEnabled(agency: NotificationAgency, enabled: Boolean) {
        updateState { currentState ->
            currentState.withAgencyEnabled(agency, enabled)
        }
    }

    override suspend fun setAgencyEnabled(topicName: String, enabled: Boolean) {
        // Note: topicName parameter is kept for backward compatibility but now expects agency ID
        updateState { currentState ->
            currentState.withAgencyEnabled(topicName, enabled)
        }
    }

    override suspend fun setLocationEnabled(location: NotificationLocation, enabled: Boolean) {
        updateState { currentState ->
            currentState.withLocationEnabled(location, enabled)
        }
    }

    override suspend fun setLocationEnabled(topicName: String, enabled: Boolean) {
        // Note: topicName parameter is kept for backward compatibility but now expects location ID
        updateState { currentState ->
            currentState.withLocationEnabled(topicName, enabled)
        }
    }

    override suspend fun getAvailableAgencies(): List<NotificationAgency> {
        return NotificationAgency.getAll()
    }

    override suspend fun getAvailableLocations(): List<NotificationLocation> {
        return NotificationLocation.getAll()
    }

    override suspend fun requestNotificationPermission(): Boolean {
        return requestPlatformNotificationPermission()
    }

    override suspend fun hasNotificationPermission(): Boolean {
        return hasPlatformNotificationPermission()
    }

    /**
     * Core state update method — persist-first pattern:
     * 1. Lock mutex to prevent concurrent writes
     * 2. Compute new state from current state
     * 3. Persist to disk FIRST
     * 4. Only update in-memory state if persistence succeeded
     * 5. On failure: keep old state, set error
     */
    private suspend fun updateState(update: (NotificationState) -> NotificationState) {
        stateMutex.withLock {
            try {
                log.d { "Notification state update requested" }

                val oldState = _state.value
                val newState = update(oldState)

                log.i { "State updated - agencies: ${newState.subscribedAgencies.size}, locations: ${newState.subscribedLocations.size}" }
                log.v { "Subscribed agencies: ${newState.subscribedAgencies}" }
                log.v { "Subscribed locations: ${newState.subscribedLocations}" }

                // Persist to storage FIRST — only update in-memory state on success
                val result = storage.saveState(newState)
                if (result.isSuccess) {
                    _state.value = newState
                    log.d { "Notification state persisted and applied" }
                } else {
                    log.e(result.exceptionOrNull()) { "Failed to persist notification state — keeping old state" }
                    _state.value = oldState.withError(result.exceptionOrNull()?.message)
                    return
                }

                // Trigger background FCM subscription updates (debounced)
                log.d { "Triggering SubscriptionProcessor update..." }
                subscriptionProcessor.requestUpdate(newState)

            } catch (e: Exception) {
                log.e(e) { "Notification state update failed" }
                _state.value = _state.value.withError(e.message)
            }
        }
    }
}