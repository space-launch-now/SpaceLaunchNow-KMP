package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.platform.AndroidBuildConfig

actual fun initializeBuildConfig() {
    // Copy the values the application module registered into the common BuildConfig holder.
    BuildConfig.VERSION_NAME = AndroidBuildConfig.VERSION_NAME
    BuildConfig.VERSION_CODE = AndroidBuildConfig.VERSION_CODE
    BuildConfig.IS_DEBUG = AndroidBuildConfig.IS_DEBUG
}
