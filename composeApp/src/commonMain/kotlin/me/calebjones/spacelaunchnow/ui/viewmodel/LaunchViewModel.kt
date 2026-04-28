package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.cache.LaunchCache
import me.calebjones.spacelaunchnow.data.repository.AgencyRepository
import me.calebjones.spacelaunchnow.data.repository.ArticlesRepository
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.VideoLink
import me.calebjones.spacelaunchnow.ui.state.VideoPlayerState

class LaunchViewModel(
    private val repository: LaunchRepository,
    private val launchCache: LaunchCache,
    private val articlesRepository: ArticlesRepository,
    private val eventsRepository: EventsRepository,
    private val agencyRepository: AgencyRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _upcomingLaunches = MutableStateFlow<PaginatedResult<Launch>?>(null)
    val upcomingLaunches: StateFlow<PaginatedResult<Launch>?> = _upcomingLaunches

    private val _upcomingLaunchesNormal = MutableStateFlow<PaginatedResult<Launch>?>(null)
    val upcomingLaunchesNormal: StateFlow<PaginatedResult<Launch>?> = _upcomingLaunchesNormal

    private val _launchDetails = MutableStateFlow<Launch?>(null)
    val launchDetails: StateFlow<Launch?> = _launchDetails

    private val _agencyDataMap = MutableStateFlow<Map<Int, Agency>>(emptyMap())
    val agencyDataMap: StateFlow<Map<Int, Agency>> = _agencyDataMap

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Video Player State
    private val _videoPlayerState = MutableStateFlow(VideoPlayerState())
    val videoPlayerState: StateFlow<VideoPlayerState> = _videoPlayerState

    // Related News State
    private val _relatedNews = MutableStateFlow<List<Article>>(emptyList())
    val relatedNews: StateFlow<List<Article>> = _relatedNews

    private val _isNewsLoading = MutableStateFlow(false)
    val isNewsLoading: StateFlow<Boolean> = _isNewsLoading

    private val _newsError = MutableStateFlow<String?>(null)
    val newsError: StateFlow<String?> = _newsError

    // Related Events State
    private val _relatedEvents = MutableStateFlow<List<Event>>(emptyList())
    val relatedEvents: StateFlow<List<Event>> = _relatedEvents

    private val _isEventsLoading = MutableStateFlow(false)
    val isEventsLoading: StateFlow<Boolean> = _isEventsLoading

    private val _eventsError = MutableStateFlow<String?>(null)
    val eventsError: StateFlow<String?> = _eventsError

    // Stale-while-revalidate: Track when we're refreshing with stale data displayed
    private val _isRefreshingWithStaleData = MutableStateFlow(false)
    val isRefreshingWithStaleData: StateFlow<Boolean> = _isRefreshingWithStaleData

    // ========== Analytics ==========

    fun trackLinkOpened(url: String, launchId: String) {
        analyticsManager.track(
            AnalyticsEvent.ThirdPartyReferral(
                provider = "launch_detail",
                url = url,
                contentType = "launch_link",
                contentId = launchId
            )
        )
    }

    fun trackVideoOpened(videoUrl: String, videoSource: String) {
        analyticsManager.track(
            AnalyticsEvent.VideoOpenedExternal(
                videoUrl = videoUrl,
                videoSource = videoSource
            )
        )
    }

    fun fetchUpcomingLaunchesNormal(limit: Int) {
        viewModelScope.launch {
            val result = repository.getUpcomingLaunchesNormalDomain(limit = limit)

            result.onSuccess { dataResult ->
                _upcomingLaunchesNormal.value = dataResult.data
            }.onFailure { exception ->
                _error.value = exception.message
            }
        }
    }

    fun fetchLaunchDetails(id: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _error.value = null
            _isRefreshingWithStaleData.value = false

            // Check if we have detailed data in cache first (unless forcing refresh)
            if (!forceRefresh) {
                val cachedLaunch = launchCache.getCachedLaunch(id)
                if (cachedLaunch != null) {
                    _launchDetails.value = cachedLaunch
                    updateVideoPlayerState(cachedLaunch)
                    _isLoading.value = false
                    return@launch
                }
            }

            // Stale-while-revalidate: Check for stale data to display while fetching
            val staleData = repository.getStaleDetailedLaunch(id)?.toDomain()
            if (staleData != null) {
                // We have stale data - show it immediately while refreshing
                _launchDetails.value = staleData
                updateVideoPlayerState(staleData)
                _isRefreshingWithStaleData.value = true
                _isLoading.value = false // Don't show full shimmer, we have data
            } else {
                // No data at all - show loading shimmer
                _isLoading.value = true
            }

            val result = repository.getLaunchDetailDomain(id, forceRefresh = forceRefresh)
            result.onSuccess { launch ->
                _launchDetails.value = launch
                updateVideoPlayerState(launch)
                // Cache the detailed data for future use
                launchCache.cacheLaunch(launch)
                analyticsManager.track(AnalyticsEvent.LaunchViewed(launch.id, launch.name))
            }.onFailure { exception ->
                // Only show error if we don't have any data to display
                if (_launchDetails.value == null) {
                    _error.value = exception.message
                }
            }
            _isLoading.value = false
            _isRefreshingWithStaleData.value = false
        }
    }

    /**
     * Force refresh launch details (bypasses cache)
     * Useful for pull-to-refresh functionality
     */
    fun refreshLaunchDetails(id: String) {
        fetchLaunchDetails(id, forceRefresh = true)
    }

    /**
     * Set launch details directly (used when we have preloaded data)
     */
    fun setLaunchDetails(launch: Launch) {
        _launchDetails.value = launch
        updateVideoPlayerState(launch)
        _error.value = null
        _isLoading.value = false
    }

    /**
     * Clear launch details so the UI shows a loading state while new data is fetched.
     */
    fun clearLaunchDetails() {
        _launchDetails.value = null
        _error.value = null
        _isLoading.value = true
    }

    /**
     * Get cached launch normal data if available. This can be used by the UI
     * to show basic information while detailed data is being fetched.
     */
    fun getCachedLaunch(id: String): Launch? {
        return launchCache.getCachedLaunch(id)
    }

    fun fetchAgencyData(agencyId: Int) {
        viewModelScope.launch {
            if (_agencyDataMap.value.containsKey(agencyId)) {
                // Data already fetched, no need to fetch again
                return@launch
            }

            val result = agencyRepository.getAgencyDetailDomain(agencyId)
            result.onSuccess { agencyData ->
                _agencyDataMap.value = _agencyDataMap.value.toMutableMap().apply {
                    put(agencyId, agencyData)
                }
            }.onFailure { exception ->
                _error.value = exception.message
            }
        }
    }

    // Video Player State Management

    private fun updateVideoPlayerState(launch: Launch) {
        val youTubeVideos = launch.vidUrls

        _videoPlayerState.value = _videoPlayerState.value.copy(
            availableVideos = youTubeVideos,
            selectedVideoIndex = if (youTubeVideos.isNotEmpty())
                _videoPlayerState.value.selectedVideoIndex.coerceAtMost(youTubeVideos.size - 1)
            else 0
        )
    }

    /**
     * Select a different video by index
     */
    fun selectVideo(index: Int) {
        val currentState = _videoPlayerState.value
        if (index >= 0 && index < currentState.availableVideos.size) {
            _videoPlayerState.value = currentState.copy(
                selectedVideoIndex = index,
                isPlayerVisible = false // Reset player when changing videos
            )
        }
    }

    /**
     * Show/hide the video player
     */
    fun setPlayerVisible(visible: Boolean) {
        _videoPlayerState.value = _videoPlayerState.value.copy(
            isPlayerVisible = visible
        )
    }

    fun setFullscreen(fullscreen: Boolean) {
        _videoPlayerState.value = _videoPlayerState.value.copy(isFullscreen = fullscreen)
    }

    /**
     * Get the currently selected video
     */
    fun getCurrentVideo(): VideoLink? {
        val state = _videoPlayerState.value
        return if (state.selectedVideoIndex < state.availableVideos.size) {
            state.availableVideos[state.selectedVideoIndex]
        } else null
    }

    /**
     * Check if there are multiple videos available
     */
    fun hasMultipleVideos(): Boolean {
        return _videoPlayerState.value.availableVideos.size > 1
    }

    // Related News Methods

    /**
     * Fetch related news articles for a launch
     */
    fun fetchRelatedNews(launchId: String, limit: Int = 20) {
        viewModelScope.launch {
            _isNewsLoading.value = true
            _newsError.value = null

            val result = articlesRepository.getArticlesByLaunch(
                launchIds = listOf(launchId),
                limit = limit
            )

            result.onSuccess { paginatedList ->
                print("Related news size: ${paginatedList.results.size}")
                _relatedNews.value = paginatedList.results
            }.onFailure { exception ->
                print("Error fetching related news: ${exception.message}")
                _newsError.value = exception.message
            }

            _isNewsLoading.value = false
        }
    }

    /**
     * Clear related news state
     */
    fun clearRelatedNews() {
        _relatedNews.value = emptyList()
        _newsError.value = null
        _isNewsLoading.value = false
    }

    // Related Events Methods

    /**
     * Fetch related events for a launch
     */
    fun fetchRelatedEvents(launchId: String, limit: Int = 20) {
        viewModelScope.launch {
            _isEventsLoading.value = true
            _eventsError.value = null

            val result = eventsRepository.getEventsByLaunchIdDomain(
                launchId = launchId,
                limit = limit
            )

            result.onSuccess { paginatedResult ->
                print("Related events size: ${paginatedResult.results.size}")
                _relatedEvents.value = paginatedResult.results
            }.onFailure { exception ->
                print("Error fetching related events: ${exception.message}")
                _eventsError.value = exception.message
            }

            _isEventsLoading.value = false
        }
    }

    /**
     * Clear related events state
     */
    fun clearRelatedEvents() {
        _relatedEvents.value = emptyList()
        _eventsError.value = null
        _isEventsLoading.value = false
    }
}
