package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.BuildConfig

actual object AppSecrets {
    actual val apiKey: String = BuildConfig.API_KEY
    actual val revenueCatAndroidKey: String = BuildConfig.REVENUECAT_ANDROID_KEY
    actual val revenueCatIosKey: String = BuildConfig.REVENUECAT_IOS_KEY

    // AdMob ad unit IDs
    actual val androidBannerAdUnitId: String = BuildConfig.ANDROID_BANNER_AD_UNIT_ID
    actual val iosBannerAdUnitId: String = BuildConfig.IOS_BANNER_AD_UNIT_ID
    actual val androidInterstitialAdUnitId: String = BuildConfig.ANDROID_INTERSTITIAL_AD_UNIT_ID
    actual val iosInterstitialAdUnitId: String = BuildConfig.IOS_INTERSTITIAL_AD_UNIT_ID
    actual val androidRewardedAdUnitId: String = BuildConfig.ANDROID_REWARDED_AD_UNIT_ID
    actual val iosRewardedAdUnitId: String = BuildConfig.IOS_REWARDED_AD_UNIT_ID

    actual val datadogEnabled: Boolean = BuildConfig.DATADOG_ENABLED
    actual val dataDogClientToken: String = BuildConfig.DATADOG_CLIENT_TOKEN
    actual val dataDogApplicationId: String = BuildConfig.DATADOG_APPLICATION_ID
    actual val dataDogEnv: String = BuildConfig.DATADOG_ENVIRONMENT
}