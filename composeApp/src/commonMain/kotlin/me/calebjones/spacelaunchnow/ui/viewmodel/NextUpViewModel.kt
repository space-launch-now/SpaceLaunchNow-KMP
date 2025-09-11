package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchBasic
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import kotlin.time.Duration.Companion.hours


class NextUpViewModel(private val repository: LaunchRepository) : ViewModel() {

    private val _nextLaunch = MutableStateFlow<LaunchDetailed?>(null)
    val nextLaunch: StateFlow<LaunchDetailed?> = _nextLaunch

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchNextLaunch() {
        viewModelScope.launch {
            try {
                // Set loading state to true
                _isLoading.value = true

                // Using normal mode to get LaunchNormal objects
                val futureDeferred = async {
                    repository.getNextDetailedLaunch(
                        limit = 2
                    )
                }

                val futureResult = futureDeferred.await()

                futureResult.onSuccess { paginatedLaunches: PaginatedLaunchDetailedList ->
                    println("=== VIEWMODEL: RECEIVED PAGINATED LAUNCHES ===")
                    println("Total launches: ${paginatedLaunches.results.size}")
                    println("Count: ${paginatedLaunches.count}")
                    println("Next page: ${paginatedLaunches.next}")
                    println("Previous page: ${paginatedLaunches.previous}")
                    paginatedLaunches.results.forEachIndexed { index, launch ->
                        println("Launch $index: ${launch.name} - ${launch.net} - ID: ${launch.id}")
                    }
                    println("=== END VIEWMODEL LAUNCHES ===")
                    
                    if (paginatedLaunches.results.isNotEmpty()) {
                        _nextLaunch.value = paginatedLaunches.results.first()
                        _isLoading.value = false
                    } else {
                        // If results are empty
                        println("No launches returned from API.")
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
                    println("Failed to get upcoming launches: $errorMessage")
                    _error.value = errorMessage
                    _nextLaunch.value = null
                    _isLoading.value = false
                }

            } catch (exception: Exception) {
                _error.value = exception.message
                _isLoading.value = false
            }
        }
    }
}