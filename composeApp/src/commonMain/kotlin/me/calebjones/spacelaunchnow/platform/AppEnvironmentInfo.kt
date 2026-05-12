package me.calebjones.spacelaunchnow.platform

/**
 * Read-only snapshot of device + app environment used for analytics and
 * RevenueCat subscriber attributes. Values are stable for the process lifetime.
 */
expect class AppEnvironmentInfo() {
    val appVersionName: String
    val appBuildNumber: String
    val osVersion: String
    val deviceModel: String
    val locale: String
    val country: String
    /** "phone" / "tablet" / "desktop" */
    val formFactor: String
}
