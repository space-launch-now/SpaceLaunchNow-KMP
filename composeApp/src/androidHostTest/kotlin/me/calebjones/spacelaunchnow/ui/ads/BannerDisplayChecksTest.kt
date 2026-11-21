package me.calebjones.spacelaunchnow.ui.ads

import android.content.res.Configuration
import app.lexilabs.basic.ads.AdState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BannerDisplayChecksTest {

    private val base = DisplayConfigKey(density = 2.75f, fontScale = 1f, widthClass = "COMPACT")

    @Test
    fun firstKeyIsNotAChange() {
        assertFalse(base.changedFrom(null))
    }

    @Test
    fun sameKeyIsNotAChange() {
        assertFalse(base.changedFrom(base.copy()))
    }

    @Test
    fun densityFontScaleOrWidthClassIsAChange() {
        assertTrue(base.copy(density = 3.5f).changedFrom(base))
        assertTrue(base.copy(fontScale = 1.3f).changedFrom(base))
        assertTrue(base.copy(widthClass = "MEDIUM").changedFrom(base))
    }

    @Test
    fun onlyLoadedStatesAreReloaded() {
        assertTrue(isBannerLoaded(AdState.READY))
        assertTrue(isBannerLoaded(AdState.SHOWING))
        assertTrue(isBannerLoaded(AdState.SHOWN))
        assertFalse(isBannerLoaded(AdState.NONE))
        assertFalse(isBannerLoaded(AdState.LOADING))
        assertFalse(isBannerLoaded(AdState.FAILING))
    }

    @Test
    fun orientationLabels() {
        assertEquals("portrait", orientationLabel(Configuration.ORIENTATION_PORTRAIT))
        assertEquals("landscape", orientationLabel(Configuration.ORIENTATION_LANDSCAPE))
        assertEquals("unknown", orientationLabel(Configuration.ORIENTATION_UNDEFINED))
    }

    @Test
    fun visibleSizeIsNullWhenBannerReachesPastTheWindow() {
        assertEquals(null, visibleSizeForCheck(0, 1900, 320, 50, 1080, 1920, 320, 20))
        assertEquals(null, visibleSizeForCheck(-10, 100, 320, 50, 1080, 1920, 310, 50))
    }

    @Test
    fun visibleSizeIsReportedWhenBannerIsInsideTheWindow() {
        assertEquals(320 to 50, visibleSizeForCheck(40, 100, 320, 50, 1080, 1920, 320, 50))
        assertEquals(300 to 50, visibleSizeForCheck(40, 100, 320, 50, 1080, 1920, 300, 50))
    }

    @Test
    fun clippedBannerInsideTheWindowIsAMismatch() {
        val clipped = visibleSizeForCheck(40, 100, 880, 138, 1080, 1920, 800, 138)
        assertTrue(AdFrameCheck.mismatch(880 to 138, 880 to 138, clipped))
        assertFalse(AdFrameCheck.mismatch(880 to 138, 880 to 138, 880 to 138))
    }

    @Test
    fun handlerLoadedAtAnotherConfigIsStale() {
        val keys = LoadedDisplayKeys()
        val handler = Any()
        keys.markLoaded(handler, base)
        assertFalse(keys.isStale(handler, base.copy()))
        assertTrue(keys.isStale(handler, base.copy(density = 3.5f)))
        keys.markLoaded(handler, base.copy(density = 3.5f))
        assertFalse(keys.isStale(handler, base.copy(density = 3.5f)))
    }

    @Test
    fun unknownHandlerIsNotStale() {
        assertFalse(LoadedDisplayKeys().isStale(Any(), base))
    }

    @Test
    fun reloadSwapsOnlyWhenANewViewIsSet() {
        val old = Any()
        assertTrue(bannerViewSwapped(old, Any()))
        assertTrue(bannerViewSwapped(null, Any()))
        assertFalse(bannerViewSwapped(old, old))
        assertFalse(bannerViewSwapped(old, null))
    }
}
