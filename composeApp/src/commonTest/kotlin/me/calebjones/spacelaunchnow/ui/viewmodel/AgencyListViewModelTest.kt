package me.calebjones.spacelaunchnow.ui.viewmodel

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManagerImpl
import me.calebjones.spacelaunchnow.data.repository.AgencyRepository
import me.calebjones.spacelaunchnow.data.repository.FakeAgencyRepository
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.util.TestSpaceLoggerInit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for [AgencyListViewModel] pagination (issue 211).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AgencyListViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        TestSpaceLoggerInit.ensureInitialized()
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadMore_overlappingPage_doesNotDuplicateIds() = runTest(dispatcher) {
        val repository = FakeAgencyRepository().apply {
            agenciesDomainResult = Result.success(
                PaginatedResult(
                    count = 3,
                    next = "next",
                    previous = null,
                    results = listOf(sampleAgency(1, "One"), sampleAgency(2, "Two"))
                )
            )
        }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        repository.agenciesDomainResult = Result.success(
            PaginatedResult(
                count = 3,
                next = "next",
                previous = null,
                results = listOf(sampleAgency(2, "Two"), sampleAgency(3, "Three"))
            )
        )
        viewModel.loadMore()
        advanceUntilIdle()

        assertEquals(listOf(1, 2, 3), viewModel.uiState.value.agencies.map { it.id })
    }

    @Test
    fun loadAgencies_whileLoadMoreInFlight_cancelsStalePageAndClearsFlag() = runTest(dispatcher) {
        val repository = GatedAgencyRepository()
        val viewModel = AgencyListViewModel(repository, AnalyticsManagerImpl(emptyList()))
        advanceUntilIdle()
        assertEquals(listOf(1, 2), viewModel.uiState.value.agencies.map { it.id })

        viewModel.loadMore()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isLoadingMore)

        // Reload through the funnel directly: copy() alone would leave isLoadingMore set.
        repository.firstPage = listOf(sampleAgency(9, "Nine"))
        viewModel.loadAgencies()
        advanceUntilIdle()
        repository.gate.complete(Unit)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(9), state.agencies.map { it.id })
        assertNull(state.error)
        assertFalse(state.isLoadingMore)
        assertEquals(1, state.currentPage)
    }

    @Test
    fun updateSearchQuery_whileLoadMoreInFlight_cancelsStalePage() = runTest(dispatcher) {
        val repository = GatedAgencyRepository()
        val viewModel = AgencyListViewModel(repository, AnalyticsManagerImpl(emptyList()))
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        repository.firstPage = listOf(sampleAgency(9, "Nine"))
        viewModel.updateSearchQuery("nine")
        advanceUntilIdle()
        repository.gate.complete(Unit)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(9), state.agencies.map { it.id })
        assertNull(state.error)
        assertFalse(state.isLoadingMore)
    }

    // -- Helpers ----------------------------------------------------------

    /** Page 1 returns [firstPage]; any later page suspends on [gate], then fails if cancelled. */
    private inner class GatedAgencyRepository : AgencyRepository {
        var firstPage: List<Agency> = listOf(sampleAgency(1, "One"), sampleAgency(2, "Two"))
        val gate = CompletableDeferred<Unit>()

        override suspend fun getAgenciesDomain(
            limit: Int,
            offset: Int,
            ordering: String?,
            search: String?,
            featured: Boolean?,
            typeId: Int?,
            countryCode: List<String>?
        ): Result<PaginatedResult<Agency>> {
            if (offset == 0) {
                return Result.success(
                    PaginatedResult(count = 40, next = "next", previous = null, results = firstPage)
                )
            }
            // Mirrors AgencyRepositoryImpl: a cancelled fetch surfaces as a failed Result.
            return try {
                gate.await()
                Result.success(
                    PaginatedResult(
                        count = 40,
                        next = null,
                        previous = null,
                        results = listOf(sampleAgency(3, "Three"))
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        override suspend fun searchAgenciesDomain(
            searchQuery: String,
            limit: Int
        ): Result<PaginatedResult<Agency>> = Result.failure(UnsupportedOperationException())

        override suspend fun getAgencyDetailDomain(id: Int): Result<Agency> =
            Result.failure(UnsupportedOperationException())
    }

    private fun createViewModel(repository: FakeAgencyRepository): AgencyListViewModel =
        AgencyListViewModel(
            agencyRepository = repository,
            analyticsManager = AnalyticsManagerImpl(emptyList())
        )

    private fun sampleAgency(id: Int, name: String): Agency = Agency(
        id = id,
        name = name,
        abbrev = null,
        typeName = null,
        countries = emptyList(),
        imageUrl = null,
        logoUrl = null,
        socialLogoUrl = null,
        description = null,
        administrator = null,
        foundingYear = null
    )
}
