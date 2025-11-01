package me.calebjones.spacelaunchnow.analytics

import com.datadog.kmp.rum.RumConfiguration

internal actual fun rumPlatformSetup(rumConfigurationBuilder: RumConfiguration.Builder) {
    with(rumConfigurationBuilder) {
        trackUiKitViews()
        trackUiKitActions()
    }
}
