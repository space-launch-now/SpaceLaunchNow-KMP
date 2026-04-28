package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.domain.model.Launch

/**
 * Common interface for sharing launch information across platforms
 */
interface LaunchSharingService {
    suspend fun shareLaunch(launch: Launch)
    suspend fun shareUrl(url: String, text: String? = null)
}

/**
 * Platform-specific sharing functionality using expect/actual pattern
 */
expect fun createPlatformSharingService(): LaunchSharingService

/**
 * Common sharing utility that formats launch information for sharing
 */
object SharingFormatUtil {

    /**
     * Formats launch information for sharing
     * @param useUtc If true, displays time in UTC instead of local timezone
     */
    fun formatLaunchShareText(launch: Launch, useUtc: Boolean = false): String {
        val title = LaunchFormatUtil.formatLaunchTitle(launch)
        val launchTime = launch.net?.let { DateTimeUtil.formatLaunchDate(it, useUtc) } ?: "TBD"

        val launchUrl = "https://spacelaunchnow.app/launch/${launch.slug}"

        return buildString {
            appendLine(title)
            appendLine("NET: $launchTime")
            appendLine("Link: $launchUrl")

            // Add link to SpaceLaunchNow if you have deep linking
            appendLine()
            appendLine("Learn more about this launch via Space Launch Now!")
        }
    }
}