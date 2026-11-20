package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.platform.AndroidBuildConfig

actual object AppSecrets {
    actual val apiKey: String = AndroidBuildConfig.API_KEY
    actual val revenueCatAndroidKey: String = AndroidBuildConfig.REVENUECAT_ANDROID_KEY
    actual val revenueCatIosKey: String = AndroidBuildConfig.REVENUECAT_IOS_KEY

    // Debug Menu TOTP Secret
    actual val totpSecret: String = AndroidBuildConfig.TOTP_SECRET

    // AdMob ad unit IDs
    actual val androidBannerAdUnitId: String = AndroidBuildConfig.ANDROID_BANNER_AD_UNIT_ID
    actual val iosBannerAdUnitId: String = AndroidBuildConfig.IOS_BANNER_AD_UNIT_ID
    actual val androidInterstitialAdUnitId: String = AndroidBuildConfig.ANDROID_INTERSTITIAL_AD_UNIT_ID
    actual val iosInterstitialAdUnitId: String = AndroidBuildConfig.IOS_INTERSTITIAL_AD_UNIT_ID
    actual val androidRewardedAdUnitId: String = AndroidBuildConfig.ANDROID_REWARDED_AD_UNIT_ID
    actual val iosRewardedAdUnitId: String = AndroidBuildConfig.IOS_REWARDED_AD_UNIT_ID

    actual val datadogEnabled: Boolean = AndroidBuildConfig.DATADOG_ENABLED
    actual val dataDogClientToken: String = AndroidBuildConfig.DATADOG_CLIENT_TOKEN
    actual val dataDogApplicationId: String = AndroidBuildConfig.DATADOG_APPLICATION_ID
    actual val dataDogEnv: String = AndroidBuildConfig.DATADOG_ENVIRONMENT

    // Google Maps API Key
    actual val mapsApiKey: String = AndroidBuildConfig.MAPS_API_KEY
}