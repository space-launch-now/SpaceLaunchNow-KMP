package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigNormal
import me.calebjones.spacelaunchnow.data.model.ManufacturerFilter
import me.calebjones.spacelaunchnow.data.model.RocketFilters
import me.calebjones.spacelaunchnow.data.model.SortField
import me.calebjones.spacelaunchnow.data.repository.RocketRepository
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class RocketViewModel(
    private val repository: RocketRepository
) : ViewModel() {
    private val log = logger()

    // Filter state
    private val _filters = MutableStateFlow(RocketFilters.DEFAULT)
    val filters: StateFlow<RocketFilters> = _filters
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Rocket data
    private val _rockets = MutableStateFlow<List<LauncherConfigNormal>>(emptyList())
    val rockets: StateFlow<List<LauncherConfigNormal>> = _rockets

    private val _rocketDetails = MutableStateFlow<LauncherConfigDetailed?>(null)
    val rocketDetails: StateFlow<LauncherConfigDetailed?> = _rocketDetails

    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore
    
    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore
    
    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    init {
        // Setup search debouncing (300ms)
        viewModelScope.launch {
            searchQuery
                .debounce(300.milliseconds)
                .distinctUntilChanged()
                .collectLatest { query ->
                    log.d { "Search query debounced: '$query'" }
                    _filters.value = _filters.value.copy(
                        searchQuery = query.takeIf { it.isNotEmpty() },
                        offset = 0 // Reset pagination on search
                    )
                    loadRockets()
                }
        }
    }

    /**
     * Update search query. Debouncing happens automatically in init block.
     */
    fun updateSearchQuery(query: String) {
        log.d { "Search query updated: '$query'" }
        _searchQuery.value = query
    }
    
    /**
     * Set sort field for rocket list.
     */
    fun setSortField(field: SortField) {
        log.d { "Setting sort field: ${field.displayName}" }
        _filters.value = _filters.value.copy(
            sortField = field,
            offset = 0 // Reset pagination on sort change
        )
        loadRockets()
    }
    
    /**
     * Clear all filters (search, active status).
     * Preserves sort preference.
     */
    fun clearFilters() {
        log.d { "Clearing all filters" }
        _filters.value = RocketFilters.DEFAULT.copy(sortField = _filters.value.sortField)
        _searchQuery.value = "" // Also clear search UI
        loadRockets()
    }
    
    /**
     * Load rockets with current filter state.
     */
    fun loadRockets() {
        viewModelScope.launch {
            log.d { "Loading rockets with filters: ${_filters.value}" }
            _error.value = null
            _isLoading.value = true

            val result = repository.getRocketsFiltered(_filters.value)
            result.onSuccess { paginatedList ->
                log.i { "Successfully loaded ${paginatedList.results.size} rockets" }
                _rockets.value = paginatedList.results
                _hasMore.value = paginatedList.next != null
                _totalCount.value = paginatedList.count
            }.onFailure { exception ->
                log.e(exception) { "Failed to load rockets" }
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }
    
    /**
     * Load next page of rockets (pagination).
     * Appends results to existing list.
     */
    fun loadNextPage() {
        if (!_hasMore.value || _isLoadingMore.value) {
            log.d { "Not loading next page - hasMore: ${_hasMore.value}, isLoadingMore: ${_isLoadingMore.value}" }
            return
        }
        
        viewModelScope.launch {
            log.d { "Loading next page - current offset: ${_filters.value.offset}" }
            _isLoadingMore.value = true
            
            val currentOffset = _filters.value.offset
            _filters.value = _filters.value.copy(
                offset = currentOffset + _filters.value.limit
            )
            
            val result = repository.getRocketsFiltered(_filters.value)
            result.onSuccess { paginatedList ->
                log.i { "Successfully loaded ${paginatedList.results.size} more rockets" }
                _rockets.value = _rockets.value + paginatedList.results // APPEND
                _hasMore.value = paginatedList.next != null
            }.onFailure { exception ->
                log.e(exception) { "Failed to load next page" }
                _error.value = exception.message
                // Rollback offset on failure
                _filters.value = _filters.value.copy(offset = currentOffset)
            }
            _isLoadingMore.value = false
        }
    }
    
    // Legacy methods maintained for backward compatibility
    
    fun fetchRockets(limit: Int = 20, offset: Int = 0) {
        _filters.value = RocketFilters.DEFAULT.copy(limit = limit, offset = offset)
        loadRockets()
    }

    fun fetchRocketDetails(id: Int) {
        viewModelScope.launch {
            log.d { "Fetching rocket details for id: $id" }
            _error.value = null
            _isLoading.value = true

            val result = repository.getRocketDetails(id)
            result.onSuccess { rocket ->
                log.i { "Successfully loaded rocket details: ${rocket.name}" }
                _rocketDetails.value = rocket
            }.onFailure { exception ->
                log.e(exception) { "Failed to fetch rocket details for id: $id" }
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun searchRockets(query: String, limit: Int = 20) {
        _searchQuery.value = query
        // Debouncing handled automatically in init block
    }
}
