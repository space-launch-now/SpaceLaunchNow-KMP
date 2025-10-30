package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock.System
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedUpdateEndpointList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.UpdateEndpoint
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.api.snapi.models.PaginatedArticleList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedEventEndpointNormalList
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepository
import me.calebjones.spacelaunchnow.data.repository.ArticlesRepository
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.cache.LaunchCache

/**
 * Consolidated ViewModel for the Home Screen that manages:
 * - Featured launch (NextUp)
 * - Upcoming launches list
 * - Updates/News feed (replaces UpdatesViewModel)
 * - News articles from SNAPI
 * - Overall loading and error states
 */
class HomeViewModel(
    private val launchRepository: LaunchRepository,
    private val updatesRepository: UpdatesRepository,
    private val articlesRepository: ArticlesRepository,
    private val eventsRepository: EventsRepository,
    private val launchCache: LaunchCache
) : ViewModel() {

    // Featured Launch (replaces NextUpViewModel functionality)
    private val _featuredLaunch = MutableStateFlow<LaunchNormal?>(null)
    val featuredLaunch: StateFlow<LaunchNormal?> = _featuredLaunch.asStateFlow()

    // Upcoming Launches List (incorporates LaunchViewModel functionality)
    private val _upcomingLaunches = MutableStateFlow<List<LaunchNormal>>(emptyList())
    val upcomingLaunches: StateFlow<List<LaunchNormal>> = _upcomingLaunches.asStateFlow()

    // Previous Launches List (for bidirectional carousel)
    private val _previousLaunches = MutableStateFlow<List<LaunchNormal>>(emptyList())
    val previousLaunches: StateFlow<List<LaunchNormal>> = _previousLaunches.asStateFlow()

    // This Day in History Launches (launches that happened on this day in previous years)
    private val _historyLaunches = MutableStateFlow<List<LaunchNormal>>(emptyList())
    val historyLaunches: StateFlow<List<LaunchNormal>> = _historyLaunches.asStateFlow()
    
    // Count of history launches (computed from API)
    private val _historyLaunchesCount = MutableStateFlow<Int>(0)
    val historyLaunchesCount: StateFlow<Int> = _historyLaunchesCount.asStateFlow()

    // Quick Stats - accurate counts from API
    private val _next24HoursCount = MutableStateFlow<Int>(0)
    val next24HoursCount: StateFlow<Int> = _next24HoursCount.asStateFlow()

    private val _nextWeekCount = MutableStateFlow<Int>(0)
    val nextWeekCount: StateFlow<Int> = _nextWeekCount.asStateFlow()

    private val _nextMonthCount = MutableStateFlow<Int>(0)
    val nextMonthCount: StateFlow<Int> = _nextMonthCount.asStateFlow()

    // Combined Launches (previous + upcoming for carousel)
    private val _combinedLaunches = MutableStateFlow<List<LaunchNormal>>(emptyList())
    val combinedLaunches: StateFlow<List<LaunchNormal>> = _combinedLaunches.asStateFlow()

    // Index where upcoming launches start in combined list
    private val _upcomingStartIndex = MutableStateFlow<Int>(0)
    val upcomingStartIndex: StateFlow<Int> = _upcomingStartIndex.asStateFlow()

    // Updates/News Feed (replaces UpdatesViewModel functionality)
    private val _updates = MutableStateFlow<List<UpdateEndpoint>>(emptyList())
    val updates: StateFlow<List<UpdateEndpoint>> = _updates.asStateFlow()

    // News Articles from SNAPI
    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    // Events from Launch Library
    private val _events = MutableStateFlow<List<EventEndpointNormal>>(emptyList())
    val events: StateFlow<List<EventEndpointNormal>> = _events.asStateFlow()

    // Loading States
    private val _isFeaturedLaunchLoading = MutableStateFlow(false)
    val isFeaturedLaunchLoading: StateFlow<Boolean> = _isFeaturedLaunchLoading.asStateFlow()

    private val _isUpcomingLaunchesLoading = MutableStateFlow(false)
    val isUpcomingLaunchesLoading: StateFlow<Boolean> = _isUpcomingLaunchesLoading.asStateFlow()

    private val _isPreviousLaunchesLoading = MutableStateFlow(false)
    val isPreviousLaunchesLoading: StateFlow<Boolean> = _isPreviousLaunchesLoading.asStateFlow()

    private val _isHistoryLaunchesLoading = MutableStateFlow(false)
    val isHistoryLaunchesLoading: StateFlow<Boolean> = _isHistoryLaunchesLoading.asStateFlow()

    private val _isUpdatesLoading = MutableStateFlow(false)
    val isUpdatesLoading: StateFlow<Boolean> = _isUpdatesLoading.asStateFlow()

    private val _isArticlesLoading = MutableStateFlow(false)
    val isArticlesLoading: StateFlow<Boolean> = _isArticlesLoading.asStateFlow()

    private val _isEventsLoading = MutableStateFlow(false)
    val isEventsLoading: StateFlow<Boolean> = _isEventsLoading.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error States
    private val _featuredLaunchError = MutableStateFlow<String?>(null)
    val featuredLaunchError: StateFlow<String?> = _featuredLaunchError.asStateFlow()

    private val _upcomingLaunchesError = MutableStateFlow<String?>(null)
    val upcomingLaunchesError: StateFlow<String?> = _upcomingLaunchesError.asStateFlow()

    private val _previousLaunchesError = MutableStateFlow<String?>(null)
    val previousLaunchesError: StateFlow<String?> = _previousLaunchesError.asStateFlow()

    private val _historyLaunchesError = MutableStateFlow<String?>(null)
    val historyLaunchesError: StateFlow<String?> = _historyLaunchesError.asStateFlow()

    private val _updatesError = MutableStateFlow<String?>(null)
    val updatesError: StateFlow<String?> = _updatesError.asStateFlow()

    private val _articlesError = MutableStateFlow<String?>(null)
    val articlesError: StateFlow<String?> = _articlesError.asStateFlow()

    private val _eventsError = MutableStateFlow<String?>(null)
    val eventsError: StateFlow<String?> = _eventsError.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Loads all home screen data in parallel for better performance
     * This is the main entry point for the home screen
     */
    fun loadHomeScreenData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                clearErrors()

                // Load upcoming launches, updates, articles, events, and quick stats in parallel
                // Featured launch will be derived from the first upcoming launch
                val upcomingLaunchesDeferred = async { loadUpcomingLaunches() }
                val updatesDeferred = async { loadUpdates() }
                val articlesDeferred = async { loadArticles() }
                val eventsDeferred = async { loadEvents() }
                val next24HoursDeferred = async { loadNext24Hours() }
                val nextWeekDeferred = async { loadNextWeek() }
                val nextMonthDeferred = async { loadNextMonth() }

                // Wait for all to complete
                upcomingLaunchesDeferred.await()
                updatesDeferred.await()
                articlesDeferred.await()
                eventsDeferred.await()
                next24HoursDeferred.await()
                nextWeekDeferred.await()
                nextMonthDeferred.await()

                _isLoading.value = false
            } catch (exception: Exception) {
                _error.value = exception.message ?: "Unknown error occurred"
                _isLoading.value = false
            }
        }
    }

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
        // Skip loading if we already have data and not forcing refresh
        if (!forceRefresh && _upcomingLaunches.value.isNotEmpty() && !_isUpcomingLaunchesLoading.value && _upcomingLaunchesError.value == null) {
            return
        }
        
        viewModelScope.launch {
            try {
                _isUpcomingLaunchesLoading.value = true
                _isPreviousLaunchesLoading.value = true
                _upcomingLaunchesError.value = null
                _previousLaunchesError.value = null

                // Load both upcoming and previous launches in parallel
                val upcomingDeferred = async { launchRepository.getUpcomingLaunchesNormal(limit = limit) }
                val previousDeferred = async { launchRepository.getPreviousLaunchesNormal(limit = 5) }

                val upcomingResult = upcomingDeferred.await()
                val previousResult = previousDeferred.await()
                
                upcomingResult.onSuccess { paginatedLaunches: PaginatedLaunchNormalList ->
                    println("=== HomeViewModel: Received Upcoming Launches ===")
                    println("Total launches: ${paginatedLaunches.results.size}")
                    
                    // Set the first launch as the featured launch and remove it from upcoming list
                    if (paginatedLaunches.results.isNotEmpty()) {
                        val featuredLaunch = paginatedLaunches.results.first()
                        _featuredLaunch.value = featuredLaunch
                        
                        // Store the LaunchNormal in cache for quick access
                        launchCache.cacheLaunchNormal(featuredLaunch)
                        
                        // Pre-fetch detailed data in the background for instant loading if user clicks "Explore"
                        preFetchLaunchDetails(featuredLaunch.id)
                        
                        // Remove the first launch from upcoming list to avoid duplication
                        _upcomingLaunches.value = paginatedLaunches.results.drop(1)
                        println("Featured launch set from first upcoming: ${featuredLaunch.name}")
                        println("Upcoming launches list contains ${paginatedLaunches.results.size - 1} items (excluding featured)")
                        _featuredLaunchError.value = null
                    } else {
                        _featuredLaunch.value = null
                        _upcomingLaunches.value = emptyList()
                        println("No launches available for featured launch")
                    }
                    
                    _isUpcomingLaunchesLoading.value = false
                    _isFeaturedLaunchLoading.value = false
                }.onFailure { exception ->
                    val errorMessage = formatErrorMessage(exception)
                    println("Failed to get upcoming launches: $errorMessage")
                    _upcomingLaunchesError.value = errorMessage
                    _upcomingLaunches.value = emptyList()
                    
                    // Also handle featured launch error since it depends on upcoming launches
                    _featuredLaunch.value = null
                    _featuredLaunchError.value = errorMessage
                    
                    _isUpcomingLaunchesLoading.value = false
                    _isFeaturedLaunchLoading.value = false
                }

                previousResult.onSuccess { paginatedPreviousLaunches: PaginatedLaunchNormalList ->
                    println("=== HomeViewModel: Received Previous Launches ===")
                    println("Total previous launches: ${paginatedPreviousLaunches.results.size}")
                    
                    _previousLaunches.value = paginatedPreviousLaunches.results
                    _isPreviousLaunchesLoading.value = false
                }.onFailure { exception ->
                    val errorMessage = formatErrorMessage(exception)
                    println("Failed to get previous launches: $errorMessage")
                    _previousLaunchesError.value = errorMessage
                    _previousLaunches.value = emptyList()
                    _isPreviousLaunchesLoading.value = false
                }

                // Combine previous and upcoming launches for the carousel
                // Previous launches are in reverse chronological order (most recent first)
                // We want them in chronological order (oldest first) so they appear before upcoming
                val previousReversed = _previousLaunches.value.reversed()
                _combinedLaunches.value = previousReversed + _upcomingLaunches.value
                _upcomingStartIndex.value = previousReversed.size

                println("=== Combined Launches ===")
                println("Previous: ${previousReversed.size}, Upcoming: ${_upcomingLaunches.value.size}")
                println("Upcoming start index: ${_upcomingStartIndex.value}")

            } catch (exception: Exception) {
                _upcomingLaunchesError.value = exception.message
                _isUpcomingLaunchesLoading.value = false
                _isPreviousLaunchesLoading.value = false
            }
        }
    }

    /**
     * Loads launches that happened on this day in history (same day and month in previous years)
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

                val result = launchRepository.getLaunchesByDayAndMonth(day = day, month = month, limit = 100)
                
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
     * Replaces UpdatesViewModel.fetchLatestUpdates()
     */
    fun loadUpdates(limit: Int = 10, forceRefresh: Boolean = false) {
        // Skip loading if we already have data and not forcing refresh
        if (!forceRefresh && _updates.value.isNotEmpty() && !_isUpdatesLoading.value && _updatesError.value == null) {
            return
        }
        
        viewModelScope.launch {
            try {
                _isUpdatesLoading.value = true
                _updatesError.value = null

                val result = updatesRepository.getLatestUpdates(limit = limit)
                
                result.onSuccess { paginatedUpdates: PaginatedUpdateEndpointList ->
                    println("=== HomeViewModel: Received Updates ===")
                    println("Total updates: ${paginatedUpdates.results.size}")
                    
                    _updates.value = paginatedUpdates.results
                    _isUpdatesLoading.value = false
                }.onFailure { exception: Throwable ->
                    val errorMessage = formatErrorMessage(exception)
                    println("Failed to get updates: $errorMessage")
                    _updatesError.value = errorMessage
                    _updates.value = emptyList()
                    _isUpdatesLoading.value = false
                }

            } catch (exception: Exception) {
                _updatesError.value = exception.message
                _isUpdatesLoading.value = false
            }
        }
    }

    /**
     * Loads the latest news articles from SNAPI
     */
    fun loadArticles(limit: Int = 5, forceRefresh: Boolean = false) {
        // Skip loading if we already have data and not forcing refresh
        if (!forceRefresh && _articles.value.isNotEmpty() && !_isArticlesLoading.value && _articlesError.value == null) {
            return
        }
        
        viewModelScope.launch {
            try {
                _isArticlesLoading.value = true
                _articlesError.value = null

                val result = articlesRepository.getArticles(limit = limit)
                
                result.onSuccess { paginatedArticles: PaginatedArticleList ->
                    println("=== HomeViewModel: Received Articles ===")
                    println("Total articles: ${paginatedArticles.results.size}")
                    
                    _articles.value = paginatedArticles.results
                    _isArticlesLoading.value = false
                }.onFailure { exception: Throwable ->
                    val errorMessage = formatErrorMessage(exception)
                    println("Failed to get articles: $errorMessage")
                    _articlesError.value = errorMessage
                    _articles.value = emptyList()
                    _isArticlesLoading.value = false
                }

            } catch (exception: Exception) {
                _articlesError.value = exception.message
                _isArticlesLoading.value = false
            }
        }
    }

    fun loadEvents(limit: Int = 10, forceRefresh: Boolean = false) {
        // Skip loading if we already have data and not forcing refresh
        if (!forceRefresh && _events.value.isNotEmpty() && !_isEventsLoading.value && _eventsError.value == null) {
            return
        }
        
        viewModelScope.launch {
            try {
                _isEventsLoading.value = true
                _eventsError.value = null

                val result = eventsRepository.getUpcomingEvents(limit = limit)
                
                result.onSuccess { paginatedEvents: PaginatedEventEndpointNormalList ->
                    println("=== HomeViewModel: Received Events ===")
                    println("Total events: ${paginatedEvents.results.size}")
                    
                    _events.value = paginatedEvents.results
                    _isEventsLoading.value = false
                }.onFailure { exception: Throwable ->
                    val errorMessage = formatErrorMessage(exception)
                    println("Failed to get events: $errorMessage")
                    _eventsError.value = errorMessage
                    _events.value = emptyList()
                    _isEventsLoading.value = false
                }

            } catch (exception: Exception) {
                _eventsError.value = exception.message
                _isEventsLoading.value = false
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
        viewModelScope.launch {
            try {
                _isLoading.value = true
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

                _isLoading.value = false
            } catch (exception: Exception) {
                _error.value = exception.message ?: "Unknown error occurred"
                _isLoading.value = false
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
