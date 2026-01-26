import Foundation

/**
 * V5 Notification Data
 *
 * Swift representation of V5 notification payload.
 * Used by both main app and Notification Service Extension for parsing
 * and filtering V5 notifications.
 *
 * V5 payloads are distinguished from V4 by the presence of 'lsp_id' field.
 */
struct V5NotificationData {
    // Display Content
    let notificationType: String      // tenMinutes, oneHour, twentyFourHour, netstampChanged, inFlight, success, failure, webcastLive
    let title: String                  // Server-provided title
    let body: String                   // Server-provided body
    
    // Launch Identification
    let launchUuid: String             // UUID for deep linking (primary key)
    let launchId: String               // Library ID (legacy)
    let launchName: String             // Display name
    
    // Launch Details
    let launchImage: String?           // Image URL (optional)
    let launchNet: String              // ISO 8601 datetime
    let launchLocation: String         // Location display name
    let webcast: Bool                  // Has webcast
    let webcastLive: Bool              // Is webcast currently live
    
    // V5 Filtering IDs (Extended)
    let lspId: Int?                    // Launch Service Provider ID
    let locationId: Int?               // Launch location ID
    let programIds: [Int]              // Program IDs
    let statusId: Int?                 // Launch status ID (optional)
    let orbitId: Int?                  // Target orbit ID (optional)
    let missionTypeId: Int?            // Mission type ID (optional)
    let launcherFamilyId: Int?         // Launcher family ID (optional)
    
    // MARK: - Payload Field Keys
    
    struct Keys {
        static let notificationType = "notification_type"
        static let title = "title"
        static let body = "body"
        static let launchUuid = "launch_uuid"
        static let launchId = "launch_id"
        static let launchName = "launch_name"
        static let launchImage = "launch_image"
        static let launchNet = "launch_net"
        static let launchLocation = "launch_location"
        static let webcast = "webcast"
        static let webcastLive = "webcast_live"
        static let lspId = "lsp_id"
        static let locationId = "location_id"
        static let programIds = "program_ids"
        static let statusId = "status_id"
        static let orbitId = "orbit_id"
        static let missionTypeId = "mission_type_id"
        static let launcherFamilyId = "launcher_family_id"
    }
    
    // MARK: - Factory Methods
    
    /**
     * Check if a notification payload is V5 format
     *
     * V5 payloads are identified by the presence of the 'lsp_id' field.
     *
     * - Parameter userInfo: The notification payload dictionary
     * - Returns: true if this is a V5 payload
     */
    static func isV5Payload(_ userInfo: [AnyHashable: Any]) -> Bool {
        return userInfo[Keys.lspId] != nil
    }
    
    /**
     * Parse V5 notification data from userInfo dictionary
     *
     * - Parameter userInfo: The notification payload dictionary (from APNs/FCM)
     * - Returns: V5NotificationData or nil if required fields are missing
     */
    static func fromUserInfo(_ userInfo: [AnyHashable: Any]) -> V5NotificationData? {
        guard let notificationType = userInfo[Keys.notificationType] as? String,
              let launchUuid = userInfo[Keys.launchUuid] as? String,
              let launchName = userInfo[Keys.launchName] as? String,
              let launchNet = userInfo[Keys.launchNet] as? String else {
            return nil
        }
        
        // Title can come from 'title' field or fallback to 'launch_name'
        let title = (userInfo[Keys.title] as? String) ?? launchName
        
        // Parse program IDs from comma-separated string
        let programIdsString = userInfo[Keys.programIds] as? String
        let programIds: [Int] = programIdsString?
            .components(separatedBy: ",")
            .compactMap { Int($0.trimmingCharacters(in: .whitespaces)) } ?? []
        
        return V5NotificationData(
            notificationType: notificationType,
            title: title,
            body: (userInfo[Keys.body] as? String) ?? "",
            launchUuid: launchUuid,
            launchId: (userInfo[Keys.launchId] as? String) ?? "",
            launchName: launchName,
            launchImage: userInfo[Keys.launchImage] as? String,
            launchNet: launchNet,
            launchLocation: (userInfo[Keys.launchLocation] as? String) ?? "",
            webcast: (userInfo[Keys.webcast] as? String)?.lowercased() == "true",
            webcastLive: (userInfo[Keys.webcastLive] as? String)?.lowercased() == "true",
            lspId: Int(userInfo[Keys.lspId] as? String ?? ""),
            locationId: Int(userInfo[Keys.locationId] as? String ?? ""),
            programIds: programIds,
            statusId: Int(userInfo[Keys.statusId] as? String ?? ""),
            orbitId: Int(userInfo[Keys.orbitId] as? String ?? ""),
            missionTypeId: Int(userInfo[Keys.missionTypeId] as? String ?? ""),
            launcherFamilyId: Int(userInfo[Keys.launcherFamilyId] as? String ?? "")
        )
    }
    
    // MARK: - Helper Methods
    
    /**
     * Check if this notification has any filtering IDs
     */
    func hasFilteringIds() -> Bool {
        return lspId != nil || locationId != nil || !programIds.isEmpty ||
            statusId != nil || orbitId != nil || missionTypeId != nil || launcherFamilyId != nil
    }
    
    /**
     * Get a debug-friendly description
     */
    func debugDescription() -> String {
        var parts: [String] = [
            "type=\(notificationType)",
            "launch=\(launchName)",
            "lspId=\(lspId ?? -1)",
            "locationId=\(locationId ?? -1)"
        ]
        if !programIds.isEmpty {
            parts.append("programs=\(programIds)")
        }
        if let statusId = statusId {
            parts.append("status=\(statusId)")
        }
        if let orbitId = orbitId {
            parts.append("orbit=\(orbitId)")
        }
        parts.append("webcast=\(webcast)")
        return "V5Notification(\(parts.joined(separator: ", ")))"
    }
}

// MARK: - V5 Filter Preferences

/**
 * V5 Filter Preferences
 *
 * User preferences for V5 notification filtering.
 * Stored in shared UserDefaults (App Groups) for access by NSE.
 */
struct V5FilterPreferences {
    // Master enable/disable
    let enableNotifications: Bool
    
    // Notification type toggles
    let enabledNotificationTypes: Set<String>
    
    // V5 Filter Categories (nil = follow all, empty = block all)
    let subscribedLspIds: Set<Int>?
    let subscribedLocationIds: Set<Int>?
    let subscribedProgramIds: Set<Int>?
    let subscribedOrbitIds: Set<Int>?
    let subscribedMissionTypeIds: Set<Int>?
    let subscribedLauncherFamilyIds: Set<Int>?
    
    // Filter mode
    let useStrictMatching: Bool
    
    // Special filters
    let webcastOnly: Bool
    
    // MARK: - UserDefaults Keys
    
    struct Keys {
        static let enableNotifications = "v5_enable_notifications"
        static let enabledNotificationTypes = "v5_enabled_notification_types"
        static let subscribedLspIds = "v5_subscribed_lsp_ids"
        static let subscribedLocationIds = "v5_subscribed_location_ids"
        static let subscribedProgramIds = "v5_subscribed_program_ids"
        static let subscribedOrbitIds = "v5_subscribed_orbit_ids"
        static let subscribedMissionTypeIds = "v5_subscribed_mission_type_ids"
        static let subscribedLauncherFamilyIds = "v5_subscribed_launcher_family_ids"
        static let useStrictMatching = "v5_use_strict_matching"
        static let webcastOnly = "v5_webcast_only"
        static let hasV5Migration = "v5_has_migration"
    }
    
    // MARK: - Default Values
    
    static let defaultNotificationTypes: Set<String> = [
        "tenMinutes", "oneHour", "twentyFourHour", "netstampChanged",
        "inFlight", "success", "failure", "webcastLive"
    ]
    
    static let `default` = V5FilterPreferences(
        enableNotifications: true,
        enabledNotificationTypes: defaultNotificationTypes,
        subscribedLspIds: nil,
        subscribedLocationIds: nil,
        subscribedProgramIds: nil,
        subscribedOrbitIds: nil,
        subscribedMissionTypeIds: nil,
        subscribedLauncherFamilyIds: nil,
        useStrictMatching: false,
        webcastOnly: false
    )
    
    // MARK: - Factory Methods
    
    /**
     * Load preferences from shared UserDefaults (App Groups)
     *
     * - Parameter userDefaults: The shared UserDefaults instance
     * - Returns: V5FilterPreferences loaded from storage, or defaults
     */
    static func fromUserDefaults(_ userDefaults: UserDefaults) -> V5FilterPreferences {
        return V5FilterPreferences(
            enableNotifications: userDefaults.object(forKey: Keys.enableNotifications) as? Bool ?? true,
            enabledNotificationTypes: Set(userDefaults.stringArray(forKey: Keys.enabledNotificationTypes) ?? Array(defaultNotificationTypes)),
            subscribedLspIds: userDefaults.array(forKey: Keys.subscribedLspIds) as? [Int].map { Set($0) },
            subscribedLocationIds: userDefaults.array(forKey: Keys.subscribedLocationIds) as? [Int].map { Set($0) },
            subscribedProgramIds: userDefaults.array(forKey: Keys.subscribedProgramIds) as? [Int].map { Set($0) },
            subscribedOrbitIds: userDefaults.array(forKey: Keys.subscribedOrbitIds) as? [Int].map { Set($0) },
            subscribedMissionTypeIds: userDefaults.array(forKey: Keys.subscribedMissionTypeIds) as? [Int].map { Set($0) },
            subscribedLauncherFamilyIds: userDefaults.array(forKey: Keys.subscribedLauncherFamilyIds) as? [Int].map { Set($0) },
            useStrictMatching: userDefaults.bool(forKey: Keys.useStrictMatching),
            webcastOnly: userDefaults.bool(forKey: Keys.webcastOnly)
        )
    }
    
    /**
     * Save preferences to shared UserDefaults (App Groups)
     *
     * - Parameter userDefaults: The shared UserDefaults instance
     */
    func saveToUserDefaults(_ userDefaults: UserDefaults) {
        userDefaults.set(enableNotifications, forKey: Keys.enableNotifications)
        userDefaults.set(Array(enabledNotificationTypes), forKey: Keys.enabledNotificationTypes)
        
        if let lspIds = subscribedLspIds {
            userDefaults.set(Array(lspIds), forKey: Keys.subscribedLspIds)
        } else {
            userDefaults.removeObject(forKey: Keys.subscribedLspIds)
        }
        
        if let locationIds = subscribedLocationIds {
            userDefaults.set(Array(locationIds), forKey: Keys.subscribedLocationIds)
        } else {
            userDefaults.removeObject(forKey: Keys.subscribedLocationIds)
        }
        
        if let programIds = subscribedProgramIds {
            userDefaults.set(Array(programIds), forKey: Keys.subscribedProgramIds)
        } else {
            userDefaults.removeObject(forKey: Keys.subscribedProgramIds)
        }
        
        if let orbitIds = subscribedOrbitIds {
            userDefaults.set(Array(orbitIds), forKey: Keys.subscribedOrbitIds)
        } else {
            userDefaults.removeObject(forKey: Keys.subscribedOrbitIds)
        }
        
        if let missionTypeIds = subscribedMissionTypeIds {
            userDefaults.set(Array(missionTypeIds), forKey: Keys.subscribedMissionTypeIds)
        } else {
            userDefaults.removeObject(forKey: Keys.subscribedMissionTypeIds)
        }
        
        if let launcherFamilyIds = subscribedLauncherFamilyIds {
            userDefaults.set(Array(launcherFamilyIds), forKey: Keys.subscribedLauncherFamilyIds)
        } else {
            userDefaults.removeObject(forKey: Keys.subscribedLauncherFamilyIds)
        }
        
        userDefaults.set(useStrictMatching, forKey: Keys.useStrictMatching)
        userDefaults.set(webcastOnly, forKey: Keys.webcastOnly)
    }
    
    /**
     * Check if any category-based filtering is active
     */
    func hasActiveFilters() -> Bool {
        return subscribedLspIds != nil ||
            subscribedLocationIds != nil ||
            subscribedProgramIds != nil ||
            subscribedOrbitIds != nil ||
            subscribedMissionTypeIds != nil ||
            subscribedLauncherFamilyIds != nil
    }
}

// MARK: - V5 Notification Filter

/**
 * V5 Notification Filter
 *
 * Evaluates V5 notifications against user preferences.
 * Returns whether the notification should be displayed.
 */
enum V5NotificationFilter {
    
    enum FilterResult {
        case allowed
        case blocked(reason: String)
        
        var shouldShow: Bool {
            if case .allowed = self { return true }
            return false
        }
        
        var blockReason: String? {
            if case .blocked(let reason) = self { return reason }
            return nil
        }
    }
    
    /**
     * Determine if V5 notification should be shown based on user preferences
     *
     * - Parameters:
     *   - payload: The V5 notification payload
     *   - preferences: User filter preferences
     * - Returns: FilterResult indicating whether to show or block
     */
    static func shouldShow(
        payload: V5NotificationData,
        preferences: V5FilterPreferences
    ) -> FilterResult {
        // 1. Check master enable
        if !preferences.enableNotifications {
            return .blocked(reason: "Notifications disabled globally")
        }
        
        // 2. Check notification type
        if !preferences.enabledNotificationTypes.contains(payload.notificationType) {
            return .blocked(reason: "Notification type '\(payload.notificationType)' disabled")
        }
        
        // 3. Check webcast-only filter
        if preferences.webcastOnly && !payload.webcast {
            return .blocked(reason: "Webcast-only filter enabled, launch has no webcast")
        }
        
        // 4. Check filter categories
        var filters: [Bool] = []
        
        // LSP filter
        if let lspIds = preferences.subscribedLspIds {
            if lspIds.isEmpty {
                return .blocked(reason: "No LSPs subscribed")
            }
            if let lspId = payload.lspId {
                filters.append(lspIds.contains(lspId))
            }
        }
        
        // Location filter
        if let locationIds = preferences.subscribedLocationIds {
            if locationIds.isEmpty {
                return .blocked(reason: "No locations subscribed")
            }
            if let locationId = payload.locationId {
                filters.append(locationIds.contains(locationId))
            }
        }
        
        // Program filter
        if let programIds = preferences.subscribedProgramIds {
            if programIds.isEmpty {
                return .blocked(reason: "No programs subscribed")
            }
            filters.append(payload.programIds.contains { programIds.contains($0) })
        }
        
        // Orbit filter
        if let orbitIds = preferences.subscribedOrbitIds {
            if orbitIds.isEmpty {
                return .blocked(reason: "No orbits subscribed")
            }
            if let orbitId = payload.orbitId {
                filters.append(orbitIds.contains(orbitId))
            }
        }
        
        // Mission type filter
        if let missionTypeIds = preferences.subscribedMissionTypeIds {
            if missionTypeIds.isEmpty {
                return .blocked(reason: "No mission types subscribed")
            }
            if let missionTypeId = payload.missionTypeId {
                filters.append(missionTypeIds.contains(missionTypeId))
            }
        }
        
        // Launcher family filter
        if let launcherFamilyIds = preferences.subscribedLauncherFamilyIds {
            if launcherFamilyIds.isEmpty {
                return .blocked(reason: "No launcher families subscribed")
            }
            if let launcherFamilyId = payload.launcherFamilyId {
                filters.append(launcherFamilyIds.contains(launcherFamilyId))
            }
        }
        
        // 5. Apply filter logic (strict = AND, flexible = OR)
        if !filters.isEmpty {
            let passed: Bool
            if preferences.useStrictMatching {
                passed = filters.allSatisfy { $0 }  // AND: all must match
            } else {
                passed = filters.contains { $0 }    // OR: any must match
            }
            
            if !passed {
                return .blocked(reason: "Filter criteria not met (strict=\(preferences.useStrictMatching))")
            }
        }
        
        return .allowed
    }
}
