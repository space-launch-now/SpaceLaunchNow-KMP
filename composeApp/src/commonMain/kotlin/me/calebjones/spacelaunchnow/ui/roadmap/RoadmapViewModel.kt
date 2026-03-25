package me.calebjones.spacelaunchnow.ui.roadmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.model.RoadmapCategory
import me.calebjones.spacelaunchnow.data.model.RoadmapData
import me.calebjones.spacelaunchnow.data.model.RoadmapItem
import me.calebjones.spacelaunchnow.data.model.RoadmapPriority
import me.calebjones.spacelaunchnow.data.model.RoadmapStatus
import me.calebjones.spacelaunchnow.data.repository.RemoteConfigRepository

data class RoadmapUiState(
    val isLoading: Boolean = true,
    val roadmapData: RoadmapData? = null,
    val errorMessage: String? = null
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
     * Falls back to placeholder data if remote config fails
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
            
            // If remote data is empty or failed, use placeholder
            val finalData = if (roadmapData == null || roadmapData.items.isEmpty()) {
                loadPlaceholderData()
            } else {
                roadmapData
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                roadmapData = finalData
            )
        } catch (e: Exception) {
            handleError(e)
        }
    }
    
    private fun handleError(e: Throwable?) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            roadmapData = loadPlaceholderData(),
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
     * Placeholder data for fallback when remote config is unavailable
     * This data structure matches what Firebase Remote Config provides
     */
    private fun loadPlaceholderData(): RoadmapData {
        return RoadmapData(
            items = listOf(
                RoadmapItem(
                    id = "1",
                    title = "Schedule page Sorting",
                    description = "Customizable schedule page with advanced sorting options",
                    status = RoadmapStatus.COMPLETED,
                    quarter = "Early December 2025",
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.HIGH
                ),
                RoadmapItem(
                    id = "2",
                    title = "Starship | Rockets | Agencies | ISS Tracking",
                    description = "New pages with additional content",
                    status = RoadmapStatus.IN_TESTING,
                    quarter = "February 2026",
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.MEDIUM
                ),
                RoadmapItem(
                    id = "3",
                    title = "Notification Filter Issues",
                    description = "Users receiving notifications for locations they did not select",
                    status = RoadmapStatus.IN_PROGRESS,
                    quarter = "February 2026",
                    category = RoadmapCategory.BUG_FIX,
                    priority = RoadmapPriority.HIGH
                ),
                RoadmapItem(
                    id = "4",
                    title = "Initial iOS Launch",
                    description = "MVP release of the all new Space Launch Now app for iOS devices",
                    status = RoadmapStatus.IN_PROGRESS,
                    quarter = "Q1 2026",
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.HIGH
                ),
                RoadmapItem(
                    id = "5",
                    title = "Astronauts Profiles",
                    description = "Profiles for astronauts with mission history and biographies",
                    status = RoadmapStatus.BACKLOG,
                    quarter = "Q1 2026",
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.MEDIUM
                ),
                RoadmapItem(
                    id = "6",
                    title = "Launch Vehicles Page",
                    description = "Detailed pages for launch vehicles with specifications and images",
                    status = RoadmapStatus.BACKLOG,
                    quarter = "Early 2026",
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.LOW
                ),
                RoadmapItem(
                    id = "7",
                    title = "Additional Widgets",
                    description = "More home screen widgets with customizable themes",
                    status = RoadmapStatus.BACKLOG,
                    quarter = "Early 2026",
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.MEDIUM
                )
            ),
            lastUpdated = "February 2026",
            message = "This roadmap represents planned features and is subject to change based on user feedback and priorities."
        )
    }

    /**
     * Clear any error messages
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
