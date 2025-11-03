package me.calebjones.spacelaunchnow.util

import platform.Foundation.NSBundle
import platform.Foundation.NSDictionary
import platform.Foundation.dictionaryWithContentsOfFile

actual object AppSecrets {
    actual val apiKey: String
        get() {
            val key = getStringResource("Secrets", "plist", "apiKey") ?: ""
            println("🔑 AppSecrets.apiKey: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}")
            return key
        }

    actual val revenueCatAndroidKey: String
        get() {
            val key = getStringResource("Secrets", "plist", "revenueCatAndroidKey") ?: ""
            println("🔑 AppSecrets.revenueCatAndroidKey: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}")
            return key
        }

    actual val revenueCatIosKey: String
        get() {
            println("🔍 AppSecrets: Attempting to load revenueCatIosKey...")
            val key = getStringResource("Secrets", "plist", "revenueCatIosKey") ?: ""
            println("🔑 AppSecrets.revenueCatIosKey result: ${if (key.isNotEmpty()) "✅ loaded (${key.take(15)}...)" else "❌ EMPTY"}")
            return key
        }

    // Debug Menu TOTP Secret
    actual val totpSecret: String
        get() {
            val key = getStringResource("Secrets", "plist", "totpSecret") ?: "JBSWY3DPEHPK3PXP"
            println("🔑 AppSecrets.totpSecret: ${if (key.isNotEmpty()) "✅ loaded" else "❌ using default"}")
            return key
        }

    // AdMob ad unit IDs
    actual val androidBannerAdUnitId: String
        get() {
            val key = getStringResource("Secrets", "plist", "androidBannerAdUnitId") ?: ""
            println("🔑 AppSecrets.androidBannerAdUnitId: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}")
            return key
        }

    actual val iosBannerAdUnitId: String
        get() {
            val key = getStringResource("Secrets", "plist", "iosBannerAdUnitId") ?: ""
            println("🔑 AppSecrets.iosBannerAdUnitId: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}")
            return key
        }

    actual val androidInterstitialAdUnitId: String
        get() {
            val key = getStringResource("Secrets", "plist", "androidInterstitialAdUnitId") ?: ""
            println("🔑 AppSecrets.androidInterstitialAdUnitId: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}")
            return key
        }

    actual val iosInterstitialAdUnitId: String
        get() {
            val key = getStringResource("Secrets", "plist", "iosInterstitialAdUnitId") ?: ""
            println("🔑 AppSecrets.iosInterstitialAdUnitId: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}")
            return key
        }

    actual val androidRewardedAdUnitId: String
        get() {
            val key = getStringResource("Secrets", "plist", "androidRewardedAdUnitId") ?: ""
            println("🔑 AppSecrets.androidRewardedAdUnitId: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}")
            return key
        }

    actual val iosRewardedAdUnitId: String
        get() {
            val key = getStringResource("Secrets", "plist", "iosRewardedAdUnitId") ?: ""
            println("🔑 AppSecrets.iosRewardedAdUnitId: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}")
            return key
        }

    actual val datadogEnabled: Boolean
        get() {
            val key = getStringResource("Secrets", "plist", "datadogEnabled") ?: "false"
            println("🔑 AppSecrets.datadogEnabled: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}")
            return key.lowercase() == "true"
        }

    actual val dataDogClientToken: String
        get() {
            val key = getStringResource("Secrets", "plist", "dataDogClientToken") ?: ""
            println("🔑 AppSecrets.dataDogClientToken: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}")
            return key
        }

    actual val dataDogApplicationId: String
        get() {
            val key = getStringResource("Secrets", "plist", "dataDogApplicationId") ?: ""
            println("🔑 AppSecrets.dataDogApplicationId: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}")
            return key
        }

    actual val dataDogEnv: String
        get() {
            val key = getStringResource("Secrets", "plist", "dataDogEnv") ?: "production"
            println("🔑 AppSecrets.dataDogEnv: ${if (key.isNotEmpty()) "✅ loaded" else "❌ EMPTY"}")
            return key
        }
}

internal fun getStringResource(filename: String, fileType: String, valueKey: String): String? {
    println("📂 AppSecrets: Looking for $filename.$fileType with key '$valueKey'")
    
    val path = NSBundle.mainBundle.pathForResource(filename, fileType)
    println("📂 AppSecrets: Path result: ${path ?: "❌ FILE NOT FOUND IN BUNDLE"}")
    
    if (path == null) {
        println("❌ AppSecrets: $filename.$fileType is NOT in the app bundle!")
        println("💡 AppSecrets: Make sure Secrets.plist is added to 'Copy Bundle Resources' in Xcode")
        return null
    }
    
    val dict = NSDictionary.dictionaryWithContentsOfFile(path)
    println("📖 AppSecrets: Dictionary loaded: ${if (dict != null) "✅ YES" else "❌ NO"}")
    
    if (dict == null) {
        println("❌ AppSecrets: Failed to parse plist file!")
        return null
    }
    
    // Print dictionary info
    println("🔑 AppSecrets: Dictionary info: $dict")
    
    val value = dict.get(valueKey) as? String
    println("📖 AppSecrets: Value for '$valueKey': ${if (value != null) "✅ Found (${value.take(10)}...)" else "❌ NULL"}")
    
    return value
}
