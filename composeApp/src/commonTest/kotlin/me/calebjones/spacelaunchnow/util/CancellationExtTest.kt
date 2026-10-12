package me.calebjones.spacelaunchnow.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CancellationExtTest {

    @Test
    fun cancellationExceptionIsRethrownAsTheSameInstance() {
        val cancellation = CancellationException("scope closed")

        val thrown = assertFailsWith<CancellationException> { cancellation.rethrowIfCancellation() }

        assertSame(cancellation, thrown)
    }

    @Test
    fun otherThrowablesAreNotRethrown() {
        IllegalStateException("boom").rethrowIfCancellation()
        RuntimeException("wrapped", CancellationException("inner")).rethrowIfCancellation()
    }

    @Test
    fun catchBlockThatCallsItLetsAJobCancellationUnwind() = runTest {
        var reachedLogLine = false
        val started = CompletableDeferred<Unit>()

        val job = launch {
            try {
                started.complete(Unit)
                kotlinx.coroutines.awaitCancellation()
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                reachedLogLine = true
            }
        }
        started.await()
        job.cancel()
        job.join()

        assertTrue(job.isCancelled)
        assertEquals(false, reachedLogLine)
    }
}
