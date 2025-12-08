package me.calebjones.spacelaunchnow.ui.components

import androidx.compose.runtime.Composable

/**
 * iOS implementation of PlatformBackHandler.
 * No-op on iOS - back navigation is handled by native swipe gestures
 * and UINavigationController, not by a system back button.
 */
@Composable
actual fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    // No-op on iOS - back navigation is handled by native gestures
}
