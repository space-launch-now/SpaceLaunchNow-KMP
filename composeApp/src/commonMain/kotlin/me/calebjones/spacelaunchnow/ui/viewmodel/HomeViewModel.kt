package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

/**
 * Represents the state of a view section on the Home Screen
 *
 * @param data The actual data (can be a single object or list)
 * @param isLoading True if currently loading data
 * @param isUserInitiated True if the load was triggered by user action (pull-to-refresh/retry)
 * @param error Error message if the load failed, null otherwise
 * @param dataSource Where the data originated from (network, cache, or stale cache)
 * @param cacheTimestamp When the data was originally cached (epoch milliseconds)
 */
data class ViewState<T>(
    val data: T,
    val isLoading: Boolean = false,
    val isUserInitiated: Boolean = false,
    val error: String? = null,
    val dataSource: me.calebjones.spacelaunchnow.data.model.DataSource = me.calebjones.spacelaunchnow.data.model.DataSource.NETWORK,
    val cacheTimestamp: Long? = null
)

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

    // ========== Legacy State (for backward compatibility during migration) ==========

    // Combined Launches (previous + upcoming for carousel)
    private val _combinedLaunches = MutableStateFlow<List<LaunchNormal>>(emptyList())
    val combinedLaunches: StateFlow<List<LaunchNormal>> = _combinedLaunches.asStateFlow()

    // Index where upcoming launches start in combined list
    private val _upcomingStartIndex = MutableStateFlow<Int>(0)
    val upcomingStartIndex: StateFlow<Int> = _upcomingStartIndex.asStateFlow()

    // Previous Launches List (for bidirectional carousel)
    private val _previousLaunches = MutableStateFlow<List<LaunchNormal>>(emptyList())
    val previousLaunches: StateFlow<List<LaunchNormal>> = _previousLaunches.asStateFlow()

    // ========== Legacy State Properties (still used by old functions) ==========
    // These will be removed once all views are migrated to ViewState pattern

    // Additional legacy states for previous launches
    private val _previousLaunchesError = MutableStateFlow<String?>(null)
    val previousLaunchesError: StateFlow<String?> = _previousLaunchesError.asStateFlow()

    private val _isPreviousLaunchesLoading = MutableStateFlow(false)
    val isPreviousLaunchesLoading: StateFlow<Boolean> = _isPreviousLaunchesLoading.asStateFlow()

    private val _isPreviousLaunchesRefreshing = MutableStateFlow(false)
    val isPreviousLaunchesRefreshing: StateFlow<Boolean> =
        _isPreviousLaunchesRefreshing.asStateFlow()

    private val _isUpcomingLaunchesRefreshing = MutableStateFlow(false)
    val isUpcomingLaunchesRefreshing: StateFlow<Boolean> =
        _isUpcomingLaunchesRefreshing.asStateFlow()

    // Global app loading state (deprecated - will be removed)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Pull-to-refresh global state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Legacy individual property states - used by old functions until migration complete
    private val _upcomingLaunches = MutableStateFlow<List<LaunchNormal>>(emptyList())
    val upcomingLaunches: StateFlow<List<LaunchNormal>> = _upcomingLaunches.asStateFlow()

    private val _isUpcomingLaunchesLoading = MutableStateFlow(false)
    val isUpcomingLaunchesLoading: StateFlow<Boolean> = _isUpcomingLaunchesLoading.asStateFlow()

    private val _isFeaturedLaunchLoading = MutableStateFlow(false)
    val isFeaturedLaunchLoading: StateFlow<Boolean> = _isFeaturedLaunchLoading.asStateFlow()

    private val _upcomingLaunchesError = MutableStateFlow<String?>(null)
    val upcomingLaunchesError: StateFlow<String?> = _upcomingLaunchesError.asStateFlow()

    private val _featuredLaunch = MutableStateFlow<LaunchNormal?>(null)
    val featuredLaunch: StateFlow<LaunchNormal?> = _featuredLaunch.asStateFlow()

    private val _featuredLaunchError = MutableStateFlow<String?>(null)
    val featuredLaunchError: StateFlow<String?> = _featuredLaunchError.asStateFlow()

    private val _lastDataUpdate = MutableStateFlow<Long>(0)
    val lastDataUpdate: StateFlow<Long> = _lastDataUpdate.asStateFlow()

    private val _updates = MutableStateFlow<List<UpdateEndpoint>>(emptyList())
    val updates: StateFlow<List<UpdateEndpoint>> = _updates.asStateFlow()

    private val _isUpdatesLoading = MutableStateFlow(false)
    val isUpdatesLoading: StateFlow<Boolean> = _isUpdatesLoading.asStateFlow()

    private val _isUpdatesRefreshing = MutableStateFlow(false)
    val isUpdatesRefreshing: StateFlow<Boolean> = _isUpdatesRefreshing.asStateFlow()

    private val _updatesError = MutableStateFlow<String?>(null)
    val updatesError: StateFlow<String?> = _updatesError.asStateFlow()

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    private val _isArticlesLoading = MutableStateFlow(false)
    val isArticlesLoading: StateFlow<Boolean> = _isArticlesLoading.asStateFlow()

    private val _isArticlesRefreshing = MutableStateFlow(false)
    val isArticlesRefreshing: StateFlow<Boolean> = _isArticlesRefreshing.asStateFlow()

    private val _articlesError = MutableStateFlow<String?>(null)
    val articlesError: StateFlow<String?> = _articlesError.asStateFlow()

    private val _events = MutableStateFlow<List<EventEndpointNormal>>(emptyList())
    val events: StateFlow<List<EventEndpointNormal>> = _events.asStateFlow()

    private val _isEventsLoading = MutableStateFlow(false)
    val isEventsLoading: StateFlow<Boolean> = _isEventsLoading.asStateFlow()

    private val _isEventsRefreshing = MutableStateFlow(false)
    val isEventsRefreshing: StateFlow<Boolean> = _isEventsRefreshing.asStateFlow()

    private val _eventsError = MutableStateFlow<String?>(null)
    val eventsError: StateFlow<String?> = _eventsError.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // History launches for "This Day in History"
    private val _historyLaunchesCount = MutableStateFlow(0)
    val historyLaunchesCount: StateFlow<Int> = _historyLaunchesCount.asStateFlow()

    private val _historyLaunches = MutableStateFlow<List<LaunchNormal>>(emptyList())
    val historyLaunches: StateFlow<List<LaunchNormal>> = _historyLaunches.asStateFlow()

    private val _isHistoryLaunchesLoading = MutableStateFlow(false)
    val isHistoryLaunchesLoading: StateFlow<Boolean> = _isHistoryLaunchesLoading.asStateFlow()

    private val _historyLaunchesError = MutableStateFlow<String?>(null)
    val historyLaunchesError: StateFlow<String?> = _historyLaunchesError.asStateFlow()

    // Quick stats counts
    private val _next24HoursCount = MutableStateFlow(0)
    val next24HoursCount: StateFlow<Int> = _next24HoursCount.asStateFlow()

    private val _nextWeekCount = MutableStateFlow(0)
    val nextWeekCount: StateFlow<Int> = _nextWeekCount.asStateFlow()

    private val _nextMonthCount = MutableStateFlow(0)
    val nextMonthCount: StateFlow<Int> = _nextMonthCount.asStateFlow()

    /**
     * Loads all home screen data in parallel for better performance
     * This is the main entry point for the home screen
     *
     * The main loading indicator (_isLoading) only tracks the featured launch load.
     * Other sections (updates, articles, events, stats) load independently in the background.
     */
    fun loadHomeScreenData() {
        viewModelScope.launch {
            try {
                clearErrors()

                // Load featured launch (upcoming launches) - this drives the main loading indicator
                loadUpcomingLaunches()

                // Load everything else in parallel in the background
                // These don't affect the main _isLoading state
                launch { loadUpdates() }
                launch { loadArticles() }
                launch { loadEvents() }
                launch { loadNext24Hours() }
                launch { loadNextWeek() }
                launch { loadNextMonth() }

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
    fun loadFeaturedLaunchNew(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _featuredLaunchState.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }

                // Wait for actual filter settings from DataStore
                val currentFilters = notificationStateStorage.stateFlow.first()
                val filterParams = launchFilterService.getFilterParams(currentFilters)

                val result = launchRepository.getUpcomingLaunchesNormal(
                    limit = 1,
                    forceRefresh = forceRefresh,
                    agencyIds = filterParams.agencyIds,
                    locationIds = filterParams.locationIds
                )

                result.onSuccess { dataResult ->
                    val paginatedLaunches = dataResult.data
                    _featuredLaunchState.update {
                        it.copy(
                            data = paginatedLaunches.results.firstOrNull(),
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }

                    // Pre-fetch detailed data if we have a launch
                    paginatedLaunches.results.firstOrNull()?.let { launch ->
                        preFetchLaunchDetails(launch.id)
                    }
                }.onFailure { exception ->
                    _featuredLaunchState.update {
                        it.copy(
                            error = formatErrorMessage(exception),
                            isLoading = false
                        )
                    }
                }
            } catch (exception: Exception) {
                _featuredLaunchState.update {
                    it.copy(
                        error = exception.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Loads upcoming launches with simplified state management
     * @param limit Number of launches to load
     * @param forceRefresh If true, bypass cache (user-initiated refresh)
     */
    fun loadUpcomingLaunchesNew(limit: Int = 10, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                // Update state: loading started
                _upcomingLaunchesState.update {
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

                val result = launchRepository.getUpcomingLaunchesNormal(
                    limit = limit,
                    forceRefresh = forceRefresh,
                    agencyIds = filterParams.agencyIds,
                    locationIds = filterParams.locationIds
                )

                result.onSuccess { dataResult ->
                    _upcomingLaunchesState.update {
                        it.copy(
                            data = dataResult.data.results,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }

                    // Update combined launches for carousel
                    updateCombinedLaunches()
                }.onFailure { exception ->
                    _upcomingLaunchesState.update {
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
            }
        }
    }

    /**
     * Loads updates/news feed with simplified state management
     * @param limit Number of updates to load
     * @param forceRefresh If true, bypass cache (user-initiated refresh)
     */
    fun loadUpdatesNew(limit: Int = 10, forceRefresh: Boolean = false) {
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
    fun loadArticlesNew(limit: Int = 5, forceRefresh: Boolean = false) {
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
    fun loadEventsNew(limit: Int = 10, forceRefresh: Boolean = false) {
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
                    async { loadFeaturedLaunchNew(forceRefresh = true) },
                    async { loadUpcomingLaunchesNew(forceRefresh = true) },
                    async { loadUpdatesNew(forceRefresh = true) },
                    async { loadArticlesNew(forceRefresh = true) },
                    async { loadEventsNew(forceRefresh = true) },
                    async {
                        loadHistoryLaunchesNew(
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
     * Updates combined launches list from previous and upcoming launches
     */
    private fun updateCombinedLaunches() {
        val previousReversed = _previousLaunches.value.reversed()
        val newCombined = previousReversed + _upcomingLaunchesState.value.data
        _combinedLaunches.value = newCombined
        _upcomingStartIndex.value = previousReversed.size
    }

    // ========== Legacy Loading Functions (will be removed after migration) ==========

    /**
     * Loads the featured launch (first item from upcoming launches)
     * This now delegates to loadUpcomingLaunches since featured launch is derived from it
     */
    fun loadFeaturedLaunch(forceRefresh: Boolean = false) {
        // Featured launch is now derived from upcoming launches
        // So we just need to load upcoming launches
        loadUpcomingLaunches(forceRefresh = forceRefresh)
    }

    /**
     * Loads the upcoming launches list along with previous launches for bidirectional carousel
     * Incorporates LaunchViewModel.fetchUpcomingLaunchesNormal()
     */
    fun loadUpcomingLaunches(limit: Int = 10, forceRefresh: Boolean = false) {
        println("=== HomeViewModel.loadUpcomingLaunches ===")
        println("Parameters: limit=$limit, forceRefresh=$forceRefresh")

        viewModelScope.launch {
            try {
                val hasExistingData = _upcomingLaunches.value.isNotEmpty()

                // STATE 1: Fresh load, no data - show shimmer
                if (!hasExistingData) {
                    _isLoading.value = true  // Main app loading indicator
                    _isUpcomingLaunchesLoading.value = true
                    _isPreviousLaunchesLoading.value = true
                    _isFeaturedLaunchLoading.value = true
                }
                // STATE 2: Stale data (automatic) - show data + pull-to-refresh indicator
                // STATE 3: Fresh data - do nothing, skip the call
                // STATE 4: User refresh - show data + pull-to-refresh indicator
                else if (forceRefresh) {
                    _isUpcomingLaunchesRefreshing.value = true
                    _isPreviousLaunchesRefreshing.value = true
                } else {
                    // STATE 3: Have fresh data, not forcing refresh - skip
                    println("Skipping load: data exists and not forcing refresh")
                    return@launch
                }

                _upcomingLaunchesError.value = null
                _previousLaunchesError.value = null

                // Wait for actual filter settings from DataStore (not the default initial value)
                val currentFilters = notificationStateStorage.stateFlow.first()
                println("=== HomeViewModel Filter Settings (Legacy) ===")
                println("followAllLaunches: ${currentFilters.followAllLaunches}")
                println("subscribedAgencies: ${currentFilters.subscribedAgencies}")
                println("subscribedLocations: ${currentFilters.subscribedLocations}")

                val filterParams = launchFilterService.getFilterParams(currentFilters)
                println("filterParams.agencyIds: ${filterParams.agencyIds}")
                println("filterParams.locationIds: ${filterParams.locationIds}")

                println("Calling repository methods with forceRefresh=$forceRefresh, filters: agencies=${filterParams.agencyIds?.size}, locations=${filterParams.locationIds?.size}")
                // Load both upcoming and previous launches in parallel
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

                val upcomingResult = upcomingDeferred.await()
                val previousResult = previousDeferred.await()

                upcomingResult.onSuccess { dataResult ->
                    val paginatedLaunches = dataResult.data
                    println("=== HomeViewModel: Received Upcoming Launches ===")
                    println("Total launches: ${paginatedLaunches.results.size}")

                    // Set the first launch as the featured launch and remove it from upcoming list
                    if (paginatedLaunches.results.isNotEmpty()) {
                        val featuredLaunch = paginatedLaunches.results.first()
                        _featuredLaunch.value = featuredLaunch

                        // ALSO update NEW ViewState for views that use it
                        _featuredLaunchState.update {
                            it.copy(
                                data = featuredLaunch,
                                isLoading = false,
                                error = null,
                                dataSource = dataResult.source,
                                cacheTimestamp = dataResult.timestamp
                            )
                        }

                        // Store the LaunchNormal in cache for quick access
                        launchCache.cacheLaunchNormal(featuredLaunch)

                        // Pre-fetch detailed data in the background for instant loading if user clicks "Explore"
                        preFetchLaunchDetails(featuredLaunch.id)

                        // Remove the first launch from upcoming list to avoid duplication
                        _upcomingLaunches.value = paginatedLaunches.results.drop(1)
                        
                        // ALSO update NEW ViewState for upcoming launches
                        _upcomingLaunchesState.update {
                            it.copy(
                                data = paginatedLaunches.results.drop(1),
                                isLoading = false,
                                error = null,
                                dataSource = dataResult.source,
                                cacheTimestamp = dataResult.timestamp
                            )
                        }
                        
                        println("Featured launch set from first upcoming: ${featuredLaunch.name}")
                        println("Upcoming launches list contains ${paginatedLaunches.results.size - 1} items (excluding featured)")
                        _featuredLaunchError.value = null
                    } else {
                        _featuredLaunch.value = null
                        _upcomingLaunches.value = emptyList()
                        
                        // ALSO update NEW ViewStates
                        _featuredLaunchState.update {
                            it.copy(data = null, isLoading = false, error = null)
                        }
                        _upcomingLaunchesState.update {
                            it.copy(data = emptyList(), isLoading = false, error = null)
                        }
                        
                        println("No launches available for featured launch")
                    }

                    _isLoading.value = false  // Clear main app loading indicator
                    _isUpcomingLaunchesLoading.value = false
                    _isUpcomingLaunchesRefreshing.value = false
                    _isFeaturedLaunchLoading.value = false

                    // Trigger animation by updating timestamp
                    _lastDataUpdate.value = System.now().toEpochMilliseconds()
                }.onFailure { exception ->
                    val errorMessage = formatErrorMessage(exception)
                    println("Failed to get upcoming launches: $errorMessage")
                    _upcomingLaunchesError.value = errorMessage
                    _upcomingLaunches.value = emptyList()

                    // Also handle featured launch error since it depends on upcoming launches
                    _featuredLaunch.value = null
                    _featuredLaunchError.value = errorMessage

                    // ALSO update NEW ViewStates
                    _featuredLaunchState.update {
                        it.copy(data = null, isLoading = false, error = errorMessage)
                    }
                    _upcomingLaunchesState.update {
                        it.copy(data = emptyList(), isLoading = false, error = errorMessage)
                    }

                    _isLoading.value = false  // Clear main app loading indicator even on error
                    _isUpcomingLaunchesLoading.value = false
                    _isUpcomingLaunchesRefreshing.value = false
                    _isFeaturedLaunchLoading.value = false
                }

                previousResult.onSuccess { dataResult ->
                    val paginatedPreviousLaunches = dataResult.data
                    println("=== HomeViewModel: Received Previous Launches ===")
                    println("Total previous launches: ${paginatedPreviousLaunches.results.size}")

                    _previousLaunches.value = paginatedPreviousLaunches.results
                    _isPreviousLaunchesLoading.value = false
                    _isPreviousLaunchesRefreshing.value = false

                    // Trigger animation by updating timestamp
                    _lastDataUpdate.value = System.now().toEpochMilliseconds()
                }.onFailure { exception ->
                    val errorMessage = formatErrorMessage(exception)
                    println("Failed to get previous launches: $errorMessage")
                    _previousLaunchesError.value = errorMessage
                    _previousLaunches.value = emptyList()
                    _isPreviousLaunchesLoading.value = false
                    _isPreviousLaunchesRefreshing.value = false
                }

                // Combine previous and upcoming launches for the carousel
                // Previous launches are in reverse chronological order (most recent first)
                // We want them in chronological order (oldest first) so they appear before upcoming
                val previousReversed = _previousLaunches.value.reversed()
                val newCombined = previousReversed + _upcomingLaunches.value

                println("=== Combined Launches Update ===")
                println("Previous: ${previousReversed.size}, Upcoming: ${_upcomingLaunches.value.size}")
                println("Old combined size: ${_combinedLaunches.value.size}")
                println("New combined size: ${newCombined.size}")

                _combinedLaunches.value = newCombined
                _upcomingStartIndex.value = previousReversed.size

                println("Combined launches updated successfully")
                println("Upcoming start index: ${_upcomingStartIndex.value}")

            } catch (exception: Exception) {
                _upcomingLaunchesError.value = exception.message
                _isLoading.value = false  // Clear main app loading indicator on exception
                _isUpcomingLaunchesLoading.value = false
                _isPreviousLaunchesLoading.value = false
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
    fun loadHistoryLaunchesNew(
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
     * Loads launches that happened on this day in history (same day and month in previous years)
     * LEGACY - Use loadHistoryLaunchesNew instead
     */
    fun loadHistoryLaunches(day: Int, month: Int, forceRefresh: Boolean = false) {
        // Skip loading if we already have data and not forcing refresh
        if (!forceRefresh && _historyLaunchesCount.value > 0 && !_isHistoryLaunchesLoading.value && _historyLaunchesError.value == null) {
            return
        }

        viewModelScope.launch {
            try {
                _isHistoryLaunchesLoading.value = true
                _historyLaunchesError.value = null

                val result =
                    launchRepository.getLaunchesByDayAndMonth(day = day, month = month, limit = 100)

                result.onSuccess { paginatedLaunches ->
                    println("=== HomeViewModel: Received History Launches ===")
                    println("Total launches on $month/$day: ${paginatedLaunches.count}")

                    // Store both the count and the actual launches (reversed to show most recent first)
                    _historyLaunchesCount.value = paginatedLaunches.count
                    _historyLaunches.value = paginatedLaunches.results.reversed()
                    _isHistoryLaunchesLoading.value = false
                }.onFailure { exception ->
                    val errorMessage = formatErrorMessage(exception)
                    println("Failed to get history launches: $errorMessage")
                    _historyLaunchesError.value = errorMessage
                    _historyLaunchesCount.value = 0
                    _historyLaunches.value = emptyList()
                    _isHistoryLaunchesLoading.value = false
                }
            } catch (exception: Exception) {
                _historyLaunchesError.value = exception.message
                _historyLaunchesCount.value = 0
                _isHistoryLaunchesLoading.value = false
            }
        }
    }

    /**
     * Loads the latest updates/news feed
     * LEGACY - Use loadUpdatesNew instead
     * Replaces UpdatesViewModel.fetchLatestUpdates()
     */
    fun loadUpdates(limit: Int = 10, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                val hasExistingData = _updates.value.isNotEmpty()

                if (!hasExistingData) {
                    // STATE 1: Fresh load, no data - show shimmer
                    _isUpdatesLoading.value = true
                } else if (forceRefresh) {
                    // STATE 2/4: Stale or user refresh - show pull-to-refresh
                    _isUpdatesRefreshing.value = true
                } else {
                    // STATE 3: Have fresh data - skip
                    return@launch
                }

                _updatesError.value = null

                val result =
                    updatesRepository.getLatestUpdates(limit = limit, forceRefresh = forceRefresh)

                result.onSuccess { dataResult ->
                    val paginatedUpdates = dataResult.data
                    println("=== HomeViewModel: Received Updates ===")
                    println("Total updates: ${paginatedUpdates.results.size}")

                    _updates.value = paginatedUpdates.results
                    _isUpdatesLoading.value = false
                    _isUpdatesRefreshing.value = false
                }.onFailure { exception: Throwable ->
                    val errorMessage = formatErrorMessage(exception)
                    println("Failed to get updates: $errorMessage")
                    _updatesError.value = errorMessage
                    _updates.value = emptyList()
                    _isUpdatesLoading.value = false
                    _isUpdatesRefreshing.value = false
                }

            } catch (exception: Exception) {
                _updatesError.value = exception.message
                _isUpdatesLoading.value = false
                _isUpdatesRefreshing.value = false
            }
        }
    }

    /**
     * Loads the latest news articles from SNAPI
     */
    fun loadArticles(limit: Int = 5, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                val hasExistingData = _articles.value.isNotEmpty()

                if (!hasExistingData) {
                    // STATE 1: Fresh load, no data - show shimmer
                    _isArticlesLoading.value = true
                } else if (forceRefresh) {
                    // STATE 2/4: Stale or user refresh - show pull-to-refresh
                    _isArticlesRefreshing.value = true
                } else {
                    // STATE 3: Have fresh data - skip
                    return@launch
                }

                _articlesError.value = null

                val result =
                    articlesRepository.getArticles(limit = limit, forceRefresh = forceRefresh)

                result.onSuccess { dataResult ->
                    val paginatedArticles = dataResult.data
                    println("=== HomeViewModel: Received Articles ===")
                    println("Total articles: ${paginatedArticles.results.size}")

                    _articles.value = paginatedArticles.results
                    _isArticlesLoading.value = false
                    _isArticlesRefreshing.value = false
                }.onFailure { exception: Throwable ->
                    val errorMessage = formatErrorMessage(exception)
                    println("Failed to get articles: $errorMessage")
                    _articlesError.value = errorMessage
                    _articles.value = emptyList()
                    _isArticlesLoading.value = false
                    _isArticlesRefreshing.value = false
                }

            } catch (exception: Exception) {
                _articlesError.value = exception.message
                _isArticlesLoading.value = false
                _isArticlesRefreshing.value = false
            }
        }
    }

    fun loadEvents(limit: Int = 10, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                val hasExistingData = _events.value.isNotEmpty()

                if (!hasExistingData) {
                    // STATE 1: Fresh load, no data - show shimmer
                    _isEventsLoading.value = true
                } else if (forceRefresh) {
                    // STATE 2/4: Stale or user refresh - show pull-to-refresh
                    _isEventsRefreshing.value = true
                } else {
                    // STATE 3: Have fresh data - skip
                    return@launch
                }

                _eventsError.value = null

                val result =
                    eventsRepository.getUpcomingEvents(limit = limit, forceRefresh = forceRefresh)

                result.onSuccess { dataResult ->
                    val paginatedEvents = dataResult.data
                    println("=== HomeViewModel: Received Events ===")
                    println("Total events: ${paginatedEvents.results.size}")

                    _events.value = paginatedEvents.results
                    _isEventsLoading.value = false
                    _isEventsRefreshing.value = false
                }.onFailure { exception: Throwable ->
                    val errorMessage = formatErrorMessage(exception)
                    println("Failed to get events: $errorMessage")
                    _eventsError.value = errorMessage
                    _events.value = emptyList()
                    _isEventsLoading.value = false
                    _isEventsRefreshing.value = false
                }

            } catch (exception: Exception) {
                _eventsError.value = exception.message
                _isEventsLoading.value = false
                _isEventsRefreshing.value = false
            }
        }
    }

    /**
     * Refreshes all home screen data
     */
    fun refresh() {
        loadHomeScreenData()
    }

    /**
     * Refreshes only the featured launch
     */
    fun refreshFeaturedLaunch() {
        loadFeaturedLaunch()
    }

    /**
     * Refreshes only the upcoming launches
     */
    fun refreshUpcomingLaunches() {
        loadUpcomingLaunches()
    }

    /**
     * Refreshes only the updates
     */
    fun refreshUpdates() {
        loadUpdates()
    }

    /**
     * Refreshes only the articles
     */
    fun refreshArticles() {
        loadArticles()
    }

    /**
     * Force refreshes all home screen data (bypasses cache)
     * Useful for pull-to-refresh functionality
     */
    fun refreshHomeScreenData() {
        // Set refreshing state immediately (synchronously) before launching coroutine
        // This ensures pull-to-refresh indicator stays visible
        _isRefreshing.value = true

        viewModelScope.launch {
            try {
                println("=== HomeViewModel.refreshHomeScreenData ===")
                println("Force refreshing all home screen data")
                clearErrors()

                // Force refresh all home screen data in parallel
                // Featured launch is derived from upcoming launches, so no separate call needed
                val upcomingLaunchesDeferred = async { loadUpcomingLaunches(forceRefresh = true) }
                val updatesDeferred = async { loadUpdates(forceRefresh = true) }
                val articlesDeferred = async { loadArticles(forceRefresh = true) }
                val eventsDeferred = async { loadEvents(forceRefresh = true) }

                // Wait for all to complete
                upcomingLaunchesDeferred.await()
                updatesDeferred.await()
                articlesDeferred.await()
                eventsDeferred.await()

                _isRefreshing.value = false

                // Trigger animation by updating timestamp
                _lastDataUpdate.value = System.now().toEpochMilliseconds()
            } catch (exception: Exception) {
                _error.value = exception.message ?: "Unknown error occurred"
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Retries only the failed network requests
     * Useful for automatically retrying when app comes back from background
     */
    fun retryFailedRequests() {
        viewModelScope.launch {
            try {
                clearErrors()

                // Only retry requests that have errors
                val retryTasks = mutableListOf<suspend () -> Unit>()

                if (_featuredLaunchError.value != null || _upcomingLaunchesError.value != null) {
                    retryTasks.add { loadUpcomingLaunches(forceRefresh = true) }
                }

                if (_updatesError.value != null) {
                    retryTasks.add { loadUpdates(forceRefresh = true) }
                }

                if (_articlesError.value != null) {
                    retryTasks.add { loadArticles(forceRefresh = true) }
                }

                if (_eventsError.value != null) {
                    retryTasks.add { loadEvents(forceRefresh = true) }
                }

                // Execute all retry tasks in parallel
                if (retryTasks.isNotEmpty()) {
                    println("HomeViewModel: Retrying ${retryTasks.size} failed requests")
                    val deferredTasks = retryTasks.map { task -> async { task() } }
                    deferredTasks.forEach { it.await() }
                    println("HomeViewModel: Retry completed")
                }

            } catch (exception: Exception) {
                println("HomeViewModel: Error during retry: ${exception.message}")
                _error.value = exception.message ?: "Unknown error occurred"
            }
        }
    }

    /**
     * Clears all error states
     */
    fun clearErrors() {
        _error.value = null
        _featuredLaunchError.value = null
        _upcomingLaunchesError.value = null
        _previousLaunchesError.value = null
        _updatesError.value = null
        _articlesError.value = null
        _eventsError.value = null
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
