package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import me.calebjones.spacelaunchnow.data.notifications.v6.PushReconcileEvent
import me.calebjones.spacelaunchnow.data.notifications.v6.ReconcileFailureClass
import me.calebjones.spacelaunchnow.data.notifications.v6.ReconcileTrigger
import me.calebjones.spacelaunchnow.data.notifications.v6.V6ReconcileResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class RecordingSink(
    var active: Boolean = true,
    var severity: Severity = Severity.Warn,
) : DatadogSink {
    class Upload(val severity: Severity, val message: String, val attrs: Map<String, Any?>)

    val uploads = mutableListOf<Upload>()
    override fun isRemoteLoggingActive() = active
    override fun writerSeverity() = severity
    override fun upload(severity: Severity, message: String, attributes: Map<String, Any?>) {
        uploads += Upload(severity, message, attributes)
    }
}

private class LineWriter : LogWriter() {
    val lines = mutableListOf<Pair<Severity, String>>()
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        lines += severity to message
    }
}

class DiagnosticEventsTest {

    private val writer = LineWriter()
    private val sink = RecordingSink()

    @BeforeTest
    fun setUp() {
        PushDiagnostics.reset()
        SpaceLogger.initialize(LogConfig(Severity.Verbose, listOf(writer)))
        DiagnosticEvents.sink = sink
    }

    @AfterTest
    fun tearDown() {
        DiagnosticEvents.sink = DefaultDatadogSink
        PushDiagnostics.reset()
        SpaceLogger.initialize(LogConfig(Severity.Verbose, emptyList()))
    }

    private fun emitInfo(upload: Boolean = true) = DiagnosticEvents.emit(
        kind = DiagnosticEventKind.PUSH_RECONCILE,
        severity = Severity.Info,
        tag = "SLN-Test",
        attrs = mapOf("trigger" to "app_start"),
        upload = upload,
    )

    @Test
    fun standardUploadsAnInfoEventOnceAndWritesTheKermitLine() {
        sink.severity = Severity.Warn // STANDARD
        emitInfo()

        assertEquals(1, writer.lines.size)
        assertEquals(Severity.Info, writer.lines.single().first)
        assertEquals("[EVENT] push_reconcile trigger=app_start", writer.lines.single().second)
        val up = sink.uploads.single()
        assertEquals(Severity.Info, up.severity)
        assertEquals("[EVENT] push_reconcile", up.message)
        assertEquals("push_reconcile", up.attrs["log_kind"])
        assertEquals("SLN-Test", up.attrs["tag"])
        assertTrue(up.attrs.containsKey("platform"))
        assertEquals("app_start", up.attrs["trigger"])
    }

    @Test
    fun verboseDoesNotUploadDirectlyBecauseTheWriterAlreadyDoes() {
        sink.severity = Severity.Debug // VERBOSE
        emitInfo()

        assertEquals(1, writer.lines.size)
        assertTrue(sink.uploads.isEmpty())
    }

    @Test
    fun uploadFalseNeverUploads() {
        emitInfo(upload = false)
        assertEquals(1, writer.lines.size)
        assertTrue(sink.uploads.isEmpty())
    }

    @Test
    fun noUploadWhenRemoteLoggingIsInactive() {
        sink.active = false
        emitInfo()
        assertEquals(1, writer.lines.size)
        assertTrue(sink.uploads.isEmpty())
    }

    @Test
    fun warnIsLeftToTheWriterAtStandard() {
        sink.severity = Severity.Warn
        DiagnosticEvents.emit(DiagnosticEventKind.PUSH_RECONCILE, Severity.Warn, "SLN-Test", mapOf("a" to 1))
        assertTrue(sink.uploads.isEmpty())
    }

    // --- push_reconcile emission ---

    private fun reconcile(result: V6ReconcileResult, trigger: ReconcileTrigger) =
        PushReconcileEvent.emit(result, trigger, changeoverComplete = true, confirmedTopicCount = 7)

    @Test
    fun cleanPostApnsIsInfoWithTriggerTokenPresentAndConfirmedCount() {
        PushDiagnostics.recordTokenSuccess("fVeryLongRawFcmTokenValue_abcdef123456:APA91-xyzSUFFIX")
        reconcile(V6ReconcileResult(7, 0), ReconcileTrigger.POST_APNS)

        val up = sink.uploads.single()
        assertEquals(Severity.Info, up.severity)
        assertEquals("post_apns", up.attrs["trigger"])
        assertEquals("none", up.attrs["failure_class"])
        assertEquals(true, up.attrs["token_present"])
        assertEquals(7, up.attrs["confirmed_topic_count"])
        assertEquals(true, up.attrs["changeover_complete"])
        assertFalse(writer.lines.single().second.contains("SUFFIX"))
    }

    @Test
    fun apnsPendingIsDebugAndDoesNotUpload() {
        reconcile(V6ReconcileResult(4, 4, failureClass = ReconcileFailureClass.APNS_PENDING), ReconcileTrigger.APP_START)

        assertTrue(sink.uploads.isEmpty())
        assertEquals(Severity.Debug, writer.lines.single().first)
        assertTrue(writer.lines.single().second.contains("failure_class=apns_pending"))
        assertTrue(writer.lines.single().second.contains("trigger=app_start"))
    }

    @Test
    fun realFailureIsWarn() {
        sink.severity = Severity.Error // writer would drop Warn, so the direct upload shows it
        reconcile(V6ReconcileResult(4, 2, failureClass = ReconcileFailureClass.FCM_ERROR), ReconcileTrigger.SETTINGS)

        assertEquals(Severity.Warn, writer.lines.single().first)
        assertEquals(Severity.Warn, sink.uploads.single().severity)
        assertEquals("fcm_error", sink.uploads.single().attrs["failure_class"])
    }

    @Test
    fun skippedPassEmitsNothing() {
        reconcile(V6ReconcileResult(0, 0, skipped = true), ReconcileTrigger.APP_START)
        assertTrue(writer.lines.isEmpty())
        assertTrue(sink.uploads.isEmpty())
    }

    @Test
    fun everyTriggerHasItsSpecWireName() {
        assertEquals(
            listOf("app_start", "post_apns", "settings", "kill_switch", "token_refresh", "force", "debug"),
            ReconcileTrigger.entries.map { it.wire },
        )
    }
}
