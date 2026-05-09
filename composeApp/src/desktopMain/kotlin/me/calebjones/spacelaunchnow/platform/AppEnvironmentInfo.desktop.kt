package me.calebjones.spacelaunchnow.platform

import java.util.Locale

actual class AppEnvironmentInfo actual constructor() {
    actual val appVersionName: String = System.getProperty("app.version") ?: "desktop-dev"
    actual val appBuildNumber: String = System.getProperty("app.build") ?: "0"
    actual val osVersion: String =
        "${System.getProperty("os.name")} ${System.getProperty("os.version")}"
    actual val deviceModel: String = System.getProperty("os.arch") ?: "unknown"
    actual val locale: String = Locale.getDefault().toLanguageTag()
    actual val country: String = Locale.getDefault().country
    actual val formFactor: String = "desktop"
}
