package me.calebjones.spacelaunchnow.data.notifications

import kotlinx.coroutines.runBlocking
import me.calebjones.spacelaunchnow.data.model.NotificationFilter
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
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
        log.d { "Evaluating notification filter from iOS..." }
        
        // Get current notification state (use cache if available, refresh in background)
        val state = getCachedOrFetchState()
        
        // Use shared filter logic
        val result = NotificationFilter.shouldShowFromMap(data, state)
        
        log.d { "Filter result: ${if (result) "SHOW" else "SUPPRESS"}" }
        return result
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
        
        // First call or cache cleared - need to fetch synchronously
        log.d { "Fetching notification state synchronously" }
        return runBlocking {
            try {
                val state = notificationStateStorage.getState()
                cachedState = state
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
     */
    fun refreshState() {
        log.d { "Refreshing notification state cache" }
        runBlocking {
            try {
                cachedState = notificationStateStorage.getState()
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
