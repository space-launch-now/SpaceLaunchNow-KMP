package me.calebjones.spacelaunchnow.ui.theme

import androidx.compose.runtime.Composable

@Composable
actual fun initPlatformSpecific(colorExtractor: ImageColorExtractor) {
    // No special initialization needed for Desktop
}

actual class ImageColorExtractor {
    // Desktop implementation - basic color extraction
    fun extractColors(imageUrl: String): List<Int> {
        // For Desktop, return empty list or default colors
        return emptyList()
    }
}
