package me.calebjones.spacelaunchnow.data.repository

import android.app.Activity
import android.content.Context
import me.calebjones.spacelaunchnow.data.notifications.AndroidNotificationPermissionHandler
import me.calebjones.spacelaunchnow.data.notifications.NotificationPermissionManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object PermissionHelper : KoinComponent {
    val context: Context by inject()
}

actual suspend fun requestPlatformNotificationPermission(): Boolean {
    // For Android, we return whether permission is already granted
    // The actual permission request happens in MainActivity
    return try {
        val context = PermissionHelper.context
        val handler = AndroidNotificationPermissionHandler(context)

        val hasPermission = handler.hasNotificationPermission()
        println("requestPlatformNotificationPermission: hasPermission = $hasPermission")

        if (!hasPermission) {
            println("Android notification permission not granted. Attempting to request permission...")
            // Try to request permission through the manager
            return NotificationPermissionManager.requestPermissionFromSettings()
        }

        hasPermission
    } catch (e: Exception) {
        println("Failed to check notification permission: ${e.message}")
        false
    }
}

actual suspend fun hasPlatformNotificationPermission(): Boolean {
    return try {
        val context = PermissionHelper.context
        val handler = AndroidNotificationPermissionHandler(context)
        handler.hasNotificationPermission()
    } catch (e: Exception) {
        println("Failed to check notification permission: ${e.message}")
        false
    }
}