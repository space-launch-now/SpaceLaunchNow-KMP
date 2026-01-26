package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * V5 Notification Payload
 *
 * Represents the V5 notification payload received from server.
 * V5 payloads include extended filtering IDs (lsp_id, location_id, program_ids, etc.)
 * that allow for more granular client-side filtering.
 *
 * Differences from V4:
 * - Uses lsp_id instead of agency_id
 * - Includes program_ids (comma-separated list)
 * - Includes optional: status_id, orbit_id, mission_type_id, launcher_family_id
 * - Server provides title and body (Android must construct notification)
 */
@Serializable
data class V5NotificationPayload(
    // Display Content
    val notificationType: String,      // tenMinutes, oneHour, twentyFourHour, netstampChanged, inFlight, success, failure, webcastLive
    val title: String,                  // Server-provided title
    val body: String,                   // Server-provided body

    // Launch Identification
    val launchUuid: String,             // UUID for deep linking (primary key)
    val launchId: String,               // Library ID (legacy)
    val launchName: String,             // Display name

    // Launch Details
    val launchImage: String?,           // Image URL (optional)
    val launchNet: String,              // ISO 8601 datetime
    val launchLocation: String,         // Location display name
    val webcast: Boolean,               // Has webcast
    val webcastLive: Boolean,           // Is webcast currently live

    // V5 Filtering IDs (Extended)
    val lspId: Int?,                    // Launch Service Provider ID
    val locationId: Int?,               // Launch location ID
    val programIds: List<Int>,          // Program IDs (parsed from comma-separated string)
    val statusId: Int?,                 // Launch status ID (optional)
    val orbitId: Int?,                  // Target orbit ID (optional)
    val missionTypeId: Int?,            // Mission type ID (optional)
    val launcherFamilyId: Int?          // Launcher family ID (optional)
) {
    companion object {
        private val log = logger()

        /**
         * Parse V5 payload from FCM data map
         *
         * @param data The FCM data payload as a Map<String, String>
         * @return V5NotificationPayload or null if required fields are missing
         */
        fun fromMap(data: Map<String, String>): V5NotificationPayload? {
            return try {
                V5NotificationPayload(
                    notificationType = data[NotificationTopicConfig.PayloadFields.NOTIFICATION_TYPE] ?: return null,
                    title = data[NotificationTopicConfig.PayloadFields.TITLE]
                        ?: data[NotificationTopicConfig.PayloadFields.LAUNCH_NAME]
                        ?: return null,
                    body = data[NotificationTopicConfig.PayloadFields.BODY] ?: "",
                    launchUuid = data[NotificationTopicConfig.PayloadFields.LAUNCH_UUID] ?: return null,
                    launchId = data[NotificationTopicConfig.PayloadFields.LAUNCH_ID] ?: "",
                    launchName = data[NotificationTopicConfig.PayloadFields.LAUNCH_NAME] ?: return null,
                    launchImage = data[NotificationTopicConfig.PayloadFields.LAUNCH_IMAGE],
                    launchNet = data[NotificationTopicConfig.PayloadFields.LAUNCH_NET] ?: return null,
                    launchLocation = data[NotificationTopicConfig.PayloadFields.LAUNCH_LOCATION] ?: "",
                    webcast = data[NotificationTopicConfig.PayloadFields.WEBCAST]?.lowercase() == "true",
                    webcastLive = data[NotificationTopicConfig.PayloadFields.WEBCAST_LIVE]?.lowercase() == "true",
                    // V5 Extended Fields
                    lspId = data[NotificationTopicConfig.PayloadFields.LSP_ID]?.toIntOrNull(),
                    locationId = data[NotificationTopicConfig.PayloadFields.LOCATION_ID]?.toIntOrNull(),
                    programIds = data[NotificationTopicConfig.PayloadFields.PROGRAM_IDS]
                        ?.split(",")
                        ?.mapNotNull { it.trim().toIntOrNull() }
                        ?: emptyList(),
                    statusId = data[NotificationTopicConfig.PayloadFields.STATUS_ID]?.toIntOrNull(),
                    orbitId = data[NotificationTopicConfig.PayloadFields.ORBIT_ID]?.toIntOrNull(),
                    missionTypeId = data[NotificationTopicConfig.PayloadFields.MISSION_TYPE_ID]?.toIntOrNull(),
                    launcherFamilyId = data[NotificationTopicConfig.PayloadFields.LAUNCHER_FAMILY_ID]?.toIntOrNull()
                )
            } catch (e: Exception) {
                log.e(e) { "❌ Failed to parse V5 notification payload: ${e.message}" }
                null
            }
        }

        /**
         * Detect if payload is V5 format
         *
         * V5 payloads are distinguished by the presence of 'lsp_id' field.
         * V4 payloads use 'agency_id' instead.
         *
         * @param data The FCM data payload as a Map<String, String>
         * @return true if this is a V5 payload
         */
        fun isV5Payload(data: Map<String, String>): Boolean {
            return data.containsKey(NotificationTopicConfig.PayloadFields.LSP_ID)
        }
    }

    /**
     * Check if this notification has any filtering IDs
     */
    fun hasFilteringIds(): Boolean {
        return lspId != null || locationId != null || programIds.isNotEmpty() ||
            statusId != null || orbitId != null || missionTypeId != null || launcherFamilyId != null
    }

    /**
     * Convert to a debug-friendly string
     */
    fun toDebugString(): String {
        return buildString {
            append("V5Notification(")
            append("type=$notificationType, ")
            append("launch=$launchName, ")
            append("lspId=$lspId, ")
            append("locationId=$locationId, ")
            if (programIds.isNotEmpty()) append("programs=$programIds, ")
            if (statusId != null) append("status=$statusId, ")
            if (orbitId != null) append("orbit=$orbitId, ")
            if (missionTypeId != null) append("missionType=$missionTypeId, ")
            if (launcherFamilyId != null) append("launcherFamily=$launcherFamilyId, ")
            append("webcast=$webcast)")
        }
    }
}
