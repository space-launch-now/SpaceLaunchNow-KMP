package me.calebjones.spacelaunchnow.ui.viewmodel

import kotlinx.coroutines.Dispatchers
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
        assertEquals("events failed", viewModel.eventsState.value.error)
    }
}
