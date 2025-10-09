package me.calebjones.spacelaunchnow.data.repository

actual suspend fun requestPlatformNotificationPermission(): Boolean {
    // TODO: Implement iOS notification permission request
    // For now, return true as iOS handles permissions differently
    return true
}

actual suspend fun hasPlatformNotificationPermission(): Boolean {
    // TODO: Implement iOS notification permission check
    // For now, return true as iOS handles permissions differently
    return true
}