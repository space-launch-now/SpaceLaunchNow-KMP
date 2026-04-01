package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.cache.LaunchCache
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.services.LaunchFilterService
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Manages the featured launch display (NextUpView) and additional featured launches row.
 * Uses dedicated API call with upcomingWithRecent filter, fetching 4 launches total.
 * The first launch is displayed in the hero card, remaining 3 are shown in a horizontal row.
 */
class FeaturedLaunchViewModel(
    private val launchRepository: LaunchRepository,
    private val launchFilterService: LaunchFilterService,
    private val notificationStateStorage: NotificationStateStorage,
    private val launchCache: LaunchCache
) : ViewModel() {

    private val log = logger()

    // Featured Launch State (hero card - first result)
    // Initialize with isLoading=true to show shimmer instead of empty state before first load
    private val _featuredLaunchState =
        MutableStateFlow(ViewState<LaunchNormal?>(data = null, isLoading = true))
    val featuredLaunchState: StateFlow<ViewState<LaunchNormal?>> =
        _featuredLaunchState.asStateFlow()

    // Additional Featured Launches State (row of 3 - results 2-4)
    // Initialize with isLoading=true to show shimmer instead of empty state before first load
    private val _additionalFeaturedLaunches =
        MutableStateFlow(ViewState<List<LaunchNormal>>(data = emptyList(), isLoading = true))
    val additionalFeaturedLaunches: StateFlow<ViewState<List<LaunchNormal>>> =
        _additionalFeaturedLaunches.asStateFlow()

    /**
     * Loads featured launches using dedicated API call with upcomingWithRecent filter.
     * Fetches 4 launches: first for hero card, remaining 3 for additional featured row.
     *
     * @param forceRefresh If true, bypass cache (user-initiated refresh)
     */
    fun loadFeaturedLaunch(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                log.d { "Loading featured launches (4 total) - forceRefresh: $forceRefresh" }

                _featuredLaunchState.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }
                _additionalFeaturedLaunches.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }
                log.d { "Set isLoading=true for featured launch states" }

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

                    val allLaunches = paginatedLaunches.results
                    val firstLaunch = allLaunches.firstOrNull()
                    val additionalLaunches =
                        if (allLaunches.size > 1) allLaunches.drop(1).take(3) else emptyList()

                    if (firstLaunch != null) {
                        log.i { "Featured launch: ${firstLaunch.name} (ID: ${firstLaunch.id})" }
                        log.i { "Additional featured launches: ${additionalLaunches.size}" }
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

                    _additionalFeaturedLaunches.update {
                        it.copy(
                            data = additionalLaunches,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }
                    log.d { "Updated featuredLaunchState: hasData=${_featuredLaunchState.value.data != null}, additionalCount=${additionalLaunches.size}" }

                    // If we got stale data and didn't already force refresh, trigger a background refresh
                    if (dataResult.source == DataSource.STALE_CACHE && !forceRefresh) {
                        log.i { "Received stale cache data, triggering background refresh" }
                        viewModelScope.launch {
                            loadFeaturedLaunch(forceRefresh = true)
                        }
                    }

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
                    _additionalFeaturedLaunches.update {
                        it.copy(
                            error = errorMsg,
                            isLoading = false
                        )
                    }
                    log.d { "Updated featuredLaunchState with error, isLoading=false" }
                }
            } catch (exception: Exception) {
                log.e(exception) { "Exception in loadFeaturedLaunch: ${exception.message}" }

                val errorMsg = exception.message ?: "Unknown error"
                _featuredLaunchState.update {
                    it.copy(
                        error = errorMsg,
                        isLoading = false
                    )
                }
                _additionalFeaturedLaunches.update {
                    it.copy(
                        error = errorMsg,
                        isLoading = false
                    )
                }
                log.d { "Updated featuredLaunchState with exception error, isLoading=false" }
            }
        }
    }

    private suspend fun preFetchLaunchDetails(launchId: String) {
        try {
            // Pre-cache the detailed launch data for faster detail screen loading
            launchCache.getCachedLaunchDetailed(launchId)
        } catch (e: Exception) {
            log.w(e) { "Failed to pre-fetch launch details" }
        }
    }

    private fun formatErrorMessage(exception: Throwable): String {
        return exception.message ?: "An unknown error occurred"
    }

    fun refresh() = loadFeaturedLaunch(forceRefresh = true)
}
