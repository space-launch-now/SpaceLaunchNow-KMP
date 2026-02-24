//
//  NSEFilterPreferences.swift
//  NotificationServiceExtension
//
//  Filter preferences for the Notification Service Extension.
//  Mirrors the relevant fields from Kotlin NotificationState, read from shared
//  App Group UserDefaults written by NSEPreferenceBridge.kt.
//

import Foundation

/// NSE-local snapshot of notification filter preferences.
/// Loaded from shared App Group UserDefaults written by the Kotlin NSEPreferenceBridge.
struct NSEFilterPreferences {
    let enableNotifications: Bool
    let followAllLaunches: Bool
    let useStrictMatching: Bool
    /// Pre-expanded agency IDs (primary + additionalIds) — use Set.contains() directly.
    let subscribedAgencies: Set<String>
    /// Pre-expanded location IDs (primary + additionalIds) — use Set.contains() directly.
    let subscribedLocations: Set<String>

    private static let appGroup = "group.me.spacelaunchnow.spacelaunchnow"

    enum Keys {
        static let enableNotifications = "nse_enable_notifications"
        static let followAllLaunches = "nse_follow_all_launches"
        static let useStrictMatching = "nse_use_strict_matching"
        static let subscribedAgencies = "nse_subscribed_agencies"
        static let subscribedLocations = "nse_subscribed_locations"
    }

    /// Load current preferences from shared App Group UserDefaults.
    ///
    /// Defaults to allow-all when keys are missing (fresh install or bridge not yet written by
    /// the Kotlin side). This is the safe default — new users see all notifications.
    static func load() -> NSEFilterPreferences {
        let defaults = UserDefaults(suiteName: appGroup)

        // Bool keys: use object(forKey:) first so we can distinguish missing vs explicit false.
        let enableNotifications: Bool = defaults?.object(forKey: Keys.enableNotifications) != nil
            ? defaults!.bool(forKey: Keys.enableNotifications)
            : true

        // followAllLaunches defaults to true — allow all when bridge hasn't written yet.
        let followAllLaunches: Bool = defaults?.object(forKey: Keys.followAllLaunches) != nil
            ? defaults!.bool(forKey: Keys.followAllLaunches)
            : true

        let useStrictMatching: Bool = defaults?.object(forKey: Keys.useStrictMatching) != nil
            ? defaults!.bool(forKey: Keys.useStrictMatching)
            : false

        let agencies = Set(defaults?.stringArray(forKey: Keys.subscribedAgencies) ?? [])
        let locations = Set(defaults?.stringArray(forKey: Keys.subscribedLocations) ?? [])

        return NSEFilterPreferences(
            enableNotifications: enableNotifications,
            followAllLaunches: followAllLaunches,
            useStrictMatching: useStrictMatching,
            subscribedAgencies: agencies,
            subscribedLocations: locations
        )
    }
}
