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

/**
 * Initialize Datadog RUM for Kotlin Multiplatform
 * Following official docs: https://docs.datadoghq.com/real_user_monitoring/application_monitoring/kotlin_multiplatform/setup/
 */
fun initializeDatadog(context: Any? = null) {
    // context should be application context on Android and can be null on iOS
    val datadogEnabled = EnvironmentManager.getEnv("DATADOG_ENABLED", "false")
    print("Datadog Enabled: $datadogEnabled")
    if (!datadogEnabled.toBoolean()) {
        println("Datadog is disabled")
        return
    }

    val appClientToken = EnvironmentManager.getEnv("DATADOG_CLIENT_TOKEN", "")
    val appEnvironment = EnvironmentManager.getEnv("DATADOG_ENVIRONMENT", "development")

    if (appClientToken.isEmpty()) {
        println("Datadog client token not found")
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

    val logger = Logger.Builder()
        .setNetworkInfoEnabled(true)
        .setPrintLogsToConsole(true)
        .setRemoteSampleRate(100f)
        .setBundleWithRumEnabled(true)
        .setName("SLN")
        .build()

    logger.debug("A debug message.")
    logger.info("Some relevant information?")
    logger.warn("An important warning...")
    logger.error("An error was met!")
    logger.critical("What a Terrible Failure!")
    println("YTESSSSSS QUEEEEEN MainApplication?")


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
     * Track a custom action (button click, navigation, etc.)
     * Note: Actions are tracked automatically by platform-specific configurations.
     * This is a placeholder for when manual action tracking is added to the SDK.
     */
    fun trackAction(
        name: String,
        type: RumActionType = RumActionType.CUSTOM,
        attributes: Map<String, Any?> = emptyMap()
    ) {
        // Manual RUM event tracking not yet available in KMP SDK
        // Events are tracked automatically via platform configurations
        println("Datadog Action: $name (type=$type, attributes=$attributes)")
    }

    /**
     * Track an error with optional throwable and attributes
     * Note: Errors are tracked automatically via crash tracking.
     * This is a placeholder for when manual error tracking is added to the SDK.
     */
    fun trackError(
        message: String,
        source: RumErrorSource = RumErrorSource.SOURCE,
        throwable: Throwable? = null,
        attributes: Map<String, Any?> = emptyMap()
    ) {
        // Manual RUM event tracking not yet available in KMP SDK
        // Crashes are tracked automatically
        println("Datadog Error: $message (source=$source, throwable=$throwable, attributes=$attributes)")
        throwable?.printStackTrace()
    }

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
        println("Datadog User Set: id=$id, extraInfo=$extraInfo")
    }

    /**
     * Clear user information (e.g., on logout)
     */
    fun clearUser() {
        Datadog.clearAllData()
        println("Datadog User Cleared")
    }
}
