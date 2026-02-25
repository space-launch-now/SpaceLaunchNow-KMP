package me.calebjones.spacelaunchnow.util

actual fun initializeBuildConfig() {
    // Read version from Secrets.plist (generated from version.properties by generate-ios-secrets.sh)
    BuildConfig.VERSION_NAME = getStringResource("Secrets", "plist", "versionName") ?: "unknown"
    BuildConfig.VERSION_CODE = getIntResource("Secrets", "plist", "versionCode") ?: 0
    
    // Read debug flag from Secrets.plist (generated from .env DEBUG variable)
    // Production-safe: defaults to false if not explicitly set to "true"
    // Unlike Android which uses Gradle build types, iOS must rely on explicit configuration
    // CI/CD: Set DEBUG=true for debug builds, DEBUG=false (or omit) for production
    val debugString = getStringResource("Secrets", "plist", "debug") ?: "false"
    BuildConfig.IS_DEBUG = debugString.lowercase() == "true"
}