package me.calebjones.spacelaunchnow.widgets

import android.content.Context

/**
 * Platform-specific widget update functionality.
 * Android implementation triggers actual widget updates.
 */
actual class PlatformWidgetUpdater actual constructor(private val context: Any?) {
    
    actual suspend fun updateAllWidgets() {
        val androidContext = context as? Context ?: run {
            println("PlatformWidgetUpdater: Context is null, cannot update widgets")
            return
        }
        
        try {
            WidgetUpdater.updateAllWidgets(androidContext)
        } catch (e: Exception) {
            println("PlatformWidgetUpdater: Failed to update widgets: ${e.message}")
            e.printStackTrace()
        }
    }
}
