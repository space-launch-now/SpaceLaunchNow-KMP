package me.calebjones.spacelaunchnow.data.notifications

import kotlinx.coroutines.runBlocking
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import co.touchlab.kermit.Severity
import me.calebjones.spacelaunchnow.util.logging.DiagnosticEventKind
import me.calebjones.spacelaunchnow.util.logging.DiagnosticEvents
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Bridge for iOS (Swift) to access notification filtering logic from shared Kotlin code.
 * 
 * Swift AppDelegate calls these methods to filter notifications using the same
 * logic as Android, ensuring consistent behavior across platforms.
 * 
 * Usage from Swift:
 * ```swift
 * import ComposeApp
 * 
 * // Check if notification should be shown
 * let dataDict: [String: String] = extractData(from: userInfo)
 * let shouldShow = IosNotificationBridge.shared.shouldShowNotification(data: dataDict)
 * ```
 */
object IosNotificationBridge : KoinComponent {
    private val log = SpaceLogger.getLogger("IosNotificationBridge")
    
    private val notificationStateStorage: NotificationStateStorage by inject()
    
    // Cache for notification state to avoid blocking on every notification
    private var cachedState: NotificationState? = null
    
    /**
     * Check if a notification should be shown based on user preferences.
     * 
     * This uses the shared NotificationFilter logic from commonMain,
     * ensuring iOS and Android have identical filtering behavior.
     * 
     * @param data Map of notification data (from FCM userInfo)
     * @return true if notification should be displayed, false if it should be suppressed
     */
    fun shouldShowNotification(data: Map<String, String>): Boolean {
        val state = getCachedOrFetchState()

        // Once this device's V5->V6 changeover has completed it only receives server-targeted
        // sends, and the legacy filter is skipped (kill switch only). Until then it is still on
        // the V5 broadcast and must filter.
        val passthrough = !LocalFilterPolicy.isActive(state)
        val result = LocalFilterPolicy.legacyLaunchAllowedFromMap(data, state)
        FilterDecisionEvent.emit(data, state, shown = result, passthrough = passthrough)
        return result
    }
    
    /**
     * Emit ONE structured startup event comparing live Kotlin filter state with the
     * App Group prefs the NSE reads when the app is killed. Replaces the previous
     * ~15-line prose dump. Queryable in Datadog via log_kind:startup_state.
     * DatadogLogger buffers under PENDING consent and no-ops when uninitialized,
     * so this is always safe to call.
     */
    fun logStartupState() {
        runBlocking {
            try {
                val state = notificationStateStorage.getState()
                val nse = NSEPreferenceBridge.readStoredPrefs()
                // Drain first so the count rides on the startup_state event.
                val drainedCount = NSEPreferenceBridge.drainNseEventLog()
                // Sentinel conventions: missing NSE keys -> null for booleans (nse_keys_missing covers querying), -1 for counts (keeps the facet numeric).
                // DiagnosticEvents adds log_kind, platform, tag and UserContext attributes.
                val attributes = mapOf(
                    "live_enable_notifications" to state.enableNotifications,
                    "live_follow_all" to state.followAllLaunches,
                    "live_strict" to state.useStrictMatching,
                    "live_agency_count" to state.subscribedAgencies.size,
                    "live_location_count" to state.subscribedLocations.size,
                    "live_v6_changeover_complete" to state.hasCompletedV6Changeover,
                    "nse_app_group_available" to nse.appGroupAvailable,
                    "nse_keys_missing" to nse.anyKeyMissing,
                    "nse_v6_changeover_complete" to nse.v6ChangeoverComplete,
                    "nse_enable_notifications" to nse.enableNotifications,
                    "nse_follow_all" to nse.followAllLaunches,
                    "nse_strict" to nse.useStrictMatching,
                    "nse_agency_count" to (nse.subscribedAgencies?.size ?: -1),
                    "nse_location_count" to (nse.subscribedLocations?.size ?: -1),
                    "drained_count" to drainedCount,
                )
                DiagnosticEvents.emit(
                    kind = DiagnosticEventKind.STARTUP_STATE,
                    severity = Severity.Info,
                    tag = "SLN-IosNotificationBridge",
                    attrs = attributes,
                )
            } catch (e: Exception) {
                log.e(e) { "Failed to emit startup state" }
            }
        }
    }

    /**
     * Drain the NSE breadcrumb buffer into Datadog. Safe to call on every app foreground
     * (applicationDidBecomeActive) as well as at startup — it clears the buffer after draining,
     * so repeat calls are cheap no-ops when empty. Exposed for Swift (AppDelegate) to call.
     */
    fun drainNseEventLog() {
        try {
            NSEPreferenceBridge.drainNseEventLog()
        } catch (e: Exception) {
            log.e(e) { "Failed to drain NSE event log" }
        }
    }

    /**
     * Get the current notification state, using cache if available.
     * This is called synchronously from the notification handler, so we use
     * a cached value when possible and refresh it asynchronously.
     */
    private fun getCachedOrFetchState(): NotificationState {
        // If we have a cached state, use it (will be refreshed on next call)
        cachedState?.let { 
            log.v { "Using cached notification state" }
            return it 
        }
        
        // First call or cache cleared — fetch synchronously and sync NSE bridge
        log.d { "Fetching notification state synchronously" }
        return runBlocking {
            try {
                val state = notificationStateStorage.getState()
                cachedState = state
                NSEPreferenceBridge.syncToUserDefaults(state)
                state
            } catch (e: Exception) {
                log.e { "Failed to get notification state: ${e.message}" }
                // Return default state on error
                NotificationState.DEFAULT
            }
        }
    }
    
    /**
     * Refresh the cached notification state.
     * Call this when notification settings change to ensure the filter
     * uses the latest user preferences.
     * Also syncs current state to NSE UserDefaults bridge so the NSE can filter
     * independently when the app is killed.
     */
    fun refreshState() {
        log.d { "Refreshing notification state cache" }
        runBlocking {
            try {
                val newState = notificationStateStorage.getState()
                cachedState = newState
                NSEPreferenceBridge.syncToUserDefaults(newState)
            } catch (e: Exception) {
                log.e { "Failed to refresh notification state: ${e.message}" }
            }
        }
    }
    
    /**
     * Keep the in-app filter cache in step with what NotificationStateStorage just persisted
     * (called from NSEBridgeHook on every saveState). Without this a settings change, or the
     * V5->V6 changeover completing mid-session, would not reach willPresent /
     * didReceiveRemoteNotification until the next launch's refreshState() -- the NSE reads the
     * App Group fresh per push, so the two paths could disagree for a whole session.
     */
    fun onStateSaved(state: NotificationState) {
        cachedState = state
    }

    /**
     * Clear the cached state.
     * Useful when app enters foreground or settings change.
     */
    fun clearCache() {
        log.d { "Clearing notification state cache" }
        cachedState = null
    }
}
