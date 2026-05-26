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
    /// Broadcast-type per-type toggles (not agency/location filtered).
    let topicEvents: Bool
    let topicFeaturedNews: Bool
    let topicAnnouncements: Bool

    private static let appGroup = "group.me.spacelaunchnow.spacelaunchnow"

    enum Keys {
        static let enableNotifications = "nse_enable_notifications"
        static let followAllLaunches = "nse_follow_all_launches"
        static let useStrictMatching = "nse_use_strict_matching"
        static let subscribedAgencies = "nse_subscribed_agencies"
        static let subscribedLocations = "nse_subscribed_locations"
        static let topicEvents = "nse_topic_events"
        static let topicFeaturedNews = "nse_topic_featured_news"
        static let topicAnnouncements = "nse_topic_announcements"
    }

    /// Load current preferences from shared App Group UserDefaults.
    ///
    /// Fail-closed when keys are missing: `followAllLaunches` defaults to `false` and the
    /// subscription sets default to empty, so the filter suppresses everything rather than
    /// flooding the user with launches it can't evaluate. The Kotlin bridge writes these keys
    /// on every settings change and at app launch (NSEPreferenceBridge.syncToUserDefaults),
    /// so a missing-key state should only occur before the app has ever run.
    static func load() -> NSEFilterPreferences {
        let defaults = UserDefaults(suiteName: appGroup)

        // Bool keys: use object(forKey:) first so we can distinguish missing vs explicit false.
        let enableNotifications: Bool = defaults?.object(forKey: Keys.enableNotifications) != nil
            ? defaults!.bool(forKey: Keys.enableNotifications)
            : true

        // followAllLaunches defaults to FALSE — fail closed when the bridge hasn't written yet,
        // so unevaluable pushes are suppressed rather than shown.
        let followAllLaunches: Bool = defaults?.object(forKey: Keys.followAllLaunches) != nil
            ? defaults!.bool(forKey: Keys.followAllLaunches)
            : false

        let useStrictMatching: Bool = defaults?.object(forKey: Keys.useStrictMatching) != nil
            ? defaults!.bool(forKey: Keys.useStrictMatching)
            : false

        let agencies = Set(defaults?.stringArray(forKey: Keys.subscribedAgencies) ?? [])
        let locations = Set(defaults?.stringArray(forKey: Keys.subscribedLocations) ?? [])

        // Per-type broadcast toggles default to TRUE when missing — these topics are
        // defaultEnabled in Kotlin, so a missing key (app never ran) should not suppress them.
        let topicEvents: Bool = defaults?.object(forKey: Keys.topicEvents) != nil
            ? defaults!.bool(forKey: Keys.topicEvents)
            : true
        let topicFeaturedNews: Bool = defaults?.object(forKey: Keys.topicFeaturedNews) != nil
            ? defaults!.bool(forKey: Keys.topicFeaturedNews)
            : true
        let topicAnnouncements: Bool = defaults?.object(forKey: Keys.topicAnnouncements) != nil
            ? defaults!.bool(forKey: Keys.topicAnnouncements)
            : true

        return NSEFilterPreferences(
            enableNotifications: enableNotifications,
            followAllLaunches: followAllLaunches,
            useStrictMatching: useStrictMatching,
            subscribedAgencies: agencies,
            subscribedLocations: locations,
            topicEvents: topicEvents,
            topicFeaturedNews: topicFeaturedNews,
            topicAnnouncements: topicAnnouncements
        )
    }
}
