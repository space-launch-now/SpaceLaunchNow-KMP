package me.calebjones.spacelaunchnow.analytics

/**
 * Initialize Datadog RUM for Kotlin Multiplatform.
 * Platform-specific implementations handle SDK initialization.
 * Desktop is a no-op since the Datadog KMP SDK doesn't support JVM.
 *
 * @param context Application context on Android, null on iOS/Desktop
 * @param sampleRate Optional sample rate override (0-100%)
 * @param debugPreferences Optional DebugPreferences to observe sample rate changes dynamically
 */
expect fun initializeDatadog(
    context: Any? = null,
    sampleRate: Float? = null,
    debugPreferences: me.calebjones.spacelaunchnow.data.storage.DebugPreferences? = null
)

/**
 * Helper for common RUM/user operations.
 * Desktop actual is a no-op; Android/iOS call Datadog SDK.
 */
expect object DatadogRUM {
    fun setUser(
        id: String,
        name: String? = null,
        email: String? = null,
        extraInfo: Map<String, Any?> = emptyMap()
    )
    fun clearUser()
}

/**
 * Global Datadog Logger.
 * Desktop actual is a no-op; Android/iOS forward to Datadog SDK Logger.
 */
expect object DatadogLogger {
    fun initialize(
        sampleRate: Float = 1f,
        debugPreferences: me.calebjones.spacelaunchnow.data.storage.DebugPreferences? = null
    )
    fun debug(message: String, attributes: Map<String, Any?> = emptyMap())
    fun info(message: String, attributes: Map<String, Any?> = emptyMap())
    fun warn(message: String, attributes: Map<String, Any?> = emptyMap())
    fun error(
        message: String,
        throwable: Throwable? = null,
        attributes: Map<String, Any?> = emptyMap()
    )
    fun critical(
        message: String,
        throwable: Throwable? = null,
        attributes: Map<String, Any?> = emptyMap()
    )
}
