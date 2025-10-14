package me.calebjones.spacelaunchnow.ui.viewmodel

import me.calebjones.spacelaunchnow.data.model.NotificationData

/**
 * Platform-specific notification helper for testing
 * Shows a local notification using platform APIs
 */
expect fun showTestNotification(notificationData: NotificationData)
