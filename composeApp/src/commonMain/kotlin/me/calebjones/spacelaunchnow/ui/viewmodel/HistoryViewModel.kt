package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository

/**
 * Manages "This Day in History" launch data for the home screen.
 * 
 * This ViewModel follows the Single Responsibility Principle by focusing only on historical launches.
 * It uses the ViewState pattern for consistent state management.
 */
class HistoryViewModel(
    private val launchRepository: LaunchRepository
) : ViewModel() {

    // ========== Data Classes ==========

    /**
     * Holds history data including count and list of launches
     */
    data class HistoryData(val count: Int, val launches: List<LaunchNormal>)

    // ========== ViewState Properties ==========

    private val _historyState = MutableStateFlow(ViewState(data = HistoryData(0, emptyList())))
    val historyState: StateFlow<ViewState<HistoryData>> = _historyState.asStateFlow()

    // ========== Public API ==========

    /**
     * Loads history launches for a specific day and month
     * 
     * @param day Day of month to filter by (1-31)
     * @param month Month to filter by (1-12)
     * @param limit Number of launches to load (default: 100)
     * @param forceRefresh If true, bypass cache (user-initiated refresh)
     */
    fun loadHistoryLaunches(
        day: Int,
        month: Int,
        limit: Int = 100,
        forceRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                _historyState.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }

                val result = launchRepository.getLaunchesByDayAndMonth(
                    day = day,
                    month = month,
                    limit = limit
                )

                result.onSuccess { paginatedLaunches ->
                    println("=== HistoryViewModel: Received History Launches ===")
                    println("Total launches on $month/$day: ${paginatedLaunches.count}")

                    _historyState.update {
                        it.copy(
                            data = HistoryData(
                                count = paginatedLaunches.count,
                                launches = paginatedLaunches.results.reversed() // Most recent first
                            ),
                            isLoading = false
                        )
                    }
                }.onFailure { exception ->
                    // Keep existing data, just show error
                    _historyState.update {
                        it.copy(
                            error = formatErrorMessage(exception),
                            isLoading = false
                        )
                    }
                }
            } catch (exception: Exception) {
                _historyState.update {
                    it.copy(
                        error = exception.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Refreshes history launches for the current day/month (user-initiated)
     * Note: You must provide day and month as this ViewModel doesn't track them
     */
    fun refresh(day: Int, month: Int) = loadHistoryLaunches(day, month, forceRefresh = true)

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
