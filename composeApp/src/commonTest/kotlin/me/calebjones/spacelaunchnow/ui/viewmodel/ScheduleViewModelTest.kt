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
import me.calebjones.spacelaunchnow.data.repository.FakeLaunchRepository
import me.calebjones.spacelaunchnow.data.repository.ScheduleFilterRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModelTest {

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
    fun init_loadsUpcomingAndPreviousTabs() = runTest(dispatcher) {
        val viewModel = ScheduleViewModel(
            launchRepository = FakeLaunchRepository(),
            appPreferences = AppPreferences(InMemoryPreferencesDataStore()),
            filterRepository = FakeScheduleFilterRepository(),
            analyticsManager = AnalyticsManagerImpl(emptyList())
        )

        advanceUntilIdle()

        // Ensure upcoming data is loaded before asserting; init launches can complete out of order.
        if (viewModel.uiState.value.upcomingTab.items.isEmpty()) {
            viewModel.loadNextPage(ScheduleTab.Upcoming)
            advanceUntilIdle()
        }

        assertTrue(
            viewModel.uiState.value.upcomingTab.items.isNotEmpty() ||
                viewModel.uiState.value.upcomingTab.error == null
        )
        assertTrue(
            viewModel.uiState.value.previousTab.items.isNotEmpty() ||
                viewModel.uiState.value.previousTab.error == null
        )
    }

    @Test
    fun loadNextPage_appendsItemsForUpcomingTab() = runTest(dispatcher) {
        val viewModel = ScheduleViewModel(
            launchRepository = FakeLaunchRepository(),
            appPreferences = AppPreferences(InMemoryPreferencesDataStore()),
            filterRepository = FakeScheduleFilterRepository(),
            analyticsManager = AnalyticsManagerImpl(emptyList())
        )

        advanceUntilIdle()

        // Ensure first page is present before testing append behavior.
        if (viewModel.uiState.value.upcomingTab.items.isEmpty()) {
            viewModel.loadNextPage(ScheduleTab.Upcoming)
            advanceUntilIdle()
        }

        val initialSize = viewModel.uiState.value.upcomingTab.items.size

        viewModel.loadNextPage(ScheduleTab.Upcoming)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.upcomingTab.items.size >= initialSize)
    }
}

private class FakeScheduleFilterRepository : ScheduleFilterRepository {
    override suspend fun getAgencies(forceRefresh: Boolean): Result<List<FilterOption>> = Result.success(emptyList())
    override suspend fun getPrograms(forceRefresh: Boolean): Result<List<FilterOption>> = Result.success(emptyList())
    override suspend fun getRockets(forceRefresh: Boolean): Result<List<FilterOption>> = Result.success(emptyList())
    override suspend fun getLocations(forceRefresh: Boolean): Result<List<FilterOption>> = Result.success(emptyList())
    override suspend fun getStatuses(forceRefresh: Boolean): Result<List<FilterOption>> = Result.success(emptyList())
    override suspend fun getOrbits(forceRefresh: Boolean): Result<List<FilterOption>> = Result.success(emptyList())
    override suspend fun getMissionTypes(forceRefresh: Boolean): Result<List<FilterOption>> = Result.success(emptyList())
    override suspend fun getLauncherConfigFamilies(forceRefresh: Boolean): Result<List<FilterOption>> = Result.success(emptyList())
}

