package me.calebjones.spacelaunchnow.ui.ads

import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdFrameCheckTest {

    @Test
    fun noReportWhenSizesMatch() {
        assertFalse(AdFrameCheck.mismatch(1080 to 150, 1080 to 150))
        assertFalse(AdFrameCheck.mismatch(1080 to 150, 1080 to 150, 1080 to 150))
    }

    @Test
    fun reportWhenRealSizeDiffers() {
        assertTrue(AdFrameCheck.mismatch(1080 to 150, 1000 to 150))
        assertTrue(AdFrameCheck.mismatch(1080 to 150, 1080 to 120))
    }

    @Test
    fun reportWhenPartIsClipped() {
        assertTrue(AdFrameCheck.mismatch(1080 to 150, 1080 to 150, 1080 to 90))
        assertTrue(AdFrameCheck.mismatch(1080 to 150, 1080 to 150, 700 to 150))
    }

    @Test
    fun noReportBeforeTheAdHasASize() {
        assertFalse(AdFrameCheck.mismatch(0 to 0, 1080 to 150))
    }

    @Test
    fun limiterAllowsOnePerPlacement() {
        FrameMismatchLimiter.reset()
        assertTrue(FrameMismatchLimiter.shouldReport("NAVIGATION"))
        assertFalse(FrameMismatchLimiter.shouldReport("NAVIGATION"))
        assertTrue(FrameMismatchLimiter.shouldReport("INLINE"))
        FrameMismatchLimiter.reset()
        assertTrue(FrameMismatchLimiter.shouldReport("NAVIGATION"))
    }

    @Test
    fun eventCarriesAllParams() {
        val event = AnalyticsEvent.AdFrameMismatch("NAVIGATION", "1080x150", "1000x150", "412x915", "portrait")
        assertEquals("ad_frame_mismatch", event.name)
        assertEquals(
            mapOf(
                "placement" to "NAVIGATION", "loaded_size" to "1080x150", "real_size" to "1000x150",
                "window_dp" to "412x915", "orientation" to "portrait",
            ),
            event.toParameters(),
        )
    }
}
