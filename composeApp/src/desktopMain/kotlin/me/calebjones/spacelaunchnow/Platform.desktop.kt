package me.calebjones.spacelaunchnow

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

class DesktopPlatform: Platform {
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenWidth() = LocalWindowInfo.current
    .containerSize
    .width
    .dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenHeight() = LocalWindowInfo.current
    .containerSize
    .height
    .dp