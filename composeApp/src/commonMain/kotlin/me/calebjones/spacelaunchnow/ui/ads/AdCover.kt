package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity

/**
 * Ad cover: when something sits on top of an AdView, the ad leaves composition.
 *
 * AdMob policy "Resizing Ad Frames" and its overlap rules: a banner must not be partly covered.
 * A sheet, dialog, menu or the keyboard can cover part of an AdView or dim it with a scrim.
 * So while any of them is up ([isAdCovered]), no banner shows:
 * - The persistent nav banner leaves composition ([AdCoverGate]) and its space stays reserved,
 *   so the screen does not jump.
 * - Inline banners (SmartBannerAd, every placement) read [isAdCovered] and show blank space of
 *   their reserved height. A scrim over an inline ad counts as covered too.
 * Screens do not call this directly. The AdCovering* wrappers in ui/ads raise the cover.
 */
internal object AdCoverMath {
    fun up(count: Int): Int = count + 1

    /** Never goes below zero, so a stray extra release cannot break the next raise. */
    fun down(count: Int): Int = if (count > 0) count - 1 else 0

    fun isCovered(count: Int): Boolean = count > 0
}

/** Counts the overlays that are up now. Backed by Compose state, so readers recompose. */
class AdCoverState {
    private var count by mutableIntStateOf(0)

    val isCovered: Boolean get() = AdCoverMath.isCovered(count)

    fun raise() {
        count = AdCoverMath.up(count)
    }

    fun lower() {
        count = AdCoverMath.down(count)
    }
}

/** Provided once at the app root. The default is a state nobody raises. */
val LocalAdCover = compositionLocalOf { AdCoverState() }

/** Raises the cover while this is in composition and [active]. */
@Composable
fun RaiseAdCover(active: Boolean = true) {
    val state = LocalAdCover.current
    DisposableEffect(state, active) {
        if (active) state.raise()
        onDispose { if (active) state.lower() }
    }
}

@Composable
fun rememberAdCoverState(): AdCoverState = remember { AdCoverState() }

/** True while an overlay is up or the keyboard is showing. Banner hosts read this. */
@Composable
fun isAdCovered(): Boolean {
    // WindowInsets.isImeVisible is not in commonMain, so read the IME inset itself.
    val imeUp = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    return LocalAdCover.current.isCovered || imeUp
}

/**
 * Shows [content] (a persistent banner) unless the ad is covered. While covered it shows a spacer
 * as tall as the content was last measured, so the layout keeps its size.
 */
@Composable
fun AdCoverGate(content: @Composable () -> Unit) {
    var lastHeightPx by remember { mutableIntStateOf(0) }
    if (isAdCovered()) {
        Spacer(Modifier.height(with(LocalDensity.current) { lastHeightPx.toDp() }))
    } else {
        Box(Modifier.onSizeChanged { if (it.height > 0) lastHeightPx = it.height }) {
            content()
        }
    }
}
