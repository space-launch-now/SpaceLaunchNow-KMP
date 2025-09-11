package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.cache.LaunchCache

class LaunchViewModel(
    private val repository: LaunchRepository,
    private val launchCache: LaunchCache
) : ViewModel() {

    private val _upcomingLaunches = MutableStateFlow<PaginatedLaunchBasicList?>(null)
    val upcomingLaunches: StateFlow<PaginatedLaunchBasicList?> = _upcomingLaunches

    private val _upcomingLaunchesNormal = MutableStateFlow<PaginatedLaunchNormalList?>(null)
    val upcomingLaunchesNormal: StateFlow<PaginatedLaunchNormalList?> = _upcomingLaunchesNormal

    private val _launchDetails = MutableStateFlow<LaunchDetailed?>(null)
    val launchDetails: StateFlow<LaunchDetailed?> = _launchDetails

    private val _agencyDataMap = MutableStateFlow<Map<Int, AgencyEndpointDetailed>>(emptyMap())
    val agencyDataMap: StateFlow<Map<Int, AgencyEndpointDetailed>> = _agencyDataMap

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchUpcomingLaunchesNormal(limit: Int) {
        viewModelScope.launch {
            val result = repository.getUpcomingLaunchesNormal(limit = limit)
            
            result.onSuccess { launches ->
                _upcomingLaunchesNormal.value = launches
            }.onFailure { exception ->
                _error.value = exception.message
            }
        }
    }

    fun fetchLaunchDetails(id: String) {
        viewModelScope.launch {
            _error.value = null
            
            // Check if we have detailed data in cache first
            val cachedDetailed = launchCache.getCachedLaunchDetailed(id)
            if (cachedDetailed != null) {
                _launchDetails.value = cachedDetailed
                _isLoading.value = false
                return@launch
            }
            
            _isLoading.value = true
            
            val result = repository.getLaunchDetails(id)
            result.onSuccess { launch ->
                _launchDetails.value = launch
                // Cache the detailed data for future use
                launchCache.cacheLaunchDetailed(launch)
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }
    
    /**
     * Set launch details directly (used when we have preloaded data)
     */
    fun setLaunchDetails(launchDetailed: LaunchDetailed) {
        _launchDetails.value = launchDetailed
        _error.value = null
        _isLoading.value = false
    }

    /**
     * Get cached launch normal data if available. This can be used by the UI
     * to show basic information while detailed data is being fetched.
     */
    fun getCachedLaunchNormal(id: String): LaunchNormal? {
        return launchCache.getCachedLaunchNormal(id)
    }

    fun fetchAgencyData(agencyId: Int) {
        viewModelScope.launch {
            if (_agencyDataMap.value.containsKey(agencyId)) {
                // Data already fetched, no need to fetch again
                return@launch
            }
    
            val result = repository.getAgencyDetails(agencyId)
            result.onSuccess { agencyData ->
                _agencyDataMap.value = _agencyDataMap.value.toMutableMap().apply {
                    put(agencyId, agencyData)
                }
            }.onFailure { exception ->
                _error.value = exception.message
            }
        }
    }
}
