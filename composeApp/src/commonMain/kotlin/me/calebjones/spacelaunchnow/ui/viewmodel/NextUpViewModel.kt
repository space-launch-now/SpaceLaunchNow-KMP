package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import me.calebjones.spacelaunchnow.api.client.models.PaginatedPolymorphicLaunchEndpointList
import me.calebjones.spacelaunchnow.api.client.models.LaunchNormal
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours


class NextUpViewModel(private val repository: LaunchRepository) : ViewModel() {

    private val _nextLaunch = MutableStateFlow<LaunchNormal?>(null)
    val nextLaunch: StateFlow<LaunchNormal?> = _nextLaunch

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchNextLaunch() {
        viewModelScope.launch {
            try {

                val futureDeferred = async {
                    repository.getNextLaunch(
                        mode = "normal",
                        limit = 1
                    )
                }

                val futureResult = futureDeferred.await()

                futureResult.onSuccess { launches ->
                    if (launches.results.isNotEmpty()) {
                        _nextLaunch.value = launches.results.firstOrNull() as? LaunchNormal
                        return@launch
                    }
                }

                // If no launches are found, set the value to null
                println("No upcoming launches found.")
                _nextLaunch.value = null

            } catch (exception: Exception) {
                _error.value = exception.message
            }
        }
    }
}