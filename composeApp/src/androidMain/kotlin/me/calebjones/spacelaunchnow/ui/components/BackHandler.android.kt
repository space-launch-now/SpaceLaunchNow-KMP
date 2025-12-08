package me.calebjones.spacelaunchnow.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

/**
 * Android implementation of PlatformBackHandler using AndroidX BackHandler.
 * Intercepts the hardware/gesture back button when enabled.
 */
@Composable
actual fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    BackHandler(enabled = enabled, onBack = onBack)
}
