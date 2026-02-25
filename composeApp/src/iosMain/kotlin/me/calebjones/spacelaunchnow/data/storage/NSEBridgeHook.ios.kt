package me.calebjones.spacelaunchnow.data.storage

import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.notifications.NSEPreferenceBridge

actual fun syncNotificationStateToNSE(state: NotificationState) {
    NSEPreferenceBridge.syncToUserDefaults(state)
}
