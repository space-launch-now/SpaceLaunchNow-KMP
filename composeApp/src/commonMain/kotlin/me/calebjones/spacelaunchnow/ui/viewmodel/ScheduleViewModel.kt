package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.api.extensions.getLaunchMiniList
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchBasic
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.data.model.FilterOption
import me.calebjones.spacelaunchnow.data.repository.ScheduleFilterRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.ui.schedule.ScheduleFilterState
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
    val cachedQuery: String = "",
    val totalCount: Int? = null // Total count from API (when filtering)
)

data class ScheduleUiState(
    val selectedTab: ScheduleTab = ScheduleTab.Upcoming,
    val searchQuery: String = "",
    val isSearchExpanded: Boolean = false,
    val upcomingTab: TabState = TabState(),
    val previousTab: TabState = TabState(),
    val isRefreshing: Boolean = false,
    val filterState: ScheduleFilterState = ScheduleFilterState(),
    val isFilterSheetOpen: Boolean = false,
    val filterOptions: FilterOptions = FilterOptions(),
    val isLoadingFilterOptions: Boolean = false
)

data class FilterOptions(
    val agencies: List<FilterOption> = emptyList(),
    val programs: List<FilterOption> = emptyList(),
    val rockets: List<FilterOption> = emptyList(),
    val locations: List<FilterOption> = emptyList(),
    val statuses: List<FilterOption> = emptyList(),
    val orbits: List<FilterOption> = emptyList(),
    val missionTypes: List<FilterOption> = emptyList(),
    val launcherConfigFamilies: List<FilterOption> = emptyList()
)

class ScheduleViewModel(
    private val launchesApi: LaunchesApi,
    private val appPreferences: AppPreferences,
    private val filterRepository: ScheduleFilterRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val log = logger()

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    /** Tracks the currently selected launch ID for list-detail restoration. */
    var selectedLaunchId: String? = null

    private val cacheTtlMillis = 900_000L // 15 minute TTL
    private var queryVersion = 0
    private var loadJobsUpcoming: kotlinx.coroutines.Job? = null
    private var loadJobsPrevious: kotlinx.coroutines.Job? = null

    init {
        // Load saved filter state FIRST, then load data
        viewModelScope.launch {
            // Get initial filter state synchronously
            val savedFilterState = appPreferences.getScheduleFilterState()
            if (_uiState.value.filterState != savedFilterState) {
                log.d { "Filter state loaded from preferences on init: ${savedFilterState.activeFilterCount()} filters" }
                _uiState.value = _uiState.value.copy(filterState = savedFilterState)

                // Always invalidate cache on app start if filters exist to prevent showing wrong data
                if (savedFilterState.hasActiveFilters()) {
                    log.d { "Invalidating cache on init due to active filters" }
                    invalidateCache()
                }
            }

            // Pre-load filter options on app launch (uses cache if available)
            loadFilterOptions()

            // Now load tabs with correct filter state
            loadTab(ScheduleTab.Upcoming, reset = true)
            loadTab(ScheduleTab.Previous, reset = true)

            // Continue observing filter state changes
            appPreferences.scheduleFilterStateFlow.collect { newFilterState ->
                if (_uiState.value.filterState != newFilterState) {
                    log.d { "Filter state changed: ${newFilterState.activeFilterCount()} filters" }
                    _uiState.value = _uiState.value.copy(filterState = newFilterState)

                    // Invalidate cache and reload when filters change
                    invalidateCache()
                    loadTab(ScheduleTab.Upcoming, reset = true)
                    loadTab(ScheduleTab.Previous, reset = true)
                }
            }
        }
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
        // Cancel any existing load for this tab
        when (tab) {
            ScheduleTab.Upcoming -> {
                loadJobsUpcoming?.cancel()
                loadJobsUpcoming = viewModelScope.launch {
                    executeLoadTab(tab, reset)
                }
            }
            ScheduleTab.Previous -> {
                loadJobsPrevious?.cancel()
                loadJobsPrevious = viewModelScope.launch {
                    executeLoadTab(tab, reset)
                }
            }
        }
    }

    private suspend fun executeLoadTab(tab: ScheduleTab, reset: Boolean) {
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
                val filterState = _uiState.value.filterState

                log.d { "API call starting - tab: $tab, offset: $offset, limit: $limit, ordering: $ordering, search: ${searchQuery ?: "none"}, filters: ${filterState.activeFilterCount()}" }

                val page: PaginatedLaunchBasicList = if (tab == ScheduleTab.Previous) {
                    log.d { "Calling launchesApi.getLaunchMiniList for PREVIOUS launches..." }
                    val response = launchesApi.getLaunchMiniList(
                        limit = limit,
                        offset = offset,
                        previous = true,
                        ordering = ordering,
                        search = searchQuery,
                        lspId = filterState.selectedAgencyIds.takeIf { it.isNotEmpty() }?.toList(),
                        locationIds = filterState.selectedLocationIds.takeIf { it.isNotEmpty() }
                            ?.toList(),
                        program = filterState.selectedProgramIds.takeIf { it.isNotEmpty() }
                            ?.toList(),
                        rocketConfigurationId = filterState.selectedRocketIds.firstOrNull(), // API limitation: single ID only
                        isCrewed = filterState.isCrewed,
                        includeSuborbital = filterState.includeSuborbital,
                        statusIds = filterState.selectedStatusIds.takeIf { it.isNotEmpty() }
                            ?.toList(),
                        orbitIds = filterState.selectedOrbitIds.takeIf { it.isNotEmpty() }
                            ?.toList(),
                        missionTypeIds = filterState.selectedMissionTypeIds.takeIf { it.isNotEmpty() }
                            ?.toList(),
                        launcherConfigFamilyIds = filterState.selectedLauncherConfigFamilyIds.takeIf { it.isNotEmpty() }
                            ?.toList()
                    )
                    log.v { "API Response received - Status: ${response.status}" }
                    response.body()
                } else {
                    log.d { "Calling launchesApi.getLaunchMiniList for UPCOMING launches..." }
                    val response = launchesApi.getLaunchMiniList(
                        limit = limit,
                        offset = offset,
                        upcoming = true,
                        ordering = ordering,
                        search = searchQuery,
                        lspId = filterState.selectedAgencyIds.takeIf { it.isNotEmpty() }?.toList(),
                        locationIds = filterState.selectedLocationIds.takeIf { it.isNotEmpty() }
                            ?.toList(),
                        program = filterState.selectedProgramIds.takeIf { it.isNotEmpty() }
                            ?.toList(),
                        rocketConfigurationId = filterState.selectedRocketIds.firstOrNull(), // API limitation: single ID only
                        isCrewed = filterState.isCrewed,
                        includeSuborbital = filterState.includeSuborbital,
                        statusIds = filterState.selectedStatusIds.takeIf { it.isNotEmpty() }
                            ?.toList(),
                        orbitIds = filterState.selectedOrbitIds.takeIf { it.isNotEmpty() }
                            ?.toList(),
                        missionTypeIds = filterState.selectedMissionTypeIds.takeIf { it.isNotEmpty() }
                            ?.toList(),
                        launcherConfigFamilyIds = filterState.selectedLauncherConfigFamilyIds.takeIf { it.isNotEmpty() }
                            ?.toList()
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
                    return
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
                        cachedQuery = _uiState.value.searchQuery,
                        totalCount = page.count
                    )
                )
                log.i { "loadTab completed successfully for $tab" }

                // Track search analytics when search query is active
                val currentQuery = _uiState.value.searchQuery
                if (currentQuery.isNotBlank() && tab == _uiState.value.selectedTab) {
                    analyticsManager.track(
                        AnalyticsEvent.SearchPerformed(
                            query = currentQuery,
                            resultCount = newItems.size
                        )
                    )
                }

            } catch (t: Throwable) {
                // Check if this was a cancellation - if so, don't log as error
                if (t is kotlinx.coroutines.CancellationException) {
                    log.d { "loadTab cancelled for $tab (expected during filter changes)" }
                    throw t // Re-throw to properly handle cancellation
                }
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

    // Filter management
    fun openFilterSheet() {
        _uiState.value = _uiState.value.copy(isFilterSheetOpen = true)
        loadFilterOptions()
    }

    fun closeFilterSheet() {
        _uiState.value = _uiState.value.copy(isFilterSheetOpen = false)
    }

    fun applyFilters(newFilterState: ScheduleFilterState) {
        viewModelScope.launch {
            log.d { "Applying filters - ${newFilterState.activeFilterCount()} active" }

            // Track filter changes
            analyticsManager.track(
                AnalyticsEvent.FilterChanged(
                    filterType = "schedule",
                    value = "active_count:${newFilterState.activeFilterCount()}"
                )
            )

            // Save to preferences
            appPreferences.updateScheduleFilterState(newFilterState)

            // Update UI state
            _uiState.value = _uiState.value.copy(
                filterState = newFilterState,
                isFilterSheetOpen = false
            )

            // Invalidate cache and reload with new filters
            invalidateCache()
            loadTab(ScheduleTab.Upcoming, reset = true)
            loadTab(ScheduleTab.Previous, reset = true)
        }
    }

    fun clearFilters() {
        applyFilters(ScheduleFilterState())
    }

    /**
     * Apply a filter for a specific agency by ID.
     * Used when navigating from Agency Detail screen.
     * Returns a suspend function to ensure filter is fully applied before navigation.
     */
    suspend fun filterByAgencyAndWait(agencyId: Int) {
        log.d { "filterByAgencyAndWait called with agencyId: $agencyId" }
        
        // Cancel any in-flight loads immediately
        loadJobsUpcoming?.cancel()
        loadJobsPrevious?.cancel()
        loadJobsUpcoming = null
        loadJobsPrevious = null
        
        val newFilterState = ScheduleFilterState(
            selectedAgencyIds = setOf(agencyId)
        )
        
        // Update UI state immediately and clear both tabs
        _uiState.value = _uiState.value.copy(
            filterState = newFilterState,
            upcomingTab = TabState(),  // Clear upcoming
            previousTab = TabState()   // Clear previous
        )
        
        // Invalidate cache immediately
        invalidateCache()
        
        // Save to preferences (this will trigger the observer in init)
        appPreferences.updateScheduleFilterState(newFilterState)
        
        // Wait for the observer to trigger and start loading
        kotlinx.coroutines.delay(150)
        
        log.d { "Filter applied and ready for navigation" }
    }

    fun reloadFilterOptions() {
        viewModelScope.launch {
            log.d { "Force reloading filter options from API" }
            _uiState.value = _uiState.value.copy(isLoadingFilterOptions = true)

            try {
                // Force refresh from API (bypasses cache)
                val agencies =
                    filterRepository.getAgencies(forceRefresh = true).getOrNull() ?: emptyList()
                val programs =
                    filterRepository.getPrograms(forceRefresh = true).getOrNull() ?: emptyList()
                val rockets = emptyList<FilterOption>()
                val locations =
                    filterRepository.getLocations(forceRefresh = true).getOrNull() ?: emptyList()
                val statuses =
                    filterRepository.getStatuses(forceRefresh = true).getOrNull() ?: emptyList()
                val orbits =
                    filterRepository.getOrbits(forceRefresh = true).getOrNull() ?: emptyList()
                val missionTypes =
                    filterRepository.getMissionTypes(forceRefresh = true).getOrNull() ?: emptyList()
                val launcherConfigFamilies =
                    filterRepository.getLauncherConfigFamilies(forceRefresh = true).getOrNull()
                        ?: emptyList()

                _uiState.value = _uiState.value.copy(
                    filterOptions = FilterOptions(
                        agencies = agencies,
                        programs = programs,
                        rockets = rockets,
                        locations = locations,
                        statuses = statuses,
                        orbits = orbits,
                        missionTypes = missionTypes,
                        launcherConfigFamilies = launcherConfigFamilies
                    ),
                    isLoadingFilterOptions = false
                )

                log.i { "Filter options reloaded - Agencies: ${agencies.size}, Programs: ${programs.size}, Rockets: ${rockets.size}, Locations: ${locations.size}, Statuses: ${statuses.size}, Orbits: ${orbits.size}, Mission Types: ${missionTypes.size}, Launcher Config Families: ${launcherConfigFamilies.size}" }
            } catch (e: Exception) {
                log.e(e) { "Failed to reload filter options" }
                _uiState.value = _uiState.value.copy(isLoadingFilterOptions = false)
            }
        }
    }

    private fun loadFilterOptions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingFilterOptions = true)

            try {
                // Load all filter options in parallel (from cache if available)
                val agencies = filterRepository.getAgencies().getOrNull() ?: emptyList()
                val programs = filterRepository.getPrograms().getOrNull() ?: emptyList()
                // val rockets = filterRepository.getRockets().getOrNull() ?: emptyList()
                val rockets = emptyList<FilterOption>()
                val locations = filterRepository.getLocations().getOrNull() ?: emptyList()
                val statuses = filterRepository.getStatuses().getOrNull() ?: emptyList()
                val orbits = filterRepository.getOrbits().getOrNull() ?: emptyList()
                val missionTypes = filterRepository.getMissionTypes().getOrNull() ?: emptyList()
                val launcherConfigFamilies =
                    filterRepository.getLauncherConfigFamilies().getOrNull() ?: emptyList()

                _uiState.value = _uiState.value.copy(
                    filterOptions = FilterOptions(
                        agencies = agencies,
                        programs = programs,
                        rockets = rockets,
                        locations = locations,
                        statuses = statuses,
                        orbits = orbits,
                        missionTypes = missionTypes,
                        launcherConfigFamilies = launcherConfigFamilies
                    ),
                    isLoadingFilterOptions = false
                )

                log.i { "Filter options loaded - Agencies: ${agencies.size}, Programs: ${programs.size}, Rockets: ${rockets.size}, Locations: ${locations.size}, Statuses: ${statuses.size}, Orbits: ${orbits.size}, Mission Types: ${missionTypes.size}, Launcher Config Families: ${launcherConfigFamilies.size}" }
            } catch (e: Exception) {
                log.e(e) { "Failed to load filter options" }
                _uiState.value = _uiState.value.copy(isLoadingFilterOptions = false)
            }
        }
    }
}
