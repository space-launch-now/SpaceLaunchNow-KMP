package me.calebjones.spacelaunchnow.offer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * Close-button countdown for the holiday offer sheet. The sheet cannot be dismissed until
 * [canClose] is true. With a delay of 0 it is closable at once. [delaySeconds] must
 * already be clamped by [HolidayOfferConfig]; this class does not clamp it.
 */
class OfferCloseCountdown(delaySeconds: Int) {
    val totalMs: Long = delaySeconds * 1000L

    var elapsedMs: Long by mutableLongStateOf(0L)
        private set

    val canClose: Boolean get() = elapsedMs >= totalMs

    /** 0f at the start, 1f when done. */
    val progress: Float
        get() = if (totalMs == 0L) 1f else (elapsedMs.toFloat() / totalMs).coerceIn(0f, 1f)

    fun advance(deltaMs: Long) {
        elapsedMs = (elapsedMs + deltaMs).coerceAtMost(totalMs)
    }

    /** Runs the countdown to the end, in steps of [tickMs]. */
    suspend fun run(tickMs: Long = 50L) {
        while (!canClose) {
            val step = minOf(tickMs, totalMs - elapsedMs)
            delay(step)
            advance(step)
        }
    }
}
