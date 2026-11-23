package me.calebjones.spacelaunchnow.ui.subscription

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionViewModel
import kotlin.time.TimeSource

/**
 * Reports `paywall_dismissed` once per presentation of a full-screen paywall.
 * Create it when the screen is shown; call [dismiss] from every exit path.
 */
class PaywallDismissTracker(
    private val viewModel: SubscriptionViewModel,
    private val source: String,
    private val shownAt: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()
) {
    private var reported = false

    fun dismiss() {
        if (reported) return
        reported = true
        viewModel.trackPaywallDismissedIfNotSubscribed(source, shownAt.elapsedNow().inWholeSeconds)
    }
}

/**
 * Reports the dismissal when this composable leaves composition. This covers exits that no back
 * callback sees: the iOS swipe-back gesture (the nav controller pops the screen) and the desktop
 * window close (the application ends and its composition is disposed). [PaywallDismissTracker]
 * keeps it to one report per presentation, so the top-bar arrow and Android back do not double count.
 */
@Composable
fun DismissOnLeaveComposition(tracker: PaywallDismissTracker) {
    DisposableEffect(tracker) {
        onDispose { tracker.dismiss() }
    }
}
