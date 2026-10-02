package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * A key that changes each time the host screen resumes, for use as a SmartBannerAd refreshKey.
 *
 * A screen that stays composed under another (Home below a detail screen) never recomposes on
 * return, so a plain entity key cannot signal a new visit. Combine this with an entity id on
 * detail screens.
 */
@Composable
fun rememberScreenVisitKey(): Any {
    val lifecycleOwner = LocalLifecycleOwner.current
    var visit by remember(lifecycleOwner) { mutableIntStateOf(0) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) visit++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return visit
}
