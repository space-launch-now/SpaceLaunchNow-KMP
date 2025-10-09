package me.calebjones.spacelaunchnow.util

actual fun initializeBuildConfig() {
    // For desktop, use hardcoded values that match version.properties
    // TODO: In the future, integrate with desktop build system to get these automatically
    BuildConfig.VERSION_NAME = "4.0.0-b16"
    BuildConfig.VERSION_CODE = 4000016

    // For desktop, we can check system properties or default to debug mode
    BuildConfig.IS_DEBUG = System.getProperty("app.debug", "true").toBoolean()
}