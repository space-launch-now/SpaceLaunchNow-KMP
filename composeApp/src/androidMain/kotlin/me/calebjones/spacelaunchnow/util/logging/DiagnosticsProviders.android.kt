package me.calebjones.spacelaunchnow.util.logging

import me.calebjones.spacelaunchnow.data.notifications.AndroidNotificationPermissionHandler
import me.calebjones.spacelaunchnow.data.notifications.NseBreadcrumb
import me.calebjones.spacelaunchnow.data.repository.PermissionHelper

actual fun platformNotificationDiagnostics(): List<Pair<String, String>> {
    val notificationsEnabled = runCatching {
        AndroidNotificationPermissionHandler(PermissionHelper.context).hasNotificationPermission()
    }.getOrNull()
    val playServices = runCatching { checkPlayServicesAvailability() }
        .getOrDefault(PlayServicesAvailability.UNKNOWN)
    return PushDiagnostics.reportRows(PushDiagnostics.snapshot, notificationsEnabled, playServices)
}

actual fun recentNseBreadcrumbs(): List<NseBreadcrumb> = emptyList()
actual fun shareCopiesToClipboard(): Boolean = false
