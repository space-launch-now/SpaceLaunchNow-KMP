package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchBasic

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
     * Formats a LaunchDetailed title using the standard format
     */
    fun formatLaunchTitle(launch: LaunchDetailed): String {
        return formatLaunchTitle(
            launchServiceProviderName = launch.launchServiceProvider.name,
            launchServiceProviderAbbrev = launch.launchServiceProvider.abbrev,
            rocketConfigurationName = launch.rocket?.configuration?.name,
            launchName = launch.name
        )
    }
    
    /**
     * Formats a LaunchNormal title using the standard format
     */
    fun formatLaunchTitle(launch: LaunchNormal): String {
        return formatLaunchTitle(
            launchServiceProviderName = launch.launchServiceProvider.name,
            launchServiceProviderAbbrev = launch.launchServiceProvider.abbrev,
            rocketConfigurationName = launch.rocket?.configuration?.name,
            launchName = launch.name
        )
    }
    
    /**
     * Formats a LaunchBasic title using the standard format
     */
    fun formatLaunchTitle(launch: LaunchBasic): String {
        return formatLaunchTitle(
            launchServiceProviderName = launch.launchServiceProvider.name,
            launchServiceProviderAbbrev = launch.launchServiceProvider.abbrev,
            rocketConfigurationName = null,
            launchName = launch.name
        )
    }
}
