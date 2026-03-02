package me.calebjones.spacelaunchnow.util

import platform.Foundation.NSBundle
import platform.Foundation.NSDictionary
import platform.Foundation.dictionaryWithContentsOfFile

actual object AppSecrets {
    actual val apiKey: String
        get() = getStringResource("Secrets", "plist", "apiKey") ?: ""

    actual val revenueCatAndroidKey: String
        get() = getStringResource("Secrets", "plist", "revenueCatAndroidKey") ?: ""

    actual val revenueCatIosKey: String
        get() = getStringResource("Secrets", "plist", "revenueCatIosKey") ?: ""

    // Debug Menu TOTP Secret
    actual val totpSecret: String
        get() = getStringResource("Secrets", "plist", "totpSecret") ?: ""

    // AdMob ad unit IDs
    actual val androidBannerAdUnitId: String
        get() = getStringResource("Secrets", "plist", "androidBannerAdUnitId") ?: ""

    actual val iosBannerAdUnitId: String
        get() = getStringResource("Secrets", "plist", "iosBannerAdUnitId") ?: ""

    actual val androidInterstitialAdUnitId: String
        get() = getStringResource("Secrets", "plist", "androidInterstitialAdUnitId") ?: ""

    actual val iosInterstitialAdUnitId: String
        get() = getStringResource("Secrets", "plist", "iosInterstitialAdUnitId") ?: ""

    actual val androidRewardedAdUnitId: String
        get() = getStringResource("Secrets", "plist", "androidRewardedAdUnitId") ?: ""

    actual val iosRewardedAdUnitId: String
        get() = getStringResource("Secrets", "plist", "iosRewardedAdUnitId") ?: ""

    actual val datadogEnabled: Boolean
        get() {
            val key = getStringResource("Secrets", "plist", "datadogEnabled") ?: "false"
            return key.lowercase() == "true"
        }

    actual val dataDogClientToken: String
        get() = getStringResource("Secrets", "plist", "dataDogClientToken") ?: ""

    actual val dataDogApplicationId: String
        get() = getStringResource("Secrets", "plist", "dataDogApplicationId") ?: ""

    actual val dataDogEnv: String
        get() = getStringResource("Secrets", "plist", "dataDogEnv") ?: "production"

    // Google Maps API Key
    actual val mapsApiKey: String
        get() = getStringResource("Secrets", "plist", "mapsApiKey") ?: ""
}

internal fun getStringResource(filename: String, fileType: String, valueKey: String): String? {
    val path = NSBundle.mainBundle.pathForResource(filename, fileType) ?: return null
    val dict = NSDictionary.dictionaryWithContentsOfFile(path) ?: return null
    return dict.get(valueKey) as? String
}

internal fun getIntResource(filename: String, fileType: String, valueKey: String): Int? {
    val path = NSBundle.mainBundle.pathForResource(filename, fileType) ?: return null
    val dict = NSDictionary.dictionaryWithContentsOfFile(path) ?: return null
    return (dict.get(valueKey) as? Number)?.toInt()
}
