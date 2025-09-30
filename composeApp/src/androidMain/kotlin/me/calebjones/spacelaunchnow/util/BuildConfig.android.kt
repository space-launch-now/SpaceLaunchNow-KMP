package me.calebjones.spacelaunchnow.util

actual fun initializeBuildConfig() {
    // For Android, we can check if it's a debug build by checking the build variant
    // This is a simple heuristic - in a real app you'd use the generated BuildConfig
    try {
        val buildConfigClass = Class.forName("me.calebjones.spacelaunchnow.BuildConfig")
        val debugField = buildConfigClass.getField("DEBUG")
        BuildConfig.DEBUG = debugField.getBoolean(null)
    } catch (e: Exception) {
        // Fallback: assume debug if we can't determine
        BuildConfig.DEBUG = true
    }
}