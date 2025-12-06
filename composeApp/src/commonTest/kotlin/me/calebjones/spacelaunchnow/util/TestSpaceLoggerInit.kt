package me.calebjones.spacelaunchnow.util

import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import me.calebjones.spacelaunchnow.util.logging.LogConfig
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger

/**
 * Test utility to ensure SpaceLogger is initialized before tests run.
 * 
 * Usage in tests:
 * ```kotlin
 * @BeforeTest
 * fun setup() {
 *     TestSpaceLoggerInit.ensureInitialized()
 * }
 * ```
 */
object TestSpaceLoggerInit {
    private var initialized = false

    /**
     * Ensures SpaceLogger is initialized with a simple test configuration.
     * Safe to call multiple times - only initializes once.
     */
    fun ensureInitialized() {
        if (!initialized) {
            try {
                SpaceLogger.initialize(
                    LogConfig(
                        minSeverity = Severity.Verbose,
                        writers = listOf(platformLogWriter())
                    )
                )
                initialized = true
            } catch (_: Exception) {
                // Already initialized elsewhere, mark as initialized
                initialized = true
            }
        }
    }
}
