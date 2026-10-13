package me.calebjones.spacelaunchnow.util

import kotlin.coroutines.cancellation.CancellationException

/**
 * Rethrows this throwable when it is coroutine cancellation; does nothing otherwise.
 *
 * Call it first in any `catch (e: Exception)` that logs or converts the failure, so a scope
 * closing is never reported as a warning or error and the coroutine still unwinds. Cancellation is
 * control flow, not a failure (logging spec rule 4.1a).
 */
fun Throwable.rethrowIfCancellation() {
    if (this is CancellationException) throw this
}
