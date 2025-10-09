package me.calebjones.spacelaunchnow.util

/**
 * Build configuration constants
 */
object BuildConfig {
    // These will be set by platform-specific implementations
    var VERSION_NAME: String = "unknown"
    var VERSION_CODE: Int = 0
    const val APP_NAME = "SpaceLaunchNow"

    // This will be set by platform-specific implementations
    var IS_DEBUG: Boolean = false
}

// Platform-specific initialization function
expect fun initializeBuildConfig()
