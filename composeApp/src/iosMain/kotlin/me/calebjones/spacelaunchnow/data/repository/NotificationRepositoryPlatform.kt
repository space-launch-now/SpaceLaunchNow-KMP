package me.calebjones.spacelaunchnow.data.repository

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

actual suspend fun requestPlatformNotificationPermission(): Boolean =
    suspendCancellableCoroutine { cont ->
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound
        ) { granted, _ ->
            if (granted) {
                // Notify Swift side to call registerForRemoteNotifications
                NSNotificationCenter.defaultCenter.postNotificationName(
                    aName = "KotlinNotificationPermissionGranted",
                    `object` = null
                )
            }
            cont.resume(granted)
        }
    }

actual suspend fun hasPlatformNotificationPermission(): Boolean =
    suspendCancellableCoroutine { cont ->
        UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
            cont.resume(settings?.authorizationStatus == UNAuthorizationStatusAuthorized)
        }
    }

actual suspend fun getPlatformNotificationPermissionStatus(): NotificationPermissionStatus =
    suspendCancellableCoroutine { cont ->
        UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
            cont.resume(
                when (settings?.authorizationStatus) {
                    UNAuthorizationStatusAuthorized -> NotificationPermissionStatus.GRANTED
                    UNAuthorizationStatusDenied -> NotificationPermissionStatus.DENIED
                    // NotDetermined, Provisional, Ephemeral: the user has never
                    // explicitly answered the dialog, so it can still be shown.
                    else -> NotificationPermissionStatus.NOT_DETERMINED
                }
            )
        }
    }

actual fun openPlatformNotificationSettings(): Boolean {
    val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return false
    UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>()) { _ -> }
    return true
}