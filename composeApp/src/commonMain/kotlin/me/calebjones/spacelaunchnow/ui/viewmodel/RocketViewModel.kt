package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
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
import me.calebjones.spacelaunchnow.util.logging.isCoroutineCancellation
import me.calebjones.spacelaunchnow.util.logging.logger
import org.jetbrains.compose.resources.StringResource
import spacelaunchnow_kmp.composeapp.generated.resources.*

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

    // In-flight load-more job, cancelled when the list is reset by a reload so a page-N
    // response can never land on a freshly reset page-0 list.
    private var loadMoreJob: Job? = null

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
        // Cancelled here rather than in refresh(), because init and retry reach this
        // function directly. The flag is cleared beside the cancel: the reset below uses
        // copy(), so it does not clear isLoadingMore on its own, and the screen gates
        // load-more on that flag. The cancellation guards in loadMore() deliberately do not
        // clear it (a late write could unstick a newer load-more), so a cancellation we did
        // not cause would leave it set; accepted, see the guards below.
        loadMoreJob?.cancel()
        loadMoreJob = null
        _uiState.update { it.copy(isLoadingMore = false) }

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

        loadMoreJob = viewModelScope.launch {
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
                                rockets = (it.rockets + paginatedList.results).distinctBy { rocket -> rocket.id },
                                isLoadingMore = false,
                                currentPage = nextPage,
                                hasMore = paginatedList.next != null
                            )
                        }
                    },
                    onFailure = { exception ->
                        // Cancellation arrives here rather than as a throw because the
                        // repository catches Exception, which CancellationException extends;
                        // without this guard "Job was cancelled" would be painted over the
                        // freshly reloaded list.
                        if (exception.isCoroutineCancellation()) {
                            return@fold
                        }
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
                // Cancellation that reached us as a throw rather than a failed Result.
                if (e.isCoroutineCancellation()) {
                    return@launch
                }
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
enum class RocketSortOrder(val apiValue: String, val label: StringResource) {
    NAME_ASC("name", Res.string.common_sort_name_asc),
    NAME_DESC("-name", Res.string.common_sort_name_desc),
    FAMILY_ASC("family", Res.string.rockets_sort_family_asc),
    FAMILY_DESC("-family", Res.string.rockets_sort_family_desc),
    TOTAL_LAUNCHES_ASC("total_launch_count", Res.string.common_sort_total_launches_asc),
    TOTAL_LAUNCHES_DESC("-total_launch_count", Res.string.common_sort_total_launches_desc),
    SUCCESSFUL_LAUNCHES_ASC("successful_launches", Res.string.common_sort_successful_launches_asc),
    SUCCESSFUL_LAUNCHES_DESC("-successful_launches", Res.string.common_sort_successful_launches_desc),
    FAILED_LAUNCHES_ASC("failed_launches", Res.string.common_sort_failed_launches_asc),
    FAILED_LAUNCHES_DESC("-failed_launches", Res.string.common_sort_failed_launches_desc),
    PENDING_LAUNCHES_ASC("pending_launches", Res.string.common_sort_pending_launches_asc),
    PENDING_LAUNCHES_DESC("-pending_launches", Res.string.common_sort_pending_launches_desc),
    CONSECUTIVE_SUCCESSFUL_LAUNCHES_ASC("consecutive_successful_launches", Res.string.common_sort_consecutive_successes_asc),
    CONSECUTIVE_SUCCESSFUL_LAUNCHES_DESC("-consecutive_successful_launches", Res.string.common_sort_consecutive_successes_desc),
    ATTEMPTED_LANDINGS_ASC("attempted_landings", Res.string.common_sort_attempted_landings_asc),
    ATTEMPTED_LANDINGS_DESC("-attempted_landings", Res.string.common_sort_attempted_landings_desc),
    SUCCESSFUL_LANDINGS_ASC("successful_landings", Res.string.common_sort_successful_landings_asc),
    SUCCESSFUL_LANDINGS_DESC("-successful_landings", Res.string.common_sort_successful_landings_desc),
    FAILED_LANDINGS_ASC("failed_landings", Res.string.common_sort_failed_landings_asc),
    FAILED_LANDINGS_DESC("-failed_landings", Res.string.common_sort_failed_landings_desc),
    CONSECUTIVE_SUCCESSFUL_LANDINGS_ASC("consecutive_successful_landings", Res.string.common_sort_consecutive_landing_successes_asc),
    CONSECUTIVE_SUCCESSFUL_LANDINGS_DESC("-consecutive_successful_landings", Res.string.common_sort_consecutive_landing_successes_desc),
    LEO_CAPACITY_ASC("leo_capacity", Res.string.rockets_sort_leo_capacity_asc),
    LEO_CAPACITY_DESC("-leo_capacity", Res.string.rockets_sort_leo_capacity_desc),
    GTO_CAPACITY_ASC("gto_capacity", Res.string.rockets_sort_gto_capacity_asc),
    GTO_CAPACITY_DESC("-gto_capacity", Res.string.rockets_sort_gto_capacity_desc),
    LAUNCH_MASS_ASC("launch_mass", Res.string.rockets_sort_launch_mass_asc),
    LAUNCH_MASS_DESC("-launch_mass", Res.string.rockets_sort_launch_mass_desc),
    LAUNCH_COST_ASC("launch_cost", Res.string.rockets_sort_launch_cost_asc),
    LAUNCH_COST_DESC("-launch_cost", Res.string.rockets_sort_launch_cost_desc),
    MAIDEN_FLIGHT_ASC("maiden_flight", Res.string.rockets_sort_maiden_flight_asc),
    MAIDEN_FLIGHT_DESC("-maiden_flight", Res.string.rockets_sort_maiden_flight_desc)
}
