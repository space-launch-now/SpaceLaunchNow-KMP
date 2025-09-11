package me.calebjones.spacelaunchnow.ui.theme

import androidx.compose.runtime.Composable

@Composable
actual fun initPlatformSpecific(colorExtractor: ImageColorExtractor) {
    // No special initialization needed for Android
}

actual class ImageColorExtractor {
    // Android implementation - could use Palette API in the future
    fun extractColors(imageUrl: String): List<Int> {
        // For Android, return empty list or default colors
        // TODO: Implement Android Palette API for color extraction
        return emptyList()
    }
}
