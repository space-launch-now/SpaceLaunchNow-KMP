package me.calebjones.spacelaunchnow.ui.viewmodel

import me.calebjones.spacelaunchnow.data.model.NotificationData

actual fun showTestNotification(notificationData: NotificationData) {
    // Desktop implementation could use system tray notifications
    println("🖥️ Desktop Test Notification: ${notificationData.launchName}")
    println("   Location: ${notificationData.launchLocation}")
    println("   Time: ${notificationData.launchNet}")
    println("   Type: ${notificationData.notificationType}")
}
