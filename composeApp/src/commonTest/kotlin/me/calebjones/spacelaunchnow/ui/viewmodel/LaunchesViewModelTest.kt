package me.calebjones.spacelaunchnow.ui.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.calebjones.spacelaunchnow.cache.LaunchCache
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.repository.FakeLaunchRepository
import me.calebjones.spacelaunchnow.data.services.LaunchFilterService
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LaunchesViewModelTest {

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
    fun loadLaunches_populatesUpcomingAndPreviousLists() = runTest(dispatcher) {
        val repository = FakeLaunchRepository().apply {
            upcomingLaunchesNormalDomainResult = Result.success(
                DataResult(
                    data = PaginatedResult(
                        count = 1,
                        results = listOf(sampleLaunch(id = "up-a", name = "Upcoming A")),
                        next = null,
                        previous = null
                    ),
                    source = DataSource.NETWORK
                )
            )
            previousLaunchesNormalDomainResult = Result.success(
                DataResult(
                    data = PaginatedResult(
                        count = 1,
                        results = listOf(sampleLaunch(id = "prev-a", name = "Previous A")),
                        next = null,
                        previous = null
                    ),
                    source = DataSource.NETWORK
                )
            )
        }

        val notificationStorage = NotificationStateStorage(InMemoryPreferencesDataStore())
        val viewModel = LaunchesViewModel(
            launchRepository = repository,
            launchFilterService = LaunchFilterService(),
            notificationStateStorage = notificationStorage,
            launchCache = LaunchCache()
        )

        viewModel.loadLaunches(upcomingLimit = 1, previousLimit = 1)
        advanceUntilIdle()

        assertEquals(1, viewModel.upcomingLaunchesState.value.data.size)
        assertEquals(1, viewModel.previousLaunchesState.value.data.size)
        assertEquals("up-a", viewModel.upcomingLaunchesState.value.data.first().id)
        assertEquals("prev-a", viewModel.previousLaunchesState.value.data.first().id)
    }

    @Test
    fun loadLaunches_appliesNotificationFiltersToRepositoryCalls() = runTest(dispatcher) {
        val repository = FakeLaunchRepository().apply {
            upcomingLaunchesNormalDomainResult = Result.success(
                DataResult(PaginatedResult(count = 0, results = emptyList(), next = null, previous = null), DataSource.NETWORK)
            )
            previousLaunchesNormalDomainResult = Result.success(
                DataResult(PaginatedResult(count = 0, results = emptyList(), next = null, previous = null), DataSource.NETWORK)
            )
        }

        val notificationStorage = NotificationStateStorage(InMemoryPreferencesDataStore())
        notificationStorage.saveState(
            NotificationState.DEFAULT.copy(
                followAllLaunches = false,
                subscribedAgencies = setOf("121"),
                subscribedLocations = setOf("12")
            )
        )

        val viewModel = LaunchesViewModel(
            launchRepository = repository,
            launchFilterService = LaunchFilterService(),
            notificationStateStorage = notificationStorage,
            launchCache = LaunchCache()
        )

        viewModel.loadLaunches(upcomingLimit = 1, previousLimit = 1)
        advanceUntilIdle()

        assertNotNull(repository.lastUpcomingAgencyIds)
        assertNotNull(repository.lastUpcomingLocationIds)
        assertTrue(repository.lastUpcomingAgencyIds!!.contains(121))
        assertTrue(repository.lastPreviousAgencyIds!!.contains(121))
    }
}
