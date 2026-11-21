package me.calebjones.spacelaunchnow.ui.roadmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.model.RoadmapData
import me.calebjones.spacelaunchnow.data.repository.RemoteConfigRepository

data class RoadmapUiState(
    val isLoading: Boolean = true,
    val roadmapData: RoadmapData? = null,
    val errorMessage: String? = null,
    /** True when the screen must show the built-in fallback roadmap (see placeholderRoadmapData). */
    val usePlaceholder: Boolean = false
)

/**
 * ViewModel for the Roadmap screen
 * 
 * Fetches roadmap data from Firebase Remote Config with fallback to placeholder data.
 * Supports pull-to-refresh with force refresh capability.
 */
class RoadmapViewModel(
    private val remoteConfigRepository: RemoteConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoadmapUiState())
    val uiState: StateFlow<RoadmapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Set defaults before first fetch
            remoteConfigRepository.setDefaults()
            loadRoadmap(forceRefresh = false)
        }
    }

    /**
     * Load roadmap data from Firebase Remote Config
     * Falls back to the placeholder roadmap (usePlaceholder) if remote config fails
     */
    private suspend fun loadRoadmap(forceRefresh: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        try {
            // Fetch and activate remote config
            val fetchResult = remoteConfigRepository.fetchAndActivate(forceRefresh)
            
            // Check for fetch failure
            if (fetchResult.isFailure) {
                handleError(fetchResult.exceptionOrNull())
                return
            }
            
            // Get roadmap data from remote config
            val result = remoteConfigRepository.getRoadmapData()
            
            // Check for data fetch failure
            if (result.isFailure) {
                handleError(result.exceptionOrNull())
                return
            }
            
            val roadmapData = result.getOrNull()
            
            // If remote data is empty or failed, tell the screen to use the placeholder
            val useFallback = roadmapData == null || roadmapData.items.isEmpty()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                roadmapData = if (useFallback) null else roadmapData,
                usePlaceholder = useFallback
            )
        } catch (e: Exception) {
            handleError(e)
        }
    }
    
    private fun handleError(e: Throwable?) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            roadmapData = null,
            usePlaceholder = true,
            errorMessage = "Using cached data: ${e?.message ?: "Unknown error"}"
        )
    }

    /**
     * Refresh roadmap data with force refresh
     * Call this for pull-to-refresh to bypass cache
     */
    fun refresh() {
        viewModelScope.launch {
            loadRoadmap(forceRefresh = true)
        }
    }

    /**
     * Clear any error messages
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
