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

data class RoadmapUiState(
    val isLoading: Boolean = true,
    val roadmapData: RoadmapData? = null,
    val errorMessage: String? = null
)

/**
 * ViewModel for the Roadmap screen
 *
 * Current implementation uses hardcoded placeholder data.
 *
 * TODO: Firebase Remote Config Integration
 * To enable remote configuration:
 * 1. Add Firebase Remote Config dependency to build.gradle.kts
 * 2. Create RemoteConfigRepository in data/repository/
 * 3. Inject repository into this ViewModel
 * 4. Replace loadPlaceholderData() with fetchRemoteRoadmap()
 *
 * See docs/features/ROADMAP_FIREBASE_SETUP.md for detailed instructions
 */
class RoadmapViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RoadmapUiState())
    val uiState: StateFlow<RoadmapUiState> = _uiState.asStateFlow()

    init {
        loadRoadmap()
    }

    /**
     * Load roadmap data
     * Currently loads placeholder data
     * Future: Fetch from Firebase Remote Config or API
     */
    private fun loadRoadmap() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // TODO: Replace with Firebase Remote Config fetch
                // val remoteData = remoteConfigRepository.getRoadmapData()
                val placeholderData = loadPlaceholderData()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    roadmapData = placeholderData
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load roadmap: ${e.message}"
                )
            }
        }
    }

    /**
     * Refresh roadmap data
     * Call this to manually refresh from remote config
     */
    fun refresh() {
        loadRoadmap()
    }

    /**
     * Placeholder data for initial implementation
     * This data structure matches what Firebase Remote Config would provide
     */
    private fun loadPlaceholderData(): RoadmapData {
        return RoadmapData(
            items = listOf(
                RoadmapItem(
                    id = "1",
                    title = "Initial Android Launch",
                    description = "MVP release of the all new Space Launch Now app with core features",
                    status = RoadmapStatus.COMPLETED,
                    quarter = "Nov. 2025",
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.HIGH
                ),
                RoadmapItem(
                    id = "2",
                    title = "Home and Notification Filtering",
                    description = "Customizable home screen and notification filtering options",
                    status = RoadmapStatus.COMPLETED,
                    quarter = "November 25th 2025",
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.HIGH
                ),
                RoadmapItem(
                    id = "3",
                    title = "Starship Screen",
                    description = "Implement dedicated Starship vehicle page with launch history and stats",
                    status = RoadmapStatus.IN_PROGRESS,
                    quarter = "End of 2025",
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.MEDIUM
                ),
                RoadmapItem(
                    id = "4",
                    title = "Schedule page Sorting",
                    description = "Customizable schedule page with advanced sorting options",
                    status = RoadmapStatus.IN_PROGRESS,
                    quarter = "Early December 2025",
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.HIGH
                ),
                RoadmapItem(
                    id = "5",
                    title = "Initial iOS Launch",
                    description = "MVP release of the all new Space Launch Now app for iOS devices",
                    status = RoadmapStatus.IN_PROGRESS,
                    quarter = "End of 2025",
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.HIGH
                ),

                RoadmapItem(
                    id = "6",
                    title = "Astronauts Profiles",
                    description = "Profiles for astronauts with mission history and biographies",
                    status = RoadmapStatus.BACKLOG,
                    quarter = "Early 2026",
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.MEDIUM
                ),
                RoadmapItem(
                    id = "7",
                    title = "Launch Vehicles Page",
                    description = "Detailed pages for launch vehicles with specifications and images",
                    status = RoadmapStatus.BACKLOG,
                    quarter = "Early 2026",
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.LOW
                ),
                RoadmapItem(
                    id = "8",
                    title = "Agencies Page",
                    description = "Detailed pages for space agencies with mission profiles and launch history",
                    status = RoadmapStatus.BACKLOG,
                    quarter = "Early 2026",
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.LOW
                ),
                RoadmapItem(
                    id = "9",
                    title = "Additional Widgets",
                    description = "More home screen widgets with customizable themes",
                    status = RoadmapStatus.BACKLOG,
                    quarter = "Early 2026",
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.MEDIUM
                )
            ),
            lastUpdated = "November 2025",
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
