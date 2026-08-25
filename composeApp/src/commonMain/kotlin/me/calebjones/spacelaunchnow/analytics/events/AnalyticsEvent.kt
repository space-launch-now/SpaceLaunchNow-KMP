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

    // ── Launch Events ────────────────────────────────────────────────────────

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

    // ── Navigation Events ────────────────────────────────────────────────────

    data class TabSelected(val tab: String) :
        AnalyticsEvent("tab_selected") {
        override fun toParameters() = mapOf("tab" to tab)
    }

    data class SearchPerformed(val query: String, val resultCount: Int) :
        AnalyticsEvent("search_performed") {
        override fun toParameters() = mapOf("query" to query, "result_count" to resultCount)
    }

    // ── Content Events ───────────────────────────────────────────────────────

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

    // ── Third-party Referral Events ──────────────────────────────────────────
    // Tracks outbound clicks to partner/third-party sites (e.g., Flight Club, SpaceflightNow).
    // `provider` is the partner name; `contentType`/`contentId` are optional context.

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

    // ── Sharing Events ───────────────────────────────────────────────────────
    // Generic share for any content type. Use LaunchShared for launch-specific shares.

    data class ContentShared(
        val contentType: String,
        val contentId: String,
        val method: String? = null
    ) : AnalyticsEvent("content_shared") {
        override fun toParameters() = buildMap {
            put("content_type", contentType)
            put("content_id", contentId)
            method?.let { put("method", it) }
        }
    }

    // ── Video Engagement Events ──────────────────────────────────────────────

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

    data class VideoOpenedExternal(
        val videoUrl: String,
        val videoSource: String,
        val launchId: String? = null
    ) : AnalyticsEvent("video_opened_external") {
        override fun toParameters() = buildMap {
            put("video_url", videoUrl)
            put("video_source", videoSource)
            launchId?.let { put("launch_id", it) }
        }
    }

    // ── Subscription Events ──────────────────────────────────────────────────

    /**
     * Subscriber-context dimensions stamped on every conversion-funnel event so the
     * funnel is sliceable in both Firebase and Datadog (spec 014 FR-4). Values of
     * [subscriptionType] match the RevenueCat `subscription_state` attribute
     * (SubscriptionType.name.lowercase()).
     */
    data class FunnelDimensions(
        val subscriptionType: String,
        val isTrial: Boolean,
        val activeEntitlements: String,
        val platform: String
    ) {
        fun toParameters(): Map<String, Any> = mapOf(
            "subscription_type" to subscriptionType,
            "is_trial" to isTrial,
            "active_entitlements" to activeEntitlements,
            "platform" to platform
        )
    }

    data class PaywallViewed(
        val source: String,
        val dimensions: FunnelDimensions? = null
    ) : AnalyticsEvent("paywall_viewed") {
        override fun toParameters() = buildMap {
            put("source", source)
            dimensions?.let { putAll(it.toParameters()) }
        }
    }

    /**
     * User tapped a specific tier card on a paywall, before the system purchase
     * sheet launches — the missing middle of the conversion funnel (spec 014 FR-2).
     */
    data class PaywallTierSelected(
        val tier: String,
        val productId: String,
        val source: String,
        val dimensions: FunnelDimensions? = null
    ) : AnalyticsEvent("paywall_tier_selected") {
        override fun toParameters() = buildMap {
            put("tier", tier)
            put("product_id", productId)
            put("source", source)
            dimensions?.let { putAll(it.toParameters()) }
        }
    }

    /**
     * User left a paywall without purchasing ("Continue for free" / dismiss).
     * secondsOnScreen measures view-to-dismiss dwell for the onboarding A/B test.
     */
    data class PaywallDismissed(
        val source: String,
        val secondsOnScreen: Long,
        val dimensions: FunnelDimensions? = null
    ) : AnalyticsEvent("paywall_dismissed") {
        override fun toParameters() = buildMap {
            put("source", source)
            put("seconds_on_screen", secondsOnScreen)
            dimensions?.let { putAll(it.toParameters()) }
        }
    }

    data class PurchaseStarted(
        val productId: String,
        val dimensions: FunnelDimensions? = null,
        val source: String = "support_us"
    ) : AnalyticsEvent("purchase_started") {
        override fun toParameters() = buildMap {
            put("product_id", productId)
            put("source", source)
            dimensions?.let { putAll(it.toParameters()) }
        }
    }

    data class PurchaseCompleted(
        val productId: String,
        val revenue: Double? = null,
        val dimensions: FunnelDimensions? = null,
        val source: String = "support_us"
    ) : AnalyticsEvent("purchase_completed") {
        override fun toParameters() = buildMap {
            put("product_id", productId)
            revenue?.let { put("revenue", it) }
            put("source", source)
            dimensions?.let { putAll(it.toParameters()) }
        }
    }

    /**
     * A purchase attempt ended without completion. `step` is where it died
     * ("setup" = product lookup / not initialized, "store_purchase" = native store flow),
     * `errorCode` is coarse ("user_cancelled" for sheet dismissal, else the
     * RevenueCat PurchasesErrorCode name). Spec 018 FR-1.1.
     */
    data class PurchaseFailed(
        val productId: String,
        val step: String,
        val errorCode: String,
        val dimensions: FunnelDimensions? = null,
        val source: String = "support_us"
    ) : AnalyticsEvent("purchase_failed") {
        override fun toParameters() = buildMap {
            put("product_id", productId)
            put("step", step)
            put("error_code", errorCode)
            put("source", source)
            dimensions?.let { putAll(it.toParameters()) }
        }
    }

    data class PurchaseRestored(
        val success: Boolean,
        val dimensions: FunnelDimensions? = null,
        val source: String = "support_us"
    ) : AnalyticsEvent("purchase_restored") {
        override fun toParameters() = buildMap {
            put("success", success)
            put("source", source)
            dimensions?.let { putAll(it.toParameters()) }
        }
    }

    // ── Notification Events ──────────────────────────────────────────────────

    data class NotificationReceived(
        val type: String,
        /** "displayed" or "suppressed". Null when only the bare type is known (legacy callers). */
        val outcome: String? = null,
        /** When outcome == "suppressed": why (kill switch / per-type toggle / agency-location / etc.). */
        val reason: String? = null,
        /** "android" or "ios". */
        val platform: String? = null
    ) : AnalyticsEvent("notification_received") {
        override fun toParameters() = buildMap {
            put("type", type)
            outcome?.let { put("outcome", it) }
            reason?.let { put("reason", it) }
            platform?.let { put("platform", it) }
        }
    }

    /**
     * A notification was actually posted to the OS notification shade — the true
     * denominator for notification CTR (notification_tapped / notification_shown).
     * Unlike notification_received{outcome=displayed}, this is NOT fired when
     * OS-level notification permission is off. Spec 018 US5.
     */
    data class NotificationShown(
        val type: String,
        /** "android" or "ios". */
        val platform: String? = null
    ) : AnalyticsEvent("notification_shown") {
        override fun toParameters() = buildMap {
            put("type", type)
            platform?.let { put("platform", it) }
        }
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

    /**
     * Fired once per installation, when the V5-to-V6 topic changeover completes
     * (every legacy topic unsubscribe acknowledged by FCM). Counts devices
     * actually off the V5 broadcast — the quantity the V5 retirement decision
     * needs. Build-version adoption is only a proxy for this: a build can be
     * V6-capable while its changeover is stuck retrying.
     */
    data class V6ChangeoverCompleted(val audienceClass: String) :
        AnalyticsEvent("v6_changeover_completed") {
        override fun toParameters() = mapOf("audience_class" to audienceClass)
    }

    // ── App Lifecycle Events ─────────────────────────────────────────────────

    data class AppOpened(val source: String = "direct") :
        AnalyticsEvent("app_opened") {
        override fun toParameters() = mapOf("source" to source)
    }

    data class OnboardingStep(val step: Int, val completed: Boolean) :
        AnalyticsEvent("onboarding_step") {
        override fun toParameters() = mapOf("step" to step, "completed" to completed)
    }

    // ── Settings Events ──────────────────────────────────────────────────────

    data class ThemeChanged(val theme: String) :
        AnalyticsEvent("theme_changed") {
        override fun toParameters() = mapOf("theme" to theme)
    }

    data class FilterChanged(val filterType: String, val value: String) :
        AnalyticsEvent("filter_changed") {
        override fun toParameters() = mapOf("filter_type" to filterType, "value" to value)
    }

    // ── Widget Events ────────────────────────────────────────────────────────

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

    // ── Screen Tracking ──────────────────────────────────────────────────────
    // Auto-generated by AnalyticsScreenTracker — do not call manually.

    data class ScreenViewed(val screenName: String, val screenClass: String? = null) :
        AnalyticsEvent("screen_view") {
        override fun toParameters() = buildMap {
            put("screen_name", screenName)
            screenClass?.let { put("screen_class", it) }
        }
    }

    // ── Rewarded Ad / Temp Access Events ─────────────────────────────────────

    data class RewardedAdRequested(
        val source: String,
        val isExtension: Boolean
    ) : AnalyticsEvent("rewarded_ad_requested") {
        override fun toParameters() = mapOf(
            "source" to source,
            "is_extension" to isExtension
        )
    }

    data class RewardedAdShown(val source: String) :
        AnalyticsEvent("rewarded_ad_shown") {
        override fun toParameters() = mapOf("source" to source)
    }

    data class RewardedAdFailed(val source: String, val error: String) :
        AnalyticsEvent("rewarded_ad_failed") {
        override fun toParameters() = mapOf(
            "source" to source,
            "error" to error
        )
    }

    data class TemporaryAccessGranted(
        val feature: String,
        val source: String,
        val isExtension: Boolean
    ) : AnalyticsEvent("temporary_access_granted") {
        override fun toParameters() = mapOf(
            "feature" to feature,
            "source" to source,
            "is_extension" to isExtension
        )
    }
}
