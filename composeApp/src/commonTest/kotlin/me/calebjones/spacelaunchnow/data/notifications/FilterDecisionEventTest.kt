package me.calebjones.spacelaunchnow.data.notifications

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import me.calebjones.spacelaunchnow.data.model.NotificationState
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

class FilterDecisionEventTest {

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
    private val data = mapOf(
        "notification_type" to "twentyFourHour",
        "agency_id" to "121",
        "location_id" to "143",
        "launch_name" to "Starlink Group 99",
    )
    private val state = NotificationState(
        subscribedAgencies = setOf("121", "44"),
        subscribedLocations = setOf("143"),
        useStrictMatching = true,
        followAllLaunches = false,
        hasCompletedV6Changeover = true,
    )

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
    fun shownPassthroughIsOneInfoEventWithCountsOnly() {
        FilterDecisionEvent.emit(data, state, shown = true, passthrough = true)

        val up = sink.uploads.single()
        assertEquals(Severity.Info, up.first)
        val a = up.second
        assertEquals("filter_decision", a["log_kind"])
        assertEquals("shown", a["decision"])
        assertEquals(LocalFilterPolicy.PASSTHROUGH_REASON, a["reason"])
        assertEquals("twentyFourHour", a["notification_type"])
        assertEquals("121", a["agency_id"])
        assertEquals("143", a["location_id"])
        assertEquals(true, a["v6_passthrough"])
        assertEquals(true, a["strict"])
        assertEquals(false, a["follow_all"])
        assertEquals(2, a["agency_count"])
        assertEquals(1, a["location_count"])
        assertEquals(1, writer.lines.size)
        assertFalse(writer.lines.single().second.contains("Starlink"))
    }

    @Test
    fun suppressedIsWarn() {
        val v5 = state.copy(hasCompletedV6Changeover = false)
        FilterDecisionEvent.emit(data, v5, shown = false, passthrough = false)

        // Warn is within the Kermit writer's range at STANDARD, so no direct upload.
        assertTrue(sink.uploads.isEmpty())
        assertEquals(Severity.Warn, writer.lines.single().first)
        assertTrue(writer.lines.single().second.contains("decision=suppressed reason=filtered"))
    }

    @Test
    fun reasonDistinguishesKillSwitch() {
        val off = state.copy(enableNotifications = false)
        assertEquals("notifications_disabled", FilterDecisionEvent.reasonFor(false, true, off))
        assertEquals("filter_passed", FilterDecisionEvent.reasonFor(true, false, state))
    }
}
