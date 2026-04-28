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
import me.calebjones.spacelaunchnow.data.repository.FakeLaunchRepository
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class NextUpViewModelTest {

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
    fun fetchNextLaunch_setsFirstUpcomingLaunch_onSuccess() = runTest(dispatcher) {
        val repository = FakeLaunchRepository().apply {
            upcomingLaunchesNormalDomainResult = Result.success(
                DataResult(
                    data = PaginatedResult(
                        count = 1,
                        results = listOf(sampleLaunch(id = "next-1", name = "Next Launch")),
                        next = null,
                        previous = null
                    ),
                    source = DataSource.NETWORK
                )
            )
        }
        val viewModel = NextUpViewModel(repository)

        viewModel.fetchNextLaunch()
        advanceUntilIdle()

        assertEquals("next-1", viewModel.nextLaunch.value?.id)
        assertNull(viewModel.error.value)
    }

    @Test
    fun fetchNextLaunch_setsError_onFailure() = runTest(dispatcher) {
        val repository = FakeLaunchRepository().apply {
            upcomingLaunchesNormalDomainResult = Result.failure(Exception("boom"))
        }
        val viewModel = NextUpViewModel(repository)

        viewModel.fetchNextLaunch()
        advanceUntilIdle()

        assertEquals("boom", viewModel.error.value)
        assertNull(viewModel.nextLaunch.value)
    }
}
