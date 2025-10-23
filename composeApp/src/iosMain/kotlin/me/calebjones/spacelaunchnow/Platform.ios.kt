package me.calebjones.spacelaunchnow

import androidx.compose.runtime.Composable
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val type: PlatformType = PlatformType.IOS
}

actual fun getPlatform(): Platform = IOSPlatform()


actual fun setOrientationLandscape() {
    // iOS orientation handling is done through MediaPlayerHost
}

actual fun setOrientationPortrait() {
    // iOS orientation handling is done through MediaPlayerHost
}

actual fun setOrientationSensor() {
    // iOS orientation handling is done through MediaPlayerHost
}

@Composable
actual fun setOrientationLandscapeFromComposable() {
    // iOS orientation handling is done through MediaPlayerHost or UIKit
}

@Composable
actual fun setOrientationPortraitFromComposable() {
    // iOS orientation handling is done through MediaPlayerHost or UIKit
}

@Composable
actual fun setOrientationSensorFromComposable() {
    // iOS orientation handling is done through MediaPlayerHost or UIKit
}

@Composable
actual fun getOrientation(): Int {
    // TODO: Implement platform-specific orientation handling for iOS
    return 0 // Default to portrait as a stub
}