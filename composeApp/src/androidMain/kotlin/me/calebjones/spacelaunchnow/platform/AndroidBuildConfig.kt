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
 * Build-time configuration for the Android target.
 *
 * Secrets and ad unit IDs come from `.env` through the generated [AndroidEnvConfig]; the version
 * and the debug flag depend on the build variant, so the application module supplies them.
 */
object AndroidBuildConfig {
    private var appInfo = AndroidAppInfo(versionName = "unknown", versionCode = 0, isDebug = false)

    fun initialize(info: AndroidAppInfo) {
        appInfo = info
    }

    val API_KEY: String get() = AndroidEnvConfig.API_KEY
    val REVENUECAT_ANDROID_KEY: String get() = AndroidEnvConfig.REVENUECAT_ANDROID_KEY
    val REVENUECAT_IOS_KEY: String get() = AndroidEnvConfig.REVENUECAT_IOS_KEY
    val TOTP_SECRET: String get() = AndroidEnvConfig.TOTP_SECRET
    val ANDROID_BANNER_AD_UNIT_ID: String get() = AndroidEnvConfig.ANDROID_BANNER_AD_UNIT_ID
    val IOS_BANNER_AD_UNIT_ID: String get() = AndroidEnvConfig.IOS_BANNER_AD_UNIT_ID
    val ANDROID_INTERSTITIAL_AD_UNIT_ID: String get() = AndroidEnvConfig.ANDROID_INTERSTITIAL_AD_UNIT_ID
    val IOS_INTERSTITIAL_AD_UNIT_ID: String get() = AndroidEnvConfig.IOS_INTERSTITIAL_AD_UNIT_ID
    val ANDROID_REWARDED_AD_UNIT_ID: String get() = AndroidEnvConfig.ANDROID_REWARDED_AD_UNIT_ID
    val IOS_REWARDED_AD_UNIT_ID: String get() = AndroidEnvConfig.IOS_REWARDED_AD_UNIT_ID
    val DATADOG_CLIENT_TOKEN: String get() = AndroidEnvConfig.DATADOG_CLIENT_TOKEN
    val DATADOG_APPLICATION_ID: String get() = AndroidEnvConfig.DATADOG_APPLICATION_ID
    val DATADOG_ENABLED: Boolean get() = AndroidEnvConfig.DATADOG_ENABLED
    val DATADOG_ENVIRONMENT: String get() = AndroidEnvConfig.DATADOG_ENVIRONMENT
    val MAPS_API_KEY: String get() = AndroidEnvConfig.MAPS_API_KEY
    val VERSION_NAME: String get() = appInfo.versionName
    val VERSION_CODE: Int get() = appInfo.versionCode
    val IS_DEBUG: Boolean get() = appInfo.isDebug
}
