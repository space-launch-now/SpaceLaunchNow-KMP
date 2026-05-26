package me.calebjones.spacelaunchnow.data.notifications

import me.calebjones.spacelaunchnow.analytics.DatadogLogger
import me.calebjones.spacelaunchnow.data.model.NotificationAgency
import me.calebjones.spacelaunchnow.data.model.NotificationLocation
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import me.calebjones.spacelaunchnow.util.logging.UserContext
import platform.Foundation.NSMutableArray
import platform.Foundation.NSUserDefaults

/**
 * Bridge to write NotificationState filter preferences to shared App Group UserDefaults.
 *
 * The Notification Service Extension (NSE) runs in a separate process and cannot call Kotlin
 * code. This bridge writes filter preferences to UserDefaults keyed under the App Group so the
 * NSE can read them independently — including when the app is killed or terminated.
 *
 * IDs are pre-expanded at write time (primary + additionalIds) so the NSE only needs
 * simple Set.contains() lookups rather than knowing the expansion rules.
 *
 * Keys written (all prefixed "nse_"):
 *   nse_enable_notifications  — Bool
 *   nse_follow_all_launches   — Bool
 *   nse_use_strict_matching   — Bool
 *   nse_subscribed_agencies   — [String], expanded IDs
 *   nse_subscribed_locations  — [String], expanded IDs
 *   nse_topic_events          — Bool, EVENTS per-type toggle
 *   nse_topic_featured_news   — Bool, FEATURED_NEWS per-type toggle
 *   nse_topic_announcements   — Bool, ANNOUNCEMENTS per-type toggle
 */
object NSEPreferenceBridge {

    private val log = SpaceLogger.getLogger("NSEPreferenceBridge")

    private const val APP_GROUP = "group.me.spacelaunchnow.spacelaunchnow"

    private const val KEY_ENABLE_NOTIFICATIONS = "nse_enable_notifications"
    private const val KEY_FOLLOW_ALL_LAUNCHES = "nse_follow_all_launches"
    private const val KEY_USE_STRICT_MATCHING = "nse_use_strict_matching"
    private const val KEY_SUBSCRIBED_AGENCIES = "nse_subscribed_agencies"
    private const val KEY_SUBSCRIBED_LOCATIONS = "nse_subscribed_locations"
    private const val KEY_TOPIC_EVENTS = "nse_topic_events"
    private const val KEY_TOPIC_FEATURED_NEWS = "nse_topic_featured_news"
    private const val KEY_TOPIC_ANNOUNCEMENTS = "nse_topic_announcements"

    // Cross-process breadcrumb buffer written by the NSE (NotificationService.swift /
    // NSEBreadcrumb) and drained here. Entries are pipe-delimited "ts|type|decision|reason".
    private const val KEY_NSE_EVENT_LOG = "nse_event_log"

    // In-process guard so the two drain callers (startup coroutine + applicationDidBecomeActive,
    // which can fire close together at cold launch) cannot interleave their read-then-clear and
    // double-emit or drop a concurrently-appended breadcrumb. This is NOT cross-process atomic
    // (a rare NSE write between our read and clear may still be dropped — acceptable for
    // diagnostics, per architect); it only prevents our OWN two hooks from racing each other.
    @OptIn(ExperimentalAtomicApi::class)
    private val draining = AtomicBoolean(false)

    /**
     * Sync current [NotificationState] to shared UserDefaults for NSE access.
     *
     * Agency and location IDs are expanded at write time so the NSE filter can use simple
     * Set.contains() lookups without knowing the [NotificationAgency.additionalIds] rules.
     */
    fun syncToUserDefaults(state: NotificationState) {
        val userDefaults = NSUserDefaults(suiteName = APP_GROUP)
        if (userDefaults == null) {
            log.e { "Failed to get NSUserDefaults for app group: $APP_GROUP" }
            return
        }

        userDefaults.setBool(state.enableNotifications, forKey = KEY_ENABLE_NOTIFICATIONS)
        userDefaults.setBool(state.followAllLaunches, forKey = KEY_FOLLOW_ALL_LAUNCHES)
        userDefaults.setBool(state.useStrictMatching, forKey = KEY_USE_STRICT_MATCHING)

        val expandedAgencies = expandAgencyIds(state.subscribedAgencies)
        userDefaults.setObject(expandedAgencies.toNSArray(), forKey = KEY_SUBSCRIBED_AGENCIES)

        val expandedLocations = expandLocationIds(state.subscribedLocations)
        userDefaults.setObject(expandedLocations.toNSArray(), forKey = KEY_SUBSCRIBED_LOCATIONS)

        // Broadcast-type per-type toggles (event/news/custom). The NSE applies these in its
        // non-V5 branch so killed-app pushes honor the user's per-type preferences.
        userDefaults.setBool(state.isTopicEnabled(NotificationTopic.EVENTS), forKey = KEY_TOPIC_EVENTS)
        userDefaults.setBool(
            state.isTopicEnabled(NotificationTopic.FEATURED_NEWS),
            forKey = KEY_TOPIC_FEATURED_NEWS
        )
        userDefaults.setBool(
            state.isTopicEnabled(NotificationTopic.ANNOUNCEMENTS),
            forKey = KEY_TOPIC_ANNOUNCEMENTS
        )

        userDefaults.synchronize()

        log.d {
            "Synced NSE prefs: enabled=${state.enableNotifications}, " +
                "followAll=${state.followAllLaunches}, " +
                "agencies(expanded)=${expandedAgencies.size}, " +
                "locations(expanded)=${expandedLocations.size}, " +
                "events=${state.isTopicEnabled(NotificationTopic.EVENTS)}, " +
                "news=${state.isTopicEnabled(NotificationTopic.FEATURED_NEWS)}, " +
                "announcements=${state.isTopicEnabled(NotificationTopic.ANNOUNCEMENTS)}"
        }
    }

    /**
     * Read back and log the preferences currently stored in the shared App Group
     * UserDefaults — i.e. exactly what the Notification Service Extension reads when
     * the app is force-quit.
     *
     * Crucially logs whether each key is PRESENT or MISSING. When a key is missing the
     * NSE falls back to its allow-all defaults (followAllLaunches=true, useStrictMatching=
     * false), which silently shows every launch — including ones the user filtered out —
     * whenever the app is killed. Use this to diagnose live-state vs NSE-state drift.
     */
    fun logStoredPrefs() {
        val userDefaults = NSUserDefaults(suiteName = APP_GROUP)
        if (userDefaults == null) {
            log.e { "🟥 [NSE-PREFS] App group '$APP_GROUP' unavailable — NSE cannot read prefs" }
            return
        }

        fun presence(key: String) =
            if (userDefaults.objectForKey(key) != null) "present" else "MISSING → NSE uses default"

        val followAllPresent = userDefaults.objectForKey(KEY_FOLLOW_ALL_LAUNCHES) != null
        val strictPresent = userDefaults.objectForKey(KEY_USE_STRICT_MATCHING) != null
        val agencies = userDefaults.arrayForKey(KEY_SUBSCRIBED_AGENCIES)
        val locations = userDefaults.arrayForKey(KEY_SUBSCRIBED_LOCATIONS)

        log.i { "========================================" }
        log.i { "📦 [NSE-PREFS] App Group prefs the NSE reads when app is KILLED:" }
        log.i { "   - app group: $APP_GROUP" }
        log.i { "   - enableNotifications: ${userDefaults.boolForKey(KEY_ENABLE_NOTIFICATIONS)} (${presence(KEY_ENABLE_NOTIFICATIONS)})" }
        log.i { "   - followAllLaunches: ${userDefaults.boolForKey(KEY_FOLLOW_ALL_LAUNCHES)} (${presence(KEY_FOLLOW_ALL_LAUNCHES)})" }
        log.i { "   - useStrictMatching: ${userDefaults.boolForKey(KEY_USE_STRICT_MATCHING)} (${presence(KEY_USE_STRICT_MATCHING)})" }
        log.i { "   - subscribedAgencies (expanded): ${agencies?.size ?: 0} ids ${agencies?.take(15)}" }
        log.i { "   - subscribedLocations (expanded): ${locations?.size ?: 0} ids ${locations?.take(15)}" }
        if (!followAllPresent || !strictPresent || agencies == null || locations == null) {
            log.w {
                "⚠️ [NSE-PREFS] One or more keys MISSING → NSE falls back to ALLOW-ALL " +
                    "(followAllLaunches defaults to true). Killed-app pushes will bypass the user's filters!"
            }
        }
        log.i { "========================================" }
    }

    /**
     * Drain the NSE breadcrumb buffer into Datadog.
     *
     * The Notification Service Extension runs in a separate sandboxed process and cannot reach
     * the app's Kermit/Datadog sink, so it appends compact breadcrumbs ("ts|type|decision|reason")
     * to the shared App Group UserDefaults (see NSEBreadcrumb in NotificationService.swift). This
     * reads them all, clears the key (read-then-clear; rare loss on a concurrent NSE write is
     * acceptable for diagnostics), and re-emits each through the normal logging pipeline so they
     * reach Datadog with full user context.
     *
     * IMPORTANT — Datadog severity: the Kermit [DataDogLogWriter] defaults to minSeverity = Error
     * and drops anything below it. Rather than emit these routine diagnostics at ERROR (which
     * would pollute error dashboards / trip error-rate alerting with non-errors), we call
     * [DatadogLogger] DIRECTLY — bypassing the Kermit severity gate entirely — at an honest
     * severity: info for shown, warn for suppressed (so suppressions stand out without being
     * false errors). This ships them at their true level without globally lowering the app's
     * Datadog minSeverity. UserContext attributes (RevenueCat user id) are attached manually
     * since we are not going through DataDogLogWriter (which would otherwise add them).
     * Call once on app foreground/startup (see IosNotificationBridge).
     */
    @OptIn(ExperimentalAtomicApi::class)
    fun drainNseEventLog() {
        // Serialize our two callers: if a drain is already in progress, skip (the in-flight one
        // is clearing the buffer anyway). Prevents the startup coroutine and the first
        // applicationDidBecomeActive from interleaving their read-then-clear.
        if (!draining.compareAndSet(expectedValue = false, newValue = true)) {
            log.d { "NSE drain already in progress — skipping concurrent call" }
            return
        }
        try {
            val userDefaults = NSUserDefaults(suiteName = APP_GROUP)
            if (userDefaults == null) {
                log.e { "Failed to get NSUserDefaults for app group: $APP_GROUP (NSE drain skipped)" }
                return
            }

            @Suppress("UNCHECKED_CAST")
            val entries = userDefaults.arrayForKey(KEY_NSE_EVENT_LOG) as? List<String>
            if (entries.isNullOrEmpty()) {
                log.d { "NSE event log empty — nothing to drain" }
                return
            }

            // Read-then-clear: remove before emitting so a crash mid-drain can't double-ship the
            // whole buffer on next launch (at worst we lose this batch — acceptable for diagnostics).
            userDefaults.removeObjectForKey(KEY_NSE_EVENT_LOG)
            userDefaults.synchronize()

            log.i { "Draining ${entries.size} NSE breadcrumb(s) to Datadog" }
            val baseAttributes = UserContext.getLogAttributes()
            entries.forEach { entry ->
                val parts = entry.split("|")
                val ts = parts.getOrNull(0) ?: ""
                val type = parts.getOrNull(1) ?: "unknown"
                val decision = parts.getOrNull(2) ?: "unknown"
                val reason = parts.getOrNull(3) ?: ""

                val message = "[NSE-DELIVERY] type=$type decision=$decision reason=$reason ts=$ts platform=ios"
                val attributes = baseAttributes + mapOf(
                    // Structural diagnostic markers so Datadog dashboards/monitors can EXCLUDE
                    // these by attribute (robust) rather than by matching the message prefix
                    // (fragile). log_kind is the dedicated exclusion key.
                    "log_kind" to "nse_breadcrumb",
                    "source" to "nse_breadcrumb",
                    "notification_type" to type,
                    "decision" to decision,
                    "reason" to reason,
                    "nse_ts" to ts,
                    "platform" to "ios"
                )

                // Call DatadogLogger directly (bypasses Kermit's DataDogLogWriter ERROR gate) at an
                // honest severity: suppressions as warn (stand out), shown as info. Never ERROR —
                // these are normal diagnostics, not failures.
                if (decision == "suppressed") {
                    DatadogLogger.warn(message, attributes)
                } else {
                    DatadogLogger.info(message, attributes)
                }
            }
        } finally {
            draining.store(false)
        }
    }

    // Expand set of primary agency IDs to include all additionalIds.
    private fun expandAgencyIds(subscribedIds: Set<String>): List<String> {
        val allAgencies = NotificationAgency.getAll()
        val expanded = mutableSetOf<String>()
        for (idStr in subscribedIds) {
            val id = idStr.toIntOrNull() ?: continue
            val agency = allAgencies.find { it.id == id }
            if (agency != null) {
                expanded.add(agency.id.toString())
                agency.additionalIds.forEach { expanded.add(it.toString()) }
            } else {
                expanded.add(idStr)
            }
        }
        return expanded.toList()
    }

    // Expand set of primary location IDs to include all additionalIds via getAllIds().
    private fun expandLocationIds(subscribedIds: Set<String>): List<String> {
        val allLocations = NotificationLocation.getAll()
        val expanded = mutableSetOf<String>()
        for (idStr in subscribedIds) {
            val id = idStr.toIntOrNull() ?: continue
            val location = allLocations.find { it.id == id }
            if (location != null) {
                location.getAllIds().forEach { expanded.add(it.toString()) }
            } else {
                expanded.add(idStr)
            }
        }
        return expanded.toList()
    }

    private fun List<String>.toNSArray(): NSMutableArray {
        val array = NSMutableArray()
        forEach { item -> array.addObject(item) }
        return array
    }
}
