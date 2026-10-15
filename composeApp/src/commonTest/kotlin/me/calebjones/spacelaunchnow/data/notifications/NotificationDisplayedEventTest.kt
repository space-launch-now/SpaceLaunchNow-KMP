package me.calebjones.spacelaunchnow.data.notifications

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import me.calebjones.spacelaunchnow.util.logging.DatadogSink
import me.calebjones.spacelaunchnow.util.logging.DefaultDatadogSink
import me.calebjones.spacelaunchnow.util.logging.DiagnosticEvents
import me.calebjones.spacelaunchnow.util.logging.LogConfig
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotificationDisplayedEventTest {

    private class Sink : DatadogSink {
        val uploads = mutableListOf<Pair<Severity, Map<String, Any?>>>()
        override fun isRemoteLoggingActive() = true
        override fun writerSeverity() = Severity.Warn
        override fun upload(severity: Severity, message: String, attributes: Map<String, Any?>) {
            uploads += severity to attributes
        }
    }

    private class Lines : LogWriter() {
        val lines = mutableListOf<Pair<Severity, String>>()
        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            lines += severity to message
        }
    }

    private val sink = Sink()
    private val writer = Lines()

    @BeforeTest
    fun setUp() {
        SpaceLogger.initialize(LogConfig(Severity.Verbose, listOf(writer)))
        DiagnosticEvents.sink = sink
    }

    @AfterTest
    fun tearDown() {
        DiagnosticEvents.sink = DefaultDatadogSink
        SpaceLogger.initialize(LogConfig(Severity.Verbose, emptyList()))
    }

    @Test
    fun emitsOneInfoEventWithTheSpecAttributes() {
        NotificationDisplayedEvent.emit(
            notificationType = "twentyFourHour",
            launchId = "abc-123",
            reason = LocalFilterPolicy.PASSTHROUGH_REASON,
            channel = "launch_imminent",
            hasImage = true,
        )

        val up = sink.uploads.single()
        assertEquals(Severity.Info, up.first)
        val a = up.second
        assertEquals("notification_displayed", a["log_kind"])
        assertEquals("twentyFourHour", a["notification_type"])
        assertEquals("abc-123", a["launch_id"])
        assertEquals(LocalFilterPolicy.PASSTHROUGH_REASON, a["reason"])
        assertEquals("launch_imminent", a["channel"])
        assertEquals(true, a["has_image"])
        assertEquals(1, writer.lines.size)
        assertTrue(writer.lines.single().second.contains("has_image=true"))
        assertFalse(a.containsKey("title"))
    }
}
