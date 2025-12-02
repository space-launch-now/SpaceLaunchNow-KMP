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
import me.calebjones.spacelaunchnow.util.logging.logger

class AgencyViewModel(
    private val repository: LaunchRepository,
    private val agencyRepository: AgencyRepository
) : ViewModel() {
    private val log = logger()

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
            log.d { "Fetching agencies - limit: $limit, offset: $offset" }
            _error.value = null
            _isLoading.value = true

            val result = agencyRepository.getAgencies(limit, offset)
            result.onSuccess { paginatedList ->
                log.i { "Successfully loaded ${paginatedList.results.size} agencies" }
                _agencies.value = paginatedList.results
            }.onFailure { exception ->
                log.e(exception) { "Failed to fetch agencies" }
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun fetchAgencyDetails(id: Int) {
        viewModelScope.launch {
            log.d { "Fetching agency details for id: $id" }
            _error.value = null
            _isLoading.value = true

            val result = repository.getAgencyDetails(id)
            result.onSuccess { agency ->
                log.i { "Successfully loaded agency details: ${agency.name}" }
                _agencyDetails.value = agency
            }.onFailure { exception ->
                log.e(exception) { "Failed to fetch agency details for id: $id" }
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun searchAgencies(query: String, limit: Int = 50) {
        viewModelScope.launch {
            log.d { "Searching agencies with query: '$query', limit: $limit" }
            _error.value = null
            _isLoading.value = true

            val result = agencyRepository.searchAgencies(query, limit)
            result.onSuccess { paginatedList ->
                log.i { "Search returned ${paginatedList.results.size} agencies for query: '$query'" }
                _agencies.value = paginatedList.results
            }.onFailure { exception ->
                log.e(exception) { "Failed to search agencies with query: '$query'" }
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }
}
