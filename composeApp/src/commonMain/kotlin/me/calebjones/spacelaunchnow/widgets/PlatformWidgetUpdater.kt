package me.calebjones.spacelaunchnow.widgets

/**
 * Platform-specific widget update functionality.
 * Common expect declaration.
 */
expect class PlatformWidgetUpdater(context: Any?) {
    suspend fun updateAllWidgets()
}
