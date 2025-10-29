package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.runtime.Composable

/**
 * Desktop implementation of AdConsentPopup (no-op).
 * Desktop doesn't support ads or consent popups.
 */
@Composable
actual fun AdConsentPopup(
    onFailure: ((Throwable) -> Unit)?
) {
    // No-op: Desktop doesn't show consent popups
}

/**
 * Desktop implementation of WithPreloadedAds (no-op wrapper).
 * Desktop doesn't support ads, so this just renders the content.
 */
@Composable
actual fun WithPreloadedAds(
    context: Any?,
    content: @Composable () -> Unit
) {
    // No-op wrapper: Just render content without preloading ads
    content()
}
