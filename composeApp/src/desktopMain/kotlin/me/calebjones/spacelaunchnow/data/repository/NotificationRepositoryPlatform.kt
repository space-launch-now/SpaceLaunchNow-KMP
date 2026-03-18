package me.calebjones.spacelaunchnow.data.repository

actual suspend fun requestPlatformNotificationPermission(): Boolean {
    // Desktop doesn't require notification permissions
    return true
}

actual suspend fun hasPlatformNotificationPermission(): Boolean {
    // Desktop doesn't require notification permissions
    return true
}

actual fun openPlatformNotificationSettings(): Boolean {
    // Desktop doesn't have notification settings
    return false
}