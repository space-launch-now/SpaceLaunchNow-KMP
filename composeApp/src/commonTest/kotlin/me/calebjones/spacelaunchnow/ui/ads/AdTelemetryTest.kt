package me.calebjones.spacelaunchnow.ui.ads

import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AdTelemetryTest {

    @Test
    fun parsesCodeFromGoogleStyleMessage() {
        assertEquals("3", AdTelemetry.parseErrorCode("Error code: 3, message: No fill"))
        assertEquals("0", AdTelemetry.parseErrorCode("Ad failed to load. code=0"))
    }

    @Test
    fun parsesLeadingNumericCode() {
        assertEquals("2", AdTelemetry.parseErrorCode("2: Network error"))
    }

    @Test
    fun unknownWhenNoCode() {
        assertEquals("unknown", AdTelemetry.parseErrorCode(null))
        assertEquals("unknown", AdTelemetry.parseErrorCode("Something broke"))
    }

    @Test
    fun eventNamesFollowResult() {
        assertEquals("ad_loaded", AnalyticsEvent.AdLifecycle(AnalyticsEvent.AdResult.LOADED, "banner").name)
        assertEquals("ad_failed", AnalyticsEvent.AdLifecycle(AnalyticsEvent.AdResult.FAILED, "banner").name)
        assertEquals("ad_impression", AnalyticsEvent.AdLifecycle(AnalyticsEvent.AdResult.IMPRESSION, "banner").name)
        assertEquals("ad_clicked", AnalyticsEvent.AdLifecycle(AnalyticsEvent.AdResult.CLICKED, "banner").name)
    }

    @Test
    fun failureParamsCarryErrorOnlyWhenFailed() {
        val failed = AnalyticsEvent.AdLifecycle(
            AnalyticsEvent.AdResult.FAILED, "banner", size = "320x50", handler = "BANNER",
            errorCode = "3", errorMessage = "No fill",
        ).toParameters()
        assertEquals("3", failed["error_code"])
        assertEquals("No fill", failed["error_message"])
        assertEquals("320x50", failed["size"])

        val loaded = AnalyticsEvent.AdLifecycle(AnalyticsEvent.AdResult.LOADED, "banner").toParameters()
        assertFalse(loaded.containsKey("error_code"))
        assertEquals("banner", loaded["format"])
    }
}
