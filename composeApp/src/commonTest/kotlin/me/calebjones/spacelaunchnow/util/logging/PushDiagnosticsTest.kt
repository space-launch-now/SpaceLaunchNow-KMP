package me.calebjones.spacelaunchnow.util.logging

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PushDiagnosticsTest {

    private val rawToken = "fVeryLongRawFcmTokenValue_abcdef123456:APA91-xyzSUFFIX"

    @BeforeTest
    fun setUp() = PushDiagnostics.reset()

    @AfterTest
    fun tearDown() = PushDiagnostics.reset()

    @Test
    fun tokenSuccessStoresOnlySuffixAndLength() {
        PushDiagnostics.recordTokenSuccess(rawToken, nowEpochSeconds = 1_752_000_000)
        val snap = PushDiagnostics.snapshot
        assertEquals(true, snap.tokenPresent)
        assertEquals("SUFFIX", snap.tokenSuffix)
        assertEquals(rawToken.length, snap.tokenLength)
        assertEquals(1_752_000_000, snap.tokenRefreshedAtEpochSeconds)
    }

    @Test
    fun rawTokenNeverAppearsInSummaryAttributesOrReportRows() {
        PushDiagnostics.recordTokenSuccess(rawToken)
        val attrs = PushDiagnostics.summaryAttributes(PushDiagnostics.snapshot, "STANDARD")
        val rows = PushDiagnostics.reportRows(PushDiagnostics.snapshot, true, PlayServicesAvailability.AVAILABLE)
        assertFalse(attrs.values.any { it.toString().contains(rawToken) })
        assertFalse(rows.any { it.second.contains(rawToken) })
    }

    @Test
    fun summaryAttributesHealthyPath() {
        PushDiagnostics.recordTokenSuccess(rawToken)
        PushDiagnostics.recordForwardedToRc(nowEpochSeconds = 1_752_000_100)
        PushDiagnostics.recordNotificationsEnabled(true)
        PushDiagnostics.recordPlayServices(PlayServicesAvailability.AVAILABLE)
        PushDiagnostics.recordSubscribedTopicCount(2)

        val attrs = PushDiagnostics.summaryAttributes(PushDiagnostics.snapshot, "STANDARD")
        assertEquals(true, attrs["push.token_present"])
        assertEquals("SUFFIX", attrs["push.token_suffix"])
        assertEquals(rawToken.length, attrs["push.token_length"])
        assertEquals(true, attrs["push.forwarded_to_rc"])
        assertEquals(true, attrs["push.notifications_enabled"])
        assertEquals("available", attrs["push.play_services"])
        assertEquals(2, attrs["push.subscribed_topic_count"])
        assertEquals("STANDARD", attrs["diagnostics.level"])
    }

    @Test
    fun summaryAttributesBrokenPath() {
        PushDiagnostics.recordTokenUnavailable("null_or_blank")
        PushDiagnostics.recordForwardSkipped("token_unavailable")
        PushDiagnostics.recordNotificationsEnabled(false)
        PushDiagnostics.recordPlayServices(PlayServicesAvailability.MISSING)

        val attrs = PushDiagnostics.summaryAttributes(PushDiagnostics.snapshot, null)
        assertEquals(false, attrs["push.token_present"])
        assertEquals(false, attrs["push.forwarded_to_rc"])
        assertEquals("token_unavailable", attrs["push.forward_skip_reason"])
        assertEquals(false, attrs["push.notifications_enabled"])
        assertEquals("missing", attrs["push.play_services"])
        assertEquals(0, attrs["push.subscribed_topic_count"])
        assertEquals("unknown", attrs["diagnostics.level"])
    }

    @Test
    fun reportRowsSeededSnapshot() {
        PushDiagnostics.recordTokenSuccess(rawToken, nowEpochSeconds = 1_752_000_000)
        PushDiagnostics.recordForwardedToRc(nowEpochSeconds = 1_752_000_100)
        PushDiagnostics.recordSubscribedTopicCount(2)

        val rows = PushDiagnostics.reportRows(PushDiagnostics.snapshot, true, PlayServicesAvailability.AVAILABLE)
        val byLabel = rows.toMap()
        assertEquals("true", byLabel["Notifications enabled"])
        assertEquals("available", byLabel["Play Services"])
        assertEquals("present …SUFFIX (len ${rawToken.length})", byLabel["FCM token"])
        assertTrue(byLabel.getValue("FCM token refreshed").startsWith("2025"))
        assertTrue(byLabel.getValue("Forwarded to RevenueCat").startsWith("yes @"))
        assertEquals("2", byLabel["Subscribed topics"])
    }

    @Test
    fun reportRowsUnavailableTokenAndSkip() {
        PushDiagnostics.recordTokenUnavailable("null_or_blank")
        PushDiagnostics.recordForwardSkipped("token_unavailable")

        val rows = PushDiagnostics.reportRows(PushDiagnostics.snapshot, false, PlayServicesAvailability.NOT_APPLICABLE)
        val byLabel = rows.toMap()
        assertEquals("false", byLabel["Notifications enabled"])
        assertEquals("n/a", byLabel["Play Services"])
        assertEquals("unavailable", byLabel["FCM token"])
        assertEquals("never", byLabel["FCM token refreshed"])
        assertEquals("skipped: token_unavailable", byLabel["Forwarded to RevenueCat"])
        assertEquals("unknown", byLabel["Subscribed topics"])
    }

    @Test
    fun reportRowsDefaultsWhenNothingRecorded() {
        val rows = PushDiagnostics.reportRows(PushDiagnostics.snapshot, null, null)
        val byLabel = rows.toMap()
        assertEquals("unknown", byLabel["Notifications enabled"])
        assertEquals("unknown", byLabel["Play Services"])
        assertEquals("unavailable", byLabel["FCM token"])
        assertEquals("no", byLabel["Forwarded to RevenueCat"])
    }
}
