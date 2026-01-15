package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import me.calebjones.spacelaunchnow.analytics.DatadogLogger

/**
 * Bridge between Kermit logging and existing DataDog infrastructure
 * Forwards all logs to DatadogLogger for remote tracking
 *
 * CRITICAL: Automatically includes RevenueCat User ID on all logs
 */
class DataDogLogWriter : LogWriter(), ConfigurableLogWriter {
    // Default to ERROR only to prevent excessive Datadog costs
    // Higher log levels only enabled when diagnostic logging is on
    override var minSeverity: Severity = Severity.Error

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        // Respect per-writer severity (default WARN+ for production)
        if (severity < minSeverity) return

        // Automatically append exception message if present
        val enhancedMessage = if (throwable != null) {
            val exceptionMsg = throwable.message
            if (exceptionMsg != null && exceptionMsg.isNotBlank()) {
                "$message: $exceptionMsg"
            } else {
                message
            }
        } else {
            message
        }

        // Build attributes with user context
        val attributes = buildMap<String, Any?> {
            put("tag", tag)

            // CRITICAL: Always include RevenueCat User ID
            putAll(UserContext.getLogAttributes())
        }

        when (severity) {
            Severity.Verbose,
            Severity.Debug -> DatadogLogger.debug(enhancedMessage, attributes)

            Severity.Info -> DatadogLogger.info(enhancedMessage, attributes)

            Severity.Warn -> DatadogLogger.warn(enhancedMessage, attributes)

            Severity.Error,
            Severity.Assert -> DatadogLogger.error(enhancedMessage, throwable, attributes)
        }
    }
}

