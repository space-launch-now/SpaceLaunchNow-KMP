package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.data.repository.AgencyRepository
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * ViewModel for displaying a paginated list of agencies.
 *
 * Manages loading, pagination, error states, search functionality,
 * and filter options for the agency list screen.
 */
class AgencyListViewModel(
    private val agencyRepository: AgencyRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val log = logger()
    private val loadMutex = Mutex() // 🔒 Prevents concurrent initial loads

    private val _uiState = MutableStateFlow(AgencyListUiState())
    val uiState: StateFlow<AgencyListUiState> = _uiState.asStateFlow()

    init {
        log.w { "🚨 AgencyListViewModel init block called - ViewModel instance: ${this.hashCode()}" }
        loadAgencies()
    }

    /**
     * Load the first page of agencies.
     */
    fun loadAgencies() {
        log.i { "🔄 loadAgencies called from ViewModel ${this.hashCode()}" }
        
        viewModelScope.launch {
            // 🔒 Use mutex to prevent concurrent loads
            if (!loadMutex.tryLock()) {
                log.w { "⚠️ Already loading agencies, skipping duplicate call" }
                return@launch
            }
            
            try {
                _uiState.update { it.copy(isLoading = true, error = null, agencies = emptyList(), currentPage = 0) }
                
                val currentState = _uiState.value
                log.d { "📡 Making API call to getAgencies..." }
                val result = agencyRepository.getAgenciesDomain(
                    limit = PAGE_SIZE,
                    offset = 0,
                    ordering = currentState.selectedSortOrder.apiValue,
                    search = currentState.searchQuery.ifBlank { null },
                    featured = currentState.featuredFilter,
                    typeId = currentState.selectedTypeId,
                    countryCode = currentState.selectedCountryCodes.ifEmpty { null }
                )
                
                result.fold(
                    onSuccess = { paginatedList ->
                        log.i { "✅ Loaded ${paginatedList.results.size} agencies (page 1)" }
                        _uiState.update {
                            it.copy(
                                agencies = paginatedList.results,
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
                        log.e(exception) { "❌ Failed to load agencies" }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load agencies: ${exception.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                log.e(e) { "❌ Unexpected error loading agencies" }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
            } finally {
                // 🔓 Always unlock the mutex
                loadMutex.unlock()
            }
        }
    }

    /**
     * Load the next page of agencies (pagination).
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
                
                val result = agencyRepository.getAgenciesDomain(
                    limit = PAGE_SIZE,
                    offset = offset,
                    ordering = currentState.selectedSortOrder.apiValue,
                    search = currentState.searchQuery.ifBlank { null },
                    featured = currentState.featuredFilter,
                    typeId = currentState.selectedTypeId,
                    countryCode = currentState.selectedCountryCodes.ifEmpty { null }
                )
                
                result.fold(
                    onSuccess = { paginatedList ->
                        log.i { "✅ Loaded ${paginatedList.results.size} more agencies (page $nextPage)" }
                        _uiState.update {
                            it.copy(
                                agencies = it.agencies + paginatedList.results,
                                isLoadingMore = false,
                                currentPage = nextPage,
                                hasMore = paginatedList.next != null
                            )
                        }
                    },
                    onFailure = { exception ->
                        log.e(exception) { "❌ Failed to load more agencies" }
                        _uiState.update {
                            it.copy(
                                isLoadingMore = false,
                                error = "Failed to load more: ${exception.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                log.e(e) { "❌ Unexpected error loading more agencies" }
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
     * Refresh the agency list (pull-to-refresh).
     */
    fun refresh() {
        val currentState = _uiState.value
        _uiState.update {
            AgencyListUiState(
                searchQuery = currentState.searchQuery,
                selectedSortOrder = currentState.selectedSortOrder,
                selectedTypeId = currentState.selectedTypeId,
                selectedCountryCodes = currentState.selectedCountryCodes,
                featuredFilter = currentState.featuredFilter
            )
        }
        loadAgencies()
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
    fun updateSortOrder(sortOrder: AgencySortOrder) {
        _uiState.update { it.copy(selectedSortOrder = sortOrder) }
        analyticsManager.track(AnalyticsEvent.FilterChanged(
            filterType = "agency_sort",
            value = sortOrder.apiValue
        ))
        refresh()
    }

    /**
     * Update the type filter and reload the list.
     */
    fun updateTypeFilter(typeId: Int?) {
        _uiState.update { it.copy(selectedTypeId = typeId) }
        analyticsManager.track(AnalyticsEvent.FilterChanged(
            filterType = "agency_type",
            value = typeId?.toString() ?: "all"
        ))
        refresh()
    }

    /**
     * Update the country codes filter and reload the list.
     */
    fun updateCountryFilter(countryCodes: List<String>) {
        _uiState.update { it.copy(selectedCountryCodes = countryCodes) }
        analyticsManager.track(AnalyticsEvent.FilterChanged(
            filterType = "agency_country",
            value = countryCodes.joinToString(",").ifEmpty { "all" }
        ))
        refresh()
    }

    /**
     * Update the featured filter and reload the list.
     */
    fun updateFeaturedFilter(featured: Boolean?) {
        _uiState.update { it.copy(featuredFilter = featured) }
        analyticsManager.track(AnalyticsEvent.FilterChanged(
            filterType = "agency_featured",
            value = featured?.toString() ?: "all"
        ))
        refresh()
    }

    /**
     * Clear all filters and search.
     */
    fun clearAllFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedTypeId = null,
                selectedCountryCodes = emptyList(),
                selectedSortOrder = AgencySortOrder.NAME_ASC,
                featuredFilter = null
            )
        }
        refresh()
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}

/**
 * UI state for the agency list screen.
 */
data class AgencyListUiState(
    val agencies: List<Agency> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 0,
    val hasMore: Boolean = true,
    val totalCount: Int = 0,
    val searchQuery: String = "",
    val selectedSortOrder: AgencySortOrder = AgencySortOrder.NAME_ASC,
    val selectedTypeId: Int? = null,
    val selectedCountryCodes: List<String> = emptyList(),
    val featuredFilter: Boolean? = true
)

/**
 * Sort order options for agency list.
 */
enum class AgencySortOrder(val apiValue: String, val displayName: String) {
    NAME_ASC("name", "Name (A-Z)"),
    NAME_DESC("-name", "Name (Z-A)"),
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
    CONSECUTIVE_SUCCESSFUL_LANDINGS_DESC("-consecutive_successful_landings", "Consecutive Landing Successes (High to Low)")
}
