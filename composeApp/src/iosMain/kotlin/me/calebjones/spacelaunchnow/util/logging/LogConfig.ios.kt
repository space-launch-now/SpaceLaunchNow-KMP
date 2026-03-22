package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.util.EnvironmentManager

actual fun platformLogConfig(): LogConfig {
    val minSeverity = if (BuildConfig.IS_DEBUG) {
        Severity.Debug
    } else {
        Severity.Warn
    }
    
    val writers = buildList<LogWriter> {
        // iOS OSLog for native logging
        add(platformLogWriter())

        // Firebase Crashlytics integration — logs warnings+ and records non-fatal exceptions
        add(FirebaseCrashlyticsLogWriter())

        // DataDog integration (if enabled) - always starts with WARN for production safety
        if (shouldEnableDataDog()) {
            add(DataDogLogWriter())
        }
    }

    return LogConfig(minSeverity, writers)
}

private fun shouldEnableDataDog(): Boolean {
    return EnvironmentManager.getEnvBoolean("DATADOG_ENABLED", false)
}

