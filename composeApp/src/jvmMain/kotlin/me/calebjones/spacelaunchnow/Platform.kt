package me.calebjones.spacelaunchnow

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun setOrientationLandscape() {
    // JVM/Desktop doesn't have orientation changes
}

actual fun setOrientationPortrait() {
    // JVM/Desktop doesn't have orientation changes
}

actual fun setOrientationSensor() {
    // JVM/Desktop doesn't have orientation changes
}

@Composable
actual fun setOrientationLandscapeFromComposable() {
    // JVM/Desktop doesn't have orientation changes
}

@Composable
actual fun setOrientationPortraitFromComposable() {
    // JVM/Desktop doesn't have orientation changes
}

@Composable
actual fun setOrientationSensorFromComposable() {
    // JVM/Desktop doesn't have orientation changes
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenWidth() = LocalWindowInfo.current.containerSize.width.let { (it / 2).dp }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenHeight() = LocalWindowInfo.current.containerSize.height.let { (it / 2).dp }