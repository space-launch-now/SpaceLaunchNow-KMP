package me.calebjones.spacelaunchnow.util.logging

import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.repository.RemoteConfigRepository
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences

/**
 * Fetch → resolve → apply loop for remote diagnostics control
 * (REMOTE_LOG_SAMPLING_SPEC §4.4). Waits for the RevenueCat user id (fed by the
 * billing managers via UserContext), force-refreshes remote config once at
 * startup, then re-asserts every [REASSERT_INTERVAL_MS] so an active override
 * outlives its 72h backstop only while the config still requests it.
 *
 * Safe-by-default: a failed fetch leaves the last-known overrides untouched; a
 * successful fetch with a missing/malformed config or no matching override
 * clears them, reverting the device to its local settings.
 */
class RemoteDiagnosticsController(
    private val remoteConfigRepository: RemoteConfigRepository,
    private val loggingPreferences: LoggingPreferences,
    private val debugPreferences: DebugPreferences?,
) {
    private val log by lazy { SpaceLogger.getLogger("RemoteDiagnosticsController") }
    private var job: Job? = null

    fun start(scope: CoroutineScope = CoroutineScope(Dispatchers.Default)) {
        job?.cancel()
        job = scope.launch {
            UserContext.revenueCatUserId.collectLatest { rcUserId ->
                if (rcUserId == null) return@collectLatest
                var forceRefresh = true
                while (isActive) {
                    refreshOnce(rcUserId, forceRefresh)
                    forceRefresh = false
                    delay(REASSERT_INTERVAL_MS)
                }
            }
        }
    }

    suspend fun refreshOnce(rcUserId: String, forceRefresh: Boolean) {
        try {
            val fetch = remoteConfigRepository.fetchAndActivate(forceRefresh)
            if (fetch.isFailure) {
                log.d { "Remote config fetch failed; keeping current diagnostics overrides" }
                return
            }
            val config = parseDiagnosticsConfig(remoteConfigRepository.getDiagnosticsConfigJson())
            val resolved = resolveDiagnostics(config, rcUserId, Clock.System.now().epochSeconds)
            debugPreferences?.setRemoteDatadogSampleRateOverride(resolved.sampleRate)
            loggingPreferences.setRemoteDiagnosticLevelOverride(resolved.diagnosticLevel)
            if (resolved.sampleRate != null || resolved.diagnosticLevel != null) {
                log.i {
                    "Remote diagnostics override applied: sampleRate=${resolved.sampleRate}, " +
                        "level=${resolved.diagnosticLevel}"
                }
            }
        } catch (e: Exception) {
            log.w(e) { "Remote diagnostics refresh failed" }
        }
    }

    companion object {
        /** Re-assert cadence; must be well under the 72h override backstop. */
        const val REASSERT_INTERVAL_MS: Long = 6L * 60 * 60 * 1000
    }
}
