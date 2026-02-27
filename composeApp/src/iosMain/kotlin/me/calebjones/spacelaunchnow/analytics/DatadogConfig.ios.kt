package me.calebjones.spacelaunchnow.analytics

import com.datadog.kmp.rum.configuration.RumConfiguration

/**
 * iOS platform-specific RUM configuration.
 * Note: trackUiKitViews and trackUiKitActions are not available in KMP SDK.
 * Platform-specific view tracking should be configured differently if needed.
 */
internal actual fun rumPlatformSetup(rumConfigurationBuilder: RumConfiguration.Builder) {
    // iOS-specific RUM configuration not yet implemented
    // UIKit view/action tracking should be added when needed
}
