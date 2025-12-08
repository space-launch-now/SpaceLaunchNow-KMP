package me.calebjones.spacelaunchnow.ui.components

import androidx.compose.runtime.Composable

/**
 * Desktop implementation of PlatformBackHandler.
 * No-op on Desktop - there's no system back button on desktop platforms.
 */
@Composable
actual fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    // No-op on Desktop - no system back button
}
