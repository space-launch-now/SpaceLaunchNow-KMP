package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Interface for log writers that support dynamic severity configuration
 */
interface ConfigurableLogWriter {
    var minSeverity: Severity
}

/**
 * Centralized logging configuration for Space Launch Now
 *
 * Usage:
 * ```kotlin
 * class MyViewModel : ViewModel() {
 *     private val log = logger() // Auto-tagged with "MyViewModel"
 *
 *     fun doSomething() {
 *         log.d { "Doing something" }
 *         log.e(exception) { "Error occurred" }
 *     }
 * }
 * ```
 */

object SpaceLogger {
    private const val BASE_TAG = "SLN"

    private var baseLogger: Logger? = null
    private var consoleWriters: List<LogWriter> = emptyList()
    private var dataDogWriter: ConfigurableLogWriter? = null

    /** Reusable no-op logger returned before [initialize] is called. */
    private val NO_OP_LOGGER = Logger(
        StaticConfig(minSeverity = Severity.Assert, logWriterList = emptyList()),
        BASE_TAG
    )

    /**
     * Initialize logging with platform-specific configuration
     * Should be called in Application.onCreate() / main()
     */
    fun initialize(
        logConfig: LogConfig = platformLogConfig(),
        loggingPreferences: LoggingPreferences? = null
    ) {
        // Separate writers by type
        consoleWriters = logConfig.writers.filter { it !is DataDogLogWriter }
        dataDogWriter = logConfig.writers.filterIsInstance<DataDogLogWriter>().firstOrNull()

        // CRITICAL FIX: Create a custom Logger instance with StaticConfig
        // This prevents Kermit's default platform writers from being used
        // Initialize with Severity.Verbose to allow individual writers to control filtering
        val staticConfig = StaticConfig(
            minSeverity = Severity.Verbose,
            logWriterList = logConfig.writers
        )
        baseLogger = Logger(staticConfig, BASE_TAG)

        // Observe preferences if provided
        loggingPreferences?.let { prefs ->
            observePreferences(prefs)
        }
    }

    /**
     * Observe logging preferences and update severity dynamically
     */
    private fun observePreferences(prefs: LoggingPreferences) {
        CoroutineScope(Dispatchers.Default).launch {
            combine(
                prefs.getConsoleSeverity(),
                prefs.getDataDogSeverity(),
                prefs.isDataDogEnabled()
            ) { consoleSev, dataDogSev, dataDogEnabled ->
                Triple(consoleSev, dataDogSev, dataDogEnabled)
            }.collect { (consoleSev, dataDogSev, dataDogEnabled) ->
                setConsoleSeverity(consoleSev)
                // If DataDog is disabled, set severity to Assert (no logs)
                setDataDogSeverity(if (dataDogEnabled) dataDogSev else Severity.Assert)
            }
        }
    }

    /**
     * Get a logger with automatic tagging
     * Tag format: "SLN-{tag}"
     *
     * Uses our custom Logger instance (not the global singleton)
     */
    fun getLogger(tag: String): Logger {
        val fullTag = if (tag.isEmpty()) BASE_TAG else "$BASE_TAG-$tag"
        val logger = baseLogger
        if (logger != null) {
            return logger.withTag(fullTag)
        }
        // No-op fallback: avoids crashes when SpaceLogger hasn't been initialized yet
        // (e.g. iOS cold-start from notification tap). Uses Severity.Assert so message
        // lambdas are never evaluated and no writers are invoked.
        return NO_OP_LOGGER.withTag(fullTag)
    }

    /**
     * Set minimum severity for console/logcat output
     */
    fun setConsoleSeverity(severity: Severity) {
        consoleWriters.filterIsInstance<ConfigurableLogWriter>().forEach { writer ->
            writer.minSeverity = severity
        }
    }

    /**
     * Set minimum severity for DataDog remote logging
     */
    fun setDataDogSeverity(severity: Severity) {
        dataDogWriter?.minSeverity = severity
    }

    /**
     * Get current console severity
     */
    fun getConsoleSeverity(): Severity {
        return consoleWriters.filterIsInstance<ConfigurableLogWriter>()
            .firstOrNull()?.minSeverity ?: Severity.Warn
    }

    /**
     * Get current DataDog severity
     */
    fun getDataDogSeverity(): Severity {
        return dataDogWriter?.minSeverity ?: Severity.Warn
    }

    /**
     * Check if debug logging is enabled
     */
    fun isDebugEnabled(): Boolean {
        return baseLogger?.config?.minSeverity?.let { it <= Severity.Debug } ?: false
    }
}

/**
 * Extension function for automatic logger creation
 * Automatically tags logger with class name
 *
 * Usage: `private val log = logger()`
 */
inline fun <reified T> T.logger(): Logger {
    val tag = T::class.simpleName ?: "Unknown"
    return SpaceLogger.getLogger(tag)
}

/**
 * Platform-agnostic log configuration
 */
data class LogConfig(
    val minSeverity: Severity,
    val writers: List<LogWriter>
)

/**
 * Platform-specific configuration (expect/actual pattern)
 */
expect fun platformLogConfig(): LogConfig

