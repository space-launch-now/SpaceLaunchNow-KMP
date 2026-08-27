package me.calebjones.spacelaunchnow.util.logging

import android.util.Log
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Bridge between Kermit logging and Firebase Crashlytics.
 * Forwards warning+ logs as custom log lines and reports exceptions as non-fatal crashes.
 */
class FirebaseCrashlyticsLogWriter : LogWriter(), ConfigurableLogWriter {
    override var minSeverity: Severity = Severity.Warn

    private val crashlytics: FirebaseCrashlytics?
        get() = try {
            FirebaseCrashlytics.getInstance()
        } catch (e: Exception) {
            Log.w("CrashlyticsWriter", "FirebaseCrashlytics not available", e)
            null
        }

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        if (severity < minSeverity) return

        val instance = crashlytics ?: return

        instance.log("[$tag] $message")

        // Coroutine cancellation is normal control flow, not a defect - keep the breadcrumb
        // above, but never spend a non-fatal report on it (issue #169).
        if (throwable != null && severity >= Severity.Error && !throwable.isCoroutineCancellation()) {
            instance.recordException(throwable)
        }
    }
}
