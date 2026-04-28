package me.calebjones.spacelaunchnow.ui.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManagerImpl
import me.calebjones.spacelaunchnow.data.model.FilterOption
import me.calebjones.spacelaunchnow.data.repository.FakeRocketRepository
import me.calebjones.spacelaunchnow.data.repository.RocketFilterRepository
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.VehicleConfig
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * T161: Unit tests for [RocketViewModel].
 *
 * Exercises the domain-typed RocketRepository contract via [FakeRocketRepository].
 * Covers list loading, detail loading (success + failure), and search paths.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RocketViewModelTest {

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
    fun loadRockets_success_populatesList() = runTest(dispatcher) {
        val repository = FakeRocketRepository().apply {
            rocketsDomainResult = Result.success(
                PaginatedResult(
                    count = 1,
                    next = null,
                    previous = null,
                    results = listOf(sampleRocket(id = 42, name = "Falcon 9"))
                )
            )
        }

        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertTrue(repository.getRocketsDomainCalled)
        assertEquals(1, viewModel.uiState.value.rockets.size)
        assertEquals("Falcon 9", viewModel.uiState.value.rockets.first().name)
        assertEquals(1, viewModel.uiState.value.totalCount)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun loadRockets_failure_setsError() = runTest(dispatcher) {
        val repository = FakeRocketRepository().apply { shouldFail = true }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.rockets.isEmpty())
        assertNotNull(viewModel.uiState.value.error)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun fetchRocketDetails_success_populatesDetails() = runTest(dispatcher) {
        val rocket = sampleRocket(id = 42, name = "Falcon 9")
        val repository = FakeRocketRepository().apply {
            rocketDetailsDomainResult = Result.success(rocket)
        }

        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.fetchRocketDetails(42)
        advanceUntilIdle()

        assertTrue(repository.getRocketDetailsDomainCalled)
        assertEquals(42, repository.lastId)
        assertEquals(rocket, viewModel.rocketDetails.value)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun fetchRocketDetails_failure_setsError() = runTest(dispatcher) {
        val repository = FakeRocketRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        repository.shouldFail = true
        viewModel.fetchRocketDetails(999)
        advanceUntilIdle()

        assertNull(viewModel.rocketDetails.value)
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun updateSearchQuery_triggersReloadWithSearchValue() = runTest(dispatcher) {
        val repository = FakeRocketRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        val initialCalls = repository.getRocketsDomainCallCount

        viewModel.updateSearchQuery("falcon")
        advanceUntilIdle()

        assertTrue(repository.getRocketsDomainCallCount > initialCalls)
        assertEquals("falcon", repository.lastSearch)
    }

    // -- Helpers ----------------------------------------------------------

    private fun createViewModel(repository: FakeRocketRepository): RocketViewModel {
        return RocketViewModel(
            repository = repository,
            filterRepository = NoOpRocketFilterRepository(),
            analyticsManager = AnalyticsManagerImpl(emptyList())
        )
    }

    private fun sampleRocket(id: Int, name: String): VehicleConfig = VehicleConfig(
        id = id,
        name = name,
        fullName = name,
        family = null,
        variant = null,
        imageUrl = null
    )

    private class NoOpRocketFilterRepository : RocketFilterRepository {
        override suspend fun getPrograms(forceRefresh: Boolean): Result<List<FilterOption>> =
            Result.success(emptyList())

        override suspend fun getFamilies(forceRefresh: Boolean): Result<List<FilterOption>> =
            Result.success(emptyList())
    }
}