package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.VidURL
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.cache.LaunchCache
import me.calebjones.spacelaunchnow.data.repository.ArticlesRepository
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.ui.state.VideoPlayerState

class LaunchViewModel(
    private val repository: LaunchRepository,
    private val launchCache: LaunchCache,
    private val articlesRepository: ArticlesRepository
) : ViewModel() {

    private val _upcomingLaunches = MutableStateFlow<PaginatedLaunchBasicList?>(null)
    val upcomingLaunches: StateFlow<PaginatedLaunchBasicList?> = _upcomingLaunches

    private val _upcomingLaunchesNormal = MutableStateFlow<PaginatedLaunchNormalList?>(null)
    val upcomingLaunchesNormal: StateFlow<PaginatedLaunchNormalList?> = _upcomingLaunchesNormal

    private val _launchDetails = MutableStateFlow<LaunchDetailed?>(null)
    val launchDetails: StateFlow<LaunchDetailed?> = _launchDetails

    private val _agencyDataMap = MutableStateFlow<Map<Int, AgencyEndpointDetailed>>(emptyMap())
    val agencyDataMap: StateFlow<Map<Int, AgencyEndpointDetailed>> = _agencyDataMap

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

    fun fetchUpcomingLaunchesNormal(limit: Int) {
        viewModelScope.launch {
            val result = repository.getUpcomingLaunchesNormal(limit = limit)

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

            // Check if we have detailed data in cache first (unless forcing refresh)
            if (!forceRefresh) {
                val cachedDetailed = launchCache.getCachedLaunchDetailed(id)
                if (cachedDetailed != null) {
                    _launchDetails.value = cachedDetailed
                    updateVideoPlayerState(cachedDetailed)
                    _isLoading.value = false
                    return@launch
                }
            }

            _isLoading.value = true

            val result = repository.getLaunchDetails(id, forceRefresh = forceRefresh)
            result.onSuccess { launch ->
                _launchDetails.value = launch
                updateVideoPlayerState(launch)
                // Cache the detailed data for future use
                launchCache.cacheLaunchDetailed(launch)
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
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
    fun setLaunchDetails(launchDetailed: LaunchDetailed) {
        _launchDetails.value = launchDetailed
        updateVideoPlayerState(launchDetailed)
        _error.value = null
        _isLoading.value = false
    }

    /**
     * Get cached launch normal data if available. This can be used by the UI
     * to show basic information while detailed data is being fetched.
     */
    fun getCachedLaunchNormal(id: String): LaunchNormal? {
        return launchCache.getCachedLaunchNormal(id)
    }

    fun fetchAgencyData(agencyId: Int) {
        viewModelScope.launch {
            if (_agencyDataMap.value.containsKey(agencyId)) {
                // Data already fetched, no need to fetch again
                return@launch
            }

            val result = repository.getAgencyDetails(agencyId)
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

    private fun updateVideoPlayerState(launch: LaunchDetailed) {
        val youTubeVideos = launch.vidUrls.filter {
            me.calebjones.spacelaunchnow.util.VideoUtil.isYouTubeUrl(it.url)
        }

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
    fun getCurrentVideo(): VidURL? {
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
}
