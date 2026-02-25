import Foundation

/**
 * V5 Notification Data
 *
 * Swift representation of V5 notification payload.
 * Used by the Notification Service Extension for parsing V5 notifications
 * and enriching content (title, body, image attachment).
 *
 * V5 payloads are distinguished from V4 by the presence of 'lsp_id' field.
 * Filtering is handled by IosNotificationBridge (Kotlin), not in Swift.
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
