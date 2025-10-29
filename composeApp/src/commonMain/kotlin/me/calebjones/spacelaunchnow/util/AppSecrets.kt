package me.calebjones.spacelaunchnow.util

expect object AppSecrets {
    val apiKey: String
    val revenueCatAndroidKey: String
    val revenueCatIosKey: String
    
    // AdMob ad unit IDs
    val androidBannerAdUnitId: String
    val iosBannerAdUnitId: String
    val androidInterstitialAdUnitId: String
    val iosInterstitialAdUnitId: String
    val androidRewardedAdUnitId: String
    val iosRewardedAdUnitId: String
}