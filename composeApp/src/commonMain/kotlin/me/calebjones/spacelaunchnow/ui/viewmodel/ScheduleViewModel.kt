package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchBasic
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock.System

enum class ScheduleTab { Upcoming, Previous }

data class TabState(
    val items: List<LaunchBasic> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    val cacheTimestamp: Long? = null,
    val cachedQuery: String = ""
)

data class ScheduleUiState(
    val selectedTab: ScheduleTab = ScheduleTab.Upcoming,
    val searchQuery: String = "",
    val isSearchExpanded: Boolean = false,
    val upcomingTab: TabState = TabState(),
    val previousTab: TabState = TabState(),
    val isRefreshing: Boolean = false
)

class ScheduleViewModel(
    private val launchesApi: LaunchesApi
) : ViewModel() {

    private val log = logger()

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private val cacheTtlMillis = 900_000L // 15 minute TTL
    private var queryVersion = 0

    init {
        // Initialize both tabs on first launch
        loadTab(ScheduleTab.Upcoming, reset = true)
        loadTab(ScheduleTab.Previous, reset = true)
    }

    fun selectTab(tab: ScheduleTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        
        // Load if not already loaded
        val tabState = getTabState(tab)
        if (tabState.items.isEmpty() && !hasValidCache(tab)) {
            loadTab(tab, reset = true)
        }
    }

    fun updateSearchQuery(query: String) {
        val oldQuery = _uiState.value.searchQuery
        _uiState.value = _uiState.value.copy(searchQuery = query)

        // Debounce search
        viewModelScope.launch {
            delay(400)
            // Only proceed if query hasn't changed again
            if (_uiState.value.searchQuery == query && query != oldQuery) {
                queryVersion++
                if (query.isNotBlank()) {
                    // Clear cache and reload for search
                    invalidateCache()
                    loadTab(ScheduleTab.Upcoming, reset = true)
                    loadTab(ScheduleTab.Previous, reset = true)
                } else {
                    // Restore from cache or reload
                    restoreOrReload(ScheduleTab.Upcoming)
                    restoreOrReload(ScheduleTab.Previous)
                }
            }
        }
    }

    fun toggleSearchExpanded(expanded: Boolean) {
        _uiState.value = _uiState.value.copy(isSearchExpanded = expanded)
        if (!expanded && _uiState.value.searchQuery.isNotBlank()) {
            updateSearchQuery("")
        }
    }

    fun loadNextPage(tab: ScheduleTab) {
        val tabState = getTabState(tab)
        if (tabState.isLoading || tabState.endReached) return

        loadTab(tab, reset = false)
    }

    fun refresh() {
        viewModelScope.launch {
            log.d { "Refresh called - reloading both tabs" }
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            invalidateCache()
            
            // Reload both tabs
            log.d { "Loading Upcoming tab..." }
            loadTab(ScheduleTab.Upcoming, reset = true)
            log.d { "Loading Previous tab..." }
            loadTab(ScheduleTab.Previous, reset = true)
            
            // Wait a bit for both to complete
            delay(500)
            _uiState.value = _uiState.value.copy(isRefreshing = false)
            log.d { "Refresh completed" }
        }
    }

    private fun loadTab(tab: ScheduleTab, reset: Boolean) {
        viewModelScope.launch {
            log.d { "Loading tab: $tab, reset: $reset" }
            val requestVersion = queryVersion
            val tabState = getTabState(tab)
            
            // Stale-while-revalidate pattern:
            // - If we have existing data and are resetting (refresh), show data with isRefreshing
            // - If no data or paginating, use isLoading
            val hasExistingData = tabState.items.isNotEmpty()
            
            if (hasExistingData && reset) {
                // Background refresh - keep showing existing data
                updateTabState(tab, tabState.copy(isRefreshing = true, error = null))
                log.v { "Set isRefreshing=true for $tab (has ${tabState.items.size} existing items)" }
            } else {
                // Initial load or pagination - show loading state
                updateTabState(tab, tabState.copy(isLoading = true, error = null))
                log.v { "Set isLoading=true for $tab" }
            }

            try {
                val offset = if (reset) 0 else tabState.items.size
                val limit = 25
                val ordering = if (tab == ScheduleTab.Upcoming) "net" else "-net"
                val searchQuery = _uiState.value.searchQuery.takeIf { it.isNotBlank() }

                log.d { "API call starting - tab: $tab, offset: $offset, limit: $limit, ordering: $ordering, search: ${searchQuery ?: "none"}" }

                val page: PaginatedLaunchBasicList = if (tab == ScheduleTab.Previous) {
                    log.d { "Calling launchesApi.launchesMiniList for PREVIOUS launches..." }
                    val response = launchesApi.launchesMiniList(
                        limit = limit,
                        offset = offset,
                        previous = true,
                        ordering = ordering,
                        search = searchQuery
                    )
                    log.v { "API Response received - Status: ${response.status}" }
                    response.body()
                } else {
                    log.d { "Calling launchesApi.launchesMiniList for UPCOMING launches..." }
                    val response = launchesApi.launchesMiniList(
                        limit = limit,
                        offset = offset,
                        upcoming = true,
                        ordering = ordering,
                        search = searchQuery
                    )
                    log.d { "API Response received - Status: ${response.status}" }
                    response.body()
                }

                log.i { "Received ${page.results.size} launches for $tab - Total count: ${page.count}, Has next: ${page.next != null}" }
                if (page.results.isNotEmpty()) {
                    log.v { "First launch: ${page.results.first().name}, Last launch: ${page.results.last().name}" }
                }
                
                // Ignore if query changed during request
                if (requestVersion != queryVersion) {
                    log.d { "Query version changed, ignoring results" }
                    return@launch
                }

                val newItems = if (reset) {
                    page.results
                } else {
                    tabState.items + page.results
                }

                log.d { "Updating $tab state with ${newItems.size} total items" }
                updateTabState(
                    tab,
                    TabState(
                        items = newItems,
                        isLoading = false,
                        isRefreshing = false,
                        error = null,
                        endReached = page.next == null || page.results.isEmpty(),
                        cacheTimestamp = now(),
                        cachedQuery = _uiState.value.searchQuery
                    )
                )
                log.i { "loadTab completed successfully for $tab" }

            } catch (t: Throwable) {
                log.e(t) { "loadTab failed for $tab" }
                updateTabState(
                    tab,
                    tabState.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = t.message ?: t.toString()
                    )
                )
            }
        }
    }

    private fun restoreOrReload(tab: ScheduleTab) {
        if (hasValidCache(tab)) {
            // Cache is still valid, no need to reload
            return
        }
        
        val tabState = getTabState(tab)
        if (tabState.items.isEmpty()) {
            loadTab(tab, reset = true)
        }
    }

    private fun hasValidCache(tab: ScheduleTab): Boolean {
        val tabState = getTabState(tab)
        val timestamp = tabState.cacheTimestamp ?: return false
        val fresh = (now() - timestamp) < cacheTtlMillis
        val queryMatches = tabState.cachedQuery == _uiState.value.searchQuery
        return fresh && queryMatches && tabState.items.isNotEmpty()
    }

    private fun invalidateCache() {
        log.d { "Invalidating cache - Upcoming timestamp: ${_uiState.value.upcomingTab.cacheTimestamp}, Previous timestamp: ${_uiState.value.previousTab.cacheTimestamp}" }

        _uiState.value = _uiState.value.copy(
            upcomingTab = _uiState.value.upcomingTab.copy(cacheTimestamp = null, cachedQuery = ""),
            previousTab = _uiState.value.previousTab.copy(cacheTimestamp = null, cachedQuery = "")
        )
        
        log.d { "Cache invalidated (timestamps cleared)" }
    }

    private fun getTabState(tab: ScheduleTab): TabState {
        return when (tab) {
            ScheduleTab.Upcoming -> _uiState.value.upcomingTab
            ScheduleTab.Previous -> _uiState.value.previousTab
        }
    }

    private fun updateTabState(tab: ScheduleTab, newState: TabState) {
        _uiState.value = when (tab) {
            ScheduleTab.Upcoming -> _uiState.value.copy(upcomingTab = newState)
            ScheduleTab.Previous -> _uiState.value.copy(previousTab = newState)
        }
    }

    private fun now() = System.now().toEpochMilliseconds()
}
