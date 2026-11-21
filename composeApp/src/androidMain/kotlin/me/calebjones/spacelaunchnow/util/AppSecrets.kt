package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.platform.AndroidEnvConfig

actual object AppSecrets {
    actual val apiKey: String = AndroidEnvConfig.API_KEY
    actual val revenueCatAndroidKey: String = AndroidEnvConfig.REVENUECAT_ANDROID_KEY
    actual val revenueCatIosKey: String = AndroidEnvConfig.REVENUECAT_IOS_KEY

    // Debug Menu TOTP Secret
    actual val totpSecret: String = AndroidEnvConfig.TOTP_SECRET

    // AdMob ad unit IDs
    actual val androidBannerAdUnitId: String = AndroidEnvConfig.ANDROID_BANNER_AD_UNIT_ID
    actual val iosBannerAdUnitId: String = AndroidEnvConfig.IOS_BANNER_AD_UNIT_ID
    actual val androidInterstitialAdUnitId: String = AndroidEnvConfig.ANDROID_INTERSTITIAL_AD_UNIT_ID
    actual val iosInterstitialAdUnitId: String = AndroidEnvConfig.IOS_INTERSTITIAL_AD_UNIT_ID
    actual val androidRewardedAdUnitId: String = AndroidEnvConfig.ANDROID_REWARDED_AD_UNIT_ID
    actual val iosRewardedAdUnitId: String = AndroidEnvConfig.IOS_REWARDED_AD_UNIT_ID

    actual val datadogEnabled: Boolean = AndroidEnvConfig.DATADOG_ENABLED
    actual val dataDogClientToken: String = AndroidEnvConfig.DATADOG_CLIENT_TOKEN
    actual val dataDogApplicationId: String = AndroidEnvConfig.DATADOG_APPLICATION_ID
    actual val dataDogEnv: String = AndroidEnvConfig.DATADOG_ENVIRONMENT

    // Google Maps API Key
    actual val mapsApiKey: String = AndroidEnvConfig.MAPS_API_KEY
}