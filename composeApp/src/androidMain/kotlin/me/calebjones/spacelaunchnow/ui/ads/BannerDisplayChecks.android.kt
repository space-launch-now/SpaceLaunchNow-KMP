package me.calebjones.spacelaunchnow.ui.ads

import android.content.res.Configuration
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity
import app.lexilabs.basic.ads.AdState

/**
 * What decides the pixel size of a banner. An AdView loaded at 320x50 dp is a fixed number of
 * pixels; when density, font scale or the window size class change, that frame is stale.
 */
internal data class DisplayConfigKey(
    val density: Float,
    val fontScale: Float,
    val widthClass: String
) {
    /** True when [previous] is known and differs from this key. The first key is no change. */
    fun changedFrom(previous: DisplayConfigKey?): Boolean = previous != null && previous != this
}

/** A banner that has an AdView worth replacing: it loaded, or it is on screen. */
internal fun isBannerLoaded(state: AdState): Boolean =
    state == AdState.READY || state == AdState.SHOWING || state == AdState.SHOWN

/** "portrait" or "landscape" for the field report. */
internal fun orientationLabel(orientation: Int): String = when (orientation) {
    Configuration.ORIENTATION_LANDSCAPE -> "landscape"
    Configuration.ORIENTATION_PORTRAIT -> "portrait"
    else -> "unknown"
}

@Composable
private fun rememberDisplayConfigKey(): DisplayConfigKey {
    val density = LocalDensity.current
    val widthClass = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass
    return DisplayConfigKey(density.density, density.fontScale, widthClass.toString())
}

/**
 * Runs [onChanged] when the display config changes after the first composition. The Activity is
 * not recreated for density or screen size changes (manifest configChanges), so loaded AdViews
 * keep their old pixel size until something reloads them.
 */
@Composable
internal fun ReloadOnDisplayChange(onChanged: () -> Unit) {
    val key = rememberDisplayConfigKey()
    val last = remember { mutableStateOf<DisplayConfigKey?>(null) }
    val action = rememberUpdatedState(onChanged)
    LaunchedEffect(key) {
        if (key.changedFrom(last.value)) action.value()
        last.value = key
    }
}
