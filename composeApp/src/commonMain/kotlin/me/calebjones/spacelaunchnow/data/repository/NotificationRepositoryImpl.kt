package me.calebjones.spacelaunchnow.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import me.calebjones.spacelaunchnow.PlatformType
import me.calebjones.spacelaunchnow.data.model.NotificationAgency
import me.calebjones.spacelaunchnow.data.model.NotificationLocation
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.notifications.PushMessaging
import me.calebjones.spacelaunchnow.data.notifications.v6.PushTopicMessaging
import me.calebjones.spacelaunchnow.data.notifications.v6.TopicSubscriptionStore
import me.calebjones.spacelaunchnow.data.notifications.v6.V6ReconcileResult
import me.calebjones.spacelaunchnow.data.notifications.v6.V6SubscriptionReconciler
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.util.logging.PushDiagnostics
import me.calebjones.spacelaunchnow.util.logging.logger

expect suspend fun requestPlatformNotificationPermission(): Boolean
expect suspend fun hasPlatformNotificationPermission(): Boolean
expect fun openPlatformNotificationSettings(): Boolean

class NotificationRepositoryImpl(
    private val pushMessaging: PushMessaging,
    private val storage: NotificationStateStorage,
    private val topicSubscriptionStore: TopicSubscriptionStore,
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

    private val reconciler = V6SubscriptionReconciler(
        store = topicSubscriptionStore,
        messaging = PushTopicMessaging(pushMessaging),
        platform = when (getPlatform().type) {
            PlatformType.ANDROID -> "android"
            PlatformType.IOS -> "ios"
            PlatformType.DESKTOP -> null   // desktop is a no-op, as today
        },
        envProvider = { if (useDebugTopics()) "debug" else "prod" },
        stateProvider = { storage.getState() },
        markChangeoverComplete = {
            // Persist from storage, not _state: this can run (via onNewToken's
            // reconcile) before initialize() has loaded persisted state, when
            // _state is still DEFAULT -- basing the write on _state would
            // persist a settings wipe. updateState is wrong here for the same
            // reason.
            stateMutex.withLock {
                val persisted = storage.getState()
                val result = storage.saveState(persisted.copy(hasCompletedV6Changeover = true))
                if (result.isSuccess && !_state.value.isLoading) {
                    _state.value = _state.value.copy(hasCompletedV6Changeover = true)
                }
            }
        },
        nowMillis = { Clock.System.now().toEpochMilliseconds() },
    )

    // Same env decision the V5 path used: debug topics only in debug builds,
    // and only when the debug setting asks for them.
    private suspend fun useDebugTopics(): Boolean {
        if (!BuildConfig.IS_DEBUG || debugPreferences == null) return false
        return try {
            debugPreferences.getDebugSettings().useDebugTopics
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun reconcileSubscriptions(): V6ReconcileResult = withContext(Dispatchers.Default) {
        val result = reconciler.reconcile()
        recordReconcileDiagnostics(result)
        result
    }

    override suspend fun forceResubscribe(): V6ReconcileResult = withContext(Dispatchers.Default) {
        val result = reconciler.forceResubscribe()
        recordReconcileDiagnostics(result)
        result
    }

    private fun recordReconcileDiagnostics(result: V6ReconcileResult) {
        if (result.skipped) return
        PushDiagnostics.recordSubscribedTopicCount(topicSubscriptionStore.counts().confirmed.toInt())
        if (result.clean) PushDiagnostics.recordCleanReconcile()
    }

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

            // App-start reconcile: the retry path for anything that failed at
            // save time, and the iOS token-refresh cover. Detached: it must
            // neither gate cold start behind up to ~40 sequential FCM calls nor
            // let a reconcile failure reach the catch below, which would reset
            // loaded state to DEFAULT and hand the next toggle a wiped state to
            // persist.
            repositoryScope.launch {
                runCatching { reconcileSubscriptions() }
                    .onFailure { log.e(it) { "App-start V6 reconcile failed; next start or save retries" } }
            }

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
        // The kill switch must not wait for Save or next app start: under V6
        // the unsubscribe IS the switch-off (and the resubscribe the switch-on).
        reconcileSubscriptions()
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

            } catch (e: Exception) {
                log.e(e) { "Notification state update failed" }
                _state.value = _state.value.withError(e.message)
            }
        }
    }
}