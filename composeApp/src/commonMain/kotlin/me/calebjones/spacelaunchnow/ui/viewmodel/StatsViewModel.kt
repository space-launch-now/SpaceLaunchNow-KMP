package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

/**
 * Manages quick statistics for the home screen:
 * - Number of launches in next 24 hours
 * - Number of launches in next week
 * - Number of launches in next month
 *
 * This ViewModel follows the Single Responsibility Principle by focusing only on statistics.
 * Uses simple Int state instead of ViewState since these are quick counts.
 */
@OptIn(ExperimentalTime::class)
class StatsViewModel(
    private val launchRepository: LaunchRepository
) : ViewModel() {

    // ========== State Properties ==========

    private val _next24HoursCount = MutableStateFlow(0)
    val next24HoursCount: StateFlow<Int> = _next24HoursCount.asStateFlow()

    private val _nextWeekCount = MutableStateFlow(0)
    val nextWeekCount: StateFlow<Int> = _nextWeekCount.asStateFlow()

    private val _nextMonthCount = MutableStateFlow(0)
    val nextMonthCount: StateFlow<Int> = _nextMonthCount.asStateFlow()

    // ========== Public API ==========

    /**
     * Loads all statistics in parallel
     */
    fun loadAllStats(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            launch { loadNext24Hours(forceRefresh) }
            launch { loadNextWeek(forceRefresh) }
            launch { loadNextMonth(forceRefresh) }
        }
    }

    /**
     * Loads the count of launches in the next 24 hours using actual API time-range filtering
     */
    fun loadNext24Hours(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                val now: Instant = Clock.System.now()
                val tomorrow: Instant = now.plus(24.hours)

                val result: Result<PaginatedLaunchBasicList> =
                    launchRepository.getUpcomingLaunchesList(
                        limit = 1, // We only need the count, not the actual data
                        netGt = now,
                        netLt = tomorrow
                    )

                result.onSuccess { paginatedLaunches: PaginatedLaunchBasicList ->
                    _next24HoursCount.value = paginatedLaunches.count
                    println("Next 24 hours count: ${paginatedLaunches.count}")
                }.onFailure { exception: Throwable ->
                    println("Failed to get next 24 hours count: ${exception.message}")
                    _next24HoursCount.value = 0
                }
            } catch (exception: Exception) {
                println("Exception loading next 24 hours count: ${exception.message}")
                _next24HoursCount.value = 0
            }
        }
    }

    /**
     * Loads the count of launches in the next 7 days using actual API time-range filtering
     */
    fun loadNextWeek(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                val now: Instant = Clock.System.now()
                val nextWeek: Instant = now.plus(7.days)

                val result: Result<PaginatedLaunchBasicList> =
                    launchRepository.getUpcomingLaunchesList(
                        limit = 1, // We only need the count, not the actual data
                        netGt = now,
                        netLt = nextWeek
                    )

                result.onSuccess { paginatedLaunches: PaginatedLaunchBasicList ->
                    _nextWeekCount.value = paginatedLaunches.count
                    println("Next week count: ${paginatedLaunches.count}")
                }.onFailure { exception: Throwable ->
                    println("Failed to get next week count: ${exception.message}")
                    _nextWeekCount.value = 0
                }
            } catch (exception: Exception) {
                println("Exception loading next week count: ${exception.message}")
                _nextWeekCount.value = 0
            }
        }
    }

    /**
     * Loads the count of launches in the next 30 days using actual API time-range filtering
     */
    fun loadNextMonth(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                val now: Instant = Clock.System.now()
                val nextMonth: Instant = now.plus(30.days)

                val result: Result<PaginatedLaunchBasicList> =
                    launchRepository.getUpcomingLaunchesList(
                        limit = 1, // We only need the count, not the actual data
                        netGt = now,
                        netLt = nextMonth
                    )

                result.onSuccess { paginatedLaunches: PaginatedLaunchBasicList ->
                    _nextMonthCount.value = paginatedLaunches.count
                    println("Next month count: ${paginatedLaunches.count}")
                }.onFailure { exception: Throwable ->
                    println("Failed to get next month count: ${exception.message}")
                    _nextMonthCount.value = 0
                }
            } catch (exception: Exception) {
                println("Exception loading next month count: ${exception.message}")
                _nextMonthCount.value = 0
            }
        }
    }

    /**
     * Refreshes all statistics (user-initiated)
     */
    fun refresh() = loadAllStats(forceRefresh = true)
}
