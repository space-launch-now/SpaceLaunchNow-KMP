package me.calebjones.spacelaunchnow.platform

/**
 * Values the Android application module owns and the shared module cannot generate itself: this
 * module is an Android library, so it has no BuildConfig. `MainApplication.onCreate` registers
 * them before any other code reads them. Until then (host unit tests have no Application) they
 * keep neutral defaults.
 */
data class AndroidAppInfo(
    val versionName: String,
    val versionCode: Int,
    val isDebug: Boolean,
)

/**
 * Variant data for the Android target. Secrets and ad unit IDs come from `.env` through the
 * generated [AndroidEnvConfig]; the version and the debug flag depend on the build variant, so
 * the application module supplies them.
 */
object AndroidBuildConfig {
    private var appInfo = AndroidAppInfo(versionName = "unknown", versionCode = 0, isDebug = false)

    fun initialize(info: AndroidAppInfo) {
        appInfo = info
    }

    val VERSION_NAME: String get() = appInfo.versionName
    val VERSION_CODE: Int get() = appInfo.versionCode
    val IS_DEBUG: Boolean get() = appInfo.isDebug
}
