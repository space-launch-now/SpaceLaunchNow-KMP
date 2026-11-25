package me.calebjones.spacelaunchnow.ui.viewmodel

import me.calebjones.spacelaunchnow.test.TestMain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManagerImpl
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.api.snapi.models.PaginatedArticleList
import me.calebjones.spacelaunchnow.cache.LaunchCache
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.ArticlesRepository
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.data.repository.FakeAgencyRepository
import me.calebjones.spacelaunchnow.data.repository.FakeEventsRepository
import me.calebjones.spacelaunchnow.data.repository.FakeLaunchRepository
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import me.calebjones.spacelaunchnow.rating.NotificationRatingTrigger
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LaunchViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        TestMain.install(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        TestMain.reset()
    }

    @Test
    fun fetchUpcomingLaunchesNormal_updatesState_onSuccess() = runTest(dispatcher) {
        val repository = FakeLaunchRepository().apply {
            upcomingLaunchesNormalDomainResult = Result.success(
                DataResult(
                    data = PaginatedResult(
                        count = 1,
                        results = listOf(sampleLaunch(id = "u-1", name = "Upcoming")),
                        next = null,
                        previous = null
                    ),
                    source = DataSource.NETWORK
                )
            )
        }

        val viewModel = LaunchViewModel(
            repository = repository,
            launchCache = LaunchCache(),
            articlesRepository = NoOpArticlesRepository(),
            eventsRepository = FakeEventsRepository(),
            agencyRepository = FakeAgencyRepository(),
            analyticsManager = AnalyticsManagerImpl(emptyList()),
            notificationRatingTrigger = object : NotificationRatingTrigger {
                override suspend fun onLaunchLoaded(launchId: String, statusId: Int?) = Unit
            }
        )

        viewModel.fetchUpcomingLaunchesNormal(limit = 1)
        advanceUntilIdle()

        assertEquals(1, viewModel.upcomingLaunchesNormal.value?.results?.size)
        assertEquals("u-1", viewModel.upcomingLaunchesNormal.value?.results?.firstOrNull()?.id)
    }

    @Test
    fun fetchLaunchDetails_setsError_whenDetailLoadFails() = runTest(dispatcher) {
        val repository = FakeLaunchRepository().apply {
            launchDetailDomainResult = Result.failure(Exception("detail failed"))
        }

        val viewModel = LaunchViewModel(
            repository = repository,
            launchCache = LaunchCache(),
            articlesRepository = NoOpArticlesRepository(),
            eventsRepository = FakeEventsRepository(),
            agencyRepository = FakeAgencyRepository(),
            analyticsManager = AnalyticsManagerImpl(emptyList()),
            notificationRatingTrigger = object : NotificationRatingTrigger {
                override suspend fun onLaunchLoaded(launchId: String, statusId: Int?) = Unit
            }
        )

        viewModel.fetchLaunchDetails("missing")
        advanceUntilIdle()

        assertEquals("detail failed", viewModel.error.value)
    }

    private class RecordingTrigger : NotificationRatingTrigger {
        val calls = mutableListOf<Pair<String, Int?>>()
        override suspend fun onLaunchLoaded(launchId: String, statusId: Int?) {
            calls += launchId to statusId
        }
    }

    @Test
    fun fetchLaunchDetails_callsTrigger_onNetworkSuccess() = runTest(dispatcher) {
        val launch = sampleLaunch(id = "n-1", name = "Network")
        val trigger = RecordingTrigger()
        val viewModel = LaunchViewModel(
            repository = FakeLaunchRepository().apply { launchDetailDomainResult = Result.success(launch) },
            launchCache = LaunchCache(),
            articlesRepository = NoOpArticlesRepository(),
            eventsRepository = FakeEventsRepository(),
            agencyRepository = FakeAgencyRepository(),
            analyticsManager = AnalyticsManagerImpl(emptyList()),
            notificationRatingTrigger = trigger
        )

        viewModel.fetchLaunchDetails("n-1")
        advanceUntilIdle()

        assertEquals(listOf("n-1" to launch.status?.id), trigger.calls)
    }

    @Test
    fun fetchLaunchDetails_callsTrigger_onCacheHit() = runTest(dispatcher) {
        val launch = sampleLaunch(id = "c-1", name = "Cached")
        val cache = LaunchCache().apply { cacheLaunch(launch) }
        val trigger = RecordingTrigger()
        val viewModel = LaunchViewModel(
            repository = FakeLaunchRepository(),
            launchCache = cache,
            articlesRepository = NoOpArticlesRepository(),
            eventsRepository = FakeEventsRepository(),
            agencyRepository = FakeAgencyRepository(),
            analyticsManager = AnalyticsManagerImpl(emptyList()),
            notificationRatingTrigger = trigger
        )

        viewModel.fetchLaunchDetails("c-1")
        advanceUntilIdle()

        assertEquals(listOf("c-1" to launch.status?.id), trigger.calls)
    }
}

private class NoOpArticlesRepository : ArticlesRepository {
    override suspend fun getArticles(limit: Int, forceRefresh: Boolean): Result<DataResult<PaginatedArticleList>> =
        Result.success(DataResult(PaginatedArticleList(count = 0, next = null, previous = null, results = emptyList<Article>()), DataSource.NETWORK))

    override suspend fun getFeaturedArticles(limit: Int): Result<PaginatedArticleList> =
        Result.success(PaginatedArticleList(count = 0, next = null, previous = null, results = emptyList<Article>()))

    override suspend fun getArticlesByLaunch(launchIds: List<String>, limit: Int): Result<PaginatedArticleList> =
        Result.success(PaginatedArticleList(count = 0, next = null, previous = null, results = emptyList<Article>()))

    override suspend fun getArticleById(id: Int): Result<Article> =
        Result.failure(NotImplementedError("Not used in this test"))

    override suspend fun searchArticles(query: String, limit: Int): Result<PaginatedArticleList> =
        Result.success(PaginatedArticleList(count = 0, next = null, previous = null, results = emptyList<Article>()))

    override suspend fun getArticlesPaginated(
        limit: Int,
        offset: Int,
        search: String?,
        newsSites: List<String>?,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedArticleList>> =
        Result.success(DataResult(PaginatedArticleList(count = 0, next = null, previous = null, results = emptyList<Article>()), DataSource.NETWORK))
}
