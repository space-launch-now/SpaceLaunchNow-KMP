package me.calebjones.spacelaunchnow.data.storage

import me.calebjones.spacelaunchnow.data.model.NotificationState

/**
 * Platform hook called after notification state is saved to DataStore.
 * iOS: writes preferences to shared App Group UserDefaults for NSE access.
 * Android / Desktop: no-op.
 */
expect fun syncNotificationStateToNSE(state: NotificationState)
