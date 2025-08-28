package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.calebjones.spacelaunchnow.api.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.models.LaunchBasic
import me.calebjones.spacelaunchnow.api.models.PaginatedLaunchNormalList
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
                    repository.getNextLaunch(
                        limit = 5       // Increased limit to have more candidates for filtering
                    )
                }

                val futureResult = futureDeferred.await()

                futureResult.onSuccess { paginatedLaunches: PaginatedLaunchNormalList ->
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
                        // Filter launches to keep only those that aren't more than 1 hour in the past
                        val currentTime = Clock.System.now()
                        val oneHourAgo = currentTime - 1.hours
                        
                        // Find valid launches (include all future launches and recent past launches)
                        val validLaunches = paginatedLaunches.results
                            .filter { launch: LaunchNormal ->
                                // Keep the launch if:
                                // 1. It has a net time AND
                                // 2. The net time is either in the future OR occurred less than one hour ago
                                launch.net?.let { netTime: kotlinx.datetime.Instant -> netTime >= oneHourAgo } ?: false
                            }
                            
                        if (validLaunches.isNotEmpty()) {
                            val finalLaunch = async {
                                repository.getLaunchDetails(validLaunches.first().id)
                            }

                            val finalResult = finalLaunch.await()
                            finalResult.onSuccess { launch ->
                                println("=== VIEWMODEL: RECEIVED LAUNCH DETAILS ===")
                                println("Launch Name: ${launch.name}")
                                println("Launch NET: ${launch.net}")
                                println("Launch Status: ${launch.status?.name}")
                                println("Launch Location: ${launch.pad?.location?.name}")
                                println("Launch Image: ${launch.image?.imageUrl}")
                                println("=== END VIEWMODEL LAUNCH DETAILS ===")
                                
                                // Set the next launch to the first valid launch
                                _nextLaunch.value = launch
                                _isLoading.value = false
                                return@launch
                            }.onFailure {
                                // Handle failure to get launch details
                                println("Failed to get launch details: ${it.message}")
                                _nextLaunch.value = null
                                _isLoading.value = false
                            }
                        } else {
                            // If no valid launches found
                            println("No upcoming launches found within the time constraints.")
                            _nextLaunch.value = null
                            _isLoading.value = false
                        }
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