package me.calebjones.spacelaunchnow.analytics.core

import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent

/**
 * Interface that all analytics platform providers must implement.
 * Each provider wraps a specific analytics SDK (Firebase, Amplitude, etc.).
 *
 * Implementations:
 * - FirebaseAnalyticsProvider (androidMain + iosMain)
 * - ConsoleAnalyticsProvider (all platforms, primarily desktop/debug)
 */
interface AnalyticsProvider {

    /** Unique identifier for this provider (e.g., "firebase", "console") */
    val name: String

    /** Whether this provider is currently enabled and should receive events */
    var isEnabled: Boolean

    /** Initialize the provider with platform-specific configuration */
    suspend fun initialize(config: Map<String, Any> = emptyMap())

    /** Track a typed analytics event */
    suspend fun trackEvent(event: AnalyticsEvent)

    /** Track a screen view */
    suspend fun trackScreenView(screenName: String, screenClass: String? = null)

    /** Set the user identity across the analytics platform */
    fun setUserId(userId: String?)

    /** Set a user property/attribute */
    fun setUserProperty(key: String, value: String)

    /** Force flush any queued events to the backend */
    suspend fun flush()

    /** Clear user data and reset state (e.g., on logout) */
    fun reset()
}
