package me.calebjones.spacelaunchnow.ui.theme

import androidx.compose.runtime.Composable

@Composable
actual fun initPlatformSpecific(colorExtractor: ImageColorExtractor) {
    // No special initialization needed for Desktop
}

actual class ImageColorExtractor
