package me.calebjones.spacelaunchnow.platform

import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier
import platform.UIKit.UIDevice
import platform.UIKit.UIUserInterfaceIdiomPad

actual class AppEnvironmentInfo actual constructor() {
    actual val appVersionName: String =
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
            ?: "unknown"
    actual val appBuildNumber: String =
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion") as? String
            ?: "unknown"
    actual val osVersion: String =
        "iOS ${UIDevice.currentDevice.systemVersion}"
    actual val deviceModel: String = UIDevice.currentDevice.model
    actual val locale: String =
        (NSLocale.currentLocale.localeIdentifier).replace('_', '-')
    actual val country: String = NSLocale.currentLocale.countryCode ?: ""
    actual val formFactor: String =
        if (UIDevice.currentDevice.userInterfaceIdiom == UIUserInterfaceIdiomPad) "tablet" else "phone"
}
