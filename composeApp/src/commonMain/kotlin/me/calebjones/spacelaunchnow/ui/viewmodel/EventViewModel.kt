package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointDetailed
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.util.logging.logger

class EventViewModel(
    private val repository: EventsRepository
) : ViewModel() {
    private val log = logger()

    private val _eventDetails = MutableStateFlow<EventEndpointDetailed?>(null)
    val eventDetails: StateFlow<EventEndpointDetailed?> = _eventDetails

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchEventDetails(id: Int) {
        viewModelScope.launch {
            log.d { "Fetching event details for id: $id" }
            _error.value = null
            _isLoading.value = true

            val result = repository.getEventDetails(id)
            result.onSuccess { event ->
                log.i { "Successfully loaded event details: ${event.name}" }
                _eventDetails.value = event
            }.onFailure { exception ->
                log.e(exception) { "Failed to fetch event details for id: $id" }
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }
}
