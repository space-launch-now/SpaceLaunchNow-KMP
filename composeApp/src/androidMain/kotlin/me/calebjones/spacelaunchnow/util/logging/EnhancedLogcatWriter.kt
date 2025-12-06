package me.calebjones.spacelaunchnow.util.logging

import android.util.Log
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity

/**
 * Enhanced Android logcat writer with automatic file/line tracking
 *
 * Features:
 * - Adds "SLN-" prefix for easy filtering: `adb logcat SLN-*:V *:S`
 * - Color-coded by severity in Android Studio
 */
class EnhancedLogcatWriter : LogWriter(), ConfigurableLogWriter {
    override var minSeverity: Severity = Severity.Verbose

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        // Per-writer severity filtering (controlled by LoggingPreferences)
        if (severity < minSeverity) return

        val priority = severity.toAndroidLogPriority()
        val enhancedTag = "$tag"

        // Log message with automatic exception message appending
        if (throwable != null) {
            val exceptionMsg = throwable.message
            val enhancedMessage = if (exceptionMsg != null && exceptionMsg.isNotBlank()) {
                "$message: $exceptionMsg"
            } else {
                message
            }
            Log.println(priority, enhancedTag, "$enhancedMessage\n${Log.getStackTraceString(throwable)}")
        } else {
            Log.println(priority, enhancedTag, message)
        }
    }


    private fun Severity.toAndroidLogPriority(): Int = when (this) {
        Severity.Verbose -> Log.VERBOSE
        Severity.Debug -> Log.DEBUG
        Severity.Info -> Log.INFO
        Severity.Warn -> Log.WARN
        Severity.Error -> Log.ERROR
        Severity.Assert -> Log.ASSERT
    }
}

