package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

                // Load upcoming launches, updates, articles, and events in parallel
                // Featured launch will be derived from the first upcoming launch
                val upcomingLaunchesDeferred = async { loadUpcomingLaunches() }
                val updatesDeferred = async { loadUpdates() }
                val articlesDeferred = async { loadArticles() }
                val eventsDeferred = async { loadEvents() }

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
     * Loads the featured launch (first item from upcoming launches)
     * This now delegates to loadUpcomingLaunches since featured launch is derived from it
     */
    fun loadFeaturedLaunch(forceRefresh: Boolean = false) {
        // Featured launch is now derived from upcoming launches
        // So we just need to load upcoming launches
        loadUpcomingLaunches(forceRefresh = forceRefresh)
    }

    /**
     * Loads the upcoming launches list
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
                _upcomingLaunchesError.value = null

                val result = launchRepository.getUpcomingLaunchesNormal(limit = limit)
                
                result.onSuccess { paginatedLaunches: PaginatedLaunchNormalList ->
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

            } catch (exception: Exception) {
                _upcomingLaunchesError.value = exception.message
                _isUpcomingLaunchesLoading.value = false
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
        _updatesError.value = null
        _articlesError.value = null
        _eventsError.value = null
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
