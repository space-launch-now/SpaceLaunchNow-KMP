package me.calebjones.spacelaunchnow.widgets

/**
 * Platform-specific widget update functionality.
 * iOS implementation — delegates to WidgetKitBridge, which Swift registers with
 * a WidgetCenter.shared.reloadAllTimelines() implementation in AppDelegate.
 */
actual class PlatformWidgetUpdater actual constructor(private val context: Any?) {
    actual suspend fun updateAllWidgets() {
        WidgetKitBridge.requestReload()
    }
}
