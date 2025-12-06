package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.util.EnvironmentManager

actual fun platformLogConfig(): LogConfig {
    val minSeverity = if (BuildConfig.IS_DEBUG) {
        Severity.Debug
    } else {
        Severity.Info
    }
    
    val writers = buildList<LogWriter> {
        // Enhanced logcat with file/line tracking
        // Starts with Verbose, will be controlled by LoggingPreferences
        add(EnhancedLogcatWriter())

        // DataDog integration (if enabled) - always starts with WARN for production safety
        if (shouldEnableDataDog()) {
            add(DataDogLogWriter())
        }
    }

    return LogConfig(minSeverity, writers)
}

private fun shouldEnableDataDog(): Boolean {
    // Reuse existing DataDog initialization logic
    return EnvironmentManager.getEnvBoolean("DATADOG_ENABLED", false)
}

