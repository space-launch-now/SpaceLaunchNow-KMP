package me.calebjones.spacelaunchnow.util

actual fun initializeBuildConfig() {
    // For desktop, we can check system properties or default to debug mode
    BuildConfig.DEBUG = System.getProperty("app.debug", "true").toBoolean()
}