package me.calebjones.spacelaunchnow.ui.ads

import android.view.ViewGroup
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdView

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
            modifier = Modifier.wrapContentSize()
        )
    }
}
