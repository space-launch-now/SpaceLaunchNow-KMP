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
import me.calebjones.spacelaunchnow.data.repository.FakeEventsRepository
import me.calebjones.spacelaunchnow.data.repository.FakeLaunchRepository
import me.calebjones.spacelaunchnow.data.repository.MockRemoteConfigRepository
import me.calebjones.spacelaunchnow.data.services.LaunchFilterService
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.data.storage.PinnedContentPreferences
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class FeaturedLaunchViewModelTest {

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
    fun loadFeaturedLaunch_setsHeroAndAdditionalLaunches() = runTest(dispatcher) {
        val repository = FakeLaunchRepository().apply {
            featuredLaunchDomainResult = Result.success(
                DataResult(
                    data = PaginatedResult(
                        count = 4,
                        results = listOf(
                            sampleLaunch(id = "f-1", name = "Featured"),
                            sampleLaunch(id = "f-2", name = "A2"),
                            sampleLaunch(id = "f-3", name = "A3"),
                            sampleLaunch(id = "f-4", name = "A4")
                        ),
                        next = null,
                        previous = null
                    ),
                    source = DataSource.NETWORK
                )
            )
        }

        val notificationStorage = NotificationStateStorage(InMemoryPreferencesDataStore())
        val viewModel = FeaturedLaunchViewModel(
            launchRepository = repository,
            eventsRepository = FakeEventsRepository(),
            launchFilterService = LaunchFilterService(),
            notificationStateStorage = notificationStorage,
            launchCache = LaunchCache(),
            remoteConfigRepository = MockRemoteConfigRepository(),
            pinnedContentPreferences = PinnedContentPreferences(InMemoryPreferencesDataStore())
        )

        viewModel.loadFeaturedLaunch()
        advanceUntilIdle()

        assertEquals("f-1", viewModel.featuredLaunchState.value.data?.id)
        assertEquals(3, viewModel.additionalFeaturedLaunches.value.data.size)
        assertEquals("f-2", viewModel.additionalFeaturedLaunches.value.data.first().id)
    }

    @Test
    fun loadFeaturedLaunch_setsError_onFailureFallback() = runTest(dispatcher) {
        val repository = FakeLaunchRepository().apply {
            featuredLaunchDomainResult = Result.failure(Exception("featured failed"))
        }

        val notificationStorage = NotificationStateStorage(InMemoryPreferencesDataStore())
        val viewModel = FeaturedLaunchViewModel(
            launchRepository = repository,
            eventsRepository = FakeEventsRepository(),
            launchFilterService = LaunchFilterService(),
            notificationStateStorage = notificationStorage,
            launchCache = LaunchCache(),
            remoteConfigRepository = MockRemoteConfigRepository(),
            pinnedContentPreferences = PinnedContentPreferences(InMemoryPreferencesDataStore())
        )

        viewModel.loadFeaturedLaunch()
        advanceUntilIdle()

        assertNotNull(viewModel.featuredLaunchState.value.error)
        assertEquals("featured failed", viewModel.featuredLaunchState.value.error)
    }
}
