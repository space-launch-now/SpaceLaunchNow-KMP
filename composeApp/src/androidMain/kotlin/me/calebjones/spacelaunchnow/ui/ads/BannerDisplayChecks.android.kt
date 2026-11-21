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
import java.util.WeakHashMap

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

/**
 * The display config each handler was last loaded at. A handler whose host left composition
 * while the display changed is not reloaded by [ReloadOnDisplayChange] (it only sees changes
 * while composed), so the host compares this record with the current key when it comes back.
 * Weak keys: a dropped handler leaves no entry.
 */
internal class LoadedDisplayKeys {
    private val loadedAt = WeakHashMap<Any, DisplayConfigKey>()

    fun markLoaded(handler: Any, key: DisplayConfigKey) {
        loadedAt[handler] = key
    }

    /** True when [handler] was loaded at a different display config than [key]. Unknown: false. */
    fun isStale(handler: Any, key: DisplayConfigKey): Boolean = loadedAt[handler]?.let { it != key } == true

    companion object {
        val shared = LoadedDisplayKeys()
    }
}

/**
 * True when a reload put a new view on the handler. reloadBannerView relies on load() swapping
 * the handler's bannerView at once (true in basic-ads 1.1.1): if it did not, the replaced
 * AdView is never retired, so the caller logs it.
 */
internal fun bannerViewSwapped(previous: Any?, current: Any?): Boolean = current != null && current !== previous

/** "portrait" or "landscape" for the field report. */
internal fun orientationLabel(orientation: Int): String = when (orientation) {
    Configuration.ORIENTATION_LANDSCAPE -> "landscape"
    Configuration.ORIENTATION_PORTRAIT -> "portrait"
    else -> "unknown"
}

/**
 * The visible size of a banner, or null when it cannot be judged. A banner that reaches past a
 * window edge is scrolled partly off screen, which is not a frame fault, so it gives null. Inside
 * the window, a visible part smaller than the banner means a parent clips it.
 */
internal fun visibleSizeForCheck(
    left: Int,
    top: Int,
    width: Int,
    height: Int,
    windowWidth: Int,
    windowHeight: Int,
    visibleWidth: Int,
    visibleHeight: Int
): Pair<Int, Int>? {
    val inside = left >= 0 && top >= 0 && left + width <= windowWidth && top + height <= windowHeight
    return if (inside) visibleWidth to visibleHeight else null
}

@Composable
internal fun rememberDisplayConfigKey(): DisplayConfigKey {
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
