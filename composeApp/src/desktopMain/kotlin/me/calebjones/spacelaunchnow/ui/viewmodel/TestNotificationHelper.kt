package me.calebjones.spacelaunchnow.ui.viewmodel

import me.calebjones.spacelaunchnow.data.model.NotificationData
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger

private val log by lazy { SpaceLogger.getLogger("TestNotificationHelper") }

actual fun showTestNotification(notificationData: NotificationData) {
    // Desktop implementation could use system tray notifications
    log.d { "🖥️ Desktop Test Notification - Name: ${notificationData.launchName}, Location: ${notificationData.launchLocation}, Time: ${notificationData.launchNet}, Type: ${notificationData.notificationType}" }
}
