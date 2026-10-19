package me.calebjones.spacelaunchnow.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Clock
import me.calebjones.spacelaunchnow.PlatformType
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.data.model.NotificationAgency
import me.calebjones.spacelaunchnow.data.model.NotificationLocation
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.notifications.PushMessaging
import me.calebjones.spacelaunchnow.data.notifications.v6.AutoReconcileTrigger
import me.calebjones.spacelaunchnow.data.notifications.v6.PushReconcileEvent
import me.calebjones.spacelaunchnow.data.notifications.v6.PushTopicMessaging
import me.calebjones.spacelaunchnow.data.notifications.v6.ReconcileTrigger
import me.calebjones.spacelaunchnow.data.notifications.v6.SubscriptionCounts
import me.calebjones.spacelaunchnow.data.notifications.v6.TopicSubscriptionStore
import me.calebjones.spacelaunchnow.data.notifications.v6.V6ReconcileResult
import me.calebjones.spacelaunchnow.data.notifications.v6.V6SubscriptionReconciler
import me.calebjones.spacelaunchnow.data.notifications.v6.V6Topics
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.util.logging.PushDiagnostics
import me.calebjones.spacelaunchnow.util.logging.PushReconcileState
import me.calebjones.spacelaunchnow.util.logging.logger

expect suspend fun requestPlatformNotificationPermission(): Boolean
expect suspend fun hasPlatformNotificationPermission(): Boolean
expect suspend fun getPlatformNotificationPermissionStatus(): NotificationPermissionStatus
expect fun openPlatformNotificationSettings(): Boolean

internal const val PUSH_SUMMARY_FALLBACK_MS = 15_000L

class NotificationRepositoryImpl(
    private val pushMessaging: PushMessaging,
    private val storage: NotificationStateStorage,
    private val topicSubscriptionStore: TopicSubscriptionStore,
    private val debugPreferences: DebugPreferences? = null,
    private val analyticsManager: AnalyticsManager? = null,
    // Seams for tests; production keeps the defaults.
    private val repositoryScope: CoroutineScope = CoroutineScope(SupervisorJob()),
    private val workDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val platformName: String? = when (getPlatform().type) {
        PlatformType.ANDROID -> "android"
        PlatformType.IOS -> "ios"
        PlatformType.DESKTOP -> null   // desktop is a no-op, as today
    },
    private val summaryFallbackMs: Long = PUSH_SUMMARY_FALLBACK_MS,
) : NotificationRepository {

    private val log = logger()

    // push_summary is emitted at most once per initialize(): by the first completed app_start
    // reconcile, or by the fallback timer if none completes in time.
    private val summaryMutex = Mutex()
    private var summaryEmitted = false
    private var summaryFallbackJob: Job? = null

    // Mutex to protect state mutations — prevents concurrent writes from corrupting persistence
    private val stateMutex = Mutex()

    // Single source of truth - all UI observes this
    // Start with isLoading=true to prevent flash of default values before persistence loads
    private val _state = MutableStateFlow(NotificationState.DEFAULT.copy(isLoading = true))
    override val state: StateFlow<NotificationState> = _state.asStateFlow()

    private val reconciler = V6SubscriptionReconciler(
        store = topicSubscriptionStore,
        messaging = PushTopicMessaging(pushMessaging),
        platform = platformName,
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
                if (result.isSuccess) {
                    // Once per install, only after the flag is durably persisted
                    // (an unpersisted changeover re-runs, and must be allowed to
                    // re-fire this rather than under-count stuck devices).
                    analyticsManager?.track(
                        AnalyticsEvent.V6ChangeoverCompleted(V6Topics.audienceClass(persisted))
                    )
                    if (!_state.value.isLoading) {
                        _state.value = _state.value.copy(hasCompletedV6Changeover = true)
                    }
                }
            }
        },
        nowMillis = { Clock.System.now().toEpochMilliseconds() },
    )

    // Filter mutations apply immediately: every setter queues a debounced reconcile so
    // FCM subscriptions follow settings without an explicit save step. The debounce
    // collapses a burst of toggles (a class switch rewrites ~20 topics) into one pass;
    // a failed pass is retried by the app-start reconcile.
    private val autoReconcile = AutoReconcileTrigger(
        scope = repositoryScope,
        debounceMs = 750L
    ) {
        runCatching { reconcileSubscriptions() }
            .onFailure { log.e(it) { "Auto reconcile failed; app-start reconcile retries" } }
    }

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

    override suspend fun reconcileSubscriptions(trigger: ReconcileTrigger): V6ReconcileResult =
        withContext(workDispatcher) {
            val result = reconciler.reconcile()
            recordAndEmitReconcile(result, trigger)
            result
        }

    override suspend fun forceResubscribe(): V6ReconcileResult = withContext(workDispatcher) {
        val result = reconciler.forceResubscribe()
        recordAndEmitReconcile(result, ReconcileTrigger.FORCE)
        result
    }

    private suspend fun recordAndEmitReconcile(result: V6ReconcileResult, trigger: ReconcileTrigger) {
        if (result.skipped) return
        val confirmed = topicSubscriptionStore.counts().confirmed.toInt()
        PushDiagnostics.recordSubscribedTopicCount(confirmed)
        if (result.clean) PushDiagnostics.recordCleanReconcile()
        val changeoverComplete = runCatching { storage.getState().hasCompletedV6Changeover }.getOrDefault(false)
        val stateAttrs = pushStateAttributes(PushReconcileState.COMPLETE)
        PushReconcileEvent.emit(result, trigger, changeoverComplete, confirmed, stateAttrs)
        if (trigger == ReconcileTrigger.APP_START) {
            // Detached: the summary waits for the startup records and must not delay the caller.
            repositoryScope.launch {
                val ready = PushDiagnostics.startupRecordsReady ||
                    withTimeoutOrNull(summaryFallbackMs) { PushDiagnostics.awaitStartupRecords(); true } == true
                // Not ready in time: the fallback emits with reconcile_state=pending.
                if (ready) {
                    summaryFallbackJob?.cancel()
                    emitSummaryOnce(PushReconcileState.COMPLETE)
                }
            }
        }
    }

    /**
     * Spec S3 D9 attributes, read from persisted state and the ledger at call time, never from
     * the in-memory [_state] (still DEFAULT when a token-refresh reconcile runs before initialize()).
     * Also stored on the diagnostics snapshot so the shareable report shows the same rows.
     */
    private suspend fun pushStateAttributes(reconcileState: PushReconcileState): Map<String, Any?> {
        val persisted = runCatching { storage.getState() }.getOrNull() ?: NotificationState.DEFAULT
        val counts = runCatching { topicSubscriptionStore.counts() }.getOrNull()
            ?: SubscriptionCounts(0, 0, 0)
        val attrs = PushDiagnostics.stateAttributes(persisted, counts, persisted.hasCompletedV6Changeover)
        PushDiagnostics.recordPushState(attrs)
        return attrs + ("push.reconcile_state" to reconcileState.wire)
    }

    private suspend fun emitSummaryOnce(reconcileState: PushReconcileState) {
        val first = summaryMutex.withLock {
            if (summaryEmitted) false else { summaryEmitted = true; true }
        }
        if (!first) return
        val attrs = pushStateAttributes(reconcileState)
        PushDiagnostics.logSummary(PushDiagnostics.snapshot.diagnosticLevelName, attrs)
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

            summaryFallbackJob?.cancel()
            summaryMutex.withLock { summaryEmitted = false }
            summaryFallbackJob = repositoryScope.launch {
                delay(summaryFallbackMs)
                emitSummaryOnce(PushReconcileState.PENDING)
            }

            log.i { "notification_state_loaded agency_count=${persistedState.subscribedAgencies.size} location_count=${persistedState.subscribedLocations.size} enable_notifications=${persistedState.enableNotifications} follow_all=${persistedState.followAllLaunches}" }

            // App-start reconcile: the retry path for anything that failed at
            // save time, and the iOS token-refresh cover. Detached: it must
            // neither gate cold start behind up to ~40 sequential FCM calls nor
            // let a reconcile failure reach the catch below, which would reset
            // loaded state to DEFAULT and hand the next toggle a wiped state to
            // persist.
            repositoryScope.launch {
                runCatching { reconcileSubscriptions(ReconcileTrigger.APP_START) }
                    .onFailure { log.e(it) { "App-start V6 reconcile failed; next start or save retries" } }
            }
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
        reconcileSubscriptions(ReconcileTrigger.KILL_SWITCH)
    }

    override suspend fun setFollowAllLaunches(enabled: Boolean) {
        val agencies = getAvailableAgencies()
        val locations = getAvailableLocations()

        updateState { currentState ->
            currentState.withFollowAllLaunches(enabled, agencies, locations)
        }
        autoReconcile.request()
    }

    override suspend fun setUseStrictMatching(enabled: Boolean) {
        updateState { currentState ->
            currentState.copy(useStrictMatching = enabled)
        }
        autoReconcile.request()
    }

    override suspend fun setTopicEnabled(topic: NotificationTopic, enabled: Boolean) {
        updateState { currentState ->
            currentState.withTopicEnabled(topic, enabled)
        }
        autoReconcile.request()
    }

    override suspend fun setMuteStarlink(enabled: Boolean) {
        updateState { currentState ->
            currentState.copy(muteStarlink = enabled)
        }
        autoReconcile.request()
    }

    override suspend fun setAgencyEnabled(agency: NotificationAgency, enabled: Boolean) {
        updateState { currentState ->
            currentState.withAgencyEnabled(agency, enabled)
        }
        autoReconcile.request()
    }

    override suspend fun setAgencyEnabled(topicName: String, enabled: Boolean) {
        // Note: topicName parameter is kept for backward compatibility but now expects agency ID
        updateState { currentState ->
            currentState.withAgencyEnabled(topicName, enabled)
        }
        autoReconcile.request()
    }

    override suspend fun setLocationEnabled(location: NotificationLocation, enabled: Boolean) {
        updateState { currentState ->
            currentState.withLocationEnabled(location, enabled)
        }
        autoReconcile.request()
    }

    override suspend fun setLocationEnabled(topicName: String, enabled: Boolean) {
        // Note: topicName parameter is kept for backward compatibility but now expects location ID
        updateState { currentState ->
            currentState.withLocationEnabled(topicName, enabled)
        }
        autoReconcile.request()
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
                val oldState = _state.value
                val newState = update(oldState)

                // Persist to storage FIRST — only update in-memory state on success
                val result = storage.saveState(newState)
                if (result.isSuccess) {
                    _state.value = newState
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