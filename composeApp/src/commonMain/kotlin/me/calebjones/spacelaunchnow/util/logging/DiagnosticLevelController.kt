package me.calebjones.spacelaunchnow.util.logging

import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
            // Persist any lapsed verbose window (or stamp a legacy one) before observing.
            prefs.enforceVerboseExpiry()
            val controllerScope = this
            var expiryTimer: Job? = null
            prefs.getDiagnosticSettings().collect { settings ->
                apply(settings.level)
                // Running-session auto-revert: fire at the expiry instant. Relaunches
                // are covered by enforceVerboseExpiry() above and read-time resolution.
                expiryTimer?.cancel()
                val expiresAt = settings.verboseExpiresAtEpochSeconds
                if (settings.level == DiagnosticLevel.VERBOSE && expiresAt != null) {
                    expiryTimer = controllerScope.launch {
                        val waitMs = (expiresAt - Clock.System.now().epochSeconds) * 1000
                        if (waitMs > 0) delay(waitMs)
                        prefs.enforceVerboseExpiry() // DataStore write re-emits the flow -> apply() runs with the reverted level
                    }
                }
            }
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
