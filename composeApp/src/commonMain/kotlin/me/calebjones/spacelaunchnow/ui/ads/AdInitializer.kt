package me.calebjones.spacelaunchnow.ui.ads

/**
 * Platform-specific ad initialization interface.
 * 
 * Provides expect/actual pattern for initializing advertising SDK on supported platforms
 * (Android, iOS) while gracefully handling unsupported platforms (Desktop).
 */
expect object AdInitializer {
    /**
     * Initialize the advertising SDK for the current platform.
     * 
     * @param context Platform-specific context (Activity on Android, null on iOS, ignored on Desktop)
     * @return true if initialization succeeded, false otherwise
     */
    fun initialize(context: Any?): Boolean
    
    /**
     * Configure ad request settings (test devices, content ratings, etc.)
     * 
     * @param isDebug Whether to enable debug/test mode
     * @param testDeviceIds List of test device IDs for ad testing
     */
    fun configure(isDebug: Boolean, testDeviceIds: List<String>)
    
    /**
     * Check if ads are supported on this platform
     */
    val isSupported: Boolean
}
