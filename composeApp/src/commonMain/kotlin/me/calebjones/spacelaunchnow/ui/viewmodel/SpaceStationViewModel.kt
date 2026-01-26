package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.statement.request
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.extensions.getSpaceStationDetailed
import me.calebjones.spacelaunchnow.api.iss.IssTrackingRepository
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ExpeditionsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.SpaceStationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ExpeditionDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpaceStationDetailedEndpoint
import me.calebjones.spacelaunchnow.api.snapi.apis.ArticlesApi
import me.calebjones.spacelaunchnow.api.snapi.extensions.searchArticles
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.api.launchlibrary.models.VidURL
import me.calebjones.spacelaunchnow.api.launchlibrary.models.VidURLType
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Language
import me.calebjones.spacelaunchnow.ui.state.VideoPlayerState
import me.calebjones.spacelaunchnow.util.AppSecrets
import me.calebjones.spacelaunchnow.util.LatLng
import me.calebjones.spacelaunchnow.util.YouTubeApiUtil
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock

/**
 * Complete ISS position data including location, velocity, altitude, and visibility
 */
data class IssPositionData(
    val position: LatLng,
    val altitude: Double, // km
    val velocity: Double, // km/h
    val visibility: String // "daylight" or "eclipsed"
)

/**
 * ViewModel for Space Station detail screen
 * Manages data from multiple sources:
 * - Launch Library API (station details, crew, expeditions)
 * - ISS Tracking API (live position, orbit path)
 * - SNAPI (related news reports)
 */
class SpaceStationViewModel(
    private val spaceStationsApi: SpaceStationsApi,
    private val expeditionsApi: ExpeditionsApi,
    private val articlesApi: ArticlesApi,
    private val issTrackingRepository: IssTrackingRepository,
    private val httpClient: HttpClient
) : ViewModel() {

    private val log = logger()

    companion object {
        private const val ISS_STATION_ID = 4
        private const val POSITION_UPDATE_INTERVAL_MS = 10_000L // 10 seconds
        private const val ORBIT_PATH_UPDATE_INTERVAL_MS = 60_000L // 1 minute
        private const val API_BATCH_SIZE = 10 // wheretheiss.at API limit
    }

    // Station details from Launch Library
    private val _stationDetails = MutableStateFlow<SpaceStationDetailedEndpoint?>(null)
    val stationDetails: StateFlow<SpaceStationDetailedEndpoint?> = _stationDetails

    // Active expedition details with crew information
    private val _activeExpeditions = MutableStateFlow<List<ExpeditionDetailed>>(emptyList())
    val activeExpeditions: StateFlow<List<ExpeditionDetailed>> = _activeExpeditions

    // ISS live position with full data
    private val _issPositionData = MutableStateFlow<IssPositionData?>(null)
    val issPositionData: StateFlow<IssPositionData?> = _issPositionData
    
    // ISS live position (legacy - just coordinates)
    private val _issPosition = MutableStateFlow<LatLng?>(null)
    val issPosition: StateFlow<LatLng?> = _issPosition

    // Orbit path from wheretheiss.at positions API
    private val _orbitPath = MutableStateFlow<List<LatLng>>(emptyList())
    val orbitPath: StateFlow<List<LatLng>> = _orbitPath

    // Related news articles
    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles

    // Video player state for ISS live stream
    private val _videoPlayerState = MutableStateFlow(VideoPlayerState())
    val videoPlayerState: StateFlow<VideoPlayerState> = _videoPlayerState

    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Position polling job
    private var positionPollingJob: Job? = null

    // Track when we last updated the orbit path
    private var lastOrbitPathUpdate: Long = 0

    // Track if ISS tracking is currently active
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking

    /**
     * Fetch all data for a space station
     */
    fun fetchStationDetails(stationId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                log.d { "Fetching space station details for ID: $stationId" }

                // Fetch station details from Launch Library
                val stationResponse = spaceStationsApi.getSpaceStationDetailed(stationId)
                val station = stationResponse.body()
                _stationDetails.value = station
                log.i { "Successfully loaded station: ${station.name}" }

                // Fetch detailed expedition data for each active expedition
                fetchExpeditionDetails(station.activeExpeditions)

                // Fetch related news articles
                fetchArticles()

                // If this is the ISS, start live tracking and set up video
                if (stationId == ISS_STATION_ID) {
                    startIssTracking()
                    initializeIssVideoPlayer()
                }

                _isLoading.value = false
            } catch (exception: Exception) {
                log.e(exception) { "Error fetching station details" }
                _error.value = exception.message ?: "Failed to load station details"
                _isLoading.value = false
            }
        }
    }

    /**
     * Fetch detailed expedition data including crew for each active expedition
     */
    private suspend fun fetchExpeditionDetails(expeditions: List<me.calebjones.spacelaunchnow.api.launchlibrary.models.ExpeditionMini>) {
        try {
            log.d { "Fetching expedition details for ${expeditions.size} active expeditions" }
            val detailedExpeditions = expeditions.mapNotNull { expedition ->
                try {
                    val response = expeditionsApi.expeditionsRetrieve(expedition.id)
                    log.d { "Loaded expedition: ${response.body().name} with ${response.body().crew.size} crew members" }
                    response.body()
                } catch (e: Exception) {
                    log.e(e) { "Error fetching expedition ${expedition.id}: ${e.message}" }
                    null
                }
            }
            _activeExpeditions.value = detailedExpeditions
            log.i { "Loaded ${detailedExpeditions.size} expedition details" }
        } catch (exception: Exception) {
            log.e(exception) { "Error fetching expedition details: ${exception.message}" }
            // Don't fail the whole screen if expedition details fail
        }
    }

    /**
     * Fetch news articles related to the International Space Station
     */
    private suspend fun fetchArticles() {
        try {
            log.d { "Fetching articles for: International Space Station" }
            val articlesResponse = articlesApi.searchArticles(
                query = "International Space Station",
                limit = 10
            )
            log.d { "Articles API response status: ${articlesResponse.status}" }
            try {
                val body = articlesResponse.body()
                log.d { "Articles API response body count: ${body.count}" }
                _articles.value = body.results
                log.i { "Loaded ${body.results.size} articles" }
            } catch (bodyEx: Exception) {
                log.e(bodyEx) { "Error parsing articles body" }
            }
        } catch (exception: Exception) {
            log.e(exception) { "Error fetching articles: ${exception.message}" }
            // Don't fail the whole screen if articles fail
        }
    }

    /**
     * Initialize ISS live stream video player by fetching the current NASA live stream from YouTube API.
     * Falls back to a default video ID if the API call fails.
     */
    private fun initializeIssVideoPlayer() {
        viewModelScope.launch {
            val apiKey = AppSecrets.mapsApiKey
            val liveStreamInfo = YouTubeApiUtil.getNasaLiveStreamVideoId(httpClient, apiKey)
            
            val videoUrl: String
            val videoTitle: String
            val thumbnailUrl: String?
            
            if (liveStreamInfo != null) {
                videoUrl = YouTubeApiUtil.buildYouTubeUrl(liveStreamInfo.videoId)
                videoTitle = liveStreamInfo.title
                thumbnailUrl = liveStreamInfo.thumbnailUrl
                log.i { "Using live NASA stream: $videoTitle ($videoUrl)" }
            } else {
                // Fallback to ISS HD Earth Viewing Experiment
                videoUrl = "https://www.youtube.com/watch?v=xAieE-QtOeM"
                videoTitle = "ISS HD Earth Viewing Experiment"
                thumbnailUrl = "https://assets.science.nasa.gov/content/dam/science/astro/universe/2023/09/SpaceStation-1.png"
                log.w { "No live stream found, using fallback: $videoUrl" }
            }
            
            val nasaLiveStream = VidURL(
                url = videoUrl,
                type = VidURLType(id = 1, name = "Official"),
                language = Language(id = 1, name = "English", code = "en"),
                priority = 0,
                source = "NASA",
                publisher = "NASA",
                title = videoTitle,
                description = "Live HD views of Earth from the International Space Station",
                featureImage = thumbnailUrl,
                live = true
            )
            _videoPlayerState.value = VideoPlayerState(
                availableVideos = listOf(nasaLiveStream),
                selectedVideoIndex = 0,
                isPlayerVisible = false
            )
        }
    }

    /**
     * Select a different video by index
     */
    fun selectVideo(index: Int) {
        val currentState = _videoPlayerState.value
        if (index >= 0 && index < currentState.availableVideos.size) {
            _videoPlayerState.value = currentState.copy(
                selectedVideoIndex = index,
                isPlayerVisible = false
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
    
    /**
     * Start ISS live tracking with position polling
     */
    private fun startIssTracking() {
        // Cancel any existing polling
        positionPollingJob?.cancel()
        _isTracking.value = true

        positionPollingJob = viewModelScope.launch {
            try {
                // Get initial position first
                updateIssPosition()

                // Then fetch orbit path from API
                fetchOrbitPathFromApi()

                // Start polling for live position
                while (true) {
                    delay(POSITION_UPDATE_INTERVAL_MS)
                    updateIssPosition()

                    // Refresh orbit path every minute
                    if (Clock.System.now()
                            .toEpochMilliseconds() - lastOrbitPathUpdate > ORBIT_PATH_UPDATE_INTERVAL_MS
                    ) {
                        fetchOrbitPathFromApi()
                    }
                }
            } catch (cancellation: CancellationException) {
                // Expected when pausing tracking - don't log as error
                log.d { "ISS tracking cancelled (app paused or stopped)" }
                throw cancellation // Re-throw to properly handle coroutine cancellation
            } catch (exception: Exception) {
                log.e(exception) { "Error in ISS tracking loop" }
                _error.value = "ISS tracking unavailable"
            }
        }
    }

    /**
     * Update current ISS position
     */
    private suspend fun updateIssPosition() {
        try {
            issTrackingRepository.getCurrentPosition()
                .onSuccess { position ->
                    val latLng = LatLng(position.latitude, position.longitude)
                    
                    _issPositionData.value = IssPositionData(
                        position = latLng,
                        altitude = position.altitude,
                        velocity = position.velocity,
                        visibility = position.visibility
                    )
                    
                    // Also update the legacy issPosition field
                    _issPosition.value = latLng
                    
                    log.i { "ISS POSITION: Lat=${position.latitude}, Lon=${position.longitude}, Alt=${position.altitude}km, Velocity=${position.velocity}km/h, Visibility=${position.visibility}" }
                }
                .onFailure { exception ->
                    log.e(exception) { "Failed to fetch ISS position" }
                }
        } catch (exception: Exception) {
            log.e(exception) { "Error updating ISS position" }
        }
    }

    /**
     * Fetch orbit path from wheretheiss.at API
     * Uses chunked requests due to 10 timestamp limit per request
     * Generates path for +/- 90 minutes (180 minutes total)
     */
    private suspend fun fetchOrbitPathFromApi() {
        try {
            val currentTime = Clock.System.now().toEpochMilliseconds() / 1000
            val durationMinutes = 180 // +/- 90 minutes total
            val numPoints = 90 // One point every 2 minutes
            val intervalSeconds = (durationMinutes * 60) / numPoints

            // Generate timestamps centered on current time
            val startTime = currentTime - (durationMinutes * 60 / 2)
            val timestamps = (0 until numPoints).map { i ->
                startTime + (i * intervalSeconds)
            }

            log.d { "Fetching orbit path: $numPoints points over $durationMinutes minutes" }

            // Chunk into batches of 10 (API limit)
            val chunks = timestamps.chunked(API_BATCH_SIZE)
            val allPositions = mutableListOf<LatLng>()

            for ((index, chunk) in chunks.withIndex()) {
                log.d { "Fetching batch ${index + 1}/${chunks.size} (${chunk.size} timestamps)" }

                issTrackingRepository.getPositionsAtTimestamps(chunk)
                    .onSuccess { positions ->
                        val latLngs = positions.map { LatLng(it.latitude, it.longitude) }
                        allPositions.addAll(latLngs)
                        log.d { "Batch ${index + 1}: received ${positions.size} positions" }
                    }
                    .onFailure { exception ->
                        log.e(exception) { "Failed to fetch batch ${index + 1}" }
                    }

                // Small delay between requests to avoid rate limiting
                if (index < chunks.size - 1) {
                    delay(100)
                }
            }

            if (allPositions.isNotEmpty()) {
                _orbitPath.value = allPositions
                lastOrbitPathUpdate = Clock.System.now().toEpochMilliseconds()
                log.i { "✓ Orbit path updated: ${allPositions.size} points" }
                log.d { "First: lat=${allPositions.first().latitude}, lon=${allPositions.first().longitude}" }
                log.d { "Last: lat=${allPositions.last().latitude}, lon=${allPositions.last().longitude}" }
            } else {
                log.e { "Failed to fetch any orbit positions from API" }
            }
        } catch (exception: Exception) {
            log.e(exception) { "Error fetching orbit path from API" }
        }
    }

    /**
     * Pause ISS tracking (e.g., when app goes to background)
     */
    fun pauseTracking() {
        if (_isTracking.value) {
            log.d { "Pausing ISS tracking" }
            positionPollingJob?.cancel()
            positionPollingJob = null
            _isTracking.value = false
        }
    }

    /**
     * Resume ISS tracking (e.g., when app comes to foreground)
     */
    fun resumeTracking() {
        val station = _stationDetails.value
        if (station?.id == ISS_STATION_ID && !_isTracking.value) {
            log.i { "Resuming ISS tracking for station ${station.name}" }
            startIssTracking()
        } else {
            log.d { "Not resuming tracking - station: ${station?.name}, isISS: ${station?.id == ISS_STATION_ID}, isTracking: ${_isTracking.value}" }
        }
    }

    /**
     * Stop ISS tracking when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        positionPollingJob?.cancel()
        log.d { "SpaceStationViewModel cleared, stopped ISS tracking" }
    }
}
