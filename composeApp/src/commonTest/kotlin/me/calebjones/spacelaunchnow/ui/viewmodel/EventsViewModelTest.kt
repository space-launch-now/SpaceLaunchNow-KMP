package me.calebjones.spacelaunchnow.ui.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.FakeEventsRepository
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import org.jetbrains.compose.resources.getString
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.common_unknown_error
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class EventsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadEvents_updatesState_onSuccess() = runTest(dispatcher) {
        val repository = FakeEventsRepository().apply {
            upcomingEventsDomainResult = Result.success(
                DataResult(
                    data = PaginatedResult(
                        count = 1,
                        results = listOf(sampleEvent(id = 7, name = "Static Fire")),
                        next = null,
                        previous = null
                    ),
                    source = DataSource.NETWORK
                )
            )
        }
        val viewModel = EventsViewModel(repository)

        viewModel.loadEvents(limit = 1)
        advanceUntilIdle()

        assertEquals(1, viewModel.eventsState.value.data.size)
        assertEquals("Static Fire", viewModel.eventsState.value.data.first().name)
        assertEquals(false, viewModel.eventsState.value.isLoading)
    }

    @Test
    fun refresh_setsUserInitiatedAndError_onFailure() = runTest(dispatcher) {
        val repository = FakeEventsRepository().apply {
            upcomingEventsDomainResult = Result.failure(Exception("events failed"))
        }
        val viewModel = EventsViewModel(repository)

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(true, viewModel.eventsState.value.isUserInitiated)
        // The text loads from a resource off the test dispatcher, so wait for it.
        // The raw exception text must not reach the screen; the fixed "unknown" text does.
        val error = withContext(Dispatchers.Default) {
            withTimeout(5_000) { viewModel.eventsState.first { it.error != null }.error }
        }
        assertEquals(getString(Res.string.common_unknown_error), error)
    }
}
