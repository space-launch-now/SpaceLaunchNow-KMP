package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.BuildConfig

actual object AppSecrets {
    actual val apiKey: String = BuildConfig.API_KEY
    actual val revenueCatAndroidKey: String = BuildConfig.REVENUECAT_ANDROID_KEY
    actual val revenueCatIosKey: String = BuildConfig.REVENUECAT_IOS_KEY
}