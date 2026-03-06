package me.calebjones.spacelaunchnow.widgets

import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import kotlin.time.Clock

/**
 * Cache data transferred to the iOS widget extension via App Group NSUserDefaults.
 * Contains all information needed for the widget to determine premium access
 * without the main app process running.
 */
data class WidgetAccessCache(
    /** Current premium access status (verified on last sync) */
    val hasAccess: Boolean = false,

    /** Unix timestamp (milliseconds) when subscription expires. Null for lifetime or unknown. */
    val subscriptionExpiryMs: Long? = null,

    /** Unix timestamp (milliseconds) when this cache was last updated */
    val lastVerifiedMs: Long = 0L,

    /** Whether this user has ever had a valid premium subscription. Sticky — never reverts to false. */
    val wasEverPremium: Boolean = false,

    /** Current subscription tier */
    val subscriptionType: SubscriptionType = SubscriptionType.FREE
) {
    /**
     * Fail-safe access determination. Defaults to unlocked for users who were ever premium
     * to prevent false locks caused by timing or network issues during app startup.
     *
     * Logic:
     * - Explicit access → UNLOCKED
     * - Never been premium → LOCKED
     * - Was premium + expiry in future → UNLOCKED
     * - Was premium + expiry passed → LOCKED
     * - Was premium + no expiry (lifetime or data gap) → UNLOCKED
     */
    fun shouldShowUnlocked(): Boolean {
        if (hasAccess) return true
        if (!wasEverPremium) return false

        val currentTimeMs = Clock.System.now().toEpochMilliseconds()
        return if (subscriptionExpiryMs != null) {
            subscriptionExpiryMs > currentTimeMs
        } else {
            // Lifetime subscription or expiry data not yet available — default to unlocked
            true
        }
    }

    companion object {
        /** Consider cache stale after 7 days without a verified sync */
        const val FRESHNESS_THRESHOLD_MS = 7L * 24 * 60 * 60 * 1000

        val EMPTY = WidgetAccessCache()
    }
}
