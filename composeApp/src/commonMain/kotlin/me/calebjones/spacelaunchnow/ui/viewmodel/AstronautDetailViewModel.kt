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
import me.calebjones.spacelaunchnow.data.repository.AstronautRepository
import me.calebjones.spacelaunchnow.domain.model.AstronautDetail
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * ViewModel for displaying detailed astronaut information.
 *
 * Manages loading astronaut details, error states, and retry logic.
 */
class AstronautDetailViewModel(
    private val astronautRepository: AstronautRepository,
    private val analyticsManager: AnalyticsManager,
    private val astronautId: Int
) : ViewModel() {

    private val log = logger()

    private val _uiState = MutableStateFlow(AstronautDetailUiState())
    val uiState: StateFlow<AstronautDetailUiState> = _uiState.asStateFlow()

    init {
        loadAstronautDetail()
    }

    /**
     * Load detailed information about the astronaut.
     */
    private fun loadAstronautDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = astronautRepository.getAstronautDetail(astronautId)
                
                result.fold(
                    onSuccess = { astronaut ->
                        log.i { "✅ Loaded astronaut detail: ${astronaut.name} (ID: $astronautId)" }
                        _uiState.update {
                            it.copy(
                                astronaut = astronaut,
                                isLoading = false
                            )
                        }
                        analyticsManager.track(AnalyticsEvent.AstronautViewed(astronautId))
                    },
                    onFailure = { exception ->
                        log.e(exception) { "❌ Failed to load astronaut detail (ID: $astronautId)" }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load astronaut: ${exception.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                log.e(e) { "❌ Unexpected error loading astronaut detail (ID: $astronautId)" }
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
     * Retry loading astronaut details after a failure.
     */
    fun retry() {
        loadAstronautDetail()
    }

    /**
     * Clear any error messages.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI state for the astronaut detail screen.
 */
data class AstronautDetailUiState(
    val astronaut: AstronautDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
