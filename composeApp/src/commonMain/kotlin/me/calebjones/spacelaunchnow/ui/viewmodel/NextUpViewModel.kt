package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.calebjones.spacelaunchnow.api.client.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.client.models.LaunchDetailed
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

                // TODO update to using LIST when 2.3.1
                val futureDeferred = async {
                    repository.getNextLaunch(
                        mode = "normal", // Using normal mode to get LaunchNormal objects
                        limit = 5       // Increased limit to have more candidates for filtering
                    )
                }

                val futureResult = futureDeferred.await()

                futureResult.onSuccess { launches ->
                    if (launches.results.isNotEmpty()) {
                        // Filter launches to keep only those that aren't more than 1 hour in the past
                        val currentTime = Clock.System.now()
                        val oneHourAgo = currentTime - 1.hours
                          // Find valid launches (include all future launches and recent past launches)

                        val validLaunches = launches.results
                            .filterIsInstance<LaunchNormal>()
                            .filter { launch ->
                                // Keep the launch if:
                                // 1. It has a net time AND
                                // 2. The net time is either in the future OR occurred less than one hour ago
                                launch.net?.let { netTime -> netTime < oneHourAgo } ?: false
                            }
                            
                        if (validLaunches.isNotEmpty()) {
                            val finalLaunch = async {
                                repository.getLaunchDetails(validLaunches.first().id)
                            }

                            val finalResult = finalLaunch.await()
                            finalResult.onSuccess { launch ->
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
                        }
                    }

                    futureResult.onFailure { exception ->
                        // Handle failure to get upcoming launches
                        println("Failed to get upcoming launches: ${exception.message}")
                        _nextLaunch.value = null
                        _isLoading.value = false
                    }

                    // If no launches are found, set the value to null
                    println("No upcoming launches found within the time constraints.")
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