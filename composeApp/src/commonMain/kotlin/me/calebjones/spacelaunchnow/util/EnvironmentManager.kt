package me.calebjones.spacelaunchnow.util

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv

/**
 * Utility for managing environment variables across platforms
 */
object EnvironmentManager {
    private val dotenv = dotenv {
        directory = "./"
        ignoreIfMissing = true
    }

    /**
     * Gets an environment variable from .env file or system environment
     * Falls back to defaultValue if not found
     */
    fun getEnv(key: String, defaultValue: String = ""): String {
        return try {
            val value = dotenv[key] ?: System.getenv(key)
            value ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }
    }
}
