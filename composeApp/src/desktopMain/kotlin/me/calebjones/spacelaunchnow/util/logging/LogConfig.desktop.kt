package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter

actual fun platformLogConfig(): LogConfig {
    val minSeverity = Severity.Debug // Always debug on desktop
    
    // Desktop uses enhanced console output with colors
    return LogConfig(
        minSeverity = minSeverity,
        writers = listOf(
            EnhancedConsoleWriter(), // Starts with Verbose, controlled by preferences
            platformLogWriter()
        )
    )
}

