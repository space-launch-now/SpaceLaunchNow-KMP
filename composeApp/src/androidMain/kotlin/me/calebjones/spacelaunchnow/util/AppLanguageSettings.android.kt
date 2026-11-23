package me.calebjones.spacelaunchnow.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import me.calebjones.spacelaunchnow.util.logging.logger
import org.koin.mp.KoinPlatformTools

actual object AppLanguageSettings {
    private val log = logger()

    // ACTION_APP_LOCALE_SETTINGS exists from Android 13 (API 33).
    actual val isSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    actual fun open() {
        if (!isSupported) return
        try {
            val context = KoinPlatformTools.defaultContext().get().get<Context>()
            val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            log.e(e) { "Failed to open the app language settings: ${e.message}" }
        }
    }
}
