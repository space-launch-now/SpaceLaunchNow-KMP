package me.calebjones.spacelaunchnow.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import me.calebjones.spacelaunchnow.util.ErrorKind
import org.jetbrains.compose.resources.getString
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.station_iss_live_stream_description

/**
 * Set and reset `Dispatchers.Main` for a view model test class.
 *
 * `setMain` and `resetMain` throw `Dispatchers.Main is used concurrently with setting it` when
 * another thread reads Main at the same moment. The read comes from a view model coroutine that
 * the previous test class left in flight: the first `getString` of a text (Compose resources)
 * loads it on a `Default` worker and resumes the caller from that worker, so the caller reads
 * Main on the worker thread. Many view models call `getString` on their error path, so a test
 * that ends right after such a failure leaves that resume pending, often just as the next class
 * calls `setMain`. A later `getString` of the same text is served from the cache and does not
 * leave the thread.
 *
 * [install] loads every text that a view model reads in a coroutine before the first `setMain`,
 * so no test leaves a resume from another thread pending. Add a text to [warmUp] when a view
 * model starts to read a new one.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal object TestMain {

    private val warmedUp: Unit by lazy { runBlocking { warmUp() } }

    /** Makes [dispatcher] the Main dispatcher until [reset]. */
    fun install(dispatcher: TestDispatcher) {
        warmedUp
        Dispatchers.setMain(dispatcher)
    }

    fun reset() {
        Dispatchers.resetMain()
    }

    /** Loads each text that a view model reads through `getString`. */
    internal suspend fun warmUp() {
        ErrorKind.entries.forEach { getString(it.text) }
        getString(Res.string.station_iss_live_stream_description)
    }
}
