package me.calebjones.spacelaunchnow.analytics.core

import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent

/**
 * Central analytics dispatcher.
 * Fan-outs all events to every registered and enabled AnalyticsProvider.
 *
 * Injected via Koin as a singleton. ViewModels and repositories should depend
 * on this interface, never on individual providers.
 */
interface AnalyticsManager {

    /** Track a typed analytics event */
    fun track(event: AnalyticsEvent)

    /** Track a screen view (called automatically by navigation tracker) */
    fun trackScreenView(screenName: String, screenClass: String? = null)

    /** Set user identity across all providers */
    fun setUserId(userId: String?)

    /** Set a user property across all providers */
    fun setUserProperty(key: String, value: String)

    /** Force flush all providers */
    suspend fun flush()

    /** Reset all providers (e.g., on logout) */
    fun reset()

    /** Toggle a specific provider by name */
    fun enableProvider(name: String, enabled: Boolean)
}
