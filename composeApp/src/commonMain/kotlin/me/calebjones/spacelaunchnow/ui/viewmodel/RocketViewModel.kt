package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigNormal
import me.calebjones.spacelaunchnow.data.repository.RocketRepository
import me.calebjones.spacelaunchnow.util.logging.logger

class RocketViewModel(
    private val repository: RocketRepository
) : ViewModel() {
    private val log = logger()

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
            log.d { "Fetching rockets - limit: $limit, offset: $offset" }
            _error.value = null
            _isLoading.value = true

            val result = repository.getRockets(limit, offset)
            result.onSuccess { paginatedList ->
                log.i { "Successfully loaded ${paginatedList.results.size} rockets" }
                _rockets.value = paginatedList.results
            }.onFailure { exception ->
                log.e(exception) { "Failed to fetch rockets" }
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun fetchRocketDetails(id: Int) {
        viewModelScope.launch {
            log.d { "Fetching rocket details for id: $id" }
            _error.value = null
            _isLoading.value = true

            val result = repository.getRocketDetails(id)
            result.onSuccess { rocket ->
                log.i { "Successfully loaded rocket details: ${rocket.name}" }
                _rocketDetails.value = rocket
            }.onFailure { exception ->
                log.e(exception) { "Failed to fetch rocket details for id: $id" }
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun searchRockets(query: String, limit: Int = 20) {
        viewModelScope.launch {
            log.d { "Searching rockets with query: '$query', limit: $limit" }
            _error.value = null
            _isLoading.value = true

            val result = repository.searchRockets(query, limit)
            result.onSuccess { paginatedList ->
                log.i { "Search returned ${paginatedList.results.size} rockets for query: '$query'" }
                _rockets.value = paginatedList.results
            }.onFailure { exception ->
                log.e(exception) { "Failed to search rockets with query: '$query'" }
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }
}
