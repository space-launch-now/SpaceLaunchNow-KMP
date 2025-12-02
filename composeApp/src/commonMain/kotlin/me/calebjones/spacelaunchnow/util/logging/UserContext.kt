package me.calebjones.spacelaunchnow.util.logging

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.calebjones.spacelaunchnow.analytics.DatadogRUM

/**
 * Global user context for logging
 * Automatically attaches user information to all DataDog logs
 */
object UserContext {
    private val _revenueCatUserId = MutableStateFlow<String?>(null)
    val revenueCatUserId: StateFlow<String?> = _revenueCatUserId.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    /**
     * Set RevenueCat user ID (call when customer info updates)
     */
    fun setRevenueCatUserId(userId: String?) {
        _revenueCatUserId.value = userId

        // Update DataDog user context
        if (userId != null) {
            DatadogRUM.setUser(
                id = userId,
                extraInfo = buildMap {
                    put("rc_user_id", userId)
                    if (_isPremium.value) {
                        put("subscription_status", "premium")
                    }
                }
            )
        }
    }

    /**
     * Set premium status
     */
    fun setPremiumStatus(isPremium: Boolean) {
        _isPremium.value = isPremium

        // Update DataDog with new status
        _revenueCatUserId.value?.let { userId ->
            setRevenueCatUserId(userId) // Refresh user context
        }
    }

    /**
     * Clear user context (on logout/reset)
     */
    fun clear() {
        _revenueCatUserId.value = null
        _isPremium.value = false
    }

    /**
     * Get common log attributes to attach to every log
     */
    fun getLogAttributes(): Map<String, Any?> = buildMap {
        _revenueCatUserId.value?.let { put("rc_user_id", it) }
        put("is_premium", _isPremium.value)
    }
}

