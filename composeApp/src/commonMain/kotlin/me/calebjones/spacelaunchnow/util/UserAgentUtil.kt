package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.getPlatform

/**
 * Utility for generating dynamic user agent strings
 */
object UserAgentUtil {
    /**
     * Generates a user agent string with app version and platform info
     * Format: "SpaceLaunchNow-KMP/1.0 (Platform Details)"
     */
    fun getUserAgent(): String {
        val platform = getPlatform()
        return "${BuildConfig.APP_NAME}-v${BuildConfig.VERSION_NAME} (${platform.name})"
    }
}
