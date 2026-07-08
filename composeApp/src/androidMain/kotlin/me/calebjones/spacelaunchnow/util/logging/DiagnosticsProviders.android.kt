package me.calebjones.spacelaunchnow.util.logging

import me.calebjones.spacelaunchnow.data.notifications.NseBreadcrumb

actual fun platformNotificationDiagnostics(): List<Pair<String, String>> = emptyList()
actual fun recentNseBreadcrumbs(): List<NseBreadcrumb> = emptyList()
actual fun shareCopiesToClipboard(): Boolean = false
