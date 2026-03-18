package me.calebjones.spacelaunchnow.analytics

import me.calebjones.spacelaunchnow.data.storage.DebugPreferences

/**
 * Desktop no-op implementations for Datadog.
 * The Datadog KMP SDK does not publish JVM artifacts,
 * so all analytics calls are silently ignored on desktop.
 */

actual fun initializeDatadog(
    context: Any?,
    sampleRate: Float?,
    debugPreferences: DebugPreferences?
) {
    // No-op on desktop
}

actual object DatadogRUM {
    actual fun setUser(
        id: String,
        name: String?,
        email: String?,
        extraInfo: Map<String, Any?>
    ) {
        // No-op on desktop
    }

    actual fun clearUser() {
        // No-op on desktop
    }
}

actual object DatadogLogger {
    actual fun initialize(sampleRate: Float, debugPreferences: DebugPreferences?) {
        // No-op on desktop
    }

    actual fun debug(message: String, attributes: Map<String, Any?>) {
        // No-op on desktop
    }

    actual fun info(message: String, attributes: Map<String, Any?>) {
        // No-op on desktop
    }

    actual fun warn(message: String, attributes: Map<String, Any?>) {
        // No-op on desktop
    }

    actual fun error(message: String, throwable: Throwable?, attributes: Map<String, Any?>) {
        // No-op on desktop
    }

    actual fun critical(message: String, throwable: Throwable?, attributes: Map<String, Any?>) {
        // No-op on desktop
    }
}
