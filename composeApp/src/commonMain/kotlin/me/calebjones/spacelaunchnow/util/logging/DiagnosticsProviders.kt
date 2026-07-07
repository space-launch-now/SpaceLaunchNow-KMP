package me.calebjones.spacelaunchnow.util.logging

import me.calebjones.spacelaunchnow.data.notifications.NseBreadcrumb

/** Label/value rows describing platform notification-delivery state (NSE App Group on iOS). */
expect fun platformNotificationDiagnostics(): List<Pair<String, String>>

/** Recent NSE delivery breadcrumbs, non-destructive (iOS only; empty elsewhere). */
expect fun recentNseBreadcrumbs(): List<NseBreadcrumb>

/** True when sharePlainText copies to clipboard instead of opening a share sheet. */
expect fun shareCopiesToClipboard(): Boolean
