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
        delegate.log(severity, message, tag, throwable)
    }
}
