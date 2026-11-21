package me.calebjones.spacelaunchnow.analytics

import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.analytics.events.PaywallSource
import me.calebjones.spacelaunchnow.navigation.SupportUs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PaywallSourceTest {

    @Test
    fun `every entry has a distinct snake_case value`() {
        val values = PaywallSource.entries.map { it.value }
        assertEquals(values.size, values.toSet().size)
        assertTrue(values.all { v -> v.isNotEmpty() && v.all { it in 'a'..'z' || it == '_' } })
    }

    @Test
    fun `source values are the published list`() {
        assertEquals(
            listOf(
                "support_us", "onboarding", "home_heart", "home_remove_ads", "launch_detail_remove_ads",
                "theme_prompt", "theme_widget_apply", "theme_reset", "notification_settings",
                "calendar_sync", "settings_row", "widget_lock"
            ),
            PaywallSource.entries.map { it.value }
        )
    }

    @Test
    fun `every source maps onto the paywall and purchase funnel events`() {
        PaywallSource.entries.forEach { s ->
            val v = s.value
            assertEquals(v, AnalyticsEvent.PaywallViewed(v).toParameters()["source"])
            assertEquals(v, AnalyticsEvent.PaywallTierSelected("annual", "p", v).toParameters()["source"])
            assertEquals(v, AnalyticsEvent.PaywallDismissed(v, 3L).toParameters()["source"])
            assertEquals(v, AnalyticsEvent.PurchaseStarted("p", source = v).toParameters()["source"])
            assertEquals(v, AnalyticsEvent.PurchaseCompleted("p", source = v).toParameters()["source"])
            assertEquals(v, AnalyticsEvent.PurchaseFailed("p", "setup", "x", source = v).toParameters()["source"])
            assertEquals(v, AnalyticsEvent.PurchaseRestored(true, source = v).toParameters()["source"])
        }
    }

    @Test
    fun `support us route defaults to the support_us source`() {
        assertEquals(PaywallSource.SUPPORT_US.value, SupportUs().source)
        assertEquals("home_heart", SupportUs(PaywallSource.HOME_HEART.value).source)
    }
}
