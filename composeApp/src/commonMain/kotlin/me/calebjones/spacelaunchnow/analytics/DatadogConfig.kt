package me.calebjones.spacelaunchnow.analytics

import com.datadog.kmp.Datadog
import com.datadog.kmp.DatadogSite
import com.datadog.kmp.core.configuration.Configuration
import com.datadog.kmp.log.Logger
import com.datadog.kmp.log.Logs
import com.datadog.kmp.log.configuration.LogsConfiguration
import com.datadog.kmp.privacy.TrackingConsent
import com.datadog.kmp.rum.Rum
import com.datadog.kmp.rum.RumActionType
import com.datadog.kmp.rum.RumErrorSource
import com.datadog.kmp.rum.configuration.RumConfiguration
import me.calebjones.spacelaunchnow.util.EnvironmentManager
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger

private val log = SpaceLogger.getLogger("DatadogConfig")

/**
 * Initialize Datadog RUM for Kotlin Multiplatform
 * Following official docs: https://docs.datadoghq.com/real_user_monitoring/application_monitoring/kotlin_multiplatform/setup/
 */
fun initializeDatadog(context: Any? = null) {
    // context should be application context on Android and can be null on iOS
    val datadogEnabled = EnvironmentManager.getEnvBoolean("DATADOG_ENABLED", false)
    if (!datadogEnabled) {
        log.i { "Datadog is disabled" }
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

    // Initialize global logger
    DatadogLogger.initialize()

    DatadogLogger.info("Datadog initialized successfully", mapOf(
        "environment" to appEnvironment,
        "rum_enabled" to applicationId.isNotEmpty()
    ))
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

    fun initialize() {
        logger = Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setPrintLogsToConsole(false)
            .setRemoteSampleRate(100f)
            .setBundleWithRumEnabled(true)
            .setName("SLN")
            .build()
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

    fun error(message: String, throwable: Throwable? = null, attributes: Map<String, Any?> = emptyMap()) {
        logger?.error(message, throwable, attributes)
    }

    fun critical(message: String, throwable: Throwable? = null, attributes: Map<String, Any?> = emptyMap()) {
        logger?.critical(message, throwable, attributes)
    }
}
