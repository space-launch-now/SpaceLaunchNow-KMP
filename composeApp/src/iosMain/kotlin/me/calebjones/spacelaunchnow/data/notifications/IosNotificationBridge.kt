package me.calebjones.spacelaunchnow.data.notifications

import kotlinx.coroutines.runBlocking
import me.calebjones.spacelaunchnow.analytics.DatadogLogger
import me.calebjones.spacelaunchnow.data.model.NotificationFilter
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import me.calebjones.spacelaunchnow.util.logging.UserContext
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
        log.i { "========================================" }
        log.i { "🔍 [KOTLIN] EVALUATING NOTIFICATION FILTER" }
        log.i { "========================================" }
        
        // Get current notification state (use cache if available, refresh in background)
        val state = getCachedOrFetchState()
        
        // Print current notification settings
        log.i { "⚙️ [KOTLIN] Current Notification State:" }
        log.i { "   - Notifications Enabled: ${state.enableNotifications}" }
        log.i { "   - Follow All Launches: ${state.followAllLaunches}" }
        log.i { "   - Use Strict Matching: ${state.useStrictMatching}" }
        log.i { "   - Hide TBD Launches: ${state.hideTbdLaunches}" }
        log.i { "" }
        log.i { "⏰ [KOTLIN] Timing Settings (Topics):" }
        state.topicSettings.forEach { (topic, enabled) ->
            log.i { "   - $topic: ${if (enabled) "✅" else "❌"}" }
        }
        log.i { "" }
        log.i { "🚀 [KOTLIN] Agency Filter:" }
        if (state.subscribedAgencies.isEmpty()) {
            log.i { "   - No agencies subscribed (will block all)" }
        } else {
            log.i { "   - ${state.subscribedAgencies.size} agencies subscribed: ${state.subscribedAgencies.take(10)}${if (state.subscribedAgencies.size > 10) "..." else ""}" }
        }
        log.i { "" }
        log.i { "📍 [KOTLIN] Location Filter:" }
        if (state.subscribedLocations.isEmpty()) {
            log.i { "   - No locations subscribed (will block all)" }
        } else {
            log.i { "   - ${state.subscribedLocations.size} locations subscribed: ${state.subscribedLocations.take(10)}${if (state.subscribedLocations.size > 10) "..." else ""}" }
        }
        log.i { "" }
        
        // Print notification data being evaluated
        log.i { "📩 [KOTLIN] Evaluating Notification:" }
        log.i { "   - Type: ${data["notification_type"]}" }
        log.i { "   - Launch: ${data["launch_name"]}" }
        log.i { "   - Agency ID: ${data["agency_id"]}" }
        log.i { "   - Location ID: ${data["location_id"]}" }
        log.i { "   - Webcast: ${data["webcast"]}" }
        log.i { "   - Webcast Live: ${data["webcast_live"]}" }
        log.i { "" }
        
        // Use shared filter logic
        log.i { "🔄 [KOTLIN] Running shared NotificationFilter logic..." }
        val result = NotificationFilter.shouldShowFromMap(data, state)
        
        log.i { "" }
        log.i { "🎯 [KOTLIN] Filter Decision: ${if (result) "SHOW ✅" else "SUPPRESS 🔇"}" }
        log.i { "========================================" }
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
                // Sentinel conventions: missing NSE keys -> null for booleans (nse_keys_missing covers querying), -1 for counts (keeps the facet numeric).
                val attributes = UserContext.getLogAttributes() + mapOf(
                    "log_kind" to "startup_state",
                    "platform" to "ios",
                    "live_enable_notifications" to state.enableNotifications,
                    "live_follow_all" to state.followAllLaunches,
                    "live_strict" to state.useStrictMatching,
                    "live_agency_count" to state.subscribedAgencies.size,
                    "live_location_count" to state.subscribedLocations.size,
                    "nse_app_group_available" to nse.appGroupAvailable,
                    "nse_keys_missing" to nse.anyKeyMissing,
                    "nse_enable_notifications" to nse.enableNotifications,
                    "nse_follow_all" to nse.followAllLaunches,
                    "nse_strict" to nse.useStrictMatching,
                    "nse_agency_count" to (nse.subscribedAgencies?.size ?: -1),
                    "nse_location_count" to (nse.subscribedLocations?.size ?: -1),
                )
                DatadogLogger.info("[STARTUP] notification_state", attributes)
                log.i {
                    "startup_state live(followAll=${state.followAllLaunches}, strict=${state.useStrictMatching}, " +
                        "agencies=${state.subscribedAgencies.size}, locations=${state.subscribedLocations.size}) " +
                        "nse(keysMissing=${nse.anyKeyMissing})"
                }
            } catch (e: Exception) {
                log.e { "Failed to emit startup state: ${e.message}" }
            }
        }
        // Drain NSE breadcrumbs (no-op preserve when remote logging inactive).
        NSEPreferenceBridge.drainNseEventLog()
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
            log.e { "Failed to drain NSE event log: ${e.message}" }
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
     * Clear the cached state.
     * Useful when app enters foreground or settings change.
     */
    fun clearCache() {
        log.d { "Clearing notification state cache" }
        cachedState = null
    }
}
