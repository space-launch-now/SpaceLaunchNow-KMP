package me.calebjones.spacelaunchnow.util.logging

import kotlin.coroutines.cancellation.CancellationException

/**
 * How many links of a cause chain are inspected before giving up.
 *
 * Real wrapped cancellations are one or two links deep; the bound exists so a pathological
 * chain can never turn a log call into an unbounded walk.
 */
const val MAX_CAUSE_CHAIN_DEPTH: Int = 8

/**
 * True when this throwable is coroutine cancellation, or wraps one within
 * [MAX_CAUSE_CHAIN_DEPTH] links of its cause chain.
 *
 * Cancellation is normal control flow, not a defect: a scope closing (screen left, pull to
 * refresh restarting a load) cancels its children, and any `log.e(throwable)` on the way out
 * becomes a Crashlytics non-fatal and a Datadog error event. Remote log writers use this to
 * drop the *report* while keeping the breadcrumb.
 *
 * Matching is on type only — `kotlin.coroutines.cancellation.CancellationException`, which the
 * `kotlinx.coroutines` alias resolves to, and which covers `JobCancellationException` and
 * `ChildCancelledException`. Never match on message text such as "Job was cancelled": messages
 * are not API and real failures can carry them.
 */
fun Throwable?.isCoroutineCancellation(): Boolean {
    if (this == null) return false

    // Identity-tracked so a self-referencing or looping cause chain terminates on the loop
    // rather than only on the depth bound.
    val visited = ArrayList<Throwable>(MAX_CAUSE_CHAIN_DEPTH)
    var current: Throwable? = this
    var depth = 0

    while (current != null && depth < MAX_CAUSE_CHAIN_DEPTH) {
        if (current is CancellationException) return true
        if (visited.any { it === current }) return false
        visited.add(current)
        current = current.cause
        depth++
    }

    return false
}
