package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.Severity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.notifications.v6.SubscriptionCounts
import me.calebjones.spacelaunchnow.data.notifications.v6.V6Topics
import kotlin.time.Clock
import kotlin.time.Instant

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
    val lastCleanReconcileEpochSeconds: Long? = null,
    val diagnosticLevelName: String? = null,
    /** `push.*` state attributes from the last emit (see [PushDiagnostics.stateAttributes]); feeds the report rows. */
    val pushState: Map<String, Any?>? = null,
)

/** Value of `push.reconcile_state` (spec S3 D8). */
enum class PushReconcileState(val wire: String) {
    COMPLETE("complete"),
    PENDING("pending"),
    SKIPPED("skipped"),
}

object PushDiagnostics {

    private val log get() = SpaceLogger.getLogger("PushDiagnostics")
    private val _snapshot = MutableStateFlow(PushDiagnosticsSnapshot())

    // True once the startup sequence has recorded token, permission, Play Services and the
    // diagnostic level. push_summary waits for it so it never reads half-written records.
    private val _startupRecordsReady = MutableStateFlow(false)

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

    fun recordCleanReconcile(nowEpochSeconds: Long = Clock.System.now().epochSeconds) {
        _snapshot.update { it.copy(lastCleanReconcileEpochSeconds = nowEpochSeconds) }
    }

    fun recordDiagnosticLevel(levelName: String?) {
        _snapshot.update { it.copy(diagnosticLevelName = levelName) }
    }

    fun recordPushState(attrs: Map<String, Any?>) {
        _snapshot.update { it.copy(pushState = attrs) }
    }

    /** Called by the startup sequence once its `record*` calls are done (also when it failed part way). */
    fun recordStartupRecordsReady() {
        _startupRecordsReady.value = true
    }

    val startupRecordsReady: Boolean get() = _startupRecordsReady.value

    suspend fun awaitStartupRecords() {
        _startupRecordsReady.first { it }
    }

    fun reset() {
        _snapshot.value = PushDiagnosticsSnapshot()
        _startupRecordsReady.value = false
    }

    /**
     * Subscription-state attributes shared by `push_summary` and `push_reconcile` (spec S3 D9).
     * Read from persisted state and the ledger counts at emit time, never from in-memory state.
     * Counts and enum names only: no ID sets, no topic names.
     */
    fun stateAttributes(
        state: NotificationState,
        counts: SubscriptionCounts,
        changeoverComplete: Boolean,
    ): Map<String, Any?> = linkedMapOf(
        "push.enable_notifications" to state.enableNotifications,
        "push.audience_class" to V6Topics.audienceClass(state),
        "push.follow_all" to state.followAllLaunches,
        "push.strict" to state.useStrictMatching,
        "push.mute_starlink" to state.muteStarlink,
        "push.webcast_only" to state.isTopicEnabled(NotificationTopic.WEBCAST_ONLY),
        "push.enabled_types" to V6Topics.enabledLaunchTypes(state).joinToString(",") { it.id },
        "push.enabled_broadcasts" to V6Topics.enabledBroadcastTokens(state).joinToString(","),
        "push.agency_count" to state.subscribedAgencies.size,
        "push.location_count" to state.subscribedLocations.size,
        "push.changeover_complete" to changeoverComplete,
        "push.subscribed_topic_count" to counts.confirmed.toInt(),
        "push.pending_subscribe_count" to counts.pendingSubscribe.toInt(),
        "push.pending_unsubscribe_count" to counts.pendingUnsubscribe.toInt(),
    )

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
        put("push.token_state", tokenState(snapshot))
        snapshot.tokenUnavailableReason?.let { put("push.token_unavailable_reason", it.take(200)) }
    }

    /** `present` | `apns_pending` (iOS precondition, self-heals) | `unavailable` (anything else, incl. not yet recorded). */
    fun tokenState(snapshot: PushDiagnosticsSnapshot): String = when {
        snapshot.tokenPresent == true -> "present"
        snapshot.tokenPresent == false && isApnsPreconditionMessage(snapshot.tokenUnavailableReason) -> "apns_pending"
        else -> "unavailable"
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
        add(
            "Last clean reconcile (V6)" to (
                snapshot.lastCleanReconcileEpochSeconds?.toString() ?: "never this session"
                )
        )
        val st = snapshot.pushState
        add("Audience class" to (st?.get("push.audience_class")?.toString() ?: "unknown"))
        add("Enabled types" to listOrNone(st?.get("push.enabled_types")))
        add("Enabled broadcasts" to listOrNone(st?.get("push.enabled_broadcasts")))
        add("Changeover complete" to (st?.get("push.changeover_complete")?.toString() ?: "unknown"))
        add(
            "Pending ops" to if (st == null) {
                "unknown"
            } else {
                "subscribe ${st["push.pending_subscribe_count"]}, unsubscribe ${st["push.pending_unsubscribe_count"]}"
            }
        )
    }

    private fun listOrNone(value: Any?): String = when (val s = value?.toString()) {
        null -> "unknown"
        "" -> "none"
        else -> s
    }

    /**
     * Emit the once-per-cold-start summary. The Kermit line covers console/file (and
     * Datadog at VERBOSE). At STANDARD the DataDogLogWriter threshold is Warn and would
     * drop Info, so the structured copy goes directly to DatadogLogger. The direct copy
     * uploads ONLY when the user opted into diagnostics: isRemoteLoggingActive() is true
     * only with the SDK initialized and consent granted (STANDARD/VERBOSE), which also
     * avoids the PENDING-consent buffer. The severity gate prevents double upload at
     * VERBOSE, where the Kermit line already reaches Datadog through the writer.
     */
    fun logSummary(diagnosticLevelName: String?, stateAttrs: Map<String, Any?> = emptyMap()) {
        DiagnosticEvents.emit(
            kind = DiagnosticEventKind.PUSH_SUMMARY,
            severity = Severity.Info,
            tag = "SLN-PushDiagnostics",
            attrs = summaryAttributes(snapshot, diagnosticLevelName) + stateAttrs,
            label = "Push registration summary", // runbook string, kept (spec D2)
        )
    }
}

/**
 * True when [message] is the Firebase iOS "no APNs token yet" precondition. It self-heals
 * once APNs registers, so it is not a failure worth a warn or error line.
 */
fun isApnsPreconditionMessage(message: String?): Boolean = message?.contains("apns", ignoreCase = true) == true

private fun PlayServicesAvailability.label(): String = when (this) {
    PlayServicesAvailability.AVAILABLE -> "available"
    PlayServicesAvailability.MISSING -> "missing"
    PlayServicesAvailability.UPDATE_REQUIRED -> "update_required"
    PlayServicesAvailability.NOT_APPLICABLE -> "n/a"
    PlayServicesAvailability.UNKNOWN -> "unknown"
}
