package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.ArticlesRepository
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.data.repository.InfoRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.domain.model.EventType
import me.calebjones.spacelaunchnow.ui.newsevents.NewsEventsFilterState
import me.calebjones.spacelaunchnow.util.logging.isCoroutineCancellation
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * ViewModel for the combined News and Events screen.
 *
 * Manages two tabs:
 * - News: Articles from SNAPI with pagination, search, and news site filtering
 * - Events: Space events from Launch Library with pagination, search, and type filtering
 *
 * Follows the AgencyListViewModel pattern for pagination and state management.
 */
class NewsEventsViewModel(
    private val articlesRepository: ArticlesRepository,
    private val eventsRepository: EventsRepository,
    private val infoRepository: InfoRepository,
    private val appPreferences: AppPreferences,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 20
        private const val SEARCH_DEBOUNCE_MS = 300L
    }

    private val log = logger()
    private val newsLoadMutex = Mutex()
    private val eventsLoadMutex = Mutex()

    // Guards against two scroll-driven load-more calls in the same frame both passing the
    // in-flight check and fetching (then appending) the same page twice.
    private val newsLoadMoreMutex = Mutex()
    private val eventsLoadMoreMutex = Mutex()

    // In-flight load-more jobs, cancelled when the list is reset by a reload so a page-N
    // response can never land on a freshly reset page-0 list.
    private var newsLoadMoreJob: Job? = null
    private var eventsLoadMoreJob: Job? = null

    private val _searchInput = MutableStateFlow("")
    private val _uiState = MutableStateFlow(NewsEventsUiState())
    val uiState: StateFlow<NewsEventsUiState> = _uiState.asStateFlow()

    init {
        log.i { "NewsEventsViewModel initialized" }
        loadSavedFilters()
        loadNewsSites()
        loadEventTypes()
        observeSearchInput()
    }

    // ========== Analytics ==========

    fun trackArticleClicked(articleId: String, newsSite: String, url: String) {
        analyticsManager.track(
            AnalyticsEvent.ThirdPartyReferral(
                provider = newsSite,
                url = url,
                contentType = "news_article",
                contentId = articleId
            )
        )
    }

    /**
     * Load persisted filters, then trigger initial data loads.
     */
    private fun loadSavedFilters() {
        viewModelScope.launch {
            val saved = appPreferences.getNewsEventsFilterState()
            _uiState.update {
                it.copy(
                    selectedNewsSites = saved.selectedNewsSites,
                    selectedEventTypeIds = saved.selectedEventTypeIds
                )
            }
            loadNews()
            loadEvents()
        }
    }

    private fun persistFilters() {
        viewModelScope.launch {
            val state = _uiState.value
            appPreferences.updateNewsEventsFilterState(
                NewsEventsFilterState(
                    selectedNewsSites = state.selectedNewsSites,
                    selectedEventTypeIds = state.selectedEventTypeIds
                )
            )
        }
    }

    // ========== Tab Management ==========

    /**
     * Switch to the specified tab and load data if needed.
     */
    fun selectTab(tab: NewsEventsTab) {
        log.i { "Tab selected: $tab" }
        _uiState.update { it.copy(selectedTab = tab) }
        
        when (tab) {
            NewsEventsTab.NEWS -> if (_uiState.value.news.isEmpty()) loadNews()
            NewsEventsTab.EVENTS -> if (_uiState.value.events.isEmpty()) loadEvents()
        }
    }

    // ========== News Loading ==========

    /**
     * Load the first page of news articles.
     */
    fun loadNews(forceRefresh: Boolean = false) {
        // A reload resets the page counter and replaces the list. Any load-more still in
        // flight was issued against the old counter, so letting it complete would append a
        // page-N slice onto a page-0 list — duplicate keys plus a page counter that then
        // requests wrong offsets forever.
        //
        // The flag is cleared here, beside the cancel, rather than inside the coroutine: the
        // update below sits after a tryLock() bail, so a concurrent reload would return
        // having cancelled the load-more without ever clearing it. NewsEventsScreen gates
        // load-more on isLoadingMoreNews, so a stuck flag stalls pagination, not just the
        // spinner.
        newsLoadMoreJob?.cancel()
        newsLoadMoreJob = null
        _uiState.update { it.copy(isLoadingMoreNews = false) }

        viewModelScope.launch {
            if (!newsLoadMutex.tryLock()) {
                log.w { "Already loading news, skipping duplicate call" }
                return@launch
            }

            try {
                _uiState.update {
                    it.copy(
                        isLoadingNews = true,
                        newsError = null,
                        newsCurrentPage = 0,
                        news = if (forceRefresh) emptyList() else it.news
                    )
                }

                val result = articlesRepository.getArticlesPaginated(
                    limit = PAGE_SIZE,
                    offset = 0,
                    search = _uiState.value.searchQuery.takeIf { it.isNotBlank() },
                    newsSites = _uiState.value.selectedNewsSites.takeIf { it.isNotEmpty() },
                    forceRefresh = forceRefresh
                )

                result.onSuccess { dataResult ->
                    log.i { "Loaded ${dataResult.data.results.size} news articles (total: ${dataResult.data.count})" }
                    _uiState.update {
                        it.copy(
                            news = dataResult.data.results,
                            isLoadingNews = false,
                            newsHasMore = dataResult.data.next != null,
                            newsCurrentPage = 0,
                            newsTotalCount = dataResult.data.count,
                            newsDataSource = dataResult.source
                        )
                    }
                }.onFailure { exception ->
                    log.e { "Failed to load news: ${exception.message}" }
                    _uiState.update {
                        it.copy(
                            newsError = exception.message ?: "Failed to load news",
                            isLoadingNews = false
                        )
                    }
                }
            } finally {
                newsLoadMutex.unlock()
            }
        }
    }

    /**
     * Load the next page of news articles.
     */
    fun loadMoreNews() {
        if (!_uiState.value.newsHasMore) {
            return
        }

        // tryLock() at the call site, not inside the coroutine: a fling fires the load-more
        // threshold twice in the same frame, and both calls would otherwise pass the
        // in-flight check before either had a chance to set it.
        if (!newsLoadMoreMutex.tryLock()) {
            log.w { "Already loading more news, skipping duplicate call" }
            return
        }

        val job = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMoreNews = true) }

            val nextPage = _uiState.value.newsCurrentPage + 1
            val offset = nextPage * PAGE_SIZE

            val result = articlesRepository.getArticlesPaginated(
                limit = PAGE_SIZE,
                offset = offset,
                search = _uiState.value.searchQuery.takeIf { it.isNotBlank() },
                newsSites = _uiState.value.selectedNewsSites.takeIf { it.isNotEmpty() },
                forceRefresh = false
            )

            result.onSuccess { dataResult ->
                log.i { "Loaded page $nextPage: ${dataResult.data.results.size} more articles" }
                _uiState.update {
                    it.copy(
                        // Offset pagination over a feed that gains rows while it is being
                        // read re-serves items across page boundaries. Deduplicate by ID to
                        // prevent duplicate key errors in the LazyColumn.
                        news = (it.news + dataResult.data.results).distinctBy { article -> article.id },
                        isLoadingMoreNews = false,
                        newsHasMore = dataResult.data.next != null,
                        newsCurrentPage = nextPage
                    )
                }
            }.onFailure { exception ->
                // A reload cancelled this fetch, and it already owns the list state — the
                // flag included, cleared beside the cancel in loadNews(). Cancellation arrives
                // here rather than as a throw because the repositories catch Exception, which
                // CancellationException extends; without this guard it would surface as
                // "Job was cancelled" in newsError.
                if (exception.isCoroutineCancellation()) return@onFailure
                log.e { "Failed to load more news: ${exception.message}" }
                _uiState.update {
                    it.copy(
                        newsError = exception.message ?: "Failed to load more news",
                        isLoadingMoreNews = false
                    )
                }
            }
        }

        // Released on completion rather than in a finally block: loadNews() can cancel this
        // job before its body ever runs, and a finally would never fire — leaking the lock
        // and killing pagination for the rest of the session.
        job.invokeOnCompletion { newsLoadMoreMutex.unlock() }
        newsLoadMoreJob = job
    }

    /**
     * Refresh news articles (user-initiated pull-to-refresh).
     */
    fun refreshNews() {
        loadNews(forceRefresh = true)
    }

    // ========== Events Loading ==========

    /**
     * Load the first page of events.
     */
    fun loadEvents(forceRefresh: Boolean = false) {
        // See loadNews(): an in-flight load-more targets the pre-reset page counter, and the
        // flag clear belongs beside the cancel, above the tryLock() bail.
        eventsLoadMoreJob?.cancel()
        eventsLoadMoreJob = null
        _uiState.update { it.copy(isLoadingMoreEvents = false) }

        viewModelScope.launch {
            if (!eventsLoadMutex.tryLock()) {
                log.w { "Already loading events, skipping duplicate call" }
                return@launch
            }

            try {
                _uiState.update {
                    it.copy(
                        isLoadingEvents = true,
                        eventsError = null,
                        eventsCurrentPage = 0,
                        events = if (forceRefresh) emptyList() else it.events
                    )
                }

                val result = eventsRepository.getEventsPaginatedDomain(
                    limit = PAGE_SIZE,
                    offset = 0,
                    search = _uiState.value.searchQuery.takeIf { it.isNotBlank() },
                    typeIds = _uiState.value.selectedEventTypeIds.takeIf { it.isNotEmpty() },
                    upcoming = true,
                    forceRefresh = forceRefresh
                )

                result.onSuccess { dataResult ->
                    log.i { "Loaded ${dataResult.data.results.size} events (total: ${dataResult.data.count})" }
                    _uiState.update {
                        it.copy(
                            events = dataResult.data.results,
                            isLoadingEvents = false,
                            eventsHasMore = dataResult.data.next != null,
                            eventsCurrentPage = 0,
                            eventsTotalCount = dataResult.data.count,
                            eventsDataSource = dataResult.source
                        )
                    }
                }.onFailure { exception ->
                    log.e { "Failed to load events: ${exception.message}" }
                    _uiState.update {
                        it.copy(
                            eventsError = exception.message ?: "Failed to load events",
                            isLoadingEvents = false
                        )
                    }
                }
            } finally {
                eventsLoadMutex.unlock()
            }
        }
    }

    /**
     * Load the next page of events.
     */
    fun loadMoreEvents() {
        if (!_uiState.value.eventsHasMore) {
            return
        }

        // See loadMoreNews(): the lock is taken at the call site so a double-fire within a
        // single fling frame cannot fetch and append the same page twice.
        if (!eventsLoadMoreMutex.tryLock()) {
            log.w { "Already loading more events, skipping duplicate call" }
            return
        }

        val job = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMoreEvents = true) }

            val nextPage = _uiState.value.eventsCurrentPage + 1
            val offset = nextPage * PAGE_SIZE

            val result = eventsRepository.getEventsPaginatedDomain(
                limit = PAGE_SIZE,
                offset = offset,
                search = _uiState.value.searchQuery.takeIf { it.isNotBlank() },
                typeIds = _uiState.value.selectedEventTypeIds.takeIf { it.isNotEmpty() },
                upcoming = true,
                forceRefresh = false
            )

            result.onSuccess { dataResult ->
                log.i { "Loaded page $nextPage: ${dataResult.data.results.size} more events" }
                _uiState.update {
                    it.copy(
                        // Same offset-pagination overlap as news — deduplicate by ID.
                        events = (it.events + dataResult.data.results).distinctBy { event -> event.id },
                        isLoadingMoreEvents = false,
                        eventsHasMore = dataResult.data.next != null,
                        eventsCurrentPage = nextPage
                    )
                }
            }.onFailure { exception ->
                // See loadMoreNews(): a cancelled page fetch is not a UI error, and loadEvents()
                // already owns the state it was cancelled for.
                if (exception.isCoroutineCancellation()) return@onFailure
                log.e { "Failed to load more events: ${exception.message}" }
                _uiState.update {
                    it.copy(
                        eventsError = exception.message ?: "Failed to load more events",
                        isLoadingMoreEvents = false
                    )
                }
            }
        }

        // See loadMoreNews(): unlock on completion so a cancel-before-start cannot leak it.
        job.invokeOnCompletion { eventsLoadMoreMutex.unlock() }
        eventsLoadMoreJob = job
    }

    /**
     * Refresh events (user-initiated pull-to-refresh).
     */
    fun refreshEvents() {
        loadEvents(forceRefresh = true)
    }

    // ========== News Sites Loading ==========

    /**
     * Load available news sites for filtering.
     */
    private fun loadNewsSites() {
        viewModelScope.launch {
            val result = infoRepository.getNewsSites()
            result.onSuccess { sites ->
                log.i { "Loaded ${sites.size} news sites for filtering" }
                _uiState.update { it.copy(availableNewsSites = sites) }
            }.onFailure { exception ->
                log.e { "Failed to load news sites: ${exception.message}" }
            }
        }
    }

    // ========== Search ==========

    /**
     * Observe search input with 300ms debounce to avoid excessive API calls.
     */
    @OptIn(FlowPreview::class)
    private fun observeSearchInput() {
        viewModelScope.launch {
            _searchInput
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collectLatest { query ->
                    _uiState.update { it.copy(searchQuery = query) }
                    // Search applies to both tabs simultaneously
                    loadNews()
                    loadEvents()
                }
        }
    }

    /**
     * Update search query (debounced — triggers reload after 300ms of inactivity).
     */
    fun updateSearchQuery(query: String) {
        _searchInput.value = query
        // Update UI immediately for responsive text field
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Clear search query and reload data.
     */
    fun clearSearch() {
        if (_uiState.value.searchQuery.isNotEmpty()) {
            _searchInput.value = ""
            _uiState.update { it.copy(searchQuery = "") }
            // Clear search reloads both tabs
            loadNews()
            loadEvents()
        }
    }

    // ========== Filtering ==========

    /**
     * Load available event types from the Config API via repository.
     */
    private fun loadEventTypes() {
        viewModelScope.launch {
            eventsRepository.getEventTypesDomain()
                .onSuccess { types ->
                    log.i { "Loaded ${types.size} event types for filtering" }
                    _uiState.update { it.copy(availableEventTypes = types) }
                }
                .onFailure { e ->
                    log.e { "Failed to load event types: ${e.message}" }
                }
        }
    }

    /**
     * Toggle a news site in the multi-select filter.
     */
    fun toggleNewsSiteFilter(newsSite: String) {
        _uiState.update { state ->
            val current = state.selectedNewsSites
            val updated = if (newsSite in current) current - newsSite else current + newsSite
            state.copy(selectedNewsSites = updated)
        }
        persistFilters()
        loadNews()
    }

    /**
     * Toggle an event type ID in the filter selection.
     */
    fun toggleEventTypeFilter(typeId: Int) {
        _uiState.update { state ->
            val current = state.selectedEventTypeIds
            val updated = if (typeId in current) current - typeId else current + typeId
            state.copy(selectedEventTypeIds = updated)
        }
        persistFilters()
        loadEvents()
    }

    /**
     * Set event type filter to specific list and reload.
     */
    fun setEventTypeFilter(typeIds: List<Int>) {
        _uiState.update { it.copy(selectedEventTypeIds = typeIds) }
        loadEvents()
    }

    /**
     * Clear all filters for the current tab.
     */
    fun clearFilters() {
        when (_uiState.value.selectedTab) {
            NewsEventsTab.NEWS -> {
                _uiState.update { it.copy(selectedNewsSites = emptyList()) }
                loadNews()
            }
            NewsEventsTab.EVENTS -> {
                _uiState.update { it.copy(selectedEventTypeIds = emptyList()) }
                loadEvents()
            }
        }
        persistFilters()
    }
}

/**
 * Tabs for the News and Events screen.
 */
enum class NewsEventsTab(val displayName: String) {
    NEWS("News"),
    EVENTS("Events")
}

/**
 * UI state for the News and Events screen.
 */
data class NewsEventsUiState(
    // Tab state
    val selectedTab: NewsEventsTab = NewsEventsTab.NEWS,

    // News tab state
    val news: List<Article> = emptyList(),
    val isLoadingNews: Boolean = false,
    val isLoadingMoreNews: Boolean = false,
    val newsError: String? = null,
    val newsHasMore: Boolean = true,
    val newsCurrentPage: Int = 0,
    val newsTotalCount: Int = 0,
    val newsDataSource: DataSource = DataSource.NETWORK,

    // Events tab state
    val events: List<Event> = emptyList(),
    val isLoadingEvents: Boolean = false,
    val isLoadingMoreEvents: Boolean = false,
    val eventsError: String? = null,
    val eventsHasMore: Boolean = true,
    val eventsCurrentPage: Int = 0,
    val eventsTotalCount: Int = 0,
    val eventsDataSource: DataSource = DataSource.NETWORK,

    // Search state (shared across tabs)
    val searchQuery: String = "",

    // News filter state
    val availableNewsSites: List<String> = emptyList(),
    val selectedNewsSites: List<String> = emptyList(),

    // Events filter state
    val availableEventTypes: List<EventType> = emptyList(),
    val selectedEventTypeIds: List<Int> = emptyList()
) {
    /**
     * Check if there are active filters for the current tab.
     */
    val hasActiveFilters: Boolean
        get() = when (selectedTab) {
            NewsEventsTab.NEWS -> selectedNewsSites.isNotEmpty()
            NewsEventsTab.EVENTS -> selectedEventTypeIds.isNotEmpty()
        }

    val activeFilterCount: Int
        get() = when (selectedTab) {
            NewsEventsTab.NEWS -> selectedNewsSites.size
            NewsEventsTab.EVENTS -> selectedEventTypeIds.size
        }

    /**
     * Check if the current tab is in loading state.
     */
    val isLoading: Boolean
        get() = when (selectedTab) {
            NewsEventsTab.NEWS -> isLoadingNews
            NewsEventsTab.EVENTS -> isLoadingEvents
        }

    /**
     * Get the error for the current tab.
     */
    val currentError: String?
        get() = when (selectedTab) {
            NewsEventsTab.NEWS -> newsError
            NewsEventsTab.EVENTS -> eventsError
        }

    /**
     * Check if the current tab is empty.
     */
    val isEmpty: Boolean
        get() = when (selectedTab) {
            NewsEventsTab.NEWS -> news.isEmpty() && !isLoadingNews
            NewsEventsTab.EVENTS -> events.isEmpty() && !isLoadingEvents
        }
}
