package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.UpdateEndpoint
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.cache.LaunchCache
import me.calebjones.spacelaunchnow.data.repository.ArticlesRepository
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepository
import me.calebjones.spacelaunchnow.data.services.LaunchFilterService
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import kotlin.time.Clock
import kotlin.time.Clock.System
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

// ViewState is now in ViewState.kt - imported from same package

/**
 * Consolidated ViewModel for the Home Screen that manages:
 * - Featured launch (NextUp)
 * - Upcoming launches list
 * - Updates/News feed
 * - News articles from SNAPI
 * - Events from Launch Library
 *
 * Each section uses ViewState<T> for simplified state management with 4 states:
 * 1. Fresh load (no data) - isLoading=true, data=empty
 * 2. Loading with stale data - isLoading=true, data=present, isUserInitiated=false
 * 3. User-initiated refresh - isLoading=true, data=present, isUserInitiated=true
 * 4. Error - error!=null
 */
@OptIn(ExperimentalTime::class)
class HomeViewModel(
    private val launchRepository: LaunchRepository,
    private val updatesRepository: UpdatesRepository,
    private val articlesRepository: ArticlesRepository,
    private val eventsRepository: EventsRepository,
    private val launchCache: LaunchCache,
    private val launchFilterService: LaunchFilterService,
    private val notificationStateStorage: NotificationStateStorage
) : ViewModel() {

    // ========== NEW ViewState-based State Management ==========

    // Featured Launch State
    private val _featuredLaunchState = MutableStateFlow(ViewState<LaunchNormal?>(data = null))
    val featuredLaunchState: StateFlow<ViewState<LaunchNormal?>> =
        _featuredLaunchState.asStateFlow()

    // Upcoming Launches State
    private val _upcomingLaunchesState =
        MutableStateFlow(ViewState(data = emptyList<LaunchNormal>()))
    val upcomingLaunchesState: StateFlow<ViewState<List<LaunchNormal>>> =
        _upcomingLaunchesState.asStateFlow()

    // Updates State
    private val _updatesState = MutableStateFlow(ViewState(data = emptyList<UpdateEndpoint>()))
    val updatesState: StateFlow<ViewState<List<UpdateEndpoint>>> = _updatesState.asStateFlow()

    // Articles State
    private val _articlesState = MutableStateFlow(ViewState(data = emptyList<Article>()))
    val articlesState: StateFlow<ViewState<List<Article>>> = _articlesState.asStateFlow()

    // Events State
    private val _eventsState = MutableStateFlow(ViewState(data = emptyList<EventEndpointNormal>()))
    val eventsState: StateFlow<ViewState<List<EventEndpointNormal>>> = _eventsState.asStateFlow()

    // History Launches State (This Day in History)
    data class HistoryData(val count: Int, val launches: List<LaunchNormal>)

    private val _historyState = MutableStateFlow(ViewState(data = HistoryData(0, emptyList())))
    val historyState: StateFlow<ViewState<HistoryData>> = _historyState.asStateFlow()

    // Previous Launches State (for bidirectional carousel)
    private val _previousLaunchesState =
        MutableStateFlow(ViewState(data = emptyList<LaunchNormal>()))
    val previousLaunchesState: StateFlow<ViewState<List<LaunchNormal>>> =
        _previousLaunchesState.asStateFlow()

    // ========== Derived States for Carousel ==========

    // Combined Launches (previous + upcoming for carousel) - derived from ViewStates
    val combinedLaunches: StateFlow<List<LaunchNormal>> = kotlinx.coroutines.flow.combine(
        _previousLaunchesState,
        _upcomingLaunchesState
    ) { previousState, upcomingState ->
        previousState.data.reversed() + upcomingState.data
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Index where upcoming launches start in combined list - derived from previousLaunchesState
    val upcomingStartIndex: StateFlow<Int> = _previousLaunchesState
        .map { it.data.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Combined loading state for carousel
    val isCarouselLoading: StateFlow<Boolean> = kotlinx.coroutines.flow.combine(
        _previousLaunchesState,
        _upcomingLaunchesState
    ) { previousState, upcomingState ->
        previousState.isLoading || upcomingState.isLoading
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Combined error state for carousel (show error if either fails)
    val carouselError: StateFlow<String?> = kotlinx.coroutines.flow.combine(
        _previousLaunchesState,
        _upcomingLaunchesState
    ) { previousState, upcomingState ->
        upcomingState.error ?: previousState.error
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Quick stats counts
    private val _next24HoursCount = MutableStateFlow(0)
    val next24HoursCount: StateFlow<Int> = _next24HoursCount.asStateFlow()

    private val _nextWeekCount = MutableStateFlow(0)
    val nextWeekCount: StateFlow<Int> = _nextWeekCount.asStateFlow()

    private val _nextMonthCount = MutableStateFlow(0)
    val nextMonthCount: StateFlow<Int> = _nextMonthCount.asStateFlow()

    /**
     * Loads all home screen data with prioritization for critical UI elements
     * This is the main entry point for the home screen
     *
     * PRIORITY ORDER:
     * 1. Featured Launch (highest priority - drives main UI) - runs on Default dispatcher
     * 2. Upcoming Launches (needed for carousel) - awaits featured launch start
     * 3. Everything else (background loading) - runs on IO dispatcher
     *
     * Uses the NEW ViewState pattern for all data loading.
     * All sections load independently with their own state management.
     */
    fun loadHomeScreenData() {
        viewModelScope.launch {
            try {
                // PRIORITY 1: Featured launch - start immediately on Default dispatcher (higher priority)
                // This is the most visible UI element, so prioritize it
                val featuredJob = async { loadFeaturedLaunch() }

                // PRIORITY 2: Upcoming launches - start immediately after featured begins
                val upcomingJob = async { loadUpcomingLaunches() }

                // PRIORITY 3: Everything else - run on IO dispatcher (lower priority for background work)
                // These don't block the UI and can use lower priority IO threads
                launch(kotlinx.coroutines.Dispatchers.IO) { loadUpdates() }
                launch(kotlinx.coroutines.Dispatchers.IO) { loadArticles() }
                launch(kotlinx.coroutines.Dispatchers.IO) { loadEvents() }
                launch(kotlinx.coroutines.Dispatchers.IO) { loadNext24Hours() }
                launch(kotlinx.coroutines.Dispatchers.IO) { loadNextWeek() }
                launch(kotlinx.coroutines.Dispatchers.IO) { loadNextMonth() }

            } catch (exception: Exception) {
                println("HomeViewModel: Error loading home screen data: ${exception.message}")
            }
        }
    }

    // ========== NEW Simplified Loading Functions Using ViewState ==========

    /**
     * Loads featured launch with simplified state management
     * @param forceRefresh If true, bypass cache (user-initiated refresh)
     */
    fun loadFeaturedLaunch(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                println("[FEATURED] === HomeViewModel.loadFeaturedLaunch START ===")
                println("[FEATURED] forceRefresh: $forceRefresh")
                println("[FEATURED] Current featuredLaunchState: isLoading=${_featuredLaunchState.value.isLoading}, hasData=${_featuredLaunchState.value.data != null}, error=${_featuredLaunchState.value.error}")

                _featuredLaunchState.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }
                println("[FEATURED] ✓ Set isLoading=true for featured launch state")

                // Wait for actual filter settings from DataStore
                val currentFilters = notificationStateStorage.stateFlow.first()
                val filterParams = launchFilterService.getFilterParams(currentFilters)
                println("[FEATURED] Filter params - agencyIds: ${filterParams.agencyIds}, locationIds: ${filterParams.locationIds}")

                println("[FEATURED] Calling repository.getFeaturedLaunch with upcomingWithRecent filter...")
                val result = launchRepository.getFeaturedLaunch(
                    forceRefresh = forceRefresh,
                    agencyIds = filterParams.agencyIds,
                    locationIds = filterParams.locationIds
                )
                println("[FEATURED] Repository call completed. Success: ${result.isSuccess}")

                result.onSuccess { dataResult ->
                    val paginatedLaunches = dataResult.data
                    println("[FEATURED] === Repository SUCCESS ===")
                    println("[FEATURED] Data source: ${dataResult.source}")
                    println("[FEATURED] Results count: ${paginatedLaunches.results.size}")
                    println("[FEATURED] Cache timestamp: ${dataResult.timestamp}")

                    val firstLaunch = paginatedLaunches.results.firstOrNull()
                    if (firstLaunch != null) {
                        println("[FEATURED] Featured launch: ${firstLaunch.name} (ID: ${firstLaunch.id})")
                    } else {
                        println("[FEATURED] ⚠️ WARNING: No launches returned from repository!")
                    }

                    _featuredLaunchState.update {
                        it.copy(
                            data = firstLaunch,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }
                    println("[FEATURED] ✓ Updated featuredLaunchState: hasData=${_featuredLaunchState.value.data != null}, isLoading=${_featuredLaunchState.value.isLoading}")

                    // Pre-fetch detailed data if we have a launch
                    firstLaunch?.let { launch ->
                        println("[FEATURED] Pre-fetching launch details for ${launch.id}...")
                        preFetchLaunchDetails(launch.id)
                    }
                }.onFailure { exception ->
                    println("[FEATURED] === Repository FAILURE ===")
                    println("[FEATURED] Exception type: ${exception::class.simpleName}")
                    println("[FEATURED] Exception message: ${exception.message}")
                    val errorMsg = formatErrorMessage(exception)
                    println("[FEATURED] Formatted error: $errorMsg")

                    _featuredLaunchState.update {
                        it.copy(
                            error = errorMsg,
                            isLoading = false
                        )
                    }
                    println("[FEATURED] ✓ Updated featuredLaunchState with error, isLoading=false")
                }
            } catch (exception: Exception) {
                println("[FEATURED] === EXCEPTION in loadFeaturedLaunch ===")
                println("[FEATURED] Exception type: ${exception::class.simpleName}")
                println("[FEATURED] Exception message: ${exception.message}")
                exception.printStackTrace()

                _featuredLaunchState.update {
                    it.copy(
                        error = exception.message ?: "Unknown error",
                        isLoading = false
                    )
                }
                println("[FEATURED] ✓ Updated featuredLaunchState with exception error, isLoading=false")
            }
            println("[FEATURED] === HomeViewModel.loadFeaturedLaunch END ===")
        }
    }

    /**
     * Loads upcoming launches with simplified state management
     * Also loads previous launches in parallel for carousel
     * @param limit Number of launches to load
     * @param forceRefresh If true, bypass cache (user-initiated refresh)
     */
    fun loadUpcomingLaunches(limit: Int = 10, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                // Update states: loading started
                _upcomingLaunchesState.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }
                _previousLaunchesState.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }

                // Wait for actual filter settings from DataStore (not the default initial value)
                val currentFilters = notificationStateStorage.stateFlow.first()
                println("=== HomeViewModel Filter Settings ===")
                println("followAllLaunches: ${currentFilters.followAllLaunches}")
                println("subscribedAgencies: ${currentFilters.subscribedAgencies}")
                println("subscribedLocations: ${currentFilters.subscribedLocations}")

                val filterParams = launchFilterService.getFilterParams(currentFilters)
                println("filterParams.agencyIds: ${filterParams.agencyIds}")
                println("filterParams.locationIds: ${filterParams.locationIds}")

                // Load both upcoming and previous in parallel
                val upcomingDeferred = async {
                    launchRepository.getUpcomingLaunchesNormal(
                        limit = limit,
                        forceRefresh = forceRefresh,
                        agencyIds = filterParams.agencyIds,
                        locationIds = filterParams.locationIds
                    )
                }
                val previousDeferred = async {
                    launchRepository.getPreviousLaunchesNormal(
                        limit = 5,
                        forceRefresh = forceRefresh,
                        agencyIds = filterParams.agencyIds,
                        locationIds = filterParams.locationIds
                    )
                }

                // Handle upcoming launches result
                upcomingDeferred.await().onSuccess { dataResult ->
                    _upcomingLaunchesState.update {
                        it.copy(
                            data = dataResult.data.results,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }
                }.onFailure { exception ->
                    _upcomingLaunchesState.update {
                        it.copy(
                            error = formatErrorMessage(exception),
                            isLoading = false
                        )
                    }
                }

                // Handle previous launches result
                previousDeferred.await().onSuccess { dataResult ->
                    _previousLaunchesState.update {
                        it.copy(
                            data = dataResult.data.results,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }
                }.onFailure { exception ->
                    _previousLaunchesState.update {
                        it.copy(
                            error = formatErrorMessage(exception),
                            isLoading = false
                        )
                    }
                }
            } catch (exception: Exception) {
                _upcomingLaunchesState.update {
                    it.copy(
                        error = exception.message ?: "Unknown error",
                        isLoading = false
                    )
                }
                _previousLaunchesState.update {
                    it.copy(
                        error = exception.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Loads updates/news feed with simplified state management
     * @param limit Number of updates to load
     * @param forceRefresh If true, bypass cache (user-initiated refresh)
     */
    fun loadUpdates(limit: Int = 10, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _updatesState.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }

                val result = updatesRepository.getLatestUpdates(
                    limit = limit,
                    forceRefresh = forceRefresh
                )

                result.onSuccess { dataResult ->
                    _updatesState.update {
                        it.copy(
                            data = dataResult.data.results,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }
                }.onFailure { exception ->
                    _updatesState.update {
                        it.copy(
                            error = formatErrorMessage(exception),
                            isLoading = false
                        )
                    }
                }
            } catch (exception: Exception) {
                _updatesState.update {
                    it.copy(
                        error = exception.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Loads news articles with simplified state management
     * @param limit Number of articles to load
     * @param forceRefresh If true, bypass cache (user-initiated refresh)
     */
    fun loadArticles(limit: Int = 5, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _articlesState.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }

                val result = articlesRepository.getArticles(
                    limit = limit,
                    forceRefresh = forceRefresh
                )

                result.onSuccess { dataResult ->
                    _articlesState.update {
                        it.copy(
                            data = dataResult.data.results,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }
                }.onFailure { exception ->
                    _articlesState.update {
                        it.copy(
                            error = formatErrorMessage(exception),
                            isLoading = false
                        )
                    }
                }
            } catch (exception: Exception) {
                _articlesState.update {
                    it.copy(
                        error = exception.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Loads events with simplified state management
     * @param limit Number of events to load
     * @param forceRefresh If true, bypass cache (user-initiated refresh)
     */
    fun loadEvents(limit: Int = 10, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _eventsState.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }

                val result = eventsRepository.getUpcomingEvents(
                    limit = limit,
                    forceRefresh = forceRefresh
                )

                result.onSuccess { dataResult ->
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
     * Refreshes all home screen sections (for pull-to-refresh)
     * @param onComplete Callback invoked when all refreshes complete
     */
    fun refreshAll(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                // Get current day and month for history refresh
                val currentDate = Clock.System.now()
                    .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                val currentDay = currentDate.day
                val currentMonth = currentDate.month.number

                // Launch all refreshes in parallel
                val jobs = listOf(
                    async { loadFeaturedLaunch(forceRefresh = true) },
                    async { loadUpcomingLaunches(forceRefresh = true) },
                    async { loadUpdates(forceRefresh = true) },
                    async { loadArticles(forceRefresh = true) },
                    async { loadEvents(forceRefresh = true) },
                    async {
                        loadHistoryLaunches(
                            day = currentDay,
                            month = currentMonth,
                            forceRefresh = true
                        )
                    }
                )

                // Wait for all to complete
                jobs.forEach { it.await() }

                onComplete()
            } catch (exception: Exception) {
                println("HomeViewModel: Error during refreshAll: ${exception.message}")
                onComplete()
            }
        }
    }

    /**
     * Loads history launches with simplified state management (NEW ViewState pattern)
     * @param day Day of month to filter by
     * @param month Month to filter by
     * @param limit Number of launches to load
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
                    println("=== HomeViewModel: Received History Launches ===")
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
     * Loads the count of launches in the next 24 hours using actual API time-range filtering
     */
    fun loadNext24Hours(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                val now = System.now()
                val tomorrow = now.plus(24.hours)

                val result = launchRepository.getUpcomingLaunchesList(
                    limit = 1, // We only need the count, not the actual data
                    netGt = now,
                    netLt = tomorrow
                )

                result.onSuccess { paginatedLaunches ->
                    _next24HoursCount.value = paginatedLaunches.count
                    println("Next 24 hours count: ${paginatedLaunches.count}")
                }.onFailure { exception ->
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
                val now = System.now()
                val nextWeek = now.plus(7.days)

                val result = launchRepository.getUpcomingLaunchesList(
                    limit = 1, // We only need the count, not the actual data
                    netGt = now,
                    netLt = nextWeek
                )

                result.onSuccess { paginatedLaunches ->
                    _nextWeekCount.value = paginatedLaunches.count
                    println("Next week count: ${paginatedLaunches.count}")
                }.onFailure { exception ->
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
                val now = System.now()
                val nextMonth = now.plus(30.days)

                val result = launchRepository.getUpcomingLaunchesList(
                    limit = 1, // We only need the count, not the actual data
                    netGt = now,
                    netLt = nextMonth
                )

                result.onSuccess { paginatedLaunches ->
                    _nextMonthCount.value = paginatedLaunches.count
                    println("Next month count: ${paginatedLaunches.count}")
                }.onFailure { exception ->
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
     * Pre-fetches detailed launch data in the background to prepare for instant loading
     * when user clicks "Explore" on the featured launch
     */
    private fun preFetchLaunchDetails(launchId: String) {
        viewModelScope.launch {
            try {
                println("Pre-fetching detailed data for launch: $launchId")
                val result = launchRepository.getLaunchDetails(launchId)

                result.onSuccess { launchDetailed ->
                    // Cache the detailed launch data for instant access later
                    launchCache.cacheLaunchDetailed(launchDetailed)
                    println("Successfully pre-fetched and cached detailed data for launch: ${launchDetailed.name}")
                }.onFailure { exception ->
                    println("Failed to pre-fetch detailed data for launch $launchId: ${exception.message}")
                    // Don't show error to user since this is background prefetch
                    // The detail screen will handle the error when the user actually navigates to it
                }
            } catch (exception: Exception) {
                println("Exception during pre-fetch for launch $launchId: ${exception.message}")
                // Silently fail - this is a background optimization, not critical
            }
        }
    }

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
