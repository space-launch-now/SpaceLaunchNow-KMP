package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.BuildConfig

actual object AppSecrets {
    actual val apiKey: String = BuildConfig.API_KEY
}