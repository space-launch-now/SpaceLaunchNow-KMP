package me.calebjones.spacelaunchnow.util

actual object AppSecrets {
    actual val apiKey: String = DesktopSecret.API_KEY
    actual val revenueCatAndroidKey: String = DesktopSecret.REVENUECAT_ANDROID_KEY
    actual val revenueCatIosKey: String = DesktopSecret.REVENUECAT_IOS_KEY
    
    // AdMob ad unit IDs (Desktop doesn't use ads, so empty strings)
    actual val androidBannerAdUnitId: String = ""
    actual val iosBannerAdUnitId: String = ""
    actual val androidInterstitialAdUnitId: String = ""
    actual val iosInterstitialAdUnitId: String = ""
    actual val androidRewardedAdUnitId: String = ""
    actual val iosRewardedAdUnitId: String = ""
}