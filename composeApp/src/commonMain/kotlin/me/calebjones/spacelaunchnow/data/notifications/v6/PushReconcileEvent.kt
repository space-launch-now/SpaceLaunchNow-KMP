package me.calebjones.spacelaunchnow.data.notifications.v6

import co.touchlab.kermit.Severity
import me.calebjones.spacelaunchnow.util.logging.DiagnosticEventKind
import me.calebjones.spacelaunchnow.util.logging.DiagnosticEvents
import me.calebjones.spacelaunchnow.util.logging.PushDiagnostics

/**
 * The one structured `push_reconcile` event per finished reconcile pass (client-log-quality
 * spec 4.2). Attributes only carry counts, flags and enum names: no token text, no topic
 * names, no ID sets.
 */
internal object PushReconcileEvent {

    /** warn on a real failure; debug and no upload for the APNs precondition; info otherwise. */
    fun severityFor(result: V6ReconcileResult): Severity = when {
        result.failed > 0 && result.failureClass == ReconcileFailureClass.APNS_PENDING -> Severity.Debug
        result.failed > 0 -> Severity.Warn
        else -> Severity.Info
    }

    fun emit(
        result: V6ReconcileResult,
        trigger: ReconcileTrigger,
        changeoverComplete: Boolean,
        confirmedTopicCount: Int,
        stateAttrs: Map<String, Any?> = emptyMap(),
    ) {
        if (result.skipped) return
        val severity = severityFor(result)
        DiagnosticEvents.emit(
            kind = DiagnosticEventKind.PUSH_RECONCILE,
            severity = severity,
            tag = "SLN-PushReconcile",
            attrs = mapOf(
                "trigger" to trigger.wire,
                "attempted" to result.attempted,
                "failed" to result.failed,
                "skipped" to result.skipped,
                "failure_class" to result.failureClass.wire,
                "changeover_complete" to changeoverComplete,
                "confirmed_topic_count" to confirmedTopicCount,
                "token_present" to (PushDiagnostics.snapshot.tokenPresent ?: false),
            ) + stateAttrs,
            upload = severity != Severity.Debug,
        )
    }
}
