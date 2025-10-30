package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.runtime.Composable

/**
 * Desktop implementation of InterstitialAdHandler (no-op).
 * Desktop doesn't support ads, so this is an empty composable.
 */
@Composable
actual fun InterstitialAdHandler(
    onAdShown: (() -> Unit)?,
    onAdFailed: ((String) -> Unit)?
) {
    // No-op: Desktop doesn't show ads
    println("🎯 InterstitialAdHandler (Desktop): Ads not supported, skipping")
}
