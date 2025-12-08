package me.calebjones.spacelaunchnow.ui.components

import androidx.compose.runtime.Composable

/**
 * Cross-platform back handler for handling system back button presses.
 * 
 * On Android: Intercepts the hardware/gesture back button
 * On iOS: No-op (iOS uses native swipe gestures handled by the navigation controller)
 * On Desktop: No-op (Desktop uses window close/navigation)
 * 
 * @param enabled Whether the back handler is currently enabled
 * @param onBack Callback invoked when back is pressed (Android only)
 */
@Composable
expect fun PlatformBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
)
