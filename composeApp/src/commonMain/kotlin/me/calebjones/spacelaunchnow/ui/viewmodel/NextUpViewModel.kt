package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.util.logging.logger


class NextUpViewModel(private val repository: LaunchRepository) : ViewModel() {

    private val log = logger()

    private val _nextLaunch = MutableStateFlow<Launch?>(null)
    val nextLaunch: StateFlow<Launch?> = _nextLaunch

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchNextLaunch() {
        viewModelScope.launch {
            try {
                // Set loading state to true
                _isLoading.value = true
                log.d { "Fetching next launch..." }

                val futureDeferred = async {
                    repository.getUpcomingLaunchesNormalDomain(
                        limit = 1
                    )
                }

                val futureResult = futureDeferred.await()

                futureResult.onSuccess { dataResult ->
                    val paginatedLaunches = dataResult.data
                    log.d { "Received paginated launches - Total: ${paginatedLaunches.results.size}, Count: ${paginatedLaunches.count}" }
                    paginatedLaunches.results.forEachIndexed { index, launch ->
                        log.v { "Launch $index: ${launch.name} - ${launch.net} - ID: ${launch.id}" }
                    }

                    if (paginatedLaunches.results.isNotEmpty()) {
                        _nextLaunch.value = paginatedLaunches.results.first()
                        _isLoading.value = false
                        log.i { "Successfully loaded next launch: ${paginatedLaunches.results.first().name}" }
                    } else {
                        // If results are empty
                        log.w { "No launches returned from API" }
                        _nextLaunch.value = null
                        _isLoading.value = false
                    }
                }.onFailure { exception ->
                    // Handle failure to get upcoming launches
                    val errorMessage = when {
                        exception.message?.contains("throttled") == true -> 
                            "API rate limit exceeded. Please try again later."
                        exception.message?.contains("API Error") == true -> 
                            exception.message!!.substringAfter("API Error: ")
                        else -> exception.message ?: "Unknown error occurred"
                    }
                    log.e(exception) { "Failed to get upcoming launches: $errorMessage" }
                    _error.value = errorMessage
                    _nextLaunch.value = null
                    _isLoading.value = false
                }

            } catch (exception: Exception) {
                log.e(exception) { "Unexpected error while fetching next launch" }
                _error.value = exception.message
                _isLoading.value = false
            }
        }
    }
}