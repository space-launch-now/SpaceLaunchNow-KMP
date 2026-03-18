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
import com.datadog.kmp.rum.configuration.trackUserInteractions
import com.datadog.kmp.rum.configuration.useViewTrackingStrategy
import com.datadog.kmp.rum.tracking.ActivityViewTrackingStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences
import me.calebjones.spacelaunchnow.util.EnvironmentManager
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger

private val log by lazy { SpaceLogger.getLogger("DatadogConfig") }

actual fun initializeDatadog(
    context: Any?,
    sampleRate: Float?,
    debugPreferences: DebugPreferences?
) {
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

    val applicationId = EnvironmentManager.getEnv("DATADOG_APPLICATION_ID", "")
    if (applicationId.isNotEmpty()) {
        val rumConfiguration = RumConfiguration.Builder(applicationId)
            .trackLongTasks(1000L)
            .apply {
                useViewTrackingStrategy(ActivityViewTrackingStrategy(true))
                trackUserInteractions()
            }
            .build()
        Rum.enable(rumConfiguration)
    }

    val logsConfig = LogsConfiguration.Builder().build()
    Logs.enable(logsConfig)

    val effectiveSampleRate = sampleRate ?: 1f
    DatadogLogger.initialize(effectiveSampleRate, debugPreferences)
}

actual object DatadogRUM {
    actual fun setUser(
        id: String,
        name: String?,
        email: String?,
        extraInfo: Map<String, Any?>
    ) {
        Datadog.setUserInfo(id = id, name = name, email = email, extraInfo = extraInfo)
        log.i { "Datadog User Set: id=$id, extraInfo=$extraInfo" }
    }

    actual fun clearUser() {
        Datadog.clearAllData()
        log.i { "Datadog User Cleared" }
    }
}

actual object DatadogLogger {
    private var logger: Logger? = null
    private var currentSampleRate: Float = 1f
    private var observerJob: Job? = null

    actual fun initialize(sampleRate: Float, debugPreferences: DebugPreferences?) {
        val coercedSampleRate = sampleRate.coerceIn(0f, 100f)
        buildLogger(coercedSampleRate)

        observerJob?.cancel()

        debugPreferences?.let { prefs ->
            observerJob = CoroutineScope(Dispatchers.Default).launch {
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

    private fun buildLogger(sampleRate: Float) {
        currentSampleRate = sampleRate
        logger = Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setPrintLogsToConsole(false)
            .setRemoteSampleRate(sampleRate)
            .setBundleWithRumEnabled(false)
            .setName("SLN")
            .build()
        logger?.info("DatadogLogger initialized with sample rate: $sampleRate%")
    }

    actual fun debug(message: String, attributes: Map<String, Any?>) {
        logger?.debug(message, null, attributes)
    }

    actual fun info(message: String, attributes: Map<String, Any?>) {
        logger?.info(message, null, attributes)
    }

    actual fun warn(message: String, attributes: Map<String, Any?>) {
        logger?.warn(message, null, attributes)
    }

    actual fun error(message: String, throwable: Throwable?, attributes: Map<String, Any?>) {
        logger?.error(message, throwable, attributes)
    }

    actual fun critical(message: String, throwable: Throwable?, attributes: Map<String, Any?>) {
        logger?.critical(message, throwable, attributes)
    }
}
