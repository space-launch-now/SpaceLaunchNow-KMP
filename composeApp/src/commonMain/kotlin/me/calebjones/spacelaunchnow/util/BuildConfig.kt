package me.calebjones.spacelaunchnow.util

/**
 * Build configuration constants
 */
object BuildConfig {
    const val VERSION_NAME = "1.0"
    const val VERSION_CODE = 1
    const val APP_NAME = "SpaceLaunchNow"

    // This will be set by platform-specific implementations
    var DEBUG: Boolean = false
}

// Platform-specific initialization function
expect fun initializeBuildConfig()
