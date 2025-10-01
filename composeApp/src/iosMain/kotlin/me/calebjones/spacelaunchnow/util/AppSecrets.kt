package me.calebjones.spacelaunchnow.util

import platform.Foundation.NSBundle
import platform.Foundation.NSDictionary
import platform.Foundation.dictionaryWithContentsOfFile

actual object AppSecrets {
    actual val apiKey: String
        get() = getStringResource("Secrets", "plist", "apiKey") ?: ""
}

internal fun getStringResource(filename: String, fileType: String, valueKey: String): String? {
    val path = NSBundle.mainBundle.pathForResource(filename, fileType)
    val dict = path?.let { NSDictionary.dictionaryWithContentsOfFile(it) }
    return dict?.get(valueKey) as? String
}
