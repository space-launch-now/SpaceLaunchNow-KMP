package me.calebjones.spacelaunchnow.util

actual fun initializeBuildConfig() {
    // For iOS, we default to debug mode for now
    // In a production app, this could be determined by build configuration
    BuildConfig.DEBUG = true
}