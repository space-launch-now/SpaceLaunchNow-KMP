package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.Severity
import me.calebjones.spacelaunchnow.analytics.DatadogLogger
import me.calebjones.spacelaunchnow.analytics.DatadogRuntime
import me.calebjones.spacelaunchnow.getPlatform

/** Every structured `log_kind` the app emits (client-log-quality spec 4.2). */
enum class DiagnosticEventKind(val kind: String) {
    NSE_BREADCRUMB("nse_breadcrumb"),
    STARTUP_STATE("startup_state"),
    PUSH_SUMMARY("push_summary"),
    PUSH_RECONCILE("push_reconcile"),
    FILTER_DECISION("filter_decision"),
    NSE_PREFS_SYNC("nse_prefs_sync"),
    NOTIFICATION_DISPLAYED("notification_displayed"),
}

/**
 * Test seam over the direct Datadog upload path. Production uses [DefaultDatadogSink];
 * the desktop actuals of DatadogLogger/DatadogRuntime are no-ops, so tests install a
 * recording sink.
 */
interface DatadogSink {
    fun isRemoteLoggingActive(): Boolean

    /** Minimum severity the Kermit-to-Datadog writer currently uploads. */
    fun writerSeverity(): Severity

    fun upload(severity: Severity, message: String, attributes: Map<String, Any?>)
}

internal object DefaultDatadogSink : DatadogSink {
    override fun isRemoteLoggingActive(): Boolean = DatadogRuntime.isRemoteLoggingActive()

    override fun writerSeverity(): Severity = SpaceLogger.getDataDogSeverity()

    override fun upload(severity: Severity, message: String, attributes: Map<String, Any?>) {
        when (severity) {
            Severity.Verbose, Severity.Debug -> DatadogLogger.debug(message, attributes)
            Severity.Info -> DatadogLogger.info(message, attributes)
            Severity.Warn -> DatadogLogger.warn(message, attributes)
            Severity.Error, Severity.Assert -> DatadogLogger.error(message, null, attributes)
        }
    }
}

/**
 * The single emit path for structured diagnostic events. Always writes the Kermit line
 * (console/file, and Datadog at VERBOSE through DataDogLogWriter). When remote logging is
 * active and the Kermit writer would drop this severity (STANDARD drops below Warn), the
 * event also uploads directly. Never both, so VERBOSE does not double upload.
 * `upload = false` keeps the event Kermit-only.
 */
object DiagnosticEvents {

    internal var sink: DatadogSink = DefaultDatadogSink

    /**
     * @param label message text; defaults to `[EVENT] <kind>`. Only `push_summary` overrides it,
     * to keep the runbook string "Push registration summary".
     */
    fun emit(
        kind: DiagnosticEventKind,
        severity: Severity,
        tag: String,
        attrs: Map<String, Any?>,
        upload: Boolean = true,
        label: String? = null,
    ) {
        val message = label ?: "[EVENT] ${kind.kind}"
        val line = message + " " + attrs.entries.joinToString(" ") { "${it.key}=${it.value}" }
        // `tag` is the full "SLN-..." tag; getLogger adds the prefix itself.
        SpaceLogger.getLogger(tag.removePrefix("SLN-")).log(severity, tag, null, line)

        val s = sink
        if (upload && s.isRemoteLoggingActive() && s.writerSeverity() > severity) {
            s.upload(
                severity,
                message,
                attrs + mapOf(
                    "log_kind" to kind.kind,
                    "platform" to platformName,
                    "tag" to tag,
                ) + UserContext.getLogAttributes(),
            )
        }
    }

    private val platformName: String get() = getPlatform().type.name.lowercase()
}
