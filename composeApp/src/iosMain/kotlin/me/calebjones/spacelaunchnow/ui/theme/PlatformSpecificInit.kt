package me.calebjones.spacelaunchnow.ui.theme

import androidx.compose.runtime.Composable

@Composable
actual fun initPlatformSpecific(colorExtractor: ImageColorExtractor) {
    // No special initialization needed for iOS
}

actual class ImageColorExtractor {
    // iOS implementation - basic color extraction
    fun extractColors(imageUrl: String): List<Int> {
        // For iOS, return empty list or default colors
        return emptyList()
    }
}
