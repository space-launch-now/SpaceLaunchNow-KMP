package me.calebjones.spacelaunchnow.util

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

/**
 * iOS implementation for opening system notification settings
 */
actual object NotificationSettingsHelper {
    
    /**
     * Opens iOS app settings where users can manage notifications
     * This opens the Settings app to this specific app's settings page
     */
    actual fun openSystemNotificationSettings() {
        try {
            val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
            settingsUrl?.let { url ->
                val application = UIApplication.sharedApplication
                if (application.canOpenURL(url)) {
                    application.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
                } else {
                    println("Cannot open iOS settings URL")
                }
            }
        } catch (e: Exception) {
            println("Failed to open iOS notification settings: ${e.message}")
        }
    }
}