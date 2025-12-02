package me.calebjones.spacelaunchnow.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

class NotificationRepositoryImpl(
    private val pushMessaging: PushMessaging,
    private val storage: NotificationStateStorage,
    private val debugPreferences: DebugPreferences? = null
) : NotificationRepository {

    private val log = logger()

    // Repository scope for background work
    private val repositoryScope = CoroutineScope(SupervisorJob())

    // Single source of truth - all UI observes this
    private val _state = MutableStateFlow(NotificationState.DEFAULT)
    override val state: StateFlow<NotificationState> = _state.asStateFlow()

    // Background subscription processor
    private val subscriptionProcessor = SubscriptionProcessor(
        pushMessaging = pushMessaging,
        debugPreferences = debugPreferences,
        coroutineScope = repositoryScope,
        debounceMs = 300L,
        onSubscriptionUpdate = { actualTopics ->
            // Update state with actual subscribed topics from FCM
            _state.value = _state.value.copy(subscribedTopics = actualTopics)
        }
    )

    override suspend fun initialize() {
        log.d { "NotificationRepository initializing..." }

        try {
            // Load persisted state
            val persistedState = storage.getState()
            _state.value = persistedState

            log.i { "Loaded notification state - notificationsEnabled: ${persistedState.enableNotifications}" }
            log.v { "Topic settings: ${persistedState.topicSettings}" }

            // Start background subscription processing
            subscriptionProcessor.requestUpdate(persistedState)

            log.i { "NotificationRepository initialized successfully" }
        } catch (e: Exception) {
            log.e(e) { "Failed to initialize NotificationRepository" }
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
     * Core state update method - handles immediate state update, persistence, and background work
     */
    private suspend fun updateState(update: (NotificationState) -> NotificationState) {
        try {
            log.d { "Notification state update requested" }

            // 1. Update state immediately (instant UI feedback)
            val oldState = _state.value
            val newState = update(oldState)
            _state.value = newState

            log.i { "State updated - agencies: ${newState.subscribedAgencies.size}, locations: ${newState.subscribedLocations.size}" }
            log.v { "Subscribed agencies: ${newState.subscribedAgencies}" }
            log.v { "Subscribed locations: ${newState.subscribedLocations}" }

            // 2. Persist to storage (background)
            repositoryScope.launch {
                try {
                    storage.saveState(newState)
                    log.d { "Notification state persisted to storage" }
                } catch (e: Exception) {
                    log.e(e) { "Failed to persist notification state" }
                }
            }

            // 3. Trigger background FCM subscription updates (debounced)
            log.d { "Triggering SubscriptionProcessor update..." }
            subscriptionProcessor.requestUpdate(newState)

        } catch (e: Exception) {
            log.e(e) { "Notification state update failed" }
            _state.value = _state.value.withError(e.message)
        }
    }
}