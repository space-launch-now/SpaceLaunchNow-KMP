package me.calebjones.spacelaunchnow.data.notifications

import android.app.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

object NotificationPermissionManager {
    private var currentActivity: WeakReference<Activity>? = null
    private val _permissionRequestResult = MutableStateFlow<Boolean?>(null)
    val permissionRequestResult: StateFlow<Boolean?> = _permissionRequestResult.asStateFlow()

    fun setCurrentActivity(activity: Activity) {
        currentActivity = WeakReference(activity)
    }

    fun clearCurrentActivity() {
        currentActivity = null
    }

    fun requestPermissionFromSettings(): Boolean {
        val activity = currentActivity?.get()
        return if (activity != null) {
            val handler = AndroidNotificationPermissionHandler(activity)
            if (!handler.hasNotificationPermission()) {
                handler.requestNotificationPermission(activity)
                false // Permission requested, result will come later
            } else {
                true // Already has permission
            }
        } else {
            println("No activity available to request permission")
            false
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _permissionRequestResult.value = granted
    }

    fun clearPermissionResult() {
        _permissionRequestResult.value = null
    }
}