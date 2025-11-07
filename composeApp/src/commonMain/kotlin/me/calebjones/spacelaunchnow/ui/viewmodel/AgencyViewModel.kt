package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyNormal
import me.calebjones.spacelaunchnow.data.repository.AgencyRepository
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository

class AgencyViewModel(
    private val repository: LaunchRepository,
    private val agencyRepository: AgencyRepository
) : ViewModel() {

    private val _agencies = MutableStateFlow<List<AgencyNormal>>(emptyList())
    val agencies: StateFlow<List<AgencyNormal>> = _agencies

    private val _agencyDetails = MutableStateFlow<AgencyEndpointDetailed?>(null)
    val agencyDetails: StateFlow<AgencyEndpointDetailed?> = _agencyDetails

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchAgencies(limit: Int = 50, offset: Int = 0) {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true

            val result = agencyRepository.getAgencies(limit, offset)
            result.onSuccess { paginatedList ->
                _agencies.value = paginatedList.results
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun fetchAgencyDetails(id: Int) {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true

            val result = repository.getAgencyDetails(id)
            result.onSuccess { agency ->
                _agencyDetails.value = agency
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun searchAgencies(query: String, limit: Int = 50) {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true

            val result = agencyRepository.searchAgencies(query, limit)
            result.onSuccess { paginatedList ->
                _agencies.value = paginatedList.results
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }
}
