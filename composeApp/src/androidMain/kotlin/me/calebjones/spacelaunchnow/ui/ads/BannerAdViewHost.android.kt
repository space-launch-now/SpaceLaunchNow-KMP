package me.calebjones.spacelaunchnow.ui.ads

import android.graphics.Rect
import android.view.ViewGroup
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.delay
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger

private val log by lazy { SpaceLogger.getLogger("BannerAdViewHost") }

/** How long the layout stays unchanged before the frame check reads it. */
private const val FRAME_CHECK_SETTLE_MS = 300L

/**
 * Hosts one AdView at its own size (WRAP_CONTENT, never stretched or clipped by a fixed box).
 *
 * AdMob "Resizing Ad Frames": the library's BannerAd composable stretches the AdView to a
 * MATCH_PARENT box and keeps old AdViews around after a reload. This host shows exactly the one
 * [view] it is given. When the handler swaps in a new AdView, the caller passes the new one and
 * the old one leaves with the old key.
 *
 * Lifecycle: pauses and resumes [view] with the host's lifecycle. It does not destroy it: the
 * view is shared per placement and outlives this screen. Destroy happens when the view is
 * replaced (retireBannerView) or when the Activity ends.
 */
@Composable
internal fun BannerAdViewHost(view: AdView, placement: String) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, view) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> view.resume()
                Lifecycle.Event.ON_PAUSE -> view.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var measured by remember(view) { mutableStateOf(IntSize.Zero) }
    FrameCheck(view = view, placement = placement, measured = measured)

    key(view) {
        AndroidView(
            factory = {
                // One parent at a time: take the view from a previous host first
                (view.parent as? ViewGroup)?.removeView(view)
                val params = view.layoutParams
                if (params == null ||
                    params.width != ViewGroup.LayoutParams.WRAP_CONTENT ||
                    params.height != ViewGroup.LayoutParams.WRAP_CONTENT
                ) {
                    view.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                view
            },
            modifier = Modifier
                .wrapContentSize()
                .onGloballyPositioned { measured = it.size }
        )
    }
}

/**
 * Field check for "Resizing Ad Frames": once the layout settles, compare the AdView's real size
 * and visible part with the size it loaded at. Reports ad_frame_mismatch (once per placement per
 * session) when they differ. Silent when they match.
 */
@Composable
private fun FrameCheck(view: AdView, placement: String, measured: IntSize) {
    LaunchedEffect(view, measured) {
        if (measured.width <= 0 || measured.height <= 0) return@LaunchedEffect
        delay(FRAME_CHECK_SETTLE_MS)
        val context = view.context
        val loaded = view.adSize ?: return@LaunchedEffect
        val loadedPx = loaded.getWidthInPixels(context) to loaded.getHeightInPixels(context)
        val realPx = view.width to view.height
        if (realPx.first <= 0 || realPx.second <= 0) return@LaunchedEffect

        val visible = Rect()
        val isVisible = view.getGlobalVisibleRect(visible)
        val location = IntArray(2)
        view.getLocationInWindow(location)
        val root = view.rootView
        val clippedPx = if (isVisible) {
            visibleSizeForCheck(
                left = location[0],
                top = location[1],
                width = realPx.first,
                height = realPx.second,
                windowWidth = root.width,
                windowHeight = root.height,
                visibleWidth = visible.width(),
                visibleHeight = visible.height()
            )
        } else {
            null
        }
        if (!AdFrameCheck.mismatch(loadedPx, realPx, clippedPx)) return@LaunchedEffect

        val config = view.resources.configuration
        log.w {
            "Banner frame mismatch for $placement: loaded=$loadedPx real=$realPx visible=$clippedPx"
        }
        AdTelemetry.frameMismatch(
            placement = placement,
            loadedPx = loadedPx,
            realPx = realPx,
            windowDp = config.screenWidthDp to config.screenHeightDp,
            orientation = orientationLabel(config.orientation)
        )
    }
}
