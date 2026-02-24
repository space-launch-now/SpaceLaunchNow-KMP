package me.calebjones.spacelaunchnow.data.storage

import me.calebjones.spacelaunchnow.data.model.NotificationState

// NSE UserDefaults bridge is iOS-only
actual fun syncNotificationStateToNSE(state: NotificationState) = Unit
