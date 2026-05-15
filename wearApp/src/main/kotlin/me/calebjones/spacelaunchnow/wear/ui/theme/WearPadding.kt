package me.calebjones.spacelaunchnow.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Horizontal inset that clears the circular bezel on all watch sizes.
 *
 * Derived from chord geometry: after ScreenScaffold's ~48 dp top padding, the first
 * content item sits where a round display's usable chord width requires ~10 % of the
 * screen diameter per side. On a 225 dp Pixel Watch 2 that works out to 22.5 dp;
 * the 20 dp floor covers the smallest current Wear OS displays. Rectangular watches
 * (Galaxy Watch Classic etc.) keep the standard 14 dp.
 */
@Composable
fun wearHorizontalPadding(): Dp {
    val config = LocalConfiguration.current
    return if (config.isScreenRound) {
        (config.screenWidthDp * 0.10f).dp.coerceAtLeast(20.dp)
    } else {
        14.dp
    }
}
