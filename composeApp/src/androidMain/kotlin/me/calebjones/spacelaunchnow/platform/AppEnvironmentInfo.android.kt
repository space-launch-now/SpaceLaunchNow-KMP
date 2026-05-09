package me.calebjones.spacelaunchnow.platform

import android.os.Build
import me.calebjones.spacelaunchnow.BuildConfig
import me.calebjones.spacelaunchnow.MainApplication
import java.util.Locale

actual class AppEnvironmentInfo actual constructor() {
    actual val appVersionName: String = BuildConfig.VERSION_NAME
    actual val appBuildNumber: String = BuildConfig.VERSION_CODE.toString()
    actual val osVersion: String = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
    actual val deviceModel: String = "${Build.MANUFACTURER} ${Build.MODEL}"
    actual val locale: String = Locale.getDefault().toLanguageTag()
    actual val country: String = Locale.getDefault().country
    actual val formFactor: String = run {
        val ctx = MainApplication.instance
        val isTablet = ctx?.resources?.configuration
            ?.smallestScreenWidthDp?.let { it >= 600 } ?: false
        if (isTablet) "tablet" else "phone"
    }
}
