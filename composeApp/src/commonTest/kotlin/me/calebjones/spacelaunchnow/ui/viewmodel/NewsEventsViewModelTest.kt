package me.calebjones.spacelaunchnow.ui.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManagerImpl
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.FakeArticlesRepository
import me.calebjones.spacelaunchnow.data.repository.FakeEventsRepository
import me.calebjones.spacelaunchnow.data.repository.FakeInfoRepository
import me.calebjones.spacelaunchnow.data.repository.articlePage
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Regression tests for the duplicate-key crash on the News & Events screen (#182 / #179):
 *
 *   IllegalArgumentException: Key "39652" was already used.
 *
 * Both lists are keyed by raw id in NewsEventsScreen, so any duplicate id in the list is a
 * fatal crash during the next LazyColumn measure. Three separate paths could produce one:
 * overlapping offset pages, a double-fired load-more, and a reload racing a load-more.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NewsEventsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== News ==========

    @Test
    fun loadMoreNews_overlappingPage_keepsKeysUnique() = runTest(dispatcher) {
        // Page 2 re-serves article 20, the tail of page 1 — what SNAPI does when the feed
        // gains a row between the two fetches.
        val articles = FakeArticlesRepository().apply {
            pagesByOffset[0] = articlePage(startId = 1, count = 20, next = "page-2")
            pagesByOffset[20] = articlePage(startId = 20, count = 20, next = null)
        }
        val viewModel = createViewModel(articlesRepository = articles)
        advanceUntilIdle()
        assertEquals(20, viewModel.uiState.value.news.size)

        viewModel.loadMoreNews()
        advanceUntilIdle()

        val ids = viewModel.uiState.value.news.map { it.id }
        assertEquals(ids.size, ids.distinct().size, "duplicate article id would crash the LazyColumn")
        assertEquals(39, ids.size, "the one overlapping article should be dropped, not the whole page")
    }

    @Test
    fun loadMoreNews_calledTwiceInSameFrame_fetchesPageOnce() = runTest(dispatcher) {
        val articles = FakeArticlesRepository().apply {
            pagesByOffset[0] = articlePage(startId = 1, count = 20, next = "page-2")
            pagesByOffset[20] = articlePage(startId = 21, count = 20, next = "page-3")
        }
        val viewModel = createViewModel(articlesRepository = articles)
        advanceUntilIdle()

        // A fling crosses the load-more threshold twice before either coroutine runs.
        viewModel.loadMoreNews()
        viewModel.loadMoreNews()
        advanceUntilIdle()

        assertEquals(1, articles.offsetsRequested.count { it == 20 }, "page 2 fetched twice")
        assertEquals(40, viewModel.uiState.value.news.size)
        assertEquals(1, viewModel.uiState.value.newsCurrentPage, "page counter advanced twice")
    }

    @Test
    fun loadNews_whileLoadMoreInFlight_cancelsItAndKeepsPagerConsistent() = runTest(dispatcher) {
        val articles = FakeArticlesRepository().apply {
            pagesByOffset[0] = articlePage(startId = 1, count = 20, next = "page-2")
            pagesByOffset[20] = articlePage(startId = 21, count = 20, next = "page-3")
        }
        val viewModel = createViewModel(articlesRepository = articles)
        advanceUntilIdle()

        // Load-more in flight, then a debounced search / filter toggle resets the list.
        viewModel.loadMoreNews()
        viewModel.loadNews()
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.newsCurrentPage, "page counter left pointing at page 2")
        assertFalse(viewModel.uiState.value.isLoadingMoreNews, "load-more spinner left stuck on")
        assertEquals(20, viewModel.uiState.value.news.size, "page 2 landed on a reset page-0 list")

        // The cancelled job must still have released its guard, or pagination is dead for
        // the rest of the session.
        viewModel.loadMoreNews()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.newsCurrentPage)
        assertEquals(40, viewModel.uiState.value.news.size)
    }

    // ========== Events ==========

    @Test
    fun loadMoreEvents_overlappingPage_keepsKeysUnique() = runTest(dispatcher) {
        val events = FakeEventsRepository().apply {
            eventPagesByOffset[0] = eventPage(startId = 1, count = 20, next = "page-2")
            eventPagesByOffset[20] = eventPage(startId = 20, count = 20, next = null)
        }
        val viewModel = createViewModel(eventsRepository = events)
        advanceUntilIdle()
        assertEquals(20, viewModel.uiState.value.events.size)

        viewModel.loadMoreEvents()
        advanceUntilIdle()

        val ids = viewModel.uiState.value.events.map { it.id }
        assertEquals(ids.size, ids.distinct().size, "duplicate event id would crash the LazyColumn")
        assertEquals(39, ids.size)
    }

    @Test
    fun loadMoreEvents_calledTwiceInSameFrame_fetchesPageOnce() = runTest(dispatcher) {
        val events = FakeEventsRepository().apply {
            eventPagesByOffset[0] = eventPage(startId = 1, count = 20, next = "page-2")
            eventPagesByOffset[20] = eventPage(startId = 21, count = 20, next = "page-3")
        }
        val viewModel = createViewModel(eventsRepository = events)
        advanceUntilIdle()

        viewModel.loadMoreEvents()
        viewModel.loadMoreEvents()
        advanceUntilIdle()

        assertEquals(1, events.eventsPaginatedOffsetsRequested.count { it == 20 }, "page 2 fetched twice")
        assertEquals(40, viewModel.uiState.value.events.size)
        assertEquals(1, viewModel.uiState.value.eventsCurrentPage, "page counter advanced twice")
    }

    @Test
    fun loadEvents_whileLoadMoreInFlight_cancelsItAndKeepsPagerConsistent() = runTest(dispatcher) {
        val events = FakeEventsRepository().apply {
            eventPagesByOffset[0] = eventPage(startId = 1, count = 20, next = "page-2")
            eventPagesByOffset[20] = eventPage(startId = 21, count = 20, next = "page-3")
        }
        val viewModel = createViewModel(eventsRepository = events)
        advanceUntilIdle()

        viewModel.loadMoreEvents()
        viewModel.loadEvents()
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.eventsCurrentPage, "page counter left pointing at page 2")
        assertFalse(viewModel.uiState.value.isLoadingMoreEvents, "load-more spinner left stuck on")
        assertEquals(20, viewModel.uiState.value.events.size, "page 2 landed on a reset page-0 list")

        viewModel.loadMoreEvents()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.eventsCurrentPage)
        assertEquals(40, viewModel.uiState.value.events.size)
    }

    // -- Helpers ----------------------------------------------------------

    private fun createViewModel(
        articlesRepository: FakeArticlesRepository = FakeArticlesRepository(),
        eventsRepository: FakeEventsRepository = FakeEventsRepository()
    ): NewsEventsViewModel = NewsEventsViewModel(
        articlesRepository = articlesRepository,
        eventsRepository = eventsRepository,
        infoRepository = FakeInfoRepository(),
        appPreferences = AppPreferences(InMemoryPreferencesDataStore()),
        analyticsManager = AnalyticsManagerImpl(emptyList())
    )

    private fun eventPage(
        startId: Int,
        count: Int,
        next: String?,
        totalCount: Int = 100
    ) = DataResult(
        data = PaginatedResult(
            count = totalCount,
            next = next,
            previous = null,
            results = (startId until startId + count).map { sampleEvent(id = it, name = "Event $it") }
        ),
        source = DataSource.NETWORK
    )
}
