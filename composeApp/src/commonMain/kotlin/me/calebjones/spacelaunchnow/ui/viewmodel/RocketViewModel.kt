package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigNormal
import me.calebjones.spacelaunchnow.data.repository.RocketRepository

class RocketViewModel(
    private val repository: RocketRepository
) : ViewModel() {

    private val _rockets = MutableStateFlow<List<LauncherConfigNormal>>(emptyList())
    val rockets: StateFlow<List<LauncherConfigNormal>> = _rockets

    private val _rocketDetails = MutableStateFlow<LauncherConfigDetailed?>(null)
    val rocketDetails: StateFlow<LauncherConfigDetailed?> = _rocketDetails

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchRockets(limit: Int = 20, offset: Int = 0) {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true

            val result = repository.getRockets(limit, offset)
            result.onSuccess { paginatedList ->
                _rockets.value = paginatedList.results
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun fetchRocketDetails(id: Int) {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true

            val result = repository.getRocketDetails(id)
            result.onSuccess { rocket ->
                _rocketDetails.value = rocket
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun searchRockets(query: String, limit: Int = 20) {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true

            val result = repository.searchRockets(query, limit)
            result.onSuccess { paginatedList ->
                _rockets.value = paginatedList.results
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }
}
