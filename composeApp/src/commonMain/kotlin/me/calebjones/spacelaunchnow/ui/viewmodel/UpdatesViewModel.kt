package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepository
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.Update

class UpdatesViewModel(private val repository: UpdatesRepository) : ViewModel() {

    private val _updates = MutableStateFlow<PaginatedResult<Update>?>(null)
    val updates: StateFlow<PaginatedResult<Update>?> = _updates

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchLatestUpdates(limit: Int = 10) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val result = repository.getLatestUpdates(limit = limit)
            
            result.onSuccess { dataResult ->
                _updates.value = dataResult.data
            }.onFailure { exception ->
                _error.value = exception.message
            }
            
            _isLoading.value = false
        }
    }
}
