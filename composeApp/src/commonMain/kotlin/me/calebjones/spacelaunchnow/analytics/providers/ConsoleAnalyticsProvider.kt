package me.calebjones.spacelaunchnow.analytics.providers

import me.calebjones.spacelaunchnow.analytics.core.AnalyticsProvider
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger

private val log by lazy { SpaceLogger.getLogger("Analytics") }

/**
 * Analytics provider that logs all events to [SpaceLogger].
 *
 * Used as the debug provider on all platforms and as the sole provider on Desktop,
 * where external analytics SDKs are not available.
 */
class ConsoleAnalyticsProvider(
    override var isEnabled: Boolean = true
) : AnalyticsProvider {

    override val name: String = "console"

    private var currentUserId: String? = null

    override suspend fun initialize(config: Map<String, Any>) {
        log.d { "[Analytics] ConsoleAnalyticsProvider initialized" }
    }

    override suspend fun trackEvent(event: AnalyticsEvent) {
        if (!isEnabled) return
        log.d { "[Analytics] event=${event.name} params=${event.toParameters()}" }
    }

    override suspend fun trackScreenView(screenName: String, screenClass: String?) {
        if (!isEnabled) return
        val classInfo = screenClass?.let { " class=$it" } ?: ""
        log.d { "[Analytics] screen_view name=$screenName$classInfo" }
    }

    override fun setUserId(userId: String?) {
        currentUserId = userId
        log.d { "[Analytics] setUserId userId=$userId" }
    }

    override fun setUserProperty(key: String, value: String) {
        log.d { "[Analytics] setUserProperty $key=$value" }
    }

    override suspend fun flush() {
        // No-op — console logging is synchronous
    }

    override fun reset() {
        currentUserId = null
        log.d { "[Analytics] reset" }
    }
}
