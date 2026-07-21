package me.calebjones.spacelaunchnow.util.logging

import me.calebjones.spacelaunchnow.data.notifications.NSEPreferenceBridge
import me.calebjones.spacelaunchnow.data.notifications.NseBreadcrumb

actual fun platformNotificationDiagnostics(): List<Pair<String, String>> {
    val snap = NSEPreferenceBridge.readStoredPrefs()
    fun present(v: Any?): String = v?.toString() ?: "MISSING"
    val pushSnap = PushDiagnostics.snapshot
    return listOf(
        "App Group available" to snap.appGroupAvailable.toString(),
        "Any key missing" to snap.anyKeyMissing.toString(),
        "NSE enableNotifications" to present(snap.enableNotifications),
        "NSE followAllLaunches" to present(snap.followAllLaunches),
        "NSE useStrictMatching" to present(snap.useStrictMatching),
        "NSE agencies (expanded)" to (snap.subscribedAgencies?.let { "${it.size}: ${it.take(12).joinToString(",")}" } ?: "MISSING"),
        "NSE locations (expanded)" to (snap.subscribedLocations?.let { "${it.size}: ${it.take(12).joinToString(",")}" } ?: "MISSING"),
    ) + PushDiagnostics.reportRows(pushSnap, pushSnap.notificationsEnabled, PlayServicesAvailability.NOT_APPLICABLE)
}

actual fun recentNseBreadcrumbs(): List<NseBreadcrumb> =
    NSEPreferenceBridge.peekNseEventLog().takeLast(20)

actual fun shareCopiesToClipboard(): Boolean = false
