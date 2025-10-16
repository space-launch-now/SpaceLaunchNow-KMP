package me.calebjones.spacelaunchnow.widgets

/**
 * Platform-specific widget update functionality.
 * iOS implementation (no-op, as iOS doesn't have Glance widgets).
 */
actual class PlatformWidgetUpdater actual constructor(private val context: Any?) {
    actual suspend fun updateAllWidgets() {
        // No-op on iOS
    }
}
