package me.calebjones.spacelaunchnow

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

class DesktopPlatform : Platform {
    override val name: String = "Desktop"
    override val type: PlatformType = PlatformType.DESKTOP
}

actual fun getPlatform(): Platform = DesktopPlatform()

actual fun setOrientationLandscape() {
    // Desktop doesn't have orientation changes
}

actual fun setOrientationPortrait() {
    // Desktop doesn't have orientation changes
}

actual fun setOrientationSensor() {
    // Desktop doesn't have orientation changes
}

@Composable
actual fun setOrientationLandscapeFromComposable() {
    // Desktop doesn't have orientation changes
}

@Composable
actual fun setOrientationPortraitFromComposable() {
    // Desktop doesn't have orientation changes
}

@Composable
actual fun setOrientationSensorFromComposable() {
    // Desktop doesn't have orientation changes
}
@Composable
actual fun getOrientation(): Int {
    TODO("Not yet implemented")
}