package me.calebjones.spacelaunchnow.ui.ads

import kotlin.time.Clock

/**
 * Retry timing for banner handlers that failed to load.
 *
 * State is per handler, not per composable, so a persistent banner (the nav bar) keeps its
 * backoff across recompositions and screens. Retries never stop for the process, but the
 * delay grows fast (1s, 3s, 10s, 30s, 60s, then 120s) so a no-fill never hammers AdMob.
 *
 * Also remembers whether a handler has ever displayed an ad. The library reports FAILING for
 * any failed refresh, even while an earlier creative is still on screen, and the UI uses
 * [hasShown] to keep that creative visible.
 *
 * Main-thread only: called from composition effects.
 */
internal class BannerRetryPolicy(
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) {
    private class Entry(var attempts: Int = 0, var lastAttemptAt: Long = 0L, var shown: Boolean = false)

    private val entries = mutableMapOf<Any, Entry>()

    private fun entry(handler: Any) = entries.getOrPut(handler) { Entry() }

    /**
     * Milliseconds to wait before the next reload. Time already passed since the last attempt
     * counts toward the wait, but the result never drops below [MIN_DELAY_MS].
     */
    fun nextDelayMs(handler: Any): Long {
        val e = entry(handler)
        val backoff = BACKOFF_MS[minOf(e.attempts, BACKOFF_MS.lastIndex)]
        if (e.attempts == 0) return backoff
        val elapsed = now() - e.lastAttemptAt
        return maxOf(backoff - elapsed, MIN_DELAY_MS)
    }

    /** Call right before a retry load starts. */
    fun recordAttempt(handler: Any) {
        val e = entry(handler)
        e.attempts++
        e.lastAttemptAt = now()
    }

    /** Clears the backoff after a successful load. Keeps [hasShown]. */
    fun reset(handler: Any) {
        entry(handler).attempts = 0
    }

    fun markShown(handler: Any) {
        entry(handler).shown = true
    }

    fun hasShown(handler: Any): Boolean = entries[handler]?.shown == true

    companion object {
        const val MIN_DELAY_MS = 1_000L
        private val BACKOFF_MS = longArrayOf(1_000L, 3_000L, 10_000L, 30_000L, 60_000L, 120_000L)

        /** App-wide instance used by the banner composables. */
        val shared = BannerRetryPolicy()
    }
}
