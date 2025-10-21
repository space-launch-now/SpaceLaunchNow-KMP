package me.calebjones.spacelaunchnow

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
expect fun getScreenWidth(): Dp

@Composable
expect fun getScreenHeight(): Dp

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
fun isLandscape(): Boolean {
    return getScreenWidth() > getScreenHeight()
}

@Composable
fun isTablet(): Boolean {
    val screenWidthDp = getScreenWidth()
    val screenHeightDp = getScreenHeight()
    val smallestWidth = if (screenWidthDp < screenHeightDp) screenWidthDp else screenHeightDp
    // Use 720dp as the standard tablet threshold
    return smallestWidth >= 720.dp
}

@Composable
fun isPhone(): Boolean {
    return !isTablet() && getPlatform().type.isMobile
}

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