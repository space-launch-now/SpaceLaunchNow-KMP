package me.calebjones.spacelaunchnow.util

actual object AppSecrets {
    actual val apiKey: String = DesktopSecret.API_KEY
    actual val revenueCatAndroidKey: String = DesktopSecret.REVENUECAT_ANDROID_KEY
    actual val revenueCatIosKey: String = DesktopSecret.REVENUECAT_IOS_KEY
}