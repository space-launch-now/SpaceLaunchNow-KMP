package me.calebjones.spacelaunchnow.widgets

import android.content.Context
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Platform-specific widget update functionality.
 * Android implementation triggers actual widget updates.
 */
actual class PlatformWidgetUpdater actual constructor(private val context: Any?) {
    
    private val log = logger()

    actual suspend fun updateAllWidgets() {
        val androidContext = context as? Context ?: run {
            log.w{ "PlatformWidgetUpdater: Context is null, cannot update widgets" }
            return
        }
        
        try {
            WidgetUpdater.updateAllWidgets(androidContext)
        } catch (e: Exception) {
            log.e(e) { "PlatformWidgetUpdater: Failed to update widgets: ${e.message}" }
        }
    }
}
