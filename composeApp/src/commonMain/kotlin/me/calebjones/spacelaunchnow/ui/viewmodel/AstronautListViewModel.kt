package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointNormal
import me.calebjones.spacelaunchnow.data.repository.AstronautRepository
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * ViewModel for displaying a paginated list of astronauts.
 *
 * Manages loading, pagination, error states, search functionality,
 * and filter options for the astronaut list screen.
 */
class AstronautListViewModel(
    private val astronautRepository: AstronautRepository,
    private val filterRepository: me.calebjones.spacelaunchnow.data.repository.AstronautFilterRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val log = logger()

    private val _uiState = MutableStateFlow(AstronautListUiState())
    val uiState: StateFlow<AstronautListUiState> = _uiState.asStateFlow()

    init {
        loadFilterOptions()
        loadAstronauts()
    }

    /**
     * Load the first page of astronauts.
     */
    fun loadAstronauts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, astronauts = emptyList(), currentPage = 0) }
            
            try {
                val currentState = _uiState.value
                val result = astronautRepository.getAstronauts(
                    limit = PAGE_SIZE,
                    offset = 0,
                    ordering = currentState.selectedSortOrder.apiValue,
                    search = currentState.searchQuery.ifBlank { null },
                    statusIds = currentState.selectedStatusIds.ifEmpty { null },
                    agencyIds = currentState.selectedAgencyIds.ifEmpty { null },
                    hasFlown = currentState.hasFlownFilter,
                    inSpace = currentState.inSpaceFilter,
                    isHuman = currentState.isHumanFilter
                )
                
                result.fold(
                    onSuccess = { paginatedList ->
                        log.i { "✅ Loaded ${paginatedList.results.size} astronauts (page 1)" }
                        _uiState.update {
                            it.copy(
                                astronauts = paginatedList.results,
                                isLoading = false,
                                currentPage = 1,
                                hasMore = paginatedList.next != null,
                                totalCount = paginatedList.count ?: 0
                            )
                        }
                        // Track search analytics when search query is active
                        if (currentState.searchQuery.isNotBlank()) {
                            analyticsManager.track(
                                AnalyticsEvent.SearchPerformed(
                                    query = currentState.searchQuery,
                                    resultCount = paginatedList.results.size
                                )
                            )
                        }
                    },
                    onFailure = { exception ->
                        log.e(exception) { "❌ Failed to load astronauts" }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load astronauts: ${exception.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                log.e(e) { "❌ Unexpected error loading astronauts" }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Load the next page of astronauts (pagination).
     */
    fun loadMore() {
        val currentState = _uiState.value
        
        // Don't load if already loading or no more items
        if (currentState.isLoadingMore || !currentState.hasMore) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            
            try {
                val nextPage = currentState.currentPage + 1
                val offset = (nextPage - 1) * PAGE_SIZE
                
                val result = astronautRepository.getAstronauts(
                    limit = PAGE_SIZE,
                    offset = offset,
                    ordering = currentState.selectedSortOrder.apiValue,
                    search = currentState.searchQuery.ifBlank { null },
                    statusIds = currentState.selectedStatusIds.ifEmpty { null },
                    agencyIds = currentState.selectedAgencyIds.ifEmpty { null },
                    hasFlown = currentState.hasFlownFilter,
                    inSpace = currentState.inSpaceFilter,
                    isHuman = currentState.isHumanFilter
                )
                
                result.fold(
                    onSuccess = { paginatedList ->
                        log.i { "✅ Loaded ${paginatedList.results.size} more astronauts (page $nextPage)" }
                        _uiState.update {
                            it.copy(
                                astronauts = it.astronauts + paginatedList.results,
                                isLoadingMore = false,
                                currentPage = nextPage,
                                hasMore = paginatedList.next != null
                            )
                        }
                    },
                    onFailure = { exception ->
                        log.e(exception) { "❌ Failed to load more astronauts" }
                        _uiState.update {
                            it.copy(
                                isLoadingMore = false,
                                error = "Failed to load more: ${exception.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                log.e(e) { "❌ Unexpected error loading more astronauts" }
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Refresh the astronaut list (pull-to-refresh).
     */
    fun refresh() {
        val currentState = _uiState.value
        _uiState.update {
            AstronautListUiState(
                searchQuery = currentState.searchQuery,
                selectedSortOrder = currentState.selectedSortOrder,
                selectedStatusIds = currentState.selectedStatusIds,
                selectedAgencyIds = currentState.selectedAgencyIds,
                hasFlownFilter = currentState.hasFlownFilter,
                inSpaceFilter = currentState.inSpaceFilter,
                isHumanFilter = currentState.isHumanFilter,
                statusOptions = currentState.statusOptions,
                isLoadingFilterOptions = currentState.isLoadingFilterOptions
            )
        }
        loadAstronauts()
    }

    /**
     * Clear any error messages.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Update the search query and reload the list.
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        refresh()
    }

    /**
     * Update the sort order and reload the list.
     */
    fun updateSortOrder(sortOrder: AstronautSortOrder) {
        _uiState.update { it.copy(selectedSortOrder = sortOrder) }
        refresh()
    }

    /**
     * Update the status filter and reload the list.
     */
    fun updateStatusFilter(statusIds: List<Int>) {
        _uiState.update { it.copy(selectedStatusIds = statusIds) }
        refresh()
    }

    /**
     * Update the agency filter and reload the list.
     */
    fun updateAgencyFilter(agencyIds: List<Int>) {
        _uiState.update { it.copy(selectedAgencyIds = agencyIds) }
        refresh()
    }

    /**
     * Update the hasFlown filter and reload the list.
     */
    fun updateHasFlownFilter(hasFlown: Boolean?) {
        _uiState.update { it.copy(hasFlownFilter = hasFlown) }
        refresh()
    }

    /**
     * Update the inSpace filter and reload the list.
     */
    fun updateInSpaceFilter(inSpace: Boolean?) {
        _uiState.update { it.copy(inSpaceFilter = inSpace) }
        refresh()
    }

    /**
     * Update the isHuman filter and reload the list.
     */
    fun updateIsHumanFilter(isHuman: Boolean?) {
        _uiState.update { it.copy(isHumanFilter = isHuman) }
        refresh()
    }

    /**
     * Clear all filters and search.
     */
    fun clearAllFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedStatusIds = emptyList(),
                selectedAgencyIds = emptyList(),
                selectedSortOrder = AstronautSortOrder.NAME_ASC,
                hasFlownFilter = null,
                inSpaceFilter = null,
                isHumanFilter = null
            )
        }
        refresh()
    }

    /**
     * Load filter options (status list) from the repository.
     * Uses cache if available.
     */
    private fun loadFilterOptions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFilterOptions = true) }
            
            try {
                val statuses = filterRepository.getStatuses().getOrNull() ?: emptyList()
                
                _uiState.update {
                    it.copy(
                        statusOptions = statuses,
                        isLoadingFilterOptions = false
                    )
                }
                
                log.i { "Filter options loaded - Statuses: ${statuses.size}" }
            } catch (e: Exception) {
                log.e(e) { "Failed to load filter options" }
                _uiState.update { it.copy(isLoadingFilterOptions = false) }
            }
        }
    }

    /**
     * Force reload filter options from the API (bypasses cache).
     */
    fun reloadFilterOptions() {
        viewModelScope.launch {
            log.d { "Force reloading filter options from API" }
            _uiState.update { it.copy(isLoadingFilterOptions = true) }
            
            try {
                val statuses = filterRepository.getStatuses(forceRefresh = true).getOrNull() ?: emptyList()
                
                _uiState.update {
                    it.copy(
                        statusOptions = statuses,
                        isLoadingFilterOptions = false
                    )
                }
                
                log.i { "Filter options reloaded - Statuses: ${statuses.size}" }
            } catch (e: Exception) {
                log.e(e) { "Failed to reload filter options" }
                _uiState.update { it.copy(isLoadingFilterOptions = false) }
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}

/**
 * UI state for the astronaut list screen.
 */
data class AstronautListUiState(
    val astronauts: List<AstronautEndpointNormal> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 0,
    val hasMore: Boolean = true,
    val totalCount: Int = 0,
    val searchQuery: String = "",
    val selectedSortOrder: AstronautSortOrder = AstronautSortOrder.NAME_ASC,
    val selectedStatusIds: List<Int> = emptyList(),
    val selectedAgencyIds: List<Int> = emptyList(),
    val hasFlownFilter: Boolean? = null,
    val inSpaceFilter: Boolean? = null,
    val isHumanFilter: Boolean? = null,
    val statusOptions: List<me.calebjones.spacelaunchnow.data.model.FilterOption> = emptyList(),
    val isLoadingFilterOptions: Boolean = false
)

/**
 * Sort order options for astronaut list.
 */
enum class AstronautSortOrder(val apiValue: String, val displayName: String) {
    NAME_ASC("name", "Name (A-Z)"),
    NAME_DESC("-name", "Name (Z-A)"),
    FLIGHTS_ASC("flights_count", "Flights (Low to High)"),
    FLIGHTS_DESC("-flights_count", "Flights (High to Low)"),
    FIRST_FLIGHT_ASC("first_flight", "First Flight (Oldest)"),
    FIRST_FLIGHT_DESC("-first_flight", "First Flight (Newest)")
}
