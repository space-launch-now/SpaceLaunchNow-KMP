package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Manages upcoming space events for the home screen.
 * 
 * This ViewModel follows the Single Responsibility Principle by focusing only on events data.
 * It uses the ViewState pattern for consistent state management.
 */
class EventsViewModel(
    private val eventsRepository: EventsRepository
) : ViewModel() {

    private val log = logger()

    // ========== ViewState Properties ==========

    private val _eventsState = MutableStateFlow(ViewState(data = emptyList<Event>()))
    val eventsState: StateFlow<ViewState<List<Event>>> = _eventsState.asStateFlow()

    // ========== Public API ==========

    /**
     * Loads upcoming space events
     * 
     * @param limit Number of events to load (default: 10)
     * @param forceRefresh If true, bypass cache (user-initiated refresh)
     */
    fun loadEvents(limit: Int = 10, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _eventsState.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }

                val result = eventsRepository.getUpcomingEventsDomain(limit, forceRefresh)

                result.onSuccess { dataResult ->
                    log.i { "Received events - Total: ${dataResult.data.count}" }

                    _eventsState.update {
                        it.copy(
                            data = dataResult.data.results,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }
                }.onFailure { exception ->
                    _eventsState.update {
                        it.copy(
                            error = formatErrorMessage(exception),
                            isLoading = false
                        )
                    }
                }
            } catch (exception: Exception) {
                _eventsState.update {
                    it.copy(
                        error = exception.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Refreshes events (user-initiated)
     */
    fun refresh() = loadEvents(forceRefresh = true)

    // ========== Private Helper Methods ==========

    /**
     * Formats error messages consistently
     */
    private fun formatErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("throttled") == true ->
                "API rate limit exceeded. Please try again later."

            exception.message?.contains("API Error") == true ->
                exception.message!!.substringAfter("API Error: ")

            else -> exception.message ?: "Unknown error occurred"
        }
    }
}
