package me.calebjones.spacelaunchnow.data.notifications

import co.touchlab.kermit.Severity
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.util.logging.DiagnosticEventKind
import me.calebjones.spacelaunchnow.util.logging.DiagnosticEvents

/**
 * Emits the single `filter_decision` event for the iOS in-app notification filter
 * (client-log-quality spec 4.2). Mirrors `nse_breadcrumb` so both filters share facets.
 * Carries counts and flags only: never launch names or ID collections.
 */
object FilterDecisionEvent {

    const val REASON_FILTER_PASSED = "filter_passed"
    const val REASON_FILTERED = "filtered"
    const val REASON_NOTIFICATIONS_DISABLED = "notifications_disabled"

    fun reasonFor(shown: Boolean, passthrough: Boolean, state: NotificationState): String = when {
        shown && passthrough -> LocalFilterPolicy.PASSTHROUGH_REASON
        shown -> REASON_FILTER_PASSED
        !state.enableNotifications -> REASON_NOTIFICATIONS_DISABLED
        else -> REASON_FILTERED
    }

    fun attributes(
        data: Map<String, String>,
        state: NotificationState,
        shown: Boolean,
        passthrough: Boolean,
    ): Map<String, Any?> = mapOf(
        "decision" to if (shown) "shown" else "suppressed",
        "reason" to reasonFor(shown, passthrough, state),
        "notification_type" to data["notification_type"],
        "agency_id" to data["agency_id"],
        "location_id" to data["location_id"],
        "v6_passthrough" to passthrough,
        "strict" to state.useStrictMatching,
        "follow_all" to state.followAllLaunches,
        "agency_count" to state.subscribedAgencies.size,
        "location_count" to state.subscribedLocations.size,
    )

    fun emit(
        data: Map<String, String>,
        state: NotificationState,
        shown: Boolean,
        passthrough: Boolean,
        tag: String = "SLN-IosNotificationBridge",
    ) {
        DiagnosticEvents.emit(
            kind = DiagnosticEventKind.FILTER_DECISION,
            severity = if (shown) Severity.Info else Severity.Warn,
            tag = tag,
            attrs = attributes(data, state, shown, passthrough),
        )
    }
}
