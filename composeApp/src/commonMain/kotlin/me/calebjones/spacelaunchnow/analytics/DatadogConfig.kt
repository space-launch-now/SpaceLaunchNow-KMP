package me.calebjones.spacelaunchnow.analytics

import com.datadog.kmp.Datadog
import com.datadog.kmp.DatadogSite
import com.datadog.kmp.core.configuration.Configuration
import com.datadog.kmp.log.Logger
import com.datadog.kmp.log.Logs
import com.datadog.kmp.log.configuration.LogsConfiguration
import com.datadog.kmp.privacy.TrackingConsent
import com.datadog.kmp.rum.Rum
import com.datadog.kmp.rum.configuration.RumConfiguration
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.util.EnvironmentManager
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger

private val log by lazy { SpaceLogger.getLogger("DatadogConfig") }

/**
 * Initialize Datadog RUM for Kotlin Multiplatform
 * Following official docs: https://docs.datadoghq.com/real_user_monitoring/application_monitoring/kotlin_multiplatform/setup/
 *
 * IMPORTANT: This function should only be called when diagnostic logging is enabled
 * or when in debug mode to prevent excessive logging costs.
 *
 * @param context Application context on Android, null on iOS
 * @param sampleRate Optional sample rate override (0-100%). If null, uses debug settings or defaults to 1%
 * @param debugPreferences Optional DebugPreferences to observe sample rate changes dynamically
 */
fun initializeDatadog(
    context: Any? = null,
    sampleRate: Float? = null,
    debugPreferences: me.calebjones.spacelaunchnow.data.storage.DebugPreferences? = null
) {
    // context should be application context on Android and can be null on iOS
    val datadogEnabled = EnvironmentManager.getEnvBoolean("DATADOG_ENABLED", false)
    if (!datadogEnabled) {
        log.i { "Datadog is disabled via environment variable" }
        return
    }

    val appClientToken = EnvironmentManager.getEnv("DATADOG_CLIENT_TOKEN", "")
    val appEnvironment = EnvironmentManager.getEnv("DATADOG_ENVIRONMENT", "development")

    if (appClientToken.isEmpty()) {
        log.w { "Datadog client token not found" }
        return
    }

    val configuration = Configuration.Builder(
        clientToken = appClientToken,
        env = appEnvironment
    )
        .useSite(DatadogSite.US5)
        .trackCrashes(true)
        .build()

    Datadog.initialize(context, configuration, TrackingConsent.GRANTED)

    // Initialize RUM
    val applicationId = EnvironmentManager.getEnv("DATADOG_APPLICATION_ID", "")
    if (applicationId.isNotEmpty()) {
        initializeRum(applicationId)
    }
    val logsConfig = LogsConfiguration.Builder().build()
    Logs.enable(logsConfig)

    // Initialize global logger with sample rate (default 1% for cost savings)
    // Pass debugPreferences to enable dynamic sample rate updates
    val effectiveSampleRate = sampleRate ?: 1f
    DatadogLogger.initialize(effectiveSampleRate, debugPreferences)
}

fun initializeRum(applicationId: String) {
    val rumConfiguration = RumConfiguration.Builder(applicationId)
        .trackLongTasks(1000L)
        .apply {
            // platform specific setup
            rumPlatformSetup(this)
        }
        .build()

    Rum.enable(rumConfiguration)
}

internal expect fun rumPlatformSetup(rumConfigurationBuilder: RumConfiguration.Builder)

// Helper functions for common RUM operations
object DatadogRUM {
    /**
     * Set user information for tracking across Datadog
     * @param id User identifier (e.g., RevenueCat user ID)
     * @param name Optional user name
     * @param email Optional user email
     * @param extraInfo Additional user attributes
     */
    fun setUser(
        id: String,
        name: String? = null,
        email: String? = null,
        extraInfo: Map<String, Any?> = emptyMap()
    ) {
        Datadog.setUserInfo(
            id = id,
            name = name,
            email = email,
            extraInfo = extraInfo
        )
        log.i { "Datadog User Set: id=$id, extraInfo=$extraInfo" }
    }

    /**
     * Clear user information (e.g., on logout)
     */
    fun clearUser() {
        Datadog.clearAllData()
        log.i { "Datadog User Cleared" }
    }
}

/**
 * Global Datadog Logger
 * Use this for logging throughout the application
 */
object DatadogLogger {
    private var logger: Logger? = null
    private var currentSampleRate: Float = 1f
    private var observerJob: kotlinx.coroutines.Job? = null

    /**
     * Initialize Datadog logger with configurable sample rate
     * @param sampleRate Percentage of logs to send to Datadog (0-100). Default: 1% for cost savings
     * @param debugPreferences Optional DebugPreferences to observe sample rate changes dynamically
     */
    fun initialize(
        sampleRate: Float = 1f,
        debugPreferences: me.calebjones.spacelaunchnow.data.storage.DebugPreferences? = null
    ) {
        val coercedSampleRate = sampleRate.coerceIn(0f, 100f)
        buildLogger(coercedSampleRate)

        // Cancel previous observer if any
        observerJob?.cancel()

        // Observe sample rate changes if debugPreferences provided
        debugPreferences?.let { prefs ->
            observerJob =
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
                    prefs.debugSettingsFlow.collect { settings ->
                        val newRate = settings.datadogSampleRate.coerceIn(0f, 100f)
                        if (newRate != currentSampleRate) {
                            log.i { "Sample rate changed from $currentSampleRate% to $newRate%, rebuilding logger..." }
                            buildLogger(newRate)
                        }
                    }
                }
        }
    }

    /**
     * Build or rebuild the logger with the specified sample rate
     */
    private fun buildLogger(sampleRate: Float) {
        currentSampleRate = sampleRate
        logger = Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setPrintLogsToConsole(false)
            .setRemoteSampleRate(sampleRate)
            .setBundleWithRumEnabled(false)
            .setName("SLN")
            .build()
        // Log the initialization with the actual Datadog logger (after it's created)
        logger?.info("DatadogLogger initialized with sample rate: $sampleRate%")
    }

    fun debug(message: String, attributes: Map<String, Any?> = emptyMap()) {
        logger?.debug(message, null, attributes)
    }

    fun info(message: String, attributes: Map<String, Any?> = emptyMap()) {
        logger?.info(message, null, attributes)
    }

    fun warn(message: String, attributes: Map<String, Any?> = emptyMap()) {
        logger?.warn(message, null, attributes)
    }

    fun error(
        message: String,
        throwable: Throwable? = null,
        attributes: Map<String, Any?> = emptyMap()
    ) {
        logger?.error(message, throwable, attributes)
    }

    fun critical(
        message: String,
        throwable: Throwable? = null,
        attributes: Map<String, Any?> = emptyMap()
    ) {
        logger?.critical(message, throwable, attributes)
    }
}
