package me.calebjones.spacelaunchnow.util.logging

import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CancellationFilteringTest {

    @Test
    fun nullThrowable_isNotCancellation() {
        val absent: Throwable? = null
        assertFalse(absent.isCoroutineCancellation())
    }

    /**
     * Regression guard for the Debug Settings "Send Non-Fatal Exception" button
     * (DebugSettingsScreen.kt), which throws a RuntimeException and must keep reporting.
     */
    @Test
    fun plainRuntimeException_isNotCancellation() {
        assertFalse(RuntimeException("Non-fatal test exception from Debug Settings").isCoroutineCancellation())
        assertFalse(IllegalStateException("Job was cancelled").isCoroutineCancellation())
    }

    @Test
    fun cancellationException_isCancellation() {
        assertTrue(CancellationException("cancelled").isCoroutineCancellation())
    }

    /** JobCancellationException and ChildCancelledException reach us as subtypes. */
    @Test
    fun cancellationSubclass_isCancellation() {
        assertTrue(FakeJobCancellation("Job was cancelled").isCoroutineCancellation())
    }

    @Test
    fun wrappedCancellation_isCancellation() {
        val wrapped = IllegalStateException("repository load failed", CancellationException("cancelled"))
        assertTrue(wrapped.isCoroutineCancellation())
    }

    @Test
    fun cancellationAtDepthLimit_isCancellation() {
        val chain = wrap(MAX_CAUSE_CHAIN_DEPTH - 1, CancellationException("cancelled"))
        assertTrue(chain.isCoroutineCancellation())
    }

    @Test
    fun cancellationBeyondDepthLimit_isNotCancellation() {
        val chain = wrap(MAX_CAUSE_CHAIN_DEPTH, CancellationException("cancelled"))
        assertFalse(chain.isCoroutineCancellation())
    }

    @Test
    fun selfReferencingCause_terminates() {
        val looping = LoopingThrowable("self")
        looping.link = looping
        assertFalse(looping.isCoroutineCancellation())
    }

    @Test
    fun cyclicCauseChain_terminates() {
        val first = LoopingThrowable("first")
        val second = LoopingThrowable("second")
        first.link = second
        second.link = first
        assertFalse(first.isCoroutineCancellation())
    }

    /** Proves the cycle fixtures above are actually walked, rather than passing vacuously. */
    @Test
    fun loopingThrowable_followsOverriddenCause() {
        val looping = LoopingThrowable("wrapper")
        looping.link = CancellationException("cancelled")
        assertTrue(looping.isCoroutineCancellation())
    }

    /** Nests [root] under [levels] wrapper exceptions. */
    private fun wrap(levels: Int, root: Throwable): Throwable {
        var current = root
        repeat(levels) { index ->
            current = IllegalStateException("wrapper $index", current)
        }
        return current
    }

    private class FakeJobCancellation(message: String) : CancellationException(message)

    private class LoopingThrowable(message: String) : RuntimeException(message) {
        var link: Throwable? = null
        override val cause: Throwable?
            get() = link
    }
}
