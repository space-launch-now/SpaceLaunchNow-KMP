# Internal Contracts: Analytics Module

**Feature**: 011-analytics-module | **Date**: 2026-04-05

These are internal Kotlin interfaces — not HTTP APIs. The analytics module is a local library
consumed by ViewModels and navigation components within the app.

## Contract 1: AnalyticsProvider Interface

```kotlin
package me.calebjones.spacelaunchnow.analytics.core

/**
 * Interface that all analytics platform providers must implement.
 * Each provider wraps a specific analytics SDK (Firebase, Amplitude, etc.).
 *
 * Implementations:
 * - FirebaseAnalyticsProvider (androidMain + iosMain)
 * - ConsoleAnalyticsProvider (all platforms, primarily desktop/debug)
 * - Future: AmplitudeAnalyticsProvider, MixpanelAnalyticsProvider
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
```

## Contract 2: AnalyticsEvent Sealed Class

```kotlin
package me.calebjones.spacelaunchnow.analytics.events

/**
 * Type-safe analytics event hierarchy.
 * Each event carries its own typed parameters and knows its event name.
 *
 * Usage:
 *   analyticsManager.track(AnalyticsEvent.LaunchViewed(id = "uuid", name = "Falcon 9"))
 */
sealed class AnalyticsEvent(val name: String) {
    abstract fun toParameters(): Map<String, Any?>

    // Launch Events
    data class LaunchViewed(val launchId: String, val launchName: String) :
        AnalyticsEvent("launch_viewed") {
        override fun toParameters() = mapOf("launch_id" to launchId, "launch_name" to launchName)
    }

    data class LaunchShared(val launchId: String, val method: String) :
        AnalyticsEvent("launch_shared") {
        override fun toParameters() = mapOf("launch_id" to launchId, "method" to method)
    }

    data class LaunchReminderSet(val launchId: String) :
        AnalyticsEvent("launch_reminder_set") {
        override fun toParameters() = mapOf("launch_id" to launchId)
    }

    data class LaunchFavorited(val launchId: String, val favorited: Boolean) :
        AnalyticsEvent("launch_favorited") {
        override fun toParameters() = mapOf("launch_id" to launchId, "favorited" to favorited)
    }

    // Navigation Events
    data class TabSelected(val tab: String) :
        AnalyticsEvent("tab_selected") {
        override fun toParameters() = mapOf("tab" to tab)
    }

    data class SearchPerformed(val query: String, val resultCount: Int) :
        AnalyticsEvent("search_performed") {
        override fun toParameters() = mapOf("query" to query, "result_count" to resultCount)
    }

    // Content Events
    data class ArticleViewed(val articleId: String, val source: String) :
        AnalyticsEvent("article_viewed") {
        override fun toParameters() = mapOf("article_id" to articleId, "source" to source)
    }

    data class EventViewed(val eventId: Int) :
        AnalyticsEvent("event_viewed") {
        override fun toParameters() = mapOf("event_id" to eventId)
    }

    data class AgencyViewed(val agencyId: Int) :
        AnalyticsEvent("agency_viewed") {
        override fun toParameters() = mapOf("agency_id" to agencyId)
    }

    data class AstronautViewed(val astronautId: Int) :
        AnalyticsEvent("astronaut_viewed") {
        override fun toParameters() = mapOf("astronaut_id" to astronautId)
    }

    data class RocketViewed(val rocketId: Int) :
        AnalyticsEvent("rocket_viewed") {
        override fun toParameters() = mapOf("rocket_id" to rocketId)
    }

    data class SpaceStationViewed(val stationId: Int) :
        AnalyticsEvent("space_station_viewed") {
        override fun toParameters() = mapOf("station_id" to stationId)
    }

    // Subscription Events
    data class PaywallViewed(val source: String) :
        AnalyticsEvent("paywall_viewed") {
        override fun toParameters() = mapOf("source" to source)
    }

    data class PurchaseStarted(val productId: String) :
        AnalyticsEvent("purchase_started") {
        override fun toParameters() = mapOf("product_id" to productId)
    }

    data class PurchaseCompleted(val productId: String, val revenue: Double? = null) :
        AnalyticsEvent("purchase_completed") {
        override fun toParameters() = buildMap {
            put("product_id", productId)
            revenue?.let { put("revenue", it) }
        }
    }

    data class PurchaseRestored(val success: Boolean) :
        AnalyticsEvent("purchase_restored") {
        override fun toParameters() = mapOf("success" to success)
    }

    // Notification Events
    data class NotificationReceived(val type: String) :
        AnalyticsEvent("notification_received") {
        override fun toParameters() = mapOf("type" to type)
    }

    data class NotificationTapped(val type: String, val launchId: String? = null) :
        AnalyticsEvent("notification_tapped") {
        override fun toParameters() = buildMap {
            put("type", type)
            launchId?.let { put("launch_id", it) }
        }
    }

    data class NotificationSettingChanged(val type: String, val enabled: Boolean) :
        AnalyticsEvent("notification_setting_changed") {
        override fun toParameters() = mapOf("type" to type, "enabled" to enabled)
    }

    // App Lifecycle Events
    data class AppOpened(val source: String = "direct") :
        AnalyticsEvent("app_opened") {
        override fun toParameters() = mapOf("source" to source)
    }

    data class OnboardingStep(val step: Int, val completed: Boolean) :
        AnalyticsEvent("onboarding_step") {
        override fun toParameters() = mapOf("step" to step, "completed" to completed)
    }

    // Settings Events
    data class ThemeChanged(val theme: String) :
        AnalyticsEvent("theme_changed") {
        override fun toParameters() = mapOf("theme" to theme)
    }

    data class FilterChanged(val filterType: String, val value: String) :
        AnalyticsEvent("filter_changed") {
        override fun toParameters() = mapOf("filter_type" to filterType, "value" to value)
    }

    // Widget Events
    data class WidgetConfigured(val widgetType: String) :
        AnalyticsEvent("widget_configured") {
        override fun toParameters() = mapOf("widget_type" to widgetType)
    }

    data class WidgetTapped(val widgetType: String, val launchId: String? = null) :
        AnalyticsEvent("widget_tapped") {
        override fun toParameters() = buildMap {
            put("widget_type", widgetType)
            launchId?.let { put("launch_id", it) }
        }
    }

    // Third-Party Referral Events
    data class ThirdPartyReferral(
        val provider: String,
        val url: String,
        val contentType: String? = null,
        val contentId: String? = null
    ) : AnalyticsEvent("third_party_referral") {
        override fun toParameters() = buildMap {
            put("provider", provider)
            put("url", url)
            contentType?.let { put("content_type", it) }
            contentId?.let { put("content_id", it) }
        }
    }

    // Sharing Events (generic — launch shares use LaunchShared above)
    data class ContentShared(val contentType: String, val contentId: String, val method: String? = null) :
        AnalyticsEvent("content_shared") {
        override fun toParameters() = buildMap {
            put("content_type", contentType)
            put("content_id", contentId)
            method?.let { put("method", it) }
        }
    }

    // Video Engagement Events
    data class VideoWatchTime(
        val videoUrl: String,
        val videoSource: String,
        val durationSeconds: Long,
        val launchId: String? = null
    ) : AnalyticsEvent("video_watch_time") {
        override fun toParameters() = buildMap {
            put("video_url", videoUrl)
            put("video_source", videoSource)
            put("duration_seconds", durationSeconds)
            launchId?.let { put("launch_id", it) }
        }
    }

    data class VideoOpenedExternal(val videoUrl: String, val videoSource: String, val launchId: String? = null) :
        AnalyticsEvent("video_opened_external") {
        override fun toParameters() = buildMap {
            put("video_url", videoUrl)
            put("video_source", videoSource)
            launchId?.let { put("launch_id", it) }
        }
    }

    // Screen tracking (auto-generated by navigation integration)
    data class ScreenViewed(val screenName: String, val screenClass: String? = null) :
        AnalyticsEvent("screen_view") {
        override fun toParameters() = buildMap {
            put("screen_name", screenName)
            screenClass?.let { put("screen_class", it) }
        }
    }
}
```

## Contract 3: AnalyticsManager

```kotlin
package me.calebjones.spacelaunchnow.analytics.core

import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent

/**
 * Central analytics dispatcher.
 * Fan-outs all events to every registered and enabled AnalyticsProvider.
 *
 * Injected via Koin as a singleton.
 * ViewModels and repositories should depend on this, never on individual providers.
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

    /** Reset all providers (logout) */
    fun reset()

    /** Toggle a specific provider by name */
    fun enableProvider(name: String, enabled: Boolean)
}
```

## Contract 4: Analytics Koin Module

```kotlin
// Expected module structure in di/AnalyticsModule.kt
val analyticsModule = module {
    // Core manager
    single<AnalyticsManager> {
        AnalyticsManagerImpl(
            providers = getAll<AnalyticsProvider>(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        )
    }

    // Preferences
    single { AnalyticsPreferences(get<DataStore<Preferences>>(named("AppSettingsDataStore"))) }

    // Platform providers registered via nativeConfig() or platform modules
}

// Android nativeConfig() adds:
//   single<AnalyticsProvider>(named("firebase")) { FirebaseAnalyticsProvider(context) }

// iOS nativeConfig() adds:
//   single<AnalyticsProvider>(named("firebase")) { FirebaseAnalyticsProvider() }

// Desktop nativeConfig() adds:
//   single<AnalyticsProvider>(named("console")) { ConsoleAnalyticsProvider() }

// All platforms (commonMain):
//   single<AnalyticsProvider>(named("console")) { ConsoleAnalyticsProvider() }
```

## Contract 5: Navigation Screen Tracker

```kotlin
/**
 * Composable that automatically tracks screen views when navigation changes.
 * Place at the root NavHost level.
 *
 * Uses LaunchedEffect keyed on currentBackStackEntry to detect navigation.
 */
@Composable
fun AnalyticsScreenTracker(
    navController: NavHostController,
    analyticsManager: AnalyticsManager
)
```

## Route-to-Screen Name Mapping

| Route Class | Screen Name | Notes |
|---|---|---|
| `Home` | `"Home"` | Main feed |
| `Schedule` | `"Schedule"` | Launch schedule |
| `Explore` | `"Explore"` | Explore section |
| `Settings` | `"Settings"` | App settings |
| `NewsEvents` | `"News & Events"` | News feed |
| `LaunchDetail(id)` | `"Launch Detail"` | Include launchId param |
| `EventDetail(id)` | `"Event Detail"` | Include eventId param |
| `Onboarding` | `"Onboarding"` | First launch flow |
| `Rockets` | `"Rockets"` | Rocket list |
| `RocketDetail(id)` | `"Rocket Detail"` | |
| `Agencies` | `"Agencies"` | Agency list |
| `AgencyDetail(id)` | `"Agency Detail"` | |
| `Astronauts` | `"Astronauts"` | Astronaut list |
| `AstronautDetail(id)` | `"Astronaut Detail"` | |
| `Starship` | `"Starship"` | Starship dashboard |
| `NotificationSettings` | `"Notification Settings"` | |
| `DebugSettings` | `"Debug Settings"` | |
| `ThemeCustomization` | `"Theme Customization"` | |
| `SupportUs` | `"Support Us"` | |
| `Roadmap` | `"Roadmap"` | |
