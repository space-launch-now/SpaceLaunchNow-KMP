package me.calebjones.spacelaunchnow.ui.ads

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BannerFrameTest {

    @Test
    fun cardChromeIsWiderThanNavChrome() {
        assertEquals(32f, BannerFrame.chromeWidthDp(showCard = true))
        assertEquals(16f, BannerFrame.chromeWidthDp(showCard = false))
    }

    @Test
    fun largeBannerFitsInACardOnA360dpPhone() {
        // 360 - 32 = 328 dp >= 320 dp
        assertTrue(BannerFrame.fits(320, 100, 360f, BannerFrame.chromeWidthDp(true)))
    }

    @Test
    fun largeBannerDoesNotFitWhenTheSlotIsNarrowerThanTheAd() {
        // The old Home slot: 360 dp phone, caller padding applied twice -> 264 dp for a 320 dp ad
        assertFalse(BannerFrame.fits(320, 100, 360f - 64f, BannerFrame.chromeWidthDp(true)))
    }

    @Test
    fun exactFitIsAllowed() {
        assertTrue(BannerFrame.fits(320, 50, 336f, BannerFrame.chromeWidthDp(false)))
        assertFalse(BannerFrame.fits(320, 50, 335.9f, BannerFrame.chromeWidthDp(false)))
    }

    @Test
    fun sizesWithoutAFixedFrameNeverFit() {
        // FLUID (-2 height) and INVALID (0x0) have no fixed frame to host
        assertFalse(BannerFrame.fits(-1, -2, 1000f, 0f))
        assertFalse(BannerFrame.fits(0, 0, 1000f, 0f))
    }
}
