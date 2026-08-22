package me.calebjones.spacelaunchnow.data.storage

import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.notifications.IosNotificationBridge
import me.calebjones.spacelaunchnow.data.notifications.NSEPreferenceBridge

actual fun syncNotificationStateToNSE(state: NotificationState) {
    NSEPreferenceBridge.syncToUserDefaults(state)
    // The NSE reads the App Group fresh per push, but the in-app launch paths
    // (willPresent / didReceiveRemoteNotification) go through IosNotificationBridge's
    // cached state, which was otherwise only refreshed at launch. Keep it in step so a
    // settings change -- or the V5->V6 changeover completing mid-session -- takes effect
    // immediately instead of at the next launch.
    IosNotificationBridge.onStateSaved(state)
}
