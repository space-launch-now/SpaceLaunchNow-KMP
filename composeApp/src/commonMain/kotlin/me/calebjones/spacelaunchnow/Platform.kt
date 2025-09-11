package me.calebjones.spacelaunchnow

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@Composable
expect fun getScreenWidth(): Dp

@Composable
expect fun getScreenHeight(): Dp

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
    return !isTablet() && getPlatform().name != "Desktop"
}

@Composable
fun isDesktop(): Boolean {
    return getPlatform().name == "Desktop"
}