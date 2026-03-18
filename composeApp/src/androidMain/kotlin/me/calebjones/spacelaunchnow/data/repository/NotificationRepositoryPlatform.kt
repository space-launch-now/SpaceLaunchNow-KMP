package me.calebjones.spacelaunchnow.data.repository

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import me.calebjones.spacelaunchnow.data.notifications.AndroidNotificationPermissionHandler
import me.calebjones.spacelaunchnow.data.notifications.NotificationPermissionManager
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object PermissionHelper : KoinComponent {
    val context: Context by inject()
}

private val log by lazy { SpaceLogger.getLogger("NotificationRepository") }

actual suspend fun requestPlatformNotificationPermission(): Boolean {

    // For Android, we return whether permission is already granted
    // The actual permission request happens in MainActivity
    return try {
        val context = PermissionHelper.context
        val handler = AndroidNotificationPermissionHandler(context)

        val hasPermission = handler.hasNotificationPermission()
        log.d("requestPlatformNotificationPermission: hasPermission = $hasPermission")

        if (!hasPermission) {
            log.i("Android notification permission not granted. Attempting to request permission...")
            // Clear any stale result before requesting
            NotificationPermissionManager.clearPermissionResult()
            NotificationPermissionManager.requestPermissionFromSettings()
            // Suspend until the permission dialog result arrives
            val granted = NotificationPermissionManager.permissionRequestResult
                .filterNotNull()
                .first()
            log.d("requestPlatformNotificationPermission: dialog result = $granted")
            return granted
        }

        hasPermission
    } catch (e: Exception) {
        log.e("Failed to check notification permission: ${e.message}")
        false
    }
}

actual suspend fun hasPlatformNotificationPermission(): Boolean {
    return try {
        val context = PermissionHelper.context
        val handler = AndroidNotificationPermissionHandler(context)
        handler.hasNotificationPermission()
    } catch (e: Exception) {
        log.e("Failed to check notification permission: ${e.message}")
        false
    }
}

actual fun openPlatformNotificationSettings(): Boolean {
    return try {
        val context = PermissionHelper.context
        val intent = android.content.Intent().apply {
            action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        log.e("Failed to open notification settings: ${e.message}")
        false
    }
}