package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.models.AgencyEndpointDetailed
import me.calebjones.spacelaunchnow.api.models.PaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.api.models.PaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository

class LaunchViewModel(private val repository: LaunchRepository) : ViewModel() {

    private val _upcomingLaunches = MutableStateFlow<PaginatedLaunchBasicList?>(null)
    val upcomingLaunches: StateFlow<PaginatedLaunchBasicList?> = _upcomingLaunches

    private val _upcomingLaunchesNormal = MutableStateFlow<PaginatedLaunchNormalList?>(null)
    val upcomingLaunchesNormal: StateFlow<PaginatedLaunchNormalList?> = _upcomingLaunchesNormal

    private val _launchDetails = MutableStateFlow<LaunchDetailed?>(null)
    val launchDetails: StateFlow<LaunchDetailed?> = _launchDetails

    private val _agencyDataMap = MutableStateFlow<Map<Int, AgencyEndpointDetailed>>(emptyMap())
    val agencyDataMap: StateFlow<Map<Int, AgencyEndpointDetailed>> = _agencyDataMap


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
            val result = repository.getLaunchDetails(id)
            result.onSuccess { launch ->
                _launchDetails.value = launch
            }.onFailure { exception ->
                _error.value = exception.message
            }
        }
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
