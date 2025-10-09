package me.calebjones.spacelaunchnow.data.notifications

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class AndroidNotificationPermissionHandler(private val context: Context) {

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
        println("AndroidNotificationPermissionHandler.hasNotificationPermission() = $result")
        return result
    }

    fun requestNotificationPermission(activity: Activity): Boolean {
        println("AndroidNotificationPermissionHandler.requestNotificationPermission() called")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission()) {
                println("Requesting POST_NOTIFICATIONS permission via ActivityCompat.requestPermissions")
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
                false // Permission requested, result will come in onRequestPermissionsResult
            } else {
                println("Permission already granted, returning true")
                true // Already has permission
            }
        } else {
            println("Android version < 13, returning true (no permission needed)")
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