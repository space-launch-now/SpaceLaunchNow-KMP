package me.calebjones.spacelaunchnow.ui.viewmodel

import kotlinx.cinterop.ExperimentalForeignApi
import me.calebjones.spacelaunchnow.data.model.NotificationData
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSISO8601DateFormatter
import platform.Foundation.NSLog
import platform.Foundation.NSNumber
import platform.Foundation.NSTimeZone
import platform.Foundation.localTimeZone
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * iOS implementation of test notification display
 * Uses UNUserNotificationCenter to display local notifications
 * Matches the notification display logic from AppDelegate.swift
 */
@OptIn(ExperimentalForeignApi::class)
actual fun showTestNotification(notificationData: NotificationData) {
    NSLog("📱 [TestNotification] Triggering test notification...")
    NSLog("📱 [TestNotification] Type: ${notificationData.notificationType}")
    NSLog("📱 [TestNotification] Launch: ${notificationData.launchName}")

    // Run on main thread since we're dealing with UI
    dispatch_async(dispatch_get_main_queue()) {
        val content = UNMutableNotificationContent()

        // Format title - add 🔴 emoji if webcast is live (matches Android)
        val isWebcastLive = notificationData.webcastLive?.lowercase() == "true"
        val baseTitle = notificationData.launchName
        val displayTitle = if (isWebcastLive) "🔴 $baseTitle" else baseTitle

        // Format body using same logic as Android
        val displayBody = getNotificationBody(notificationData)

        content.setTitle("Test: $displayTitle")
        content.setBody(displayBody)
        content.setBadge(NSNumber(int = 1))
        content.setSound(UNNotificationSound.defaultSound())

        // Add custom data for handling taps
        content.setUserInfo(
            mapOf(
                "launch_id" to notificationData.launchUuid,
                "launch_uuid" to notificationData.launchUuid,
                "launch_name" to notificationData.launchName,
                "notification_type" to notificationData.notificationType,
                "is_test" to "true"
            )
        )

        NSLog("📱 [TestNotification] Title: ${content.title}")
        NSLog("📱 [TestNotification] Body: ${content.body}")

        // Create request and show notification immediately
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "test-${notificationData.launchUuid}",
            content = content,
            trigger = null // Show immediately
        )

        UNUserNotificationCenter.currentNotificationCenter()
            .addNotificationRequest(request) { error ->
                if (error != null) {
                    NSLog("❌ Failed to display test notification: ${error.localizedDescription}")
                } else {
                    NSLog("✅ Test notification displayed successfully")
                }
            }
    }
}

/**
 * Get notification body message based on notification type (matches Android strings)
 */
private fun getNotificationBody(data: NotificationData): String {
    val formattedDate = formatLaunchDate(data.launchNet)

    return when (data.notificationType.lowercase()) {
        "netstampchanged" -> "SCHEDULE UPDATE: Next attempt no earlier than $formattedDate"
        "success" -> "The launch was successful!"
        "failure" -> "A launch failure has occurred."
        "partialfailure" -> "The launch was a partial failure."
        "inflight" -> "Liftoff! Launch vehicle is now in flight!"
        "oneminute" -> "Launch attempt in less than one minute at $formattedDate"
        "tenminutes" -> "Launch attempt in less than ten minutes at $formattedDate"
        "onehour" -> "Launch attempt in less than one hour at $formattedDate"
        "twentyfourhour" -> "Launch attempt in less than 24 hours at $formattedDate"
        else -> "Launch from ${data.launchLocation}"
    }
}

/**
 * Format launch date to readable time (matches Android)
 * Input: "2025-10-15T12:00:00Z" -> Output: "12:00 PM"
 */
private fun formatLaunchDate(launchNet: String): String {
    val formatter = NSISO8601DateFormatter()

    val date = formatter.dateFromString(launchNet)

    if (date == null) {
        NSLog("⚠️ Failed to parse launch date: $launchNet")
        return launchNet
    }

    val outputFormatter = NSDateFormatter()
    outputFormatter.dateFormat = "h:mm a"
    outputFormatter.timeZone = NSTimeZone.localTimeZone
    return outputFormatter.stringFromDate(date)
}
