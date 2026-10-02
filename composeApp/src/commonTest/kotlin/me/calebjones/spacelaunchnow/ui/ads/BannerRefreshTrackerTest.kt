package me.calebjones.spacelaunchnow.ui.ads

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BannerRefreshTrackerTest {

    private companion object {
        const val T0 = 1_000_000L
        const val FLOOR = 45_000L
    }

    @Test
    fun firstUseKeepsPreloadedAd() {
        assertFalse(BannerRefreshTracker.shouldReload(Any(), "launch-a", T0, FLOOR))
    }

    @Test
    fun sameTokenDoesNotReload() {
        val banner = Any()
        BannerRefreshTracker.shouldReload(banner, "launch-a", T0, FLOOR)
        assertFalse(BannerRefreshTracker.shouldReload(banner, "launch-a", T0 + FLOOR, FLOOR))
    }

    @Test
    fun newTokenReloads() {
        val banner = Any()
        BannerRefreshTracker.shouldReload(banner, "launch-a", T0, FLOOR)
        assertTrue(BannerRefreshTracker.shouldReload(banner, "launch-b", T0 + FLOOR, FLOOR))
        assertTrue(BannerRefreshTracker.shouldReload(banner, "launch-a", T0 + 2 * FLOOR, FLOOR))
    }

    @Test
    fun bannersAreTrackedSeparately() {
        val medium = Any()
        val large = Any()
        BannerRefreshTracker.shouldReload(medium, "launch-a", T0, FLOOR)
        assertFalse(BannerRefreshTracker.shouldReload(large, "launch-b", T0 + FLOOR, FLOOR))
        assertTrue(BannerRefreshTracker.shouldReload(medium, "launch-b", T0 + FLOOR, FLOOR))
    }

    @Test
    fun newTokenInsideFloorDoesNotReload() {
        val banner = Any()
        BannerRefreshTracker.shouldReload(banner, "home-1", T0, FLOOR)
        assertFalse(BannerRefreshTracker.shouldReload(banner, "home-2", T0 + FLOOR - 1, FLOOR))
    }

    @Test
    fun newTokenAtFloorReloads() {
        val banner = Any()
        BannerRefreshTracker.shouldReload(banner, "home-1", T0, FLOOR)
        assertTrue(BannerRefreshTracker.shouldReload(banner, "home-2", T0 + FLOOR, FLOOR))
    }

    @Test
    fun floorRunsFromLastReloadNotFromBlockedAttempts() {
        val banner = Any()
        BannerRefreshTracker.shouldReload(banner, "a", T0, FLOOR)
        assertFalse(BannerRefreshTracker.shouldReload(banner, "b", T0 + 30_000, FLOOR))
        assertTrue(BannerRefreshTracker.shouldReload(banner, "c", T0 + FLOOR, FLOOR))
    }

    @Test
    fun floorIsPerBanner() {
        val medium = Any()
        val large = Any()
        BannerRefreshTracker.shouldReload(medium, "a", T0, FLOOR)
        BannerRefreshTracker.shouldReload(large, "a", T0 + 40_000, FLOOR)
        assertTrue(BannerRefreshTracker.shouldReload(medium, "b", T0 + FLOOR, FLOOR))
    }
}
