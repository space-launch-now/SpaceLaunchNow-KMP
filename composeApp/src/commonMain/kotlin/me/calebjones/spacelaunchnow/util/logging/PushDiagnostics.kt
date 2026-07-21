package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.Severity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlin.time.Instant
import me.calebjones.spacelaunchnow.analytics.DatadogLogger

/**
 * Runtime cache of the startup push-registration outcomes (spec 015 §3.4).
 * Written by the startup sequence; read synchronously by the push-registration
 * summary line and by platformNotificationDiagnostics() at report share-time.
 * Never holds the raw token — suffix/length only.
 */
data class PushDiagnosticsSnapshot(
    val tokenPresent: Boolean? = null,
    val tokenSuffix: String? = null,
    val tokenLength: Int? = null,
    val tokenRefreshedAtEpochSeconds: Long? = null,
    val tokenUnavailableReason: String? = null,
    val forwardedToRc: Boolean? = null,
    val forwardedAtEpochSeconds: Long? = null,
    val forwardSkipReason: String? = null,
    val notificationsEnabled: Boolean? = null,
    val playServices: PlayServicesAvailability? = null,
    val subscribedTopicCount: Int? = null,
)

object PushDiagnostics {

    private val log get() = SpaceLogger.getLogger("PushDiagnostics")
    private val _snapshot = MutableStateFlow(PushDiagnosticsSnapshot())

    val snapshot: PushDiagnosticsSnapshot get() = _snapshot.value

    fun recordTokenSuccess(token: String, nowEpochSeconds: Long = Clock.System.now().epochSeconds) {
        _snapshot.update {
            it.copy(
                tokenPresent = true,
                tokenSuffix = token.takeLast(6),
                tokenLength = token.length,
                tokenRefreshedAtEpochSeconds = nowEpochSeconds,
                tokenUnavailableReason = null,
            )
        }
    }

    fun recordTokenUnavailable(reason: String) {
        _snapshot.update {
            it.copy(tokenPresent = false, tokenSuffix = null, tokenLength = null, tokenUnavailableReason = reason)
        }
    }

    fun recordForwardedToRc(nowEpochSeconds: Long = Clock.System.now().epochSeconds) {
        _snapshot.update {
            it.copy(forwardedToRc = true, forwardedAtEpochSeconds = nowEpochSeconds, forwardSkipReason = null)
        }
    }

    fun recordForwardSkipped(reason: String) {
        _snapshot.update { it.copy(forwardedToRc = false, forwardSkipReason = reason) }
    }

    fun recordNotificationsEnabled(enabled: Boolean) {
        _snapshot.update { it.copy(notificationsEnabled = enabled) }
    }

    fun recordPlayServices(state: PlayServicesAvailability) {
        _snapshot.update { it.copy(playServices = state) }
    }

    fun recordSubscribedTopicCount(count: Int) {
        _snapshot.update { it.copy(subscribedTopicCount = count) }
    }

    fun reset() {
        _snapshot.value = PushDiagnosticsSnapshot()
    }

    /** Attributes for the structured "Push registration summary" line (spec 015 §3.2). */
    fun summaryAttributes(snapshot: PushDiagnosticsSnapshot, diagnosticLevelName: String?): Map<String, Any?> = buildMap {
        put("push.token_present", snapshot.tokenPresent ?: false)
        put("push.token_suffix", snapshot.tokenSuffix ?: "")
        put("push.token_length", snapshot.tokenLength ?: 0)
        put("push.forwarded_to_rc", snapshot.forwardedToRc ?: false)
        snapshot.forwardSkipReason?.let { put("push.forward_skip_reason", it) }
        put("push.notifications_enabled", snapshot.notificationsEnabled ?: "unknown")
        put("push.play_services", (snapshot.playServices ?: PlayServicesAvailability.UNKNOWN).label())
        put("push.subscribed_topic_count", snapshot.subscribedTopicCount ?: 0)
        put("diagnostics.level", diagnosticLevelName ?: "unknown")
    }

    /** Label/value rows for the shareable diagnostics report (spec 015 §4). */
    fun reportRows(
        snapshot: PushDiagnosticsSnapshot,
        notificationsEnabled: Boolean?,
        playServices: PlayServicesAvailability?,
    ): List<Pair<String, String>> = buildList {
        add("Notifications enabled" to (notificationsEnabled?.toString() ?: "unknown"))
        add("Play Services" to (playServices ?: PlayServicesAvailability.UNKNOWN).label())
        add(
            "FCM token" to if (snapshot.tokenPresent == true) {
                "present …${snapshot.tokenSuffix} (len ${snapshot.tokenLength})"
            } else {
                "unavailable"
            }
        )
        add(
            "FCM token refreshed" to (
                snapshot.tokenRefreshedAtEpochSeconds
                    ?.let { Instant.fromEpochSeconds(it).toString() } ?: "never"
                )
        )
        add(
            "Forwarded to RevenueCat" to when {
                snapshot.forwardedToRc == true ->
                    "yes @${snapshot.forwardedAtEpochSeconds?.let { Instant.fromEpochSeconds(it).toString() } ?: "?"}"
                snapshot.forwardSkipReason != null -> "skipped: ${snapshot.forwardSkipReason}"
                else -> "no"
            }
        )
        add("Subscribed topics" to (snapshot.subscribedTopicCount?.toString() ?: "unknown"))
    }

    /**
     * Emit the once-per-cold-start summary. The Kermit line covers console/file (and
     * Datadog at VERBOSE). At STANDARD the DataDogLogWriter threshold is Warn and would
     * drop Info, so the structured copy goes directly to DatadogLogger — consent still
     * blocks it entirely at OFF. The severity gate prevents double upload at VERBOSE.
     */
    fun logSummary(diagnosticLevelName: String?) {
        val attrs = summaryAttributes(snapshot, diagnosticLevelName)
        log.i { "Push registration summary " + attrs.entries.joinToString(" ") { "${it.key}=${it.value}" } }
        if (SpaceLogger.getDataDogSeverity() > Severity.Info) {
            DatadogLogger.info(
                "Push registration summary",
                attrs + mapOf("tag" to "SLN-PushDiagnostics") + UserContext.getLogAttributes(),
            )
        }
    }
}

private fun PlayServicesAvailability.label(): String = when (this) {
    PlayServicesAvailability.AVAILABLE -> "available"
    PlayServicesAvailability.MISSING -> "missing"
    PlayServicesAvailability.UPDATE_REQUIRED -> "update_required"
    PlayServicesAvailability.NOT_APPLICABLE -> "n/a"
    PlayServicesAvailability.UNKNOWN -> "unknown"
}
