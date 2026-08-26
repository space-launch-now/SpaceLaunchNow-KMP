package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.crashlytics.CrashlyticsLogWriter

/**
 * Wrapper around Kermit's CrashlyticsLogWriter that adds configurable severity support.
 * On iOS, this forwards logs to Firebase Crashlytics via the kermit-crashlytics library.
 */
class FirebaseCrashlyticsLogWriter : LogWriter(), ConfigurableLogWriter {
    override var minSeverity: Severity = Severity.Warn

    private val delegate = CrashlyticsLogWriter()

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        if (severity < minSeverity) return

        // Kermit's CrashlyticsLogWriter writes the breadcrumb AND records the non-fatal in one
        // opaque call, so cancellation is suppressed by dropping the throwable rather than the
        // call - skipping the delegate entirely would lose the breadcrumb too (issue #169).
        val reportable = if (throwable.isCoroutineCancellation()) null else throwable

        delegate.log(severity, message, tag, reportable)
    }
}
