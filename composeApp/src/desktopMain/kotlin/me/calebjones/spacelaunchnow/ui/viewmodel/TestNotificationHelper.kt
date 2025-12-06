package me.calebjones.spacelaunchnow.ui.viewmodel

import me.calebjones.spacelaunchnow.data.model.NotificationData
import me.calebjones.spacelaunchnow.logger

private val log by lazy { logger() }

actual fun showTestNotification(notificationData: NotificationData) {
    // Desktop implementation could use system tray notifications
    log.d { "🖥️ Desktop Test Notification - Name: ${notificationData.launchName}, Location: ${notificationData.launchLocation}, Time: ${notificationData.launchNet}, Type: ${notificationData.notificationType}" }
}
