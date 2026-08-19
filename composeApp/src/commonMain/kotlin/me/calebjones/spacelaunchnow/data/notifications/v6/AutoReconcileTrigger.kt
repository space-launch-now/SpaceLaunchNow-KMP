package me.calebjones.spacelaunchnow.data.notifications.v6

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Debounced trigger for V6 subscription reconciliation.
 *
 * Filter toggles apply immediately — every mutation calls [request] so FCM subscriptions
 * follow settings without an explicit save step. But a class switch rewrites ~20 topics,
 * and a user exploring the filters screen produces bursts of toggles; the conflated
 * channel plus debounce collapses a burst into one reconcile pass once they pause.
 *
 * Extracted from the repository so the collapse behaviour is testable with virtual time:
 * the repository's own scope runs on a real dispatcher, and its FCM platform is null on
 * the only target commonTest can execute (desktop), which makes reconciles unobservable
 * end-to-end.
 */
@OptIn(FlowPreview::class)
class AutoReconcileTrigger(
    scope: CoroutineScope,
    debounceMs: Long,
    private val onReconcile: suspend () -> Unit
) {
    private val requests = Channel<Unit>(Channel.CONFLATED)

    init {
        scope.launch {
            requests.receiveAsFlow()
                .debounce(debounceMs)
                .collect {
                    try {
                        onReconcile()
                    } catch (_: Exception) {
                        // The action is expected to log its own failures; this backstop
                        // only guarantees the trigger survives to serve the next request.
                        // Retry belongs to the app-start reconcile, not to the trigger.
                    }
                }
        }
    }

    /** Queue a reconcile. Safe to call from any thread; bursts collapse to one pass. */
    fun request() {
        requests.trySend(Unit)
    }
}
