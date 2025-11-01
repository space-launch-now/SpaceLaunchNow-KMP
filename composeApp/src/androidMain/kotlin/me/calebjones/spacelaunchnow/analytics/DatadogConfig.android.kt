package me.calebjones.spacelaunchnow.analytics

import com.datadog.kmp.rum.configuration.RumConfiguration
import com.datadog.kmp.rum.configuration.trackUserInteractions
import com.datadog.kmp.rum.configuration.useViewTrackingStrategy
import com.datadog.kmp.rum.tracking.ActivityViewTrackingStrategy

internal actual fun rumPlatformSetup(rumConfigurationBuilder: RumConfiguration.Builder) {
    with(rumConfigurationBuilder) {
        useViewTrackingStrategy(ActivityViewTrackingStrategy(true))
        trackUserInteractions()
    }
}
