//
//  NSENotificationFilter.swift
//  NotificationServiceExtension
//
//  Lightweight notification filter for the NSE process.
//  Mirrors the core allow/block logic of the Kotlin NotificationFilter (commonMain),
//  without webcast/topic checks that the server handles via FCM topic subscription.
//
//  Since NSEFilterPreferences pre-expands agency and location IDs at write time,
//  all matching here is a simple Set.contains() lookup.
//

import Foundation

struct NSENotificationFilter {

    /// Determine whether a V5 notification should be delivered, based on user filter preferences.
    ///
    /// - Parameters:
    ///   - payload: Parsed V5 notification payload containing lspId and locationId.
    ///   - preferences: Current filter preferences loaded from shared App Group UserDefaults.
    /// - Returns: `true` if the notification should be delivered; `false` to suppress it.
    static func shouldShow(payload: V5NotificationData, preferences: NSEFilterPreferences) -> Bool {
        // 1. Kill switch: notifications disabled globally.
        guard preferences.enableNotifications else {
            return false
        }

        // 2. Follow-all bypass: user wants every notification regardless of agency/location.
        if preferences.followAllLaunches {
            return true
        }

        let hasAgencyFilter = !preferences.subscribedAgencies.isEmpty
        let hasLocationFilter = !preferences.subscribedLocations.isEmpty

        // 3. Both filter sets empty — block everything (no subscriptions).
        guard hasAgencyFilter || hasLocationFilter else {
            return false
        }

        // 4. Agency match.
        //    IDs are pre-expanded (additionalIds included), so a plain Set.contains() suffices.
        //    If payload has no lspId, we cannot filter on agency — treat as match.
        let agencyMatch: Bool
        if let lspId = payload.lspId, hasAgencyFilter {
            agencyMatch = preferences.subscribedAgencies.contains(String(lspId))
        } else {
            agencyMatch = true
        }

        // 5. Location match.
        //    Same pre-expansion guarantee — plain Set.contains() suffices.
        //    If payload has no locationId, we cannot filter on location — treat as match.
        let locationMatch: Bool
        if let locationId = payload.locationId, hasLocationFilter {
            locationMatch = preferences.subscribedLocations.contains(String(locationId))
        } else {
            locationMatch = true
        }

        // 6. Apply strict vs flexible matching logic (mirrors Kotlin NotificationFilter).
        if preferences.useStrictMatching {
            // Strict: BOTH agency AND location filters must be active, and BOTH must match.
            guard hasAgencyFilter && hasLocationFilter else { return false }
            return agencyMatch && locationMatch
        } else {
            // Flexible: OR logic when both filters active; single-filter uses that filter.
            if hasAgencyFilter && hasLocationFilter {
                return agencyMatch || locationMatch
            } else if hasAgencyFilter {
                return agencyMatch
            } else {
                return locationMatch
            }
        }
    }
}
