package me.calebjones.spacelaunchnow.ui.ads

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Persisted interstitial pacing state. */
data class InterstitialGateState(
    val visitCount: Int = 0,
    val lastShownAtMs: Long = 0L,
)

/** Durable storage for [InterstitialGateState]. */
interface InterstitialGateStore {
    suspend fun load(): InterstitialGateState
    suspend fun save(state: InterstitialGateState)
}

/**
 * Decides when a detail-view visit should show an interstitial.
 *
 * Shows on every Nth visit once the minimum interval since the last show has passed.
 * The visit count and last-shown time persist, so short sessions still add up across
 * app launches. Callers stay synchronous: state lives in memory, writes go through
 * [store] on [scope], and [restore] merges the saved state in once it is read.
 * Thresholds are read on every call; [refreshThresholds] updates them in the background.
 *
 * Main-thread only: called from composition effects.
 */
class InterstitialGate(
    private val visitsBetween: () -> Int,
    private val minIntervalMs: () -> Long,
    private val now: () -> Long,
    private val store: InterstitialGateStore,
    private val scope: CoroutineScope,
    private val refreshThresholds: suspend () -> Unit = {},
) {
    private var state = InterstitialGateState()

    // Saves wait for [restore] so early visits never overwrite the stored counters.
    private var restored = false

    val visitCount: Int get() = state.visitCount

    /** Counts a visit. Returns true when an interstitial should show for it. */
    fun shouldShow(): Boolean {
        // Remote Config may activate after startup; new values apply from the next visit.
        scope.launch { refreshThresholds() }
        val visits = visitsBetween()
        val current = now()
        val count = state.visitCount + 1
        val dueByCount = visits > 0 && count % visits == 0
        val dueByTime = current - state.lastShownAtMs >= minIntervalMs()
        val show = dueByCount && dueByTime
        update(
            InterstitialGateState(
                visitCount = count,
                lastShownAtMs = if (show) current else state.lastShownAtMs,
            ),
        )
        return show
    }

    fun reset() = update(InterstitialGateState())

    /** Whole minutes since the last show, or 999 when none was recorded. */
    fun minutesSinceLastShown(): Long =
        if (state.lastShownAtMs > 0) (now() - state.lastShownAtMs) / 60_000L else 999L

    /** Merges saved state with visits counted before the read finished. */
    suspend fun restore() {
        val saved = try {
            store.load()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            restored = true
            update(state)
            return
        }
        restored = true
        update(
            InterstitialGateState(
                visitCount = saved.visitCount + state.visitCount,
                lastShownAtMs = maxOf(saved.lastShownAtMs, state.lastShownAtMs),
            ),
        )
    }

    private fun update(next: InterstitialGateState) {
        state = next
        if (!restored) return
        scope.launch {
            try {
                store.save(next)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                // Pacing is best effort; the in-memory state still applies.
            }
        }
    }
}
