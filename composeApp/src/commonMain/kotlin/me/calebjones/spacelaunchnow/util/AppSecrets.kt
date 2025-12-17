package me.calebjones.spacelaunchnow.util

expect object AppSecrets {
    val apiKey: String
    val revenueCatAndroidKey: String
    val revenueCatIosKey: String

    // Debug Menu TOTP Secret
    val totpSecret: String

    // AdMob ad unit IDs
    val androidBannerAdUnitId: String
    val iosBannerAdUnitId: String
    val androidInterstitialAdUnitId: String
    val iosInterstitialAdUnitId: String
    val androidRewardedAdUnitId: String
    val iosRewardedAdUnitId: String

    val datadogEnabled: Boolean
    val dataDogClientToken: String
    val dataDogApplicationId: String
    val dataDogEnv: String

    // Google Maps API Key
    val mapsApiKey: String
}