package me.calebjones.spacelaunchnow.ui.viewmodel

import android.content.Context
import me.calebjones.spacelaunchnow.data.model.NotificationData
import me.calebjones.spacelaunchnow.data.notifications.NotificationDisplayHelper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android implementation of test notification display
 * Uses the same NotificationDisplayHelper as Firebase messaging service
 */
actual fun showTestNotification(notificationData: NotificationData) {
    val context = object : KoinComponent {
        val context: Context by inject()
    }.context

    // Use the unified notification display helper
    // This ensures test notifications look and behave exactly like real FCM notifications
    NotificationDisplayHelper.showNotification(
        context = context,
        notificationData = notificationData,
        title = "Test: ${notificationData.launchName}",
        body = "Test launch ${notificationData.launchLocation} • ${notificationData.notificationType}"
    )
}
