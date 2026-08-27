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
import kotlin.test.assertTrue

/**
 * Regression tests for the duplicate-key crash on the News & Events screen (#182 / #179):
 *
 *   IllegalArgumentException: Key "39652" was already used.
 *
 * Both lists are keyed by raw id in NewsEventsScreen, so any duplicate id in the list is a
 * fatal crash during the next LazyColumn measure. Three separate paths could produce one:
 * overlapping offset pages, a double-fired load-more, and a reload racing a load-more.
 *
 * Every test about two loads overlapping holds the first fetch open with a
 * `CompletableDeferred` gate on the fake repository. That is load-bearing, not decoration: a
 * fake that returns without suspending lets `advanceUntilIdle()` run each load to completion
 * before the next one starts, so the interleaving under test never happens and the test
 * passes against the unfixed ViewModel. Assertions marked "precondition" exist to fail loudly
 * if a gate ever stops holding.
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
        val page2 = CompletableDeferred<Unit>()
        val articles = FakeArticlesRepository().apply {
            pagesByOffset[0] = articlePage(startId = 1, count = 20, next = "page-2")
            pagesByOffset[20] = articlePage(startId = 21, count = 20, next = "page-3")
            gatesByOffset[20] = page2
        }
        val viewModel = createViewModel(articlesRepository = articles)
        advanceUntilIdle()

        // A fling crosses the load-more threshold twice before either coroutine runs. The
        // gate then holds both fetches open together, so a second call that got through the
        // guard really does fetch and append page 2 a second time.
        viewModel.loadMoreNews()
        viewModel.loadMoreNews()
        advanceUntilIdle()

        assertEquals(1, articles.offsetsRequested.count { it == 20 }, "page 2 was fetched twice")

        page2.complete(Unit)
        advanceUntilIdle()

        val ids = viewModel.uiState.value.news.map { it.id }
        assertEquals(ids.size, ids.distinct().size, "page 2 was appended twice, duplicating keys")
        assertEquals(40, ids.size)
        assertEquals(1, viewModel.uiState.value.newsCurrentPage, "page counter advanced twice")
    }

    @Test
    fun loadNews_whileLoadMoreInFlight_cancelsItAndKeepsPagerConsistent() = runTest(dispatcher) {
        val page2 = CompletableDeferred<Unit>()
        val articles = FakeArticlesRepository().apply {
            pagesByOffset[0] = articlePage(startId = 1, count = 20, next = "page-2")
            pagesByOffset[20] = articlePage(startId = 21, count = 20, next = "page-3")
            gatesByOffset[20] = page2
        }
        val viewModel = createViewModel(articlesRepository = articles)
        advanceUntilIdle()
        // Startup issues two page-0 loads — the saved-filter restore and the debounced empty
        // search — so start counting fetches from a clean slate.
        articles.offsetsRequested.clear()

        // Page 2 is requested and then held open, so the load-more is genuinely suspended
        // mid-flight when the reload arrives.
        viewModel.loadMoreNews()
        advanceUntilIdle()
        assertEquals(listOf(20), articles.offsetsRequested, "precondition: page 2 is in flight")
        assertTrue(viewModel.uiState.value.isLoadingMoreNews, "precondition: load-more has started")

        // A debounced search / filter toggle resets the list while page 2 is still out.
        viewModel.loadNews()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoadingMoreNews, "load-more flag left stuck on, stalling pagination")

        // Release page 2 — the cancelled fetch must not land on the reset list.
        page2.complete(Unit)
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.newsCurrentPage, "page counter left pointing at page 2")
        assertEquals(20, viewModel.uiState.value.news.size, "page 2 landed on a reset page-0 list")

        // The cancelled job must still have released its guard, or pagination is dead for
        // the rest of the session.
        viewModel.loadMoreNews()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.newsCurrentPage)
        assertEquals(40, viewModel.uiState.value.news.size)
    }

    @Test
    fun loadNews_whenAnotherReloadHoldsTheLock_stillClearsLoadMoreFlag() = runTest(dispatcher) {
        val page2 = CompletableDeferred<Unit>()
        val reload = CompletableDeferred<Unit>()
        val articles = FakeArticlesRepository().apply {
            pagesByOffset[0] = articlePage(startId = 1, count = 20, next = "page-2")
            pagesByOffset[20] = articlePage(startId = 21, count = 20, next = "page-3")
        }
        val viewModel = createViewModel(articlesRepository = articles)
        advanceUntilIdle()

        // First reload takes newsLoadMutex and parks inside the repository.
        articles.gatesByOffset[0] = reload
        articles.gatesByOffset[20] = page2
        viewModel.loadNews()
        advanceUntilIdle()

        viewModel.loadMoreNews()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isLoadingMoreNews, "precondition: load-more has started")

        // Second reload cancels that load-more and then bails on tryLock() — the flag has to
        // be cleared anyway, because NewsEventsScreen gates load-more on it.
        viewModel.loadNews()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoadingMoreNews, "reload that bailed on the lock left the flag set")

        reload.complete(Unit)
        page2.complete(Unit)
        advanceUntilIdle()

        // And pagination still works afterwards.
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
        val page2 = CompletableDeferred<Unit>()
        val events = FakeEventsRepository().apply {
            eventPagesByOffset[0] = eventPage(startId = 1, count = 20, next = "page-2")
            eventPagesByOffset[20] = eventPage(startId = 21, count = 20, next = "page-3")
            eventGatesByOffset[20] = page2
        }
        val viewModel = createViewModel(eventsRepository = events)
        advanceUntilIdle()

        viewModel.loadMoreEvents()
        viewModel.loadMoreEvents()
        advanceUntilIdle()

        assertEquals(1, events.eventsPaginatedOffsetsRequested.count { it == 20 }, "page 2 was fetched twice")

        page2.complete(Unit)
        advanceUntilIdle()

        val ids = viewModel.uiState.value.events.map { it.id }
        assertEquals(ids.size, ids.distinct().size, "page 2 was appended twice, duplicating keys")
        assertEquals(40, ids.size)
        assertEquals(1, viewModel.uiState.value.eventsCurrentPage, "page counter advanced twice")
    }

    @Test
    fun loadEvents_whileLoadMoreInFlight_cancelsItAndKeepsPagerConsistent() = runTest(dispatcher) {
        val page2 = CompletableDeferred<Unit>()
        val events = FakeEventsRepository().apply {
            eventPagesByOffset[0] = eventPage(startId = 1, count = 20, next = "page-2")
            eventPagesByOffset[20] = eventPage(startId = 21, count = 20, next = "page-3")
            eventGatesByOffset[20] = page2
        }
        val viewModel = createViewModel(eventsRepository = events)
        advanceUntilIdle()
        // See the news twin: startup issues two page-0 loads.
        events.eventsPaginatedOffsetsRequested.clear()

        viewModel.loadMoreEvents()
        advanceUntilIdle()
        assertEquals(
            listOf(20),
            events.eventsPaginatedOffsetsRequested,
            "precondition: page 2 is in flight"
        )
        assertTrue(viewModel.uiState.value.isLoadingMoreEvents, "precondition: load-more has started")

        viewModel.loadEvents()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoadingMoreEvents, "load-more flag left stuck on, stalling pagination")

        page2.complete(Unit)
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.eventsCurrentPage, "page counter left pointing at page 2")
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
