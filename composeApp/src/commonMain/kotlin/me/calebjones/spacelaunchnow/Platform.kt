package me.calebjones.spacelaunchnow

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

/**
 * Platform types supported by the application
 */
enum class PlatformType {
    ANDROID,
    IOS,
    DESKTOP;

    val isAndroid: Boolean get() = this == ANDROID
    val isIOS: Boolean get() = this == IOS
    val isDesktop: Boolean get() = this == DESKTOP
    val isMobile: Boolean get() = this == ANDROID || this == IOS
}

interface Platform {
    val name: String
    val type: PlatformType
}

expect fun getPlatform(): Platform

@Composable
expect fun getOrientation(): Int

// Orientation management
expect fun setOrientationLandscape()
expect fun setOrientationPortrait()
expect fun setOrientationSensor()

// Orientation management with context (for Composables)
@Composable
expect fun setOrientationLandscapeFromComposable()

@Composable
expect fun setOrientationPortraitFromComposable()

@Composable
expect fun setOrientationSensorFromComposable()

@Composable
fun isDesktop(): Boolean {
    return getPlatform().type.isDesktop
}

@Composable
fun isAndroid(): Boolean {
    return getPlatform().type.isAndroid
}

@Composable
fun isIOS(): Boolean {
    return getPlatform().type.isIOS
}

@Composable
fun isMobile(): Boolean {
    return getPlatform().type.isMobile
}

@Composable
fun isLargeScreen(): Boolean {
    val windowSize = getWindowSize()
    val width = windowSize.windowWidthSizeClass

    return width == WindowWidthSizeClass.MEDIUM || width == WindowWidthSizeClass.EXPANDED
}

@Composable
fun getWindowSize(): WindowSizeClass {
    return currentWindowAdaptiveInfo().windowSizeClass
}