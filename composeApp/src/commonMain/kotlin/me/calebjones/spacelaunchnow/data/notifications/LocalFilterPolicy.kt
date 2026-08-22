package me.calebjones.spacelaunchnow.data.notifications

import me.calebjones.spacelaunchnow.data.model.FilterResult
import me.calebjones.spacelaunchnow.data.model.NotificationData
import me.calebjones.spacelaunchnow.data.model.NotificationFilter
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.model.V5NotificationFilter
import me.calebjones.spacelaunchnow.data.model.V5NotificationPayload

/**
 * The one place that decides whether a received push must still pass the legacy
 * client-side filters.
 *
 * Under V6 the server targets by topic: the subscription IS the filter, and a local
 * agency/location/type check can only disagree with it -- it keys on expanded LL2
 * IDs while the server keys on group names (`otherAgency`, sites added server-side),
 * so it suppresses launches the user explicitly asked for. A device is on V6 exactly
 * when its V5->V6 changeover has completed ([NotificationState.hasCompletedV6Changeover]);
 * until then it is still subscribed to the V5 broadcast of *every* launch and must
 * keep filtering locally, or each launch would show. App stores update the NSE and
 * the Android worker without the app opening, so "the code is V6" is not enough --
 * the gate has to be per device, and a missing flag must mean "still filtering".
 *
 * Only the master kill switch survives on the V6 path: it is cheap defence against
 * a failed unsubscribe and cannot suppress anything the user wants.
 *
 * The iOS NSE mirrors this in Swift (`NotificationService.swift`, reading the
 * `nse_v6_changeover_complete` App Group key); keep the two in step.
 *
 * Retire with the V5 broadcast (server retirement criteria): at that point every
 * caller collapses to "show", and V5NotificationFilter / NotificationFilter / the
 * NSE filter go with it.
 */
object LocalFilterPolicy {

    /** History/breadcrumb reason recorded when a push is shown without local filtering. */
    const val PASSTHROUGH_REASON = "v6_passthrough"

    /** Why a broadcast-type push (event / news / custom) was blocked. */
    enum class BroadcastBlock { KILL_SWITCH, TOGGLE_OFF }

    /** True while this device must still apply the legacy client-side filters. */
    fun isActive(state: NotificationState): Boolean = !state.hasCompletedV6Changeover

    /** V5 launch push: the legacy filter while active, kill switch only once on V6. */
    fun launchDecision(payload: V5NotificationPayload, state: NotificationState): FilterResult =
        if (isActive(state)) V5NotificationFilter.shouldShow(payload, state) else killSwitchOnly(state)

    /** V4 launch push: same gate over the V4 filter. */
    fun legacyLaunchAllowed(data: NotificationData, state: NotificationState): Boolean =
        if (isActive(state)) NotificationFilter.shouldShowNotification(data, state) else state.enableNotifications

    /**
     * Raw-map entry point (the iOS in-app paths). Under V6 nothing is parsed for
     * filtering, so an unparseable payload is shown rather than suppressed -- fail open.
     */
    fun legacyLaunchAllowedFromMap(dataMap: Map<String, String>, state: NotificationState): Boolean =
        if (isActive(state)) NotificationFilter.shouldShowFromMap(dataMap, state) else state.enableNotifications

    /** Broadcast push: per-type toggle while active, kill switch only once on V6. Null = show. */
    fun broadcastBlock(topic: NotificationTopic, state: NotificationState): BroadcastBlock? = when {
        !state.enableNotifications -> BroadcastBlock.KILL_SWITCH
        isActive(state) && !state.isTopicEnabled(topic) -> BroadcastBlock.TOGGLE_OFF
        else -> null
    }

    private fun killSwitchOnly(state: NotificationState): FilterResult =
        if (state.enableNotifications) {
            FilterResult.Allowed
        } else {
            FilterResult.blocked(FilterResult.Companion.Reasons.NOTIFICATIONS_DISABLED)
        }
}
