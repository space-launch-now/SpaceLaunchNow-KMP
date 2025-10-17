package me.calebjones.spacelaunchnow.util

/**
 * Platform-specific utilities for notification management
 */
expect object NotificationSettingsHelper {
    /**
     * Opens the system notification settings for this app
     * - Android: Opens app notification settings in system settings
     * - iOS: Opens app settings where users can manage notifications
     * - Desktop: No-op or opens system notification preferences
     */
    fun openSystemNotificationSettings()
}