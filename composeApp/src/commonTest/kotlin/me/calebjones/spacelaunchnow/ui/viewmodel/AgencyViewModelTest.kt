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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * T160: Unit tests for [AgencyViewModel].
 *
 * Exercises the domain-typed [AgencyRepository] contract via [FakeAgencyRepository].
 * Covers list loading, detail loading (success + failure), and search paths.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AgencyViewModelTest {

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
    fun fetchAgencies_success_populatesList() = runTest(dispatcher) {
        val repository = FakeAgencyRepository().apply {
            agenciesDomainResult = Result.success(
                PaginatedResult(
                    count = 1,
                    next = null,
                    previous = null,
                    results = listOf(sampleAgency(id = 121, name = "SpaceX"))
                )
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.fetchAgencies(limit = 20, offset = 0)
        advanceUntilIdle()

        assertTrue(repository.getAgenciesDomainCalled)
        assertEquals(20, repository.lastLimit)
        assertEquals(0, repository.lastOffset)
        assertEquals(1, viewModel.agencies.value.size)
        assertEquals("SpaceX", viewModel.agencies.value.first().name)
        assertNull(viewModel.error.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun fetchAgencies_failure_setsError() = runTest(dispatcher) {
        val repository = FakeAgencyRepository().apply { shouldFail = true }
        val viewModel = createViewModel(repository)

        viewModel.fetchAgencies()
        advanceUntilIdle()

        assertTrue(viewModel.agencies.value.isEmpty())
        assertNotNull(viewModel.error.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun fetchAgencyDetails_success_populatesDetails() = runTest(dispatcher) {
        val agency = sampleAgency(id = 121, name = "SpaceX")
        val repository = FakeAgencyRepository().apply {
            agencyDetailDomainResult = Result.success(agency)
        }

        val viewModel = createViewModel(repository)
        viewModel.fetchAgencyDetails(121)
        advanceUntilIdle()

        assertTrue(repository.getAgencyDetailDomainCalled)
        assertEquals(121, repository.lastDetailId)
        assertEquals(agency, viewModel.agencyDetails.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun fetchAgencyDetails_failure_setsError() = runTest(dispatcher) {
        val repository = FakeAgencyRepository().apply { shouldFail = true }
        val viewModel = createViewModel(repository)

        viewModel.fetchAgencyDetails(999)
        advanceUntilIdle()

        assertNull(viewModel.agencyDetails.value)
        assertNotNull(viewModel.error.value)
    }

    @Test
    fun searchAgencies_success_updatesList() = runTest(dispatcher) {
        val repository = FakeAgencyRepository().apply {
            searchAgenciesDomainResult = Result.success(
                PaginatedResult(
                    count = 1,
                    next = null,
                    previous = null,
                    results = listOf(sampleAgency(id = 121, name = "SpaceX"))
                )
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.searchAgencies(query = "spacex", limit = 25)
        advanceUntilIdle()

        assertTrue(repository.searchAgenciesDomainCalled)
        assertEquals("spacex", repository.lastSearchQuery)
        assertEquals(25, repository.lastLimit)
        assertEquals(1, viewModel.agencies.value.size)
        assertNull(viewModel.error.value)
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private fun createViewModel(repository: FakeAgencyRepository): AgencyViewModel {
        return AgencyViewModel(
            agencyRepository = repository,
            analyticsManager = AnalyticsManagerImpl(emptyList())
        )
    }

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
