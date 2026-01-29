package me.calebjones.spacelaunchnow.data.model

/**
 * V5 Notification Topic Configuration
 *
 * Centralizes all topic constants for V5 notification system.
 * Platform-specific topics ensure correct message handling:
 * - Android: Data-only FCM messages (app constructs notifications)
 * - iOS: Mutable-content messages (NSE intercepts before display)
 */
object NotificationTopicConfig {
    // V5 Platform-Specific Topics
    const val PROD_V5_ANDROID = "prod_v5_android"
    const val DEBUG_V5_ANDROID = "debug_v5_android"
    const val PROD_V5_IOS = "prod_v5_ios"
    const val DEBUG_V5_IOS = "debug_v5_ios"

    // V4 Topics (for migration)
    const val PROD_V4 = "k_prod_v4"
    const val DEBUG_V4 = "k_debug_v4"

    // Notification Type Topics (user-selectable timing preferences)
    val NOTIFICATION_TYPE_TOPICS = setOf(
        "tenMinutes",
        "oneHour",
        "twentyFourHour",
        "netstampChanged",
        "inFlight",
        "success",
        "failure",
        "webcastLive"
    )

    /**
     * V5 Payload Field Constants
     * Keys used in FCM data payload
     */
    object PayloadFields {
        // Core fields
        const val NOTIFICATION_TYPE = "notification_type"
        const val TITLE = "title"
        const val BODY = "body"

        // Launch identification
        const val LAUNCH_UUID = "launch_uuid"
        const val LAUNCH_ID = "launch_id"
        const val LAUNCH_NAME = "launch_name"

        // Launch details
        const val LAUNCH_IMAGE = "launch_image"
        const val LAUNCH_NET = "launch_net"
        const val LAUNCH_LOCATION = "launch_location"
        const val WEBCAST = "webcast"
        const val WEBCAST_LIVE = "webcast_live"

        // V5 Extended Filtering IDs
        const val LSP_ID = "lsp_id"
        const val LOCATION_ID = "location_id"
        const val PROGRAM_IDS = "program_ids"
        const val STATUS_ID = "status_id"
        const val ORBIT_ID = "orbit_id"
        const val MISSION_TYPE_ID = "mission_type_id"
        const val LAUNCHER_FAMILY_ID = "launcher_family_id"

        // V4 Legacy Fields (for backward compatibility)
        const val AGENCY_ID = "agency_id"
    }

    /**
     * Supported platforms for topic selection
     */
    enum class Platform {
        ANDROID,
        IOS,
        DESKTOP  // No-op for desktop
    }

    /**
     * Get the V5 topic for a specific platform and build type
     *
     * @param platform Target platform
     * @param isDebug Whether this is a debug build
     * @return The appropriate V5 topic string
     */
    fun getV5Topic(platform: Platform, isDebug: Boolean): String {
        return when {
            platform == Platform.ANDROID && isDebug -> DEBUG_V5_ANDROID
            platform == Platform.ANDROID && !isDebug -> PROD_V5_ANDROID
            platform == Platform.IOS && isDebug -> DEBUG_V5_IOS
            platform == Platform.IOS && !isDebug -> PROD_V5_IOS
            else -> PROD_V5_ANDROID // Desktop fallback (no-op anyway)
        }
    }

    /**
     * Get the V4 topic for migration purposes
     *
     * @param isDebug Whether this is a debug build
     * @return The appropriate V4 topic string
     */
    fun getV4Topic(isDebug: Boolean): String {
        return if (isDebug) DEBUG_V4 else PROD_V4
    }

    /**
     * Check if a topic is a V5 topic
     */
    fun isV5Topic(topic: String): Boolean {
        return topic in setOf(PROD_V5_ANDROID, DEBUG_V5_ANDROID, PROD_V5_IOS, DEBUG_V5_IOS)
    }

    /**
     * Check if a topic is a V4 topic
     */
    fun isV4Topic(topic: String): Boolean {
        return topic in setOf(PROD_V4, DEBUG_V4)
    }
}
