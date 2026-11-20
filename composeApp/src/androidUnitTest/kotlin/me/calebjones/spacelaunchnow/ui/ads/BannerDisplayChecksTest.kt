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
}
