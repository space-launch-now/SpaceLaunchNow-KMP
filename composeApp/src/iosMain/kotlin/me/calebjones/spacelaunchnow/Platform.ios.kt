package me.calebjones.spacelaunchnow

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
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
actual fun getScreenWidth(): Dp {
    // TODO: Implement platform-specific screen width for iOS
    return 375.dp // Default iPhone width as a stub
}

@Composable
actual fun getScreenHeight(): Dp {
    // TODO: Implement platform-specific screen height for iOS
    return 667.dp // Default iPhone height as a stub
}