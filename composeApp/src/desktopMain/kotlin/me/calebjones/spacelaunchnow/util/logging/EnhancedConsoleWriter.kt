package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Enhanced console writer for Desktop with colors and formatting
 */
class EnhancedConsoleWriter : LogWriter(), ConfigurableLogWriter {
    override var minSeverity: Severity = Severity.Verbose // Accept all, let Kermit filter

    // ANSI color codes
    private val RESET = "\u001B[0m"
    private val RED = "\u001B[31m"
    private val YELLOW = "\u001B[33m"
    private val BLUE = "\u001B[34m"
    private val CYAN = "\u001B[36m"
    private val GRAY = "\u001B[90m"

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        if (severity < minSeverity) return

        val timestamp = LocalTime.now().format(timeFormatter)
        val color = severity.toColor()
        val levelStr = severity.name.first().toString() // V, D, I, W, E

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

        val formattedMessage = "$GRAY$timestamp$RESET $color$levelStr/$tag$RESET: $enhancedMessage"

        println(formattedMessage)
        throwable?.printStackTrace()
    }

    private fun Severity.toColor(): String = when (this) {
        Severity.Verbose -> GRAY
        Severity.Debug -> CYAN
        Severity.Info -> BLUE
        Severity.Warn -> YELLOW
        Severity.Error, Severity.Assert -> RED
    }
}

