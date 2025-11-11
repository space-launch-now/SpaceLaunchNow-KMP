package me.calebjones.spacelaunchnow.analytics

import com.datadog.kmp.rum.configuration.RumConfiguration

/**
 * iOS stub implementation for Datadog.
 * Datadog is not currently set up for iOS, so this provides no-op implementations.
 */
internal actual fun rumPlatformSetup(rumConfigurationBuilder: RumConfiguration.Builder) {
    // iOS-specific RUM configuration
    // Note: trackUiKitViews and trackUiKitActions are not available in KMP SDK
    // Platform-specific view tracking should be configured differently if needed
    // This function is never called since initializeDatadog returns early when disabled
}
