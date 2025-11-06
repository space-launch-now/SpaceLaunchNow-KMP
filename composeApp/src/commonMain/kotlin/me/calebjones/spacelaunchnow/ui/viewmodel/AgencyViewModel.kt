package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyEndpointDetailed
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository

class AgencyViewModel(
    private val repository: LaunchRepository
) : ViewModel() {

    private val _agencyDetails = MutableStateFlow<AgencyEndpointDetailed?>(null)
    val agencyDetails: StateFlow<AgencyEndpointDetailed?> = _agencyDetails

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

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
}
