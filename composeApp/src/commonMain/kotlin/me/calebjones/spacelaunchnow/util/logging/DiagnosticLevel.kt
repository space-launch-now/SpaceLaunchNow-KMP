package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.Severity

/**
 * Single user-facing diagnostic control. Replaces the previous four independent
 * knobs (console severity, Datadog severity, Datadog toggle, sample rate).
 *
 * Persisted by name; do not rename constants.
 */
enum class DiagnosticLevel {
    OFF, STANDARD, VERBOSE;

    companion object {
        /**
         * Resolve level from storage. Falls back to the legacy `datadog_enabled`
         * boolean for users upgrading from the old toggle (true -> STANDARD).
         *
         * Unrecognized stored values are discarded and fall through to the legacy fallback.
         */
        fun fromStorage(stored: String?, legacyDatadogEnabled: Boolean?): DiagnosticLevel {
            stored?.let { name -> entries.firstOrNull { it.name == name }?.let { return it } }
            return if (legacyDatadogEnabled == true) STANDARD else OFF
        }
    }
}

/** What a diagnostic level means for each log sink. Single source of truth. */
data class DiagnosticPolicy(
    /** Datadog TrackingConsent GRANTED (true) vs NOT_GRANTED (false). */
    val remoteConsentGranted: Boolean,
    /** DataDogLogWriter minSeverity. */
    val remoteSeverity: Severity,
    /** Console/logcat writer minSeverity. */
    val consoleSeverity: Severity,
    /** Local diagnostics file writer minSeverity — file capture stays on even at OFF
     *  so the "Share logs" escape hatch has history from before the user opted in. */
    val fileSeverity: Severity,
)

fun DiagnosticLevel.policy(): DiagnosticPolicy = when (this) {
    DiagnosticLevel.OFF -> DiagnosticPolicy(
        remoteConsentGranted = false,
        remoteSeverity = Severity.Assert,
        consoleSeverity = Severity.Warn,
        fileSeverity = Severity.Info,
    )
    DiagnosticLevel.STANDARD -> DiagnosticPolicy(
        remoteConsentGranted = true,
        remoteSeverity = Severity.Warn,
        consoleSeverity = Severity.Warn,
        fileSeverity = Severity.Info,
    )
    DiagnosticLevel.VERBOSE -> DiagnosticPolicy(
        remoteConsentGranted = true,
        remoteSeverity = Severity.Debug,
        consoleSeverity = Severity.Debug,
        fileSeverity = Severity.Debug,
    )
}

fun DiagnosticLevel.displayLabel(): String = when (this) {
    DiagnosticLevel.OFF -> "Off"
    DiagnosticLevel.STANDARD -> "Standard"
    DiagnosticLevel.VERBOSE -> "Verbose"
}
