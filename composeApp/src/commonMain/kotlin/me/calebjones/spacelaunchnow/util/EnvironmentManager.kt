package me.calebjones.spacelaunchnow.util

/**
 * Utility for managing environment variables across platforms
 * Note: For now, returns a hardcoded API key since .env loading is platform-specific
 */
object EnvironmentManager {
    /**
     * Gets an environment variable
     * Falls back to defaultValue if not found
     * TODO: Implement proper .env loading per platform
     */
    fun getEnv(key: String, defaultValue: String = ""): String {
        return when (key) {
            "API_KEY" -> "3ef4f221a3aed270dd81bf47045c86fc768f77a2"
            else -> defaultValue
        }
    }
}
