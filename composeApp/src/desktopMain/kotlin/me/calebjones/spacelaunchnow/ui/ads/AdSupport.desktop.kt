package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Desktop implementation of AdConsentPopup (no-op).
 * Desktop doesn't support ads or consent popups; resolve consent immediately.
 */
@Composable
actual fun AdConsentPopup(
    onFailure: ((Throwable) -> Unit)?,
    onConsentResolved: (() -> Unit)?
) {
    // Desktop doesn't need consent; resolve immediately so ads aren't blocked on other platforms
    LaunchedEffect(Unit) { onConsentResolved?.invoke() }
}

/**
 * Desktop implementation of WithPreloadedAds (no-op wrapper).
 * Desktop doesn't support ads, so this just renders the content.
 */
@Composable
actual fun WithPreloadedAds(
    context: Any?,
    shouldPreloadAds: Boolean,
    content: @Composable () -> Unit
) {
    // No-op wrapper: Just render content without preloading ads
    content()
}
