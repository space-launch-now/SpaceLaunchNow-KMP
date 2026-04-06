package me.calebjones.spacelaunchnow.analytics

import me.calebjones.spacelaunchnow.analytics.core.AnalyticsProvider
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent

/**
 * Test double for [AnalyticsProvider].
 * Records all calls so tests can assert on what was tracked.
 */
class FakeAnalyticsProvider(
    override val name: String = "fake",
    override var isEnabled: Boolean = true,
    private val throwOnTrack: Boolean = false
) : AnalyticsProvider {

    val trackedEvents = mutableListOf<AnalyticsEvent>()
    val trackedScreens = mutableListOf<Pair<String, String?>>()
    var lastUserId: String? = null
    val userProperties = mutableMapOf<String, String>()
    var flushed = false
    var reset = false
    var initialized = false

    override suspend fun initialize(config: Map<String, Any>) {
        initialized = true
    }

    override suspend fun trackEvent(event: AnalyticsEvent) {
        if (throwOnTrack) throw RuntimeException("FakeAnalyticsProvider: forced failure")
        trackedEvents.add(event)
    }

    override suspend fun trackScreenView(screenName: String, screenClass: String?) {
        trackedScreens.add(screenName to screenClass)
    }

    override fun setUserId(userId: String?) {
        this.lastUserId = userId
    }

    override fun setUserProperty(key: String, value: String) {
        userProperties[key] = value
    }

    override suspend fun flush() {
        flushed = true
    }

    override fun reset() {
        reset = true
        trackedEvents.clear()
        trackedScreens.clear()
        lastUserId = null
        userProperties.clear()
    }
}
