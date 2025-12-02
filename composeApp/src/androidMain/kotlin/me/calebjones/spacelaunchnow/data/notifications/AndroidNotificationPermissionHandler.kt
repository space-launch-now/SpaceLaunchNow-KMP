package me.calebjones.spacelaunchnow.data.notifications

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import me.calebjones.spacelaunchnow.util.logging.logger

class AndroidNotificationPermissionHandler(private val context: Context) {
    private val log = logger()

    fun hasNotificationPermission(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For Android 12 and below, notification permission is granted by default
            true
        }
        log.d { "AndroidNotificationPermissionHandler.hasNotificationPermission() = $result" }
        return result
    }

    fun requestNotificationPermission(activity: Activity): Boolean {
        log.d { "AndroidNotificationPermissionHandler.requestNotificationPermission() called" }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission()) {
                log.d { "Requesting POST_NOTIFICATIONS permission via ActivityCompat.requestPermissions" }
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
                false // Permission requested, result will come in onRequestPermissionsResult
            } else {
                log.d { "Permission already granted, returning true" }
                true // Already has permission
            }
        } else {
            log.d { "Android version < 13, returning true (no permission needed)" }
            true // No permission needed for Android 12 and below
        }
    }

    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ): Boolean {
        return if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
    }

    companion object {
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }
}