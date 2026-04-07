package me.calebjones.spacelaunchnow.analytics

import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class AnalyticsEventTest {

    // ── name property ────────────────────────────────────────────────────────

    @Test fun `LaunchViewed has correct name`() = assertEquals("launch_viewed", AnalyticsEvent.LaunchViewed("id", "name").name)
    @Test fun `LaunchShared has correct name`() = assertEquals("launch_shared", AnalyticsEvent.LaunchShared("id", "method").name)
    @Test fun `LaunchReminderSet has correct name`() = assertEquals("launch_reminder_set", AnalyticsEvent.LaunchReminderSet("id").name)
    @Test fun `LaunchFavorited has correct name`() = assertEquals("launch_favorited", AnalyticsEvent.LaunchFavorited("id", true).name)
    @Test fun `TabSelected has correct name`() = assertEquals("tab_selected", AnalyticsEvent.TabSelected("home").name)
    @Test fun `SearchPerformed has correct name`() = assertEquals("search_performed", AnalyticsEvent.SearchPerformed("falcon", 5).name)
    @Test fun `ArticleViewed has correct name`() = assertEquals("article_viewed", AnalyticsEvent.ArticleViewed("a1", "sln").name)
    @Test fun `EventViewed has correct name`() = assertEquals("event_viewed", AnalyticsEvent.EventViewed(1).name)
    @Test fun `AgencyViewed has correct name`() = assertEquals("agency_viewed", AnalyticsEvent.AgencyViewed(1).name)
    @Test fun `AstronautViewed has correct name`() = assertEquals("astronaut_viewed", AnalyticsEvent.AstronautViewed(1).name)
    @Test fun `RocketViewed has correct name`() = assertEquals("rocket_viewed", AnalyticsEvent.RocketViewed(1).name)
    @Test fun `SpaceStationViewed has correct name`() = assertEquals("space_station_viewed", AnalyticsEvent.SpaceStationViewed(1).name)
    @Test fun `ThirdPartyReferral has correct name`() = assertEquals("third_party_referral", AnalyticsEvent.ThirdPartyReferral("flight_club", "https://example.com").name)
    @Test fun `ContentShared has correct name`() = assertEquals("content_shared", AnalyticsEvent.ContentShared("article", "a1").name)
    @Test fun `VideoWatchTime has correct name`() = assertEquals("video_watch_time", AnalyticsEvent.VideoWatchTime("url", "youtube", 120).name)
    @Test fun `VideoOpenedExternal has correct name`() = assertEquals("video_opened_external", AnalyticsEvent.VideoOpenedExternal("url", "youtube").name)
    @Test fun `PaywallViewed has correct name`() = assertEquals("paywall_viewed", AnalyticsEvent.PaywallViewed("home").name)
    @Test fun `PurchaseStarted has correct name`() = assertEquals("purchase_started", AnalyticsEvent.PurchaseStarted("prod_1").name)
    @Test fun `PurchaseCompleted has correct name`() = assertEquals("purchase_completed", AnalyticsEvent.PurchaseCompleted("prod_1").name)
    @Test fun `PurchaseRestored has correct name`() = assertEquals("purchase_restored", AnalyticsEvent.PurchaseRestored(true).name)
    @Test fun `NotificationReceived has correct name`() = assertEquals("notification_received", AnalyticsEvent.NotificationReceived("launch").name)
    @Test fun `NotificationTapped has correct name`() = assertEquals("notification_tapped", AnalyticsEvent.NotificationTapped("launch").name)
    @Test fun `NotificationSettingChanged has correct name`() = assertEquals("notification_setting_changed", AnalyticsEvent.NotificationSettingChanged("launch", true).name)
    @Test fun `AppOpened has correct name`() = assertEquals("app_opened", AnalyticsEvent.AppOpened().name)
    @Test fun `OnboardingStep has correct name`() = assertEquals("onboarding_step", AnalyticsEvent.OnboardingStep(1, false).name)
    @Test fun `ThemeChanged has correct name`() = assertEquals("theme_changed", AnalyticsEvent.ThemeChanged("dark").name)
    @Test fun `FilterChanged has correct name`() = assertEquals("filter_changed", AnalyticsEvent.FilterChanged("status", "go").name)
    @Test fun `WidgetConfigured has correct name`() = assertEquals("widget_configured", AnalyticsEvent.WidgetConfigured("launch_list").name)
    @Test fun `WidgetTapped has correct name`() = assertEquals("widget_tapped", AnalyticsEvent.WidgetTapped("launch_list").name)
    @Test fun `ScreenViewed has correct name`() = assertEquals("screen_view", AnalyticsEvent.ScreenViewed("Home").name)

    // ── toParameters ─────────────────────────────────────────────────────────

    @Test
    fun `LaunchViewed toParameters contains launch_id and launch_name`() {
        val params = AnalyticsEvent.LaunchViewed("abc-123", "Falcon 9").toParameters()
        assertEquals("abc-123", params["launch_id"])
        assertEquals("Falcon 9", params["launch_name"])
    }

    @Test
    fun `LaunchShared toParameters contains launch_id and method`() {
        val params = AnalyticsEvent.LaunchShared("abc-123", "share_sheet").toParameters()
        assertEquals("abc-123", params["launch_id"])
        assertEquals("share_sheet", params["method"])
    }

    @Test
    fun `PurchaseCompleted optional revenue excluded when null`() {
        val params = AnalyticsEvent.PurchaseCompleted("prod_1").toParameters()
        assertFalse(params.containsKey("revenue"))
    }

    @Test
    fun `PurchaseCompleted optional revenue included when set`() {
        val params = AnalyticsEvent.PurchaseCompleted("prod_1", revenue = 9.99).toParameters()
        assertEquals(9.99, params["revenue"])
    }

    @Test
    fun `NotificationTapped optional launchId excluded when null`() {
        val params = AnalyticsEvent.NotificationTapped("T-10min").toParameters()
        assertFalse(params.containsKey("launch_id"))
    }

    @Test
    fun `NotificationTapped optional launchId included when set`() {
        val params = AnalyticsEvent.NotificationTapped("T-10min", "launch-uuid").toParameters()
        assertEquals("launch-uuid", params["launch_id"])
    }

    @Test
    fun `ThirdPartyReferral optional fields excluded when null`() {
        val params = AnalyticsEvent.ThirdPartyReferral("flight_club", "https://fc.io").toParameters()
        assertFalse(params.containsKey("content_type"))
        assertFalse(params.containsKey("content_id"))
    }

    @Test
    fun `ThirdPartyReferral optional fields included when set`() {
        val params = AnalyticsEvent.ThirdPartyReferral(
            provider = "flight_club",
            url = "https://fc.io/launch/123",
            contentType = "launch",
            contentId = "launch-uuid"
        ).toParameters()
        assertEquals("flight_club", params["provider"])
        assertEquals("https://fc.io/launch/123", params["url"])
        assertEquals("launch", params["content_type"])
        assertEquals("launch-uuid", params["content_id"])
    }

    @Test
    fun `ContentShared optional method excluded when null`() {
        val params = AnalyticsEvent.ContentShared("article", "a1").toParameters()
        assertFalse(params.containsKey("method"))
    }

    @Test
    fun `VideoWatchTime optional launchId excluded when null`() {
        val params = AnalyticsEvent.VideoWatchTime("url", "youtube", 90L).toParameters()
        assertFalse(params.containsKey("launch_id"))
    }

    @Test
    fun `VideoWatchTime contains all required fields`() {
        val params = AnalyticsEvent.VideoWatchTime("https://youtu.be/abc", "YouTube", 300L, "launch-1").toParameters()
        assertEquals("https://youtu.be/abc", params["video_url"])
        assertEquals("YouTube", params["video_source"])
        assertEquals(300L, params["duration_seconds"])
        assertEquals("launch-1", params["launch_id"])
    }

    @Test
    fun `VideoOpenedExternal optional launchId excluded when null`() {
        val params = AnalyticsEvent.VideoOpenedExternal("url", "youtube").toParameters()
        assertFalse(params.containsKey("launch_id"))
    }

    @Test
    fun `ScreenViewed optional screenClass excluded when null`() {
        val params = AnalyticsEvent.ScreenViewed("Home").toParameters()
        assertFalse(params.containsKey("screen_class"))
    }

    @Test
    fun `AppOpened defaults to direct source`() {
        assertEquals("direct", AnalyticsEvent.AppOpened().toParameters()["source"])
    }
}
