package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointNormal
import me.calebjones.spacelaunchnow.data.repository.AstronautRepository
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * ViewModel for displaying a paginated list of astronauts.
 *
 * Manages loading, pagination, error states, and search functionality
 * for the astronaut list screen.
 */
class AstronautListViewModel(
    private val astronautRepository: AstronautRepository
) : ViewModel() {

    private val log = logger()

    private val _uiState = MutableStateFlow(AstronautListUiState())
    val uiState: StateFlow<AstronautListUiState> = _uiState.asStateFlow()

    init {
        loadAstronauts()
    }

    /**
     * Load the first page of astronauts.
     */
    fun loadAstronauts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = astronautRepository.getAstronauts(
                    limit = PAGE_SIZE,
                    offset = 0,
                    ordering = "name"
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
                    ordering = "name"
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
        _uiState.update {
            AstronautListUiState()
        }
        loadAstronauts()
    }

    /**
     * Clear any error messages.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
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
    val totalCount: Int = 0
)
