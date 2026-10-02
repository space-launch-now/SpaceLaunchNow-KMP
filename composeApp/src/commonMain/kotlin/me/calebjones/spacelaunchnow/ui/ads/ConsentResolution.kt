package me.calebjones.spacelaunchnow.ui.ads

import kotlinx.coroutines.delay

/** Result of [awaitCanRequestAds]. */
enum class ConsentPollOutcome { CAN_REQUEST_ADS, TIMED_OUT }

/** Poll interval for the consent fallback. */
const val CONSENT_POLL_INTERVAL_MS = 250L

/** Overall wait before ads are allowed to load anyway (regions with no consent form never wait). */
const val CONSENT_TIMEOUT_MS = 15_000L

/**
 * Fires a "consent resolved" callback at most once, however many UMP callbacks,
 * poll hits or timeouts try to resolve it.
 */
class ConsentResolver(private val onResolved: (() -> Unit)?) {
    var isResolved: Boolean = false
        private set

    /** @return true if this call resolved consent, false if it was already resolved. */
    fun resolve(): Boolean {
        if (isResolved) return false
        isResolved = true
        onResolved?.invoke()
        return true
    }
}

/**
 * UMP's `canRequestAds` is a plain getter, not Compose state, so nothing recomposes when it
 * flips. Poll it instead: returns as soon as it is true, or [TIMED_OUT][ConsentPollOutcome.TIMED_OUT]
 * after [timeoutMs].
 *
 * The timeout only covers UMP never calling back. While [holdTimeoutWhile] is true (a consent
 * form is required, so the user may still be reading it) the clock does not advance: ads must
 * not be requested in a consent region before the user answers.
 */
suspend fun awaitCanRequestAds(
    canRequestAds: () -> Boolean,
    holdTimeoutWhile: () -> Boolean = { false },
    intervalMs: Long = CONSENT_POLL_INTERVAL_MS,
    timeoutMs: Long = CONSENT_TIMEOUT_MS,
): ConsentPollOutcome {
    var waited = 0L
    while (true) {
        if (canRequestAds()) return ConsentPollOutcome.CAN_REQUEST_ADS
        if (waited >= timeoutMs) return ConsentPollOutcome.TIMED_OUT
        delay(intervalMs)
        if (!holdTimeoutWhile()) waited += intervalMs
    }
}
