package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.util.logging.logger
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

actual object AppLanguageSettings {
    private val log = logger()

    actual val isSupported: Boolean = true

    // iOS shows the per-app Language row on the app page of the Settings app.
    actual fun open() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        val application = UIApplication.sharedApplication
        if (application.canOpenURL(url)) {
            application.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
        } else {
            log.w { "Cannot open iOS settings URL" }
        }
    }
}
