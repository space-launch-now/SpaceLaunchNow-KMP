package me.calebjones.spacelaunchnow.ui.ads

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BannerRefreshTrackerTest {

    @Test
    fun firstUseKeepsPreloadedAd() {
        assertFalse(BannerRefreshTracker.shouldReload(Any(), "launch-a"))
    }

    @Test
    fun sameTokenDoesNotReload() {
        val banner = Any()
        BannerRefreshTracker.shouldReload(banner, "launch-a")
        assertFalse(BannerRefreshTracker.shouldReload(banner, "launch-a"))
    }

    @Test
    fun newTokenReloads() {
        val banner = Any()
        BannerRefreshTracker.shouldReload(banner, "launch-a")
        assertTrue(BannerRefreshTracker.shouldReload(banner, "launch-b"))
        assertTrue(BannerRefreshTracker.shouldReload(banner, "launch-a"))
    }

    @Test
    fun bannersAreTrackedSeparately() {
        val medium = Any()
        val large = Any()
        BannerRefreshTracker.shouldReload(medium, "launch-a")
        assertFalse(BannerRefreshTracker.shouldReload(large, "launch-b"))
        assertTrue(BannerRefreshTracker.shouldReload(medium, "launch-b"))
    }
}
