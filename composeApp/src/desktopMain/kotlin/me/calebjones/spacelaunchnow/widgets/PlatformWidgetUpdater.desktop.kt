package me.calebjones.spacelaunchnow.widgets

/**
 * Platform-specific widget update functionality.
 * Desktop implementation (no-op, as Desktop doesn't have Glance widgets).
 */
actual class PlatformWidgetUpdater actual constructor(private val context: Any?) {
    actual suspend fun updateAllWidgets() {
        // No-op on Desktop
    }
}
