package me.calebjones.spacelaunchnow.ui.ads

import me.calebjones.spacelaunchnow.ui.ads.BannerFrame.Chrome
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
