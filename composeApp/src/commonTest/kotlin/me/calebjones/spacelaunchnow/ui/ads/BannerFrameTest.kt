package me.calebjones.spacelaunchnow.ui.ads

import me.calebjones.spacelaunchnow.ui.ads.BannerFrame.Chrome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BannerFrameTest {

    @Test
    fun chromeWidthsMatchThePaddingConstants() {
        assertEquals(32, Chrome.CARD.widthDp)
        assertEquals(16, Chrome.PADDED.widthDp)
        assertEquals(0, Chrome.BARE.widthDp)
    }

    @Test
    fun cardWhenTheSlotIsWideEnough() {
        // 360 dp phone, no parent padding: 360 - 32 = 328 >= 320
        assertEquals(Chrome.CARD, BannerFrame.chromeFor(320, 100, 360f, preferCard = true))
    }

    @Test
    fun dropsChromeWhenTheCardDoesNotFit() {
        // 360 dp phone inside a 16 dp-a-side parent: slot 328; card leaves 296, padded leaves 312,
        // so a 320 dp banner goes bare and a 300 dp rectangle keeps its padding
        assertEquals(Chrome.BARE, BannerFrame.chromeFor(320, 100, 328f, preferCard = true))
        assertEquals(Chrome.PADDED, BannerFrame.chromeFor(300, 250, 328f, preferCard = true))
    }

    @Test
    fun bareAtAnExactFit() {
        assertEquals(Chrome.BARE, BannerFrame.chromeFor(320, 50, 320f, preferCard = true))
        assertEquals(Chrome.PADDED, BannerFrame.chromeFor(320, 50, 336f, preferCard = false))
    }

    @Test
    fun noneWhenTheSlotIsNarrowerThanTheAd() {
        assertEquals(Chrome.NONE, BannerFrame.chromeFor(320, 50, 319.9f, preferCard = true))
        assertEquals(Chrome.NONE, BannerFrame.chromeFor(320, 50, 319.9f, preferCard = false))
    }

    @Test
    fun navSlotsNeverGetACard() {
        assertEquals(Chrome.PADDED, BannerFrame.chromeFor(320, 50, 1000f, preferCard = false))
    }

    @Test
    fun sizesWithoutAFixedFrameNeverShow() {
        // FLUID (-2 height) and INVALID (0x0) have no fixed frame to host
        assertEquals(Chrome.NONE, BannerFrame.chromeFor(-1, -2, 1000f, preferCard = true))
        assertEquals(Chrome.NONE, BannerFrame.chromeFor(0, 0, 1000f, preferCard = true))
    }

    @Test
    fun noneWhenTheHostCannotGiveTheFullHeight() {
        assertEquals(Chrome.NONE, BannerFrame.chromeFor(320, 100, 360f, 99f, preferCard = true))
        assertEquals(Chrome.CARD, BannerFrame.chromeFor(320, 100, 360f, 100f, preferCard = true))
    }

    @Test
    fun noneWhenTheHostCannotGiveTheFullWidth() {
        assertEquals(Chrome.NONE, BannerFrame.chromeFor(320, 50, 319f, 500f, preferCard = true))
    }

    @Test
    fun unboundedHeightIsNotALimit() {
        assertEquals(Chrome.BARE, BannerFrame.chromeFor(320, 50, 320f, Float.POSITIVE_INFINITY, preferCard = true))
        assertEquals(Chrome.BARE, BannerFrame.chromeFor(320, 50, 320f, -1f, preferCard = true))
    }

    @Test
    fun fitsNeedsBothWidthAndHeight() {
        assertTrue(BannerFrame.fits(320, 50, 320f, 50f))
        assertFalse(BannerFrame.fits(320, 50, 320f, 49f))
        assertFalse(BannerFrame.fits(320, 50, 319f, 50f))
        assertFalse(BannerFrame.fits(0, 50, 400f, 400f))
    }
}
