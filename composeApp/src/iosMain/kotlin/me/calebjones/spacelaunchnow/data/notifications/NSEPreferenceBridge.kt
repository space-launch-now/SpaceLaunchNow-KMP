package me.calebjones.spacelaunchnow.data.notifications

import me.calebjones.spacelaunchnow.data.model.NotificationAgency
import me.calebjones.spacelaunchnow.data.model.NotificationLocation
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
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
 */
object NSEPreferenceBridge {

    private val log = SpaceLogger.getLogger("NSEPreferenceBridge")

    private const val APP_GROUP = "group.me.spacelaunchnow.spacelaunchnow"

    private const val KEY_ENABLE_NOTIFICATIONS = "nse_enable_notifications"
    private const val KEY_FOLLOW_ALL_LAUNCHES = "nse_follow_all_launches"
    private const val KEY_USE_STRICT_MATCHING = "nse_use_strict_matching"
    private const val KEY_SUBSCRIBED_AGENCIES = "nse_subscribed_agencies"
    private const val KEY_SUBSCRIBED_LOCATIONS = "nse_subscribed_locations"

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

        userDefaults.synchronize()

        log.d {
            "Synced NSE prefs: enabled=${state.enableNotifications}, " +
                "followAll=${state.followAllLaunches}, " +
                "agencies(expanded)=${expandedAgencies.size}, " +
                "locations(expanded)=${expandedLocations.size}"
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
