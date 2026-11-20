package me.calebjones.spacelaunchnow.platform

import android.os.Build
import me.calebjones.spacelaunchnow.util.ShareContextHolder
import java.util.Locale

actual class AppEnvironmentInfo actual constructor() {
    actual val appVersionName: String = AndroidBuildConfig.VERSION_NAME
    actual val appBuildNumber: String = AndroidBuildConfig.VERSION_CODE.toString()
    actual val osVersion: String = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
    actual val deviceModel: String = "${Build.MANUFACTURER} ${Build.MODEL}"
    actual val locale: String = Locale.getDefault().toLanguageTag()
    actual val country: String = Locale.getDefault().country
    actual val formFactor: String = run {
        val ctx = ShareContextHolder.appContext
        val isTablet = ctx?.resources?.configuration
            ?.smallestScreenWidthDp?.let { it >= 600 } ?: false
        if (isTablet) "tablet" else "phone"
    }
}
