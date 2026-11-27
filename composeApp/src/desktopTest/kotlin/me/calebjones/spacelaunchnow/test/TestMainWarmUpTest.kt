package me.calebjones.spacelaunchnow.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [TestMain.warmUp] is the reason that no view model test leaves a coroutine that resumes from
 * another thread. This test fails when a text it loads still hops threads.
 */
class TestMainWarmUpTest {

    @Test
    fun warmedTexts_resumeTheCallerWithoutLeavingItsThread() {
        runBlocking { TestMain.warmUp() }
        val dispatchThreads = CopyOnWriteArrayList<String>()
        val recorder = object : CoroutineDispatcher() {
            override fun dispatch(context: CoroutineContext, block: Runnable) {
                dispatchThreads += Thread.currentThread().name
                Dispatchers.Default.dispatch(context, block)
            }
        }

        runBlocking {
            withContext(recorder) {
                dispatchThreads.clear()
                TestMain.viewModelTexts.forEach { getString(it) }
            }
        }

        assertEquals(emptyList(), dispatchThreads.toList(), "a warmed text resumed its caller from another thread")
    }
}
