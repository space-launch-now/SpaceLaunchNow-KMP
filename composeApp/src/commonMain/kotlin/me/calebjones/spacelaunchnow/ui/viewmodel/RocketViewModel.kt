package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.data.model.FilterOption
import me.calebjones.spacelaunchnow.data.repository.RocketFilterRepository
import me.calebjones.spacelaunchnow.data.repository.RocketRepository
import me.calebjones.spacelaunchnow.domain.model.VehicleConfig
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * ViewModel for displaying a paginated list of rockets.
 *
 * Manages loading, pagination, error states, search functionality,
 * and filter options for the rocket list screen.
 */
class RocketViewModel(
    private val repository: RocketRepository,
    private val filterRepository: RocketFilterRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val log = logger()

    private val _uiState = MutableStateFlow(RocketListUiState())
    val uiState: StateFlow<RocketListUiState> = _uiState.asStateFlow()

    private val _rocketDetails = MutableStateFlow<VehicleConfig?>(null)
    val rocketDetails: StateFlow<VehicleConfig?> = _rocketDetails

    init {
        loadRockets()
        loadFilterOptions()
    }

    /**
     * Load the first page of rockets.
     */
    fun loadRockets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, rockets = emptyList(), currentPage = 0) }
            
            try {
                val currentState = _uiState.value
                
                // Convert family names to family IDs
                val familyIds = if (currentState.selectedFamilies.isNotEmpty()) {
                    currentState.familyOptions
                        .filter { it.name in currentState.selectedFamilies }
                        .map { it.id }
                } else null
                
                val result = repository.getRocketsDomain(
                    limit = PAGE_SIZE,
                    offset = 0,
                    ordering = currentState.selectedSortOrder.apiValue,
                    search = currentState.searchQuery.ifBlank { null },
                    programIds = currentState.selectedProgramIds.ifEmpty { null },
                    familyIds = familyIds,
                    active = currentState.activeFilter,
                    reusable = currentState.reusableFilter
                )
                
                result.fold(
                    onSuccess = { paginatedList ->
                        log.i { "✅ Loaded ${paginatedList.results.size} rockets (page 1)" }
                        _uiState.update {
                            it.copy(
                                rockets = paginatedList.results,
                                isLoading = false,
                                currentPage = 1,
                                hasMore = paginatedList.next != null,
                                totalCount = paginatedList.count
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
                        log.e(exception) { "❌ Failed to load rockets" }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load rockets: ${exception.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                log.e(e) { "❌ Unexpected error loading rockets" }
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
     * Load the next page of rockets (pagination).
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
                
                // Convert family names to family IDs
                val familyIds = if (currentState.selectedFamilies.isNotEmpty()) {
                    currentState.familyOptions
                        .filter { it.name in currentState.selectedFamilies }
                        .map { it.id }
                } else null
                
                val result = repository.getRocketsDomain(
                    limit = PAGE_SIZE,
                    offset = offset,
                    ordering = currentState.selectedSortOrder.apiValue,
                    search = currentState.searchQuery.ifBlank { null },
                    programIds = currentState.selectedProgramIds.ifEmpty { null },
                    familyIds = familyIds,
                    active = currentState.activeFilter,
                    reusable = currentState.reusableFilter
                )
                
                result.fold(
                    onSuccess = { paginatedList ->
                        log.i { "✅ Loaded ${paginatedList.results.size} more rockets (page $nextPage)" }
                        _uiState.update {
                            it.copy(
                                rockets = it.rockets + paginatedList.results,
                                isLoadingMore = false,
                                currentPage = nextPage,
                                hasMore = paginatedList.next != null
                            )
                        }
                    },
                    onFailure = { exception ->
                        log.e(exception) { "❌ Failed to load more rockets" }
                        _uiState.update {
                            it.copy(
                                isLoadingMore = false,
                                error = "Failed to load more: ${exception.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                log.e(e) { "❌ Unexpected error loading more rockets" }
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
     * Refresh the rocket list (pull-to-refresh).
     */
    fun refresh() {
        val currentState = _uiState.value
        _uiState.update {
            RocketListUiState(
                searchQuery = currentState.searchQuery,
                selectedSortOrder = currentState.selectedSortOrder,
                selectedProgramIds = currentState.selectedProgramIds,
                selectedFamilies = currentState.selectedFamilies,
                activeFilter = currentState.activeFilter,
                reusableFilter = currentState.reusableFilter,
                programOptions = currentState.programOptions,
                familyOptions = currentState.familyOptions
            )
        }
        loadRockets()
    }

    /**
     * Clear any error messages.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Load filter options (programs and families) from API.
     */
    private fun loadFilterOptions() {
        viewModelScope.launch {
            log.d { "Loading filter options (programs and families)" }
            _uiState.update { it.copy(isLoadingFilterOptions = true) }

            // Load both programs and families in parallel
            val programsDeferred = async { filterRepository.getPrograms(forceRefresh = false) }
            val familiesDeferred = async { filterRepository.getFamilies(forceRefresh = false) }

            val programsResult = programsDeferred.await()
            val familiesResult = familiesDeferred.await()

            programsResult.onSuccess { programs ->
                log.i { "✅ Loaded ${programs.size} program options" }
                _uiState.update { it.copy(programOptions = programs) }
            }.onFailure { exception ->
                log.e(exception) { "❌ Failed to load program options" }
            }

            familiesResult.onSuccess { families ->
                log.i { "✅ Loaded ${families.size} family options" }
                _uiState.update { it.copy(familyOptions = families) }
            }.onFailure { exception ->
                log.e(exception) { "❌ Failed to load family options" }
            }

            _uiState.update { it.copy(isLoadingFilterOptions = false) }
        }
    }

    /**
     * Reload filter options from API (force refresh).
     */
    fun reloadFilterOptions() {
        viewModelScope.launch {
            log.d { "Reloading filter options (force refresh)" }
            _uiState.update { it.copy(isLoadingFilterOptions = true) }

            // Load both programs and families in parallel with force refresh
            val programsDeferred = async { filterRepository.getPrograms(forceRefresh = true) }
            val familiesDeferred = async { filterRepository.getFamilies(forceRefresh = true) }

            val programsResult = programsDeferred.await()
            val familiesResult = familiesDeferred.await()

            programsResult.onSuccess { programs ->
                log.i { "✅ Reloaded ${programs.size} program options" }
                _uiState.update { it.copy(programOptions = programs) }
            }.onFailure { exception ->
                log.e(exception) { "❌ Failed to reload program options" }
            }

            familiesResult.onSuccess { families ->
                log.i { "✅ Reloaded ${families.size} family options" }
                _uiState.update { it.copy(familyOptions = families) }
            }.onFailure { exception ->
                log.e(exception) { "❌ Failed to reload family options" }
            }

            _uiState.update { it.copy(isLoadingFilterOptions = false) }
        }
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
    fun updateSortOrder(sortOrder: RocketSortOrder) {
        _uiState.update { it.copy(selectedSortOrder = sortOrder) }
        refresh()
    }

    /**
     * Update the program filter and reload the list.
     */
    fun updateProgramFilter(programIds: List<Int>) {
        analyticsManager.track(AnalyticsEvent.FilterChanged(filterType = "rocket_program", value = programIds.joinToString(",")))
        _uiState.update { it.copy(selectedProgramIds = programIds) }
        refresh()
    }

    /**
     * Update the family filter and reload the list.
     */
    fun updateFamilyFilter(families: List<String>) {
        analyticsManager.track(AnalyticsEvent.FilterChanged(filterType = "rocket_family", value = families.joinToString(",")))
        _uiState.update { it.copy(selectedFamilies = families) }
        refresh()
    }

    /**
     * Update the active filter and reload the list.
     */
    fun updateActiveFilter(active: Boolean?) {
        analyticsManager.track(AnalyticsEvent.FilterChanged(filterType = "rocket_active", value = active?.toString() ?: "all"))
        _uiState.update { it.copy(activeFilter = active) }
        refresh()
    }

    /**
     * Update the reusable filter and reload the list.
     */
    fun updateReusableFilter(reusable: Boolean?) {
        _uiState.update { it.copy(reusableFilter = reusable) }
        refresh()
    }

    /**
     * Clear all filters and search.
     */
    fun clearAllFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedProgramIds = emptyList(),
                selectedFamilies = emptyList(),
                activeFilter = null,
                reusableFilter = null,
                selectedSortOrder = RocketSortOrder.NAME_ASC
            )
        }
        refresh()
    }

    /**
     * Fetch rocket details by ID.
     */
    fun fetchRocketDetails(id: Int) {
        viewModelScope.launch {
            log.d { "Fetching rocket details for id: $id" }
            _uiState.update { it.copy(error = null, isLoading = true) }

            val result = repository.getRocketDetailsDomain(id)
            result.onSuccess { rocket ->
                log.i { "Successfully loaded rocket details: ${rocket.name}" }
                _rocketDetails.value = rocket
                _uiState.update { it.copy(isLoading = false) }
                analyticsManager.track(AnalyticsEvent.RocketViewed(rocket.id))
            }.onFailure { exception ->
                log.e(exception) { "Failed to fetch rocket details for id: $id" }
                _uiState.update { 
                    it.copy(
                        error = exception.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}

/**
 * UI state for the rocket list screen.
 */
data class RocketListUiState(
    val rockets: List<VehicleConfig> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 0,
    val hasMore: Boolean = true,
    val totalCount: Int = 0,
    val searchQuery: String = "",
    val selectedSortOrder: RocketSortOrder = RocketSortOrder.NAME_ASC,
    val selectedProgramIds: List<Int> = emptyList(),
    val selectedFamilies: List<String> = emptyList(),
    val activeFilter: Boolean? = null,
    val reusableFilter: Boolean? = null,
    val programOptions: List<FilterOption> = emptyList(),
    val familyOptions: List<FilterOption> = emptyList(),
    val isLoadingFilterOptions: Boolean = false
)

/**
 * Sort order options for rocket list.
 */
enum class RocketSortOrder(val apiValue: String, val displayName: String) {
    NAME_ASC("name", "Name (A-Z)"),
    NAME_DESC("-name", "Name (Z-A)"),
    FAMILY_ASC("family", "Family (A-Z)"),
    FAMILY_DESC("-family", "Family (Z-A)"),
    TOTAL_LAUNCHES_ASC("total_launch_count", "Total Launches (Low to High)"),
    TOTAL_LAUNCHES_DESC("-total_launch_count", "Total Launches (High to Low)"),
    SUCCESSFUL_LAUNCHES_ASC("successful_launches", "Successful Launches (Low to High)"),
    SUCCESSFUL_LAUNCHES_DESC("-successful_launches", "Successful Launches (High to Low)"),
    FAILED_LAUNCHES_ASC("failed_launches", "Failed Launches (Low to High)"),
    FAILED_LAUNCHES_DESC("-failed_launches", "Failed Launches (High to Low)"),
    PENDING_LAUNCHES_ASC("pending_launches", "Pending Launches (Low to High)"),
    PENDING_LAUNCHES_DESC("-pending_launches", "Pending Launches (High to Low)"),
    CONSECUTIVE_SUCCESSFUL_LAUNCHES_ASC("consecutive_successful_launches", "Consecutive Successes (Low to High)"),
    CONSECUTIVE_SUCCESSFUL_LAUNCHES_DESC("-consecutive_successful_launches", "Consecutive Successes (High to Low)"),
    ATTEMPTED_LANDINGS_ASC("attempted_landings", "Attempted Landings (Low to High)"),
    ATTEMPTED_LANDINGS_DESC("-attempted_landings", "Attempted Landings (High to Low)"),
    SUCCESSFUL_LANDINGS_ASC("successful_landings", "Successful Landings (Low to High)"),
    SUCCESSFUL_LANDINGS_DESC("-successful_landings", "Successful Landings (High to Low)"),
    FAILED_LANDINGS_ASC("failed_landings", "Failed Landings (Low to High)"),
    FAILED_LANDINGS_DESC("-failed_landings", "Failed Landings (High to Low)"),
    CONSECUTIVE_SUCCESSFUL_LANDINGS_ASC("consecutive_successful_landings", "Consecutive Landing Successes (Low to High)"),
    CONSECUTIVE_SUCCESSFUL_LANDINGS_DESC("-consecutive_successful_landings", "Consecutive Landing Successes (High to Low)"),
    LEO_CAPACITY_ASC("leo_capacity", "LEO Capacity (Low to High)"),
    LEO_CAPACITY_DESC("-leo_capacity", "LEO Capacity (High to Low)"),
    GTO_CAPACITY_ASC("gto_capacity", "GTO Capacity (Low to High)"),
    GTO_CAPACITY_DESC("-gto_capacity", "GTO Capacity (High to Low)"),
    LAUNCH_MASS_ASC("launch_mass", "Launch Mass (Low to High)"),
    LAUNCH_MASS_DESC("-launch_mass", "Launch Mass (High to Low)"),
    LAUNCH_COST_ASC("launch_cost", "Launch Cost (Low to High)"),
    LAUNCH_COST_DESC("-launch_cost", "Launch Cost (High to Low)"),
    MAIDEN_FLIGHT_ASC("maiden_flight", "Maiden Flight (Oldest)"),
    MAIDEN_FLIGHT_DESC("-maiden_flight", "Maiden Flight (Newest)")
}
