package me.calebjones.spacelaunchnow

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
actual fun getScreenWidth(): Dp {
    // TODO: Implement platform-specific screen width for iOS
    return 375.dp // Default iPhone width as a stub
}
