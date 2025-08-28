package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.models.PaginatedUpdateEndpointList
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepository

class UpdatesViewModel(private val repository: UpdatesRepository) : ViewModel() {

    private val _updates = MutableStateFlow<PaginatedUpdateEndpointList?>(null)
    val updates: StateFlow<PaginatedUpdateEndpointList?> = _updates

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchLatestUpdates(limit: Int = 10) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val result = repository.getLatestUpdates(limit = limit)
            
            result.onSuccess { updates ->
                _updates.value = updates
            }.onFailure { exception ->
                _error.value = exception.message
            }
            
            _isLoading.value = false
        }
    }
}
