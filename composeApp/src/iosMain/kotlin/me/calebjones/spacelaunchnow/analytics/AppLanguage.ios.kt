package me.calebjones.spacelaunchnow.analytics

import me.calebjones.spacelaunchnow.util.getPlatformLocaleTag
import platform.Foundation.NSBundle

actual fun getPlatformAppLanguageTag(): String =
    (NSBundle.mainBundle.preferredLocalizations.firstOrNull() as? String) ?: getPlatformLocaleTag()
