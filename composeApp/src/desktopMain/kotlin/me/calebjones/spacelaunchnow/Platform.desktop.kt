package me.calebjones.spacelaunchnow

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

class DesktopPlatform: Platform {
    override val name: String = "Desktop"
}

actual fun getPlatform(): Platform = DesktopPlatform()

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