package me.calebjones.spacelaunchnow.analytics.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger

private val log by lazy { SpaceLogger.getLogger("AnalyticsManager") }

/**
 * Fan-out analytics dispatcher.
 *
 * Dispatches every event to all enabled providers concurrently via a
 * [SupervisorJob]-backed scope — a single provider failure never affects others.
 *
 * @param providers    Registered analytics providers (injected via Koin `getAll<AnalyticsProvider>()`).
 * @param preferences  Optional [AnalyticsPreferences]; when provided, per-provider consent
 *                     changes are observed and applied automatically. If null, all providers
 *                     remain at their initial enabled state (default behaviour, preserves tests).
 * @param scope        Optional coroutine scope; defaults to [SupervisorJob] + [Dispatchers.Default].
 */
class AnalyticsManagerImpl(
    private val providers: List<AnalyticsProvider>,
    preferences: AnalyticsPreferences? = null,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : AnalyticsManager {

    init {
        if (preferences != null) {
            providers.forEach { provider ->
                preferences.isProviderEnabled(provider.name)
                    .onEach { enabled -> provider.isEnabled = enabled }
                    .launchIn(scope)
            }
        }
        // Initialize all providers at construction time
        providers.forEach { provider ->
            scope.launch {
                try {
                    provider.initialize()
                } catch (e: Exception) {
                    log.e { "Provider '${provider.name}' threw on initialize: $e" }
                }
            }
        }
    }

    override fun track(event: AnalyticsEvent) {
        providers.forEach { provider ->
            if (!provider.isEnabled) return@forEach
            scope.launch {
                try {
                    provider.trackEvent(event)
                } catch (e: Exception) {
                    log.e { "Provider '${provider.name}' threw on trackEvent(${event.name}): $e" }
                }
            }
        }
    }

    override fun trackScreenView(screenName: String, screenClass: String?) {
        providers.forEach { provider ->
            if (!provider.isEnabled) return@forEach
            scope.launch {
                try {
                    provider.trackScreenView(screenName, screenClass)
                } catch (e: Exception) {
                    log.e { "Provider '${provider.name}' threw on trackScreenView($screenName): $e" }
                }
            }
        }
    }

    override fun setUserId(userId: String?) {
        providers.forEach { provider ->
            try {
                provider.setUserId(userId)
            } catch (e: Exception) {
                log.e { "Provider '${provider.name}' threw on setUserId: $e" }
            }
        }
    }

    override fun setUserProperty(key: String, value: String) {
        providers.forEach { provider ->
            try {
                provider.setUserProperty(key, value)
            } catch (e: Exception) {
                log.e { "Provider '${provider.name}' threw on setUserProperty($key): $e" }
            }
        }
    }

    override suspend fun flush() {
        providers.forEach { provider ->
            try {
                provider.flush()
            } catch (e: Exception) {
                log.e { "Provider '${provider.name}' threw on flush: $e" }
            }
        }
    }

    override fun reset() {
        providers.forEach { provider ->
            try {
                provider.reset()
            } catch (e: Exception) {
                log.e { "Provider '${provider.name}' threw on reset: $e" }
            }
        }
    }

    override fun enableProvider(name: String, enabled: Boolean) {
        val provider = providers.find { it.name == name }
        if (provider != null) {
            provider.isEnabled = enabled
        } else {
            log.w { "enableProvider: no provider named '$name'" }
        }
    }
}
