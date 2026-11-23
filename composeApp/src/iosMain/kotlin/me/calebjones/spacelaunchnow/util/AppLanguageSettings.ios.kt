package me.calebjones.spacelaunchnow.util

actual object AppLanguageSettings {
    actual val isSupported: Boolean = true

    // iOS shows the per-app Language row on the app page of the Settings app.
    actual fun open() = NotificationSettingsHelper.openSystemNotificationSettings()
}
