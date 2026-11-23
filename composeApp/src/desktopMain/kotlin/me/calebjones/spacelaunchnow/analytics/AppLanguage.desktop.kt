package me.calebjones.spacelaunchnow.analytics

import java.util.Locale

actual fun getPlatformAppLanguageTag(): String = Locale.getDefault().toLanguageTag()
