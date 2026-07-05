package me.calebjones.spacelaunchnow.util.logging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.analytics.DatadogRuntime

/**
 * Applies the user's DiagnosticLevel to every log sink: Datadog consent,
 * remote writer severity, console severity, and the diagnostics file writer.
 * Started once at app startup; also invoked directly on user toggle for
 * immediate effect.
 */
object DiagnosticLevelController {
    private var job: Job? = null

    fun start(prefs: LoggingPreferences, scope: CoroutineScope = CoroutineScope(Dispatchers.Default)) {
        job?.cancel()
        job = scope.launch {
            prefs.getDiagnosticLevel().collect { level -> apply(level) }
        }
    }

    fun apply(level: DiagnosticLevel) {
        val policy = level.policy()
        DatadogRuntime.setConsentGranted(policy.remoteConsentGranted)
        SpaceLogger.setConsoleSeverity(policy.consoleSeverity)
        SpaceLogger.setDataDogSeverity(policy.remoteSeverity)
        DiagnosticsLog.writer?.minSeverity = policy.fileSeverity
    }
}
