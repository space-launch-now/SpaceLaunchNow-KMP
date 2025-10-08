package me.calebjones.spacelaunchnow.util

actual fun initializeBuildConfig() {
    // For iOS, use hardcoded values that match version.properties
    // TODO: In the future, integrate with iOS build system to get these automatically
    BuildConfig.VERSION_NAME = "4.0.0-b16"
    BuildConfig.VERSION_CODE = 4000016
    
    // For iOS, we default to debug mode for now
    // In a production app, this could be determined by build configuration
    BuildConfig.IS_DEBUG = true
}