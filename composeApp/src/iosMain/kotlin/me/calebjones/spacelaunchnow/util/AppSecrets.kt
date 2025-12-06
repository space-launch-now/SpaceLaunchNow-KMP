package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.logger
import platform.Foundation.NSBundle
import platform.Foundation.NSDictionary
import platform.Foundation.dictionaryWithContentsOfFile

actual object AppSecrets {
    private val log = logger()
    
    actual val apiKey: String
        get() {
            val key = getStringResource("Secrets", "plist", "apiKey") ?: ""
            log.d { "🔑 AppSecrets.apiKey: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}" }
            return key
        }

    actual val revenueCatAndroidKey: String
        get() {
            val key = getStringResource("Secrets", "plist", "revenueCatAndroidKey") ?: ""
            log.d { "🔑 AppSecrets.revenueCatAndroidKey: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}" }
            return key
        }

    actual val revenueCatIosKey: String
        get() {
            log.d { "🔍 AppSecrets: Attempting to load revenueCatIosKey..." }
            val key = getStringResource("Secrets", "plist", "revenueCatIosKey") ?: ""
            log.d { "🔑 AppSecrets.revenueCatIosKey result: ${if (key.isNotEmpty()) "✅ loaded (${key.take(15)}...)" else "❌ EMPTY"}" }
            return key
        }

    // Debug Menu TOTP Secret
    actual val totpSecret: String
        get() {
            val key = getStringResource("Secrets", "plist", "totpSecret") ?: "JBSWY3DPEHPK3PXP"
            log.d { "🔑 AppSecrets.totpSecret: ${if (key.isNotEmpty()) "✅ loaded" else "❌ using default"}" }
            return key
        }

    // AdMob ad unit IDs
    actual val androidBannerAdUnitId: String
        get() {
            val key = getStringResource("Secrets", "plist", "androidBannerAdUnitId") ?: ""
            log.d { "🔑 AppSecrets.androidBannerAdUnitId: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}" }
            return key
        }

    actual val iosBannerAdUnitId: String
        get() {
            val key = getStringResource("Secrets", "plist", "iosBannerAdUnitId") ?: ""
            log.d { "🔑 AppSecrets.iosBannerAdUnitId: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}" }
            return key
        }

    actual val androidInterstitialAdUnitId: String
        get() {
            val key = getStringResource("Secrets", "plist", "androidInterstitialAdUnitId") ?: ""
            log.d { "🔑 AppSecrets.androidInterstitialAdUnitId: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}" }
            return key
        }

    actual val iosInterstitialAdUnitId: String
        get() {
            val key = getStringResource("Secrets", "plist", "iosInterstitialAdUnitId") ?: ""
            log.d { "🔑 AppSecrets.iosInterstitialAdUnitId: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}" }
            return key
        }

    actual val androidRewardedAdUnitId: String
        get() {
            val key = getStringResource("Secrets", "plist", "androidRewardedAdUnitId") ?: ""
            log.d { "🔑 AppSecrets.androidRewardedAdUnitId: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}" }
            return key
        }

    actual val iosRewardedAdUnitId: String
        get() {
            val key = getStringResource("Secrets", "plist", "iosRewardedAdUnitId") ?: ""
            log.d { "🔑 AppSecrets.iosRewardedAdUnitId: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}" }
            return key
        }

    actual val datadogEnabled: Boolean
        get() {
            val key = getStringResource("Secrets", "plist", "datadogEnabled") ?: "false"
            log.d { "🔑 AppSecrets.datadogEnabled: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}" }
            return key.lowercase() == "true"
        }

    actual val dataDogClientToken: String
        get() {
            val key = getStringResource("Secrets", "plist", "dataDogClientToken") ?: ""
            log.d { "🔑 AppSecrets.dataDogClientToken: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}" }
            return key
        }

    actual val dataDogApplicationId: String
        get() {
            val key = getStringResource("Secrets", "plist", "dataDogApplicationId") ?: ""
            log.d { "🔑 AppSecrets.dataDogApplicationId: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}" }
            return key
        }

    actual val dataDogEnv: String
        get() {
            val key = getStringResource("Secrets", "plist", "dataDogEnv") ?: "production"
            log.d { "🔑 AppSecrets.dataDogEnv: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}" }
            return key
        }
}

private val resourceLog = me.calebjones.spacelaunchnow.logger()

internal fun getStringResource(filename: String, fileType: String, valueKey: String): String? {
    resourceLog.d { "📂 AppSecrets: Looking for $filename.$fileType with key '$valueKey'" }
    
    val path = NSBundle.mainBundle.pathForResource(filename, fileType)
    resourceLog.d { "📂 AppSecrets: Path result: ${path ?: "❌ FILE NOT FOUND IN BUNDLE"}" }
    
    if (path == null) {
        resourceLog.w { "❌ AppSecrets: $filename.$fileType is NOT in the app bundle! Make sure Secrets.plist is added to 'Copy Bundle Resources' in Xcode" }
        return null
    }
    
    val dict = NSDictionary.dictionaryWithContentsOfFile(path)
    resourceLog.d { "📖 AppSecrets: Dictionary loaded: ${if (dict != null) "✅ YES" else "❌ NO"}" }
    
    if (dict == null) {
        resourceLog.e { "❌ AppSecrets: Failed to parse plist file!" }
        return null
    }
    
    // Print dictionary info
    resourceLog.v { "🔑 AppSecrets: Dictionary info: $dict" }
    
    val value = dict.get(valueKey) as? String
    resourceLog.d { "📖 AppSecrets: Value for '$valueKey': ${if (value != null) "✅ Found (${value.take(10)}...)" else "❌ NULL"}" }
    
    return value
}
