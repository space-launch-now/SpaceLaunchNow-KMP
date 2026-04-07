package me.calebjones.spacelaunchnow.analytics.providers

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsProvider
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger

private val log by lazy { SpaceLogger.getLogger("Analytics") }

/**
 * Analytics provider backed by Firebase Analytics via the GitLive KMP SDK.
 * Registered in [me.calebjones.spacelaunchnow.di.AppModule] Android platform module.
 */
class FirebaseAnalyticsProvider(
    override var isEnabled: Boolean = true
) : AnalyticsProvider {

    override val name: String = "firebase"

    private val analytics get() = Firebase.analytics

    override suspend fun initialize(config: Map<String, Any>) {
        // Firebase is initialised by the GoogleServices plugin at app startup
    }

    override suspend fun trackEvent(event: AnalyticsEvent) {
        if (!isEnabled) return
        try {
            val params: Map<String, Any> = event.toParameters()
                .filterValues { it != null }
                .mapValues { (_, v) -> v!! }
            analytics.logEvent(event.name, params)
        } catch (e: Exception) {
            log.e(e) { "[Analytics] Firebase trackEvent failed for ${event.name}" }
        }
    }

    override suspend fun trackScreenView(screenName: String, screenClass: String?) {
        if (!isEnabled) return
        try {
            val params = buildMap<String, Any> {
                put("screen_name", screenName)
                screenClass?.let { put("screen_class", it) }
            }
            analytics.logEvent("screen_view", params)
        } catch (e: Exception) {
            log.e(e) { "[Analytics] Firebase trackScreenView failed for $screenName" }
        }
    }

    override fun setUserId(userId: String?) {
        try {
            analytics.setUserId(userId)
        } catch (e: Exception) {
            log.e(e) { "[Analytics] Firebase setUserId failed" }
        }
    }

    override fun setUserProperty(key: String, value: String) {
        try {
            analytics.setUserProperty(key, value)
        } catch (e: Exception) {
            log.e(e) { "[Analytics] Firebase setUserProperty($key) failed" }
        }
    }

    override suspend fun flush() {
        // Firebase Analytics batches automatically — no manual flush available
    }

    override fun reset() {
        try {
            analytics.resetAnalyticsData()
        } catch (e: Exception) {
            log.e(e) { "[Analytics] Firebase reset failed" }
        }
    }
}
