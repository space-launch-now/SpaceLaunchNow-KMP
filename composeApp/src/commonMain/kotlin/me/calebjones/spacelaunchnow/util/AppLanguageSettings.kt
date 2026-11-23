package me.calebjones.spacelaunchnow.util

/**
 * Opens the OS screen where the user picks the language of this app.
 * The app has no in-app language picker; it follows the OS per-app language setting.
 */
expect object AppLanguageSettings {
    /** True when this platform has a per-app language screen the row can open. */
    val isSupported: Boolean

    /**
     * - Android 13+: the per-app language screen of system settings
     * - iOS: the app page in the Settings app (it holds the Language row)
     * - Desktop: not supported
     */
    fun open()
}
