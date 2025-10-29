package me.calebjones.spacelaunchnow.util

actual fun initializeBuildConfig() {
    // For Android, get values from the generated BuildConfig class at me.calebjones.spacelaunchnow.BuildConfig
    // (Note: This is different from our custom BuildConfig class at me.calebjones.spacelaunchnow.util.BuildConfig)
    try {
        // Access the generated BuildConfig class in the root package
        val generatedBuildConfigClass = Class.forName("me.calebjones.spacelaunchnow.BuildConfig")

        // Get version information from generated fields
        val versionNameField = generatedBuildConfigClass.getField("VERSION_NAME")
        val versionCodeField = generatedBuildConfigClass.getField("VERSION_CODE")
        val debugField = generatedBuildConfigClass.getField("IS_DEBUG")

        // Set our custom BuildConfig properties from the generated values
        BuildConfig.VERSION_NAME = versionNameField.get(null) as String
        BuildConfig.VERSION_CODE = versionCodeField.getInt(null)
        BuildConfig.IS_DEBUG = debugField.getBoolean(null)

    } catch (e: Exception) {
        // Fallback values if we can't access the generated BuildConfig
        BuildConfig.VERSION_NAME = "unknown"
        BuildConfig.VERSION_CODE = 0
        BuildConfig.IS_DEBUG = false
    }
}