package me.calebjones.spacelaunchnow.analytics

import com.datadog.kmp.rum.RumConfiguration

internal actual fun rumPlatformSetup(rumConfigurationBuilder: RumConfiguration.Builder) {
    // Desktop doesn't have platform-specific tracking options yet
    // Just use the common configuration
}
