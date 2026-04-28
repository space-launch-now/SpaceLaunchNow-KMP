package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.domain.model.Launch

/**
 * Utility object for formatting launch information consistently across the application
 */
object LaunchFormatUtil {

    /**
     * Formats a launch title using the standard format: "<LSP> | <Launch Vehicle>"
     * If rocket configuration is not available, falls back to launch name or "Unknown Name"
     *
     * For LSP (Launch Service Provider):
     * - Uses abbreviation if name is longer than 15 characters and abbreviation exists
     * - Otherwise uses full name
     *
     * @param launchServiceProviderName The full name of the launch service provider
     * @param launchServiceProviderAbbrev The abbreviation of the launch service provider (nullable)
     * @param rocketConfigurationName The name of the rocket configuration (nullable)
     * @param launchName The fallback launch name (nullable)
     * @return Formatted title string
     */
    fun formatLaunchTitle(
        launchServiceProviderName: String,
        launchServiceProviderAbbrev: String?,
        rocketConfigurationName: String?,
        launchName: String?
    ): String {
        return if (rocketConfigurationName != null) {
            val providerName = if (
                launchServiceProviderName.length > 15 &&
                !launchServiceProviderAbbrev.isNullOrEmpty()
            ) {
                launchServiceProviderAbbrev
            } else {
                launchServiceProviderName
            }
            "$providerName | $rocketConfigurationName"
        } else if (!launchName.isNullOrEmpty()) {
            launchName
        } else {
            "Unknown Name"
        }
    }

    /**
     * Formats a domain Launch title using the standard format
     */
    fun formatLaunchTitle(launch: Launch): String {
        return formatLaunchTitle(
            launchServiceProviderName = launch.provider?.name ?: "",
            launchServiceProviderAbbrev = launch.provider?.abbrev,
            rocketConfigurationName = launch.rocket?.fullName ?: launch.rocket?.name,
            launchName = launch.name
        )
    }
}
