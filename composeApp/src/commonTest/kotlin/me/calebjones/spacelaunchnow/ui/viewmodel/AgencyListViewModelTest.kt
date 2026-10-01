package me.calebjones.spacelaunchnow.ui.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManagerImpl
import me.calebjones.spacelaunchnow.data.repository.FakeAgencyRepository
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.util.TestSpaceLoggerInit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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

    // -- Helpers ----------------------------------------------------------

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
