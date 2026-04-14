package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.cache.LaunchCache
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.services.LaunchFilterService
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Manages launch data for the home screen including:
 * - Featured launch (next upcoming)
 * - Upcoming launches list
 * - Previous launches list
 * - Combined carousel data
 * 
 * This ViewModel follows the Single Responsibility Principle by focusing only on launch data.
 * It uses the ViewState pattern for consistent state management and derived states for
 * automatic synchronization.
 */
class LaunchesViewModel(
    private val launchRepository: LaunchRepository,
    private val launchFilterService: LaunchFilterService,
    private val notificationStateStorage: NotificationStateStorage,
    private val launchCache: LaunchCache
) : ViewModel() {

    private val log = logger()

    // ========== ViewState Properties ==========

    // Featured Launch State (uses dedicated API call with upcomingWithRecent filter)
    // Initialize with isLoading=true to show shimmer instead of empty state before first load
    private val _featuredLaunchState = MutableStateFlow(ViewState<LaunchNormal?>(data = null, isLoading = true))
    val featuredLaunchState: StateFlow<ViewState<LaunchNormal?>> = _featuredLaunchState.asStateFlow()

    private val _upcomingLaunchesState = MutableStateFlow(ViewState(data = emptyList<LaunchNormal>()))
    val upcomingLaunchesState: StateFlow<ViewState<List<LaunchNormal>>> = _upcomingLaunchesState.asStateFlow()

    private val _previousLaunchesState = MutableStateFlow(ViewState(data = emptyList<LaunchNormal>()))
    val previousLaunchesState: StateFlow<ViewState<List<LaunchNormal>>> = _previousLaunchesState.asStateFlow()

    // ========== Derived States (Automatic Computation) ==========

    /**
     * Upcoming launches for carousel (uses full upcoming list, not derived from featured)
     */
    val upcomingForCarousel: StateFlow<List<LaunchNormal>> = _upcomingLaunchesState
        .map { it.data }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Combined launches for bidirectional carousel (previous + upcoming)
     * Previous launches are reversed so most recent appears first in the carousel.
     * Deduplicated by ID to handle edge cases where a launch appears in both lists.
     */
    val combinedLaunches: StateFlow<List<LaunchNormal>> = combine(
        _previousLaunchesState,
        upcomingForCarousel
    ) { previousState, upcomingList ->
        (previousState.data.reversed() + upcomingList).distinctBy { it.id }
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

    // ========== Public API ==========

    /**
     * Loads featured launch using dedicated API call with upcomingWithRecent filter.
     * This ensures we always have a featured launch to display, even if upcoming filters
     * would eliminate all results.
     * 
     * @param forceRefresh If true, bypass cache (user-initiated refresh)
     */
    fun loadFeaturedLaunch(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                log.d { "Loading featured launch - forceRefresh: $forceRefresh" }

                _featuredLaunchState.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }
                log.d { "Set isLoading=true for featured launch state" }

                // Wait for actual filter settings from DataStore
                val currentFilters = notificationStateStorage.stateFlow.first()
                val filterParams = launchFilterService.getFilterParams(currentFilters)
                log.v { "Filter params - agencyIds: ${filterParams.agencyIds}, locationIds: ${filterParams.locationIds}" }

                log.d { "Calling repository.getFeaturedLaunch with upcomingWithRecent filter..." }
                val result = launchRepository.getFeaturedLaunch(
                    forceRefresh = forceRefresh,
                    agencyIds = filterParams.agencyIds,
                    locationIds = filterParams.locationIds
                )
                log.d { "Repository call completed. Success: ${result.isSuccess}" }

                result.onSuccess { dataResult ->
                    val paginatedLaunches = dataResult.data
                    log.i { "Repository success - Data source: ${dataResult.source}, Results: ${paginatedLaunches.results.size}" }
                    log.v { "Cache timestamp: ${dataResult.timestamp}" }

                    val firstLaunch = paginatedLaunches.results.firstOrNull()
                    if (firstLaunch != null) {
                        log.i { "Featured launch: ${firstLaunch.name} (ID: ${firstLaunch.id})" }
                    } else {
                        log.w { "No launches returned from repository!" }
                    }
                    
                    _featuredLaunchState.update {
                        it.copy(
                            data = firstLaunch,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }
                    log.d { "Updated featuredLaunchState: hasData=${_featuredLaunchState.value.data != null}, isLoading=${_featuredLaunchState.value.isLoading}" }

                    // Pre-fetch detailed data if we have a launch
                    firstLaunch?.let { launch ->
                        log.d { "Pre-fetching launch details for ${launch.id}..." }
                        preFetchLaunchDetails(launch.id)
                    }
                }.onFailure { exception ->
                    log.e(exception) { "Repository failure: ${exception.message}" }
                    val errorMsg = formatErrorMessage(exception)

                    _featuredLaunchState.update {
                        it.copy(
                            error = errorMsg,
                            isLoading = false
                        )
                    }
                    log.d { "Updated featuredLaunchState with error, isLoading=false" }
                }
            } catch (exception: Exception) {
                log.e(exception) { "Exception in loadFeaturedLaunch: ${exception.message}" }

                _featuredLaunchState.update {
                    it.copy(
                        error = exception.message ?: "Unknown error",
                        isLoading = false
                    )
                }
                log.d { "Updated featuredLaunchState with exception error, isLoading=false" }
            }
        }
    }

    /**
     * Loads upcoming and previous launches in parallel.
     * Also loads featured launch separately to ensure it's always available.
     * This is the main entry point for loading launch data.
     * 
     * @param upcomingLimit Number of upcoming launches to load (default: 10)
     * @param previousLimit Number of previous launches to load (default: 5)
     * @param forceRefresh If true, bypass cache (user-initiated refresh)
     */
    fun loadLaunches(
        upcomingLimit: Int = 10,
        previousLimit: Int = 5,
        forceRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                // Update states: loading started
                _upcomingLaunchesState.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }
                _previousLaunchesState.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }

                // Wait for actual filter settings from DataStore
                val currentFilters = notificationStateStorage.stateFlow.first()
                log.v { "Filter settings - followAllLaunches: ${currentFilters.followAllLaunches}, subscribedAgencies: ${currentFilters.subscribedAgencies}, subscribedLocations: ${currentFilters.subscribedLocations}" }

                val filterParams = launchFilterService.getFilterParams(currentFilters)
                log.d { "Filter params - agencyIds: ${filterParams.agencyIds}, locationIds: ${filterParams.locationIds}" }

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
                    _upcomingLaunchesState.update {
                        it.copy(
                            data = dataResult.data.results,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }
                }.onFailure { exception ->
                    _upcomingLaunchesState.update {
                        it.copy(
                            error = formatErrorMessage(exception),
                            isLoading = false
                        )
                    }
                }

                // Handle previous launches result
                previousDeferred.await().onSuccess { dataResult ->
                    _previousLaunchesState.update {
                        it.copy(
                            data = dataResult.data.results,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }
                }.onFailure { exception ->
                    _previousLaunchesState.update {
                        it.copy(
                            error = formatErrorMessage(exception),
                            isLoading = false
                        )
                    }
                }
            } catch (exception: Exception) {
                _upcomingLaunchesState.update {
                    it.copy(
                        error = exception.message ?: "Unknown error",
                        isLoading = false
                    )
                }
                _previousLaunchesState.update {
                    it.copy(
                        error = exception.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Loads only upcoming launches. Used on initial home mount to defer the previous
     * launches API call until the LastLaunchCard scrolls into view.
     */
    fun loadUpcomingLaunches(limit: Int = 10, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _upcomingLaunchesState.update { it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null) }
                val filterParams = loadFilters()
                launchRepository.getUpcomingLaunchesNormal(
                    limit = limit,
                    forceRefresh = forceRefresh,
                    agencyIds = filterParams.agencyIds,
                    locationIds = filterParams.locationIds
                ).onSuccess { dataResult ->
                    _upcomingLaunchesState.update {
                        it.copy(
                            data = dataResult.data.results,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }
                }.onFailure { exception ->
                    _upcomingLaunchesState.update { it.copy(error = formatErrorMessage(exception), isLoading = false) }
                }
            } catch (exception: Exception) {
                _upcomingLaunchesState.update { it.copy(error = exception.message ?: "Unknown error", isLoading = false) }
            }
        }
    }

    /**
     * Loads only previous launches. Called lazily when the LastLaunchCard scrolls into view.
     * On phone layout this item never renders, saving one API call per home mount.
     */
    fun loadPreviousLaunches(limit: Int = 5, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _previousLaunchesState.update { it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null) }
                val filterParams = loadFilters()
                launchRepository.getPreviousLaunchesNormal(
                    limit = limit,
                    forceRefresh = forceRefresh,
                    agencyIds = filterParams.agencyIds,
                    locationIds = filterParams.locationIds
                ).onSuccess { dataResult ->
                    _previousLaunchesState.update {
                        it.copy(
                            data = dataResult.data.results,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }
                }.onFailure { exception ->
                    _previousLaunchesState.update { it.copy(error = formatErrorMessage(exception), isLoading = false) }
                }
            } catch (exception: Exception) {
                _previousLaunchesState.update { it.copy(error = exception.message ?: "Unknown error", isLoading = false) }
            }
        }
    }

    /**
     * Refreshes all launch data (user-initiated)
     */
    fun refresh() = loadLaunches(forceRefresh = true)

    // ========== Private Helper Methods ==========

    private suspend fun loadFilters() =
        launchFilterService.getFilterParams(notificationStateStorage.stateFlow.first())

    /**
     * Pre-fetches detailed launch data in the background to prepare for instant loading
     * when user clicks "Explore" on the featured launch
     */
    private fun preFetchLaunchDetails(launchId: String) {
        viewModelScope.launch {
            try {
                log.d { "Pre-fetching detailed data for launch: $launchId" }
                val result = launchRepository.getLaunchDetails(launchId)

                result.onSuccess { launchDetailed ->
                    // Cache the detailed launch data for instant access later
                    launchCache.cacheLaunchDetailed(launchDetailed)
                    log.d { "Successfully pre-fetched and cached detailed data for launch: ${launchDetailed.name}" }
                }.onFailure { exception ->
                    log.w(exception) { "Failed to pre-fetch detailed data for launch $launchId" }
                    // Don't show error to user since this is background prefetch
                    // The detail screen will handle the error when the user actually navigates to it
                }
            } catch (exception: Exception) {
                log.w(exception) { "Exception during pre-fetch for launch $launchId" }
                // Silently fail - this is a background optimization, not critical
            }
        }
    }

    /**
     * Formats error messages consistently
     */
    private fun formatErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("throttled") == true ->
                "API rate limit exceeded. Please try again later."

            exception.message?.contains("API Error") == true ->
                exception.message!!.substringAfter("API Error: ")

            else -> exception.message ?: "Unknown error occurred"
        }
    }
}
