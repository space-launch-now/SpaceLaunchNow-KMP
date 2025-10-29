package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.runtime.Composable

/**
 * Platform-specific ad support wrapper.
 * 
 * This provides a clean abstraction for ad-related functionality,
 * allowing commonMain code to work with ads without directly importing
 * platform-specific libraries.
 */
expect class PlatformAdSupport {
    /**
     * Initialize ads for the platform.
     * Returns true if ads are supported and initialized, false otherwise.
     */
    fun initialize(context: Any?, isDebug: Boolean, testDeviceIds: List<String>): Boolean
    
    /**
     * Whether ads are supported on this platform
     */
    val isSupported: Boolean
}

/**
 * Composable wrapper that provides ad-related CompositionLocals and UI.
 * On Android/iOS: Preloads ads, provides consent UI, provides ad handlers via CompositionLocal
 * On Desktop: Does nothing (no-op wrapper)
 */
@Composable
expect fun WithAdSupport(
    context: Any?,
    content: @Composable () -> Unit
)
