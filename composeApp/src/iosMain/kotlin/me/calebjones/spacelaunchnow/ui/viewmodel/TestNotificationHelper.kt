package me.calebjones.spacelaunchnow.ui.viewmodel

import me.calebjones.spacelaunchnow.data.model.NotificationData

actual fun showTestNotification(notificationData: NotificationData) {
    // iOS implementation would use UNUserNotificationCenter
    println("📱 iOS Test Notification: ${notificationData.launchName}")
    println("   Location: ${notificationData.launchLocation}")
    println("   Time: ${notificationData.launchNet}")
    println("   Type: ${notificationData.notificationType}")
}
