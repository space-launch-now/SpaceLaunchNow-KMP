package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.services.LaunchFilterService
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage

/**
 * Manages the bidirectional launch carousel (LaunchListView).
 * Handles upcoming and previous launches for the scrollable carousel.
 */
class LaunchCarouselViewModel(
    private val launchRepository: LaunchRepository,
    private val launchFilterService: LaunchFilterService,
    private val notificationStateStorage: NotificationStateStorage
) : ViewModel() {

    private val _upcomingLaunchesState = MutableStateFlow(ViewState(data = emptyList<LaunchNormal>()))
    val upcomingLaunchesState: StateFlow<ViewState<List<LaunchNormal>>> = _upcomingLaunchesState

    private val _previousLaunchesState = MutableStateFlow(ViewState(data = emptyList<LaunchNormal>()))
    val previousLaunchesState: StateFlow<ViewState<List<LaunchNormal>>> = _previousLaunchesState

    /**
     * Combined launches for bidirectional carousel (previous + upcoming)
     * Previous launches are reversed so most recent appears first in the carousel.
     */
    val combinedLaunches: StateFlow<List<LaunchNormal>> = combine(
        _previousLaunchesState,
        _upcomingLaunchesState
    ) { previousState, upcomingState ->
        previousState.data.reversed() + upcomingState.data
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Index where upcoming launches start in the combined carousel.
     * Used to scroll to the correct initial position.
     */
    val upcomingStartIndex: StateFlow<Int> = _previousLaunchesState
        .map { it.data.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /**
     * Combined loading state for carousel (loading if either upcoming or previous is loading)
     */
    val isCarouselLoading: StateFlow<Boolean> = combine(
        _previousLaunchesState,
        _upcomingLaunchesState
    ) { previousState, upcomingState ->
        previousState.isLoading || upcomingState.isLoading
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * Combined error state for carousel (show error if either fails)
     * Prioritizes upcoming launches error since that's more critical.
     */
    val carouselError: StateFlow<String?> = combine(
        _previousLaunchesState,
        _upcomingLaunchesState
    ) { previousState, upcomingState ->
        upcomingState.error ?: previousState.error
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Loads upcoming and previous launches in parallel.
     * 
     * @param upcomingLimit Number of upcoming launches to load (default: 8)
     * @param previousLimit Number of previous launches to load (default: 8)
     * @param forceRefresh If true, bypass cache (user-initiated refresh)
     */
    fun loadLaunches(
        upcomingLimit: Int = 8,
        previousLimit: Int = 8,
        forceRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                println("[CAROUSEL] === LaunchCarouselViewModel.loadLaunches START ===")
                
                // Update states: loading started
                _upcomingLaunchesState.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }
                _previousLaunchesState.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }

                // Wait for actual filter settings from DataStore
                val currentFilters = notificationStateStorage.stateFlow.first()
                println("[CAROUSEL] Filter Settings: followAll=${currentFilters.followAllLaunches}")
                
                val filterParams = launchFilterService.getFilterParams(currentFilters)
                println("[CAROUSEL] Filter params - agencyIds: ${filterParams.agencyIds}, locationIds: ${filterParams.locationIds}")

                // Load both in parallel for better performance
                val upcomingDeferred = async {
                    launchRepository.getUpcomingLaunchesNormal(
                        limit = upcomingLimit,
                        forceRefresh = forceRefresh,
                        agencyIds = filterParams.agencyIds,
                        locationIds = filterParams.locationIds
                    )
                }
                val previousDeferred = async {
                    launchRepository.getPreviousLaunchesNormal(
                        limit = previousLimit,
                        forceRefresh = forceRefresh,
                        agencyIds = filterParams.agencyIds,
                        locationIds = filterParams.locationIds
                    )
                }

                // Handle upcoming launches result
                upcomingDeferred.await().onSuccess { dataResult ->
                    println("[CAROUSEL] Upcoming launches loaded: ${dataResult.data.results.size} items")
                    _upcomingLaunchesState.update {
                        it.copy(
                            data = dataResult.data.results,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }
                }.onFailure { exception ->
                    println("[CAROUSEL] Upcoming launches failed: ${exception.message}")
                    _upcomingLaunchesState.update {
                        it.copy(
                            error = formatErrorMessage(exception),
                            isLoading = false
                        )
                    }
                }

                // Handle previous launches result
                previousDeferred.await().onSuccess { dataResult ->
                    println("[CAROUSEL] Previous launches loaded: ${dataResult.data.results.size} items")
                    _previousLaunchesState.update {
                        it.copy(
                            data = dataResult.data.results,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }
                }.onFailure { exception ->
                    println("[CAROUSEL] Previous launches failed: ${exception.message}")
                    _previousLaunchesState.update {
                        it.copy(
                            error = formatErrorMessage(exception),
                            isLoading = false
                        )
                    }
                }
                
                println("[CAROUSEL] === LaunchCarouselViewModel.loadLaunches END ===")
            } catch (exception: Exception) {
                println("[CAROUSEL] === EXCEPTION in loadLaunches ===")
                println("[CAROUSEL] ${exception.message}")
                exception.printStackTrace()
                
                _upcomingLaunchesState.update {
                    it.copy(error = exception.message ?: "Unknown error", isLoading = false)
                }
                _previousLaunchesState.update {
                    it.copy(error = exception.message ?: "Unknown error", isLoading = false)
                }
            }
        }
    }

    private fun formatErrorMessage(exception: Throwable): String {
        return exception.message ?: "An unknown error occurred"
    }

    fun refresh() = loadLaunches(forceRefresh = true)
}
