package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class CapturingWriter : LogWriter() {
    val entries = mutableListOf<Pair<Severity, String>>()
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        entries += severity to message
    }
}

class PushSummaryLoggingTest {

    private val writer = CapturingWriter()
    private val rawToken = "fVeryLongRawFcmTokenValue_abcdef123456:APA91-xyzSUFFIX"

    @BeforeTest
    fun setUp() {
        PushDiagnostics.reset()
        SpaceLogger.initialize(LogConfig(Severity.Verbose, listOf(writer)))
    }

    @AfterTest
    fun tearDown() {
        PushDiagnostics.reset()
        SpaceLogger.initialize(LogConfig(Severity.Verbose, emptyList()))
    }

    @Test
    fun summaryLogsAtInfoWithAttributesInMessage() {
        PushDiagnostics.recordTokenSuccess(rawToken)
        PushDiagnostics.recordNotificationsEnabled(true)
        PushDiagnostics.recordPlayServices(PlayServicesAvailability.AVAILABLE)

        PushDiagnostics.logSummary("STANDARD")

        val summary = writer.entries.single { it.second.startsWith("Push registration summary") }
        assertEquals(Severity.Info, summary.first)
        assertTrue(summary.second.contains("push.token_present=true"))
        assertTrue(summary.second.contains("push.play_services=available"))
        assertTrue(summary.second.contains("diagnostics.level=STANDARD"))
        assertFalse(summary.second.contains(rawToken))
    }
}
