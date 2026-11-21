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
import org.jetbrains.compose.resources.StringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item1_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item1_title
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item2_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item2_title
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item3_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item3_title
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item4_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item4_title
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item5_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item5_title
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item6_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item6_title
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item7_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item7_title
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_last_updated
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_message
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_quarter_early_2026
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_quarter_early_dec_2025
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_quarter_feb_2026
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_quarter_q1_2026

data class RoadmapUiState(
    val isLoading: Boolean = true,
    val roadmapData: RoadmapData? = null,
    /** Built-in fallback roadmap; text is a string resource, resolved by the screen. */
    val placeholderData: PlaceholderRoadmapData? = null,
    val errorMessage: String? = null
)

/** Fallback roadmap item. Texts are string resources so the view model holds no display text. */
data class PlaceholderRoadmapItem(
    val id: String,
    val title: StringResource,
    val description: StringResource,
    val status: RoadmapStatus,
    val quarter: StringResource,
    val category: RoadmapCategory,
    val priority: RoadmapPriority
)

/** Fallback roadmap, the typed twin of [RoadmapData] for text that comes from string resources. */
data class PlaceholderRoadmapData(
    val items: List<PlaceholderRoadmapItem>,
    val lastUpdated: StringResource,
    val message: StringResource
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
     * Falls back to placeholder data (see placeholderData) if remote config fails
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
            val useFallback = roadmapData == null || roadmapData.items.isEmpty()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                roadmapData = if (useFallback) null else roadmapData,
                placeholderData = if (useFallback) loadPlaceholderData() else null
            )
        } catch (e: Exception) {
            handleError(e)
        }
    }
    
    private fun handleError(e: Throwable?) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            roadmapData = null,
            placeholderData = loadPlaceholderData(),
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
    private fun loadPlaceholderData(): PlaceholderRoadmapData {
        return PlaceholderRoadmapData(
            items = listOf(
                PlaceholderRoadmapItem(
                    id = "1",
                    title = Res.string.app_roadmap_placeholder_item1_title,
                    description = Res.string.app_roadmap_placeholder_item1_description,
                    status = RoadmapStatus.COMPLETED,
                    quarter = Res.string.app_roadmap_placeholder_quarter_early_dec_2025,
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.HIGH
                ),
                PlaceholderRoadmapItem(
                    id = "2",
                    title = Res.string.app_roadmap_placeholder_item2_title,
                    description = Res.string.app_roadmap_placeholder_item2_description,
                    status = RoadmapStatus.IN_TESTING,
                    quarter = Res.string.app_roadmap_placeholder_quarter_feb_2026,
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.MEDIUM
                ),
                PlaceholderRoadmapItem(
                    id = "3",
                    title = Res.string.app_roadmap_placeholder_item3_title,
                    description = Res.string.app_roadmap_placeholder_item3_description,
                    status = RoadmapStatus.IN_PROGRESS,
                    quarter = Res.string.app_roadmap_placeholder_quarter_feb_2026,
                    category = RoadmapCategory.BUG_FIX,
                    priority = RoadmapPriority.HIGH
                ),
                PlaceholderRoadmapItem(
                    id = "4",
                    title = Res.string.app_roadmap_placeholder_item4_title,
                    description = Res.string.app_roadmap_placeholder_item4_description,
                    status = RoadmapStatus.IN_PROGRESS,
                    quarter = Res.string.app_roadmap_placeholder_quarter_q1_2026,
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.HIGH
                ),
                PlaceholderRoadmapItem(
                    id = "5",
                    title = Res.string.app_roadmap_placeholder_item5_title,
                    description = Res.string.app_roadmap_placeholder_item5_description,
                    status = RoadmapStatus.BACKLOG,
                    quarter = Res.string.app_roadmap_placeholder_quarter_q1_2026,
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.MEDIUM
                ),
                PlaceholderRoadmapItem(
                    id = "6",
                    title = Res.string.app_roadmap_placeholder_item6_title,
                    description = Res.string.app_roadmap_placeholder_item6_description,
                    status = RoadmapStatus.BACKLOG,
                    quarter = Res.string.app_roadmap_placeholder_quarter_early_2026,
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.LOW
                ),
                PlaceholderRoadmapItem(
                    id = "7",
                    title = Res.string.app_roadmap_placeholder_item7_title,
                    description = Res.string.app_roadmap_placeholder_item7_description,
                    status = RoadmapStatus.BACKLOG,
                    quarter = Res.string.app_roadmap_placeholder_quarter_early_2026,
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.MEDIUM
                )
            ),
            lastUpdated = Res.string.app_roadmap_placeholder_last_updated,
            message = Res.string.app_roadmap_placeholder_message
        )
    }

    /**
     * Clear any error messages
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
