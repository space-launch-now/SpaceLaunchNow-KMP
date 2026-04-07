package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointDetailed
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.ui.state.VideoPlayerState
import me.calebjones.spacelaunchnow.util.logging.logger

class EventViewModel(
    private val repository: EventsRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {
    private val log = logger()

    private val _eventDetails = MutableStateFlow<EventEndpointDetailed?>(null)
    val eventDetails: StateFlow<EventEndpointDetailed?> = _eventDetails

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _videoPlayerState = MutableStateFlow(VideoPlayerState())
    val videoPlayerState: StateFlow<VideoPlayerState> = _videoPlayerState

    // ========== Analytics ==========

    fun trackLinkOpened(url: String, eventId: Int) {
        analyticsManager.track(
            AnalyticsEvent.ThirdPartyReferral(
                provider = "event_detail",
                url = url,
                contentType = "event_link",
                contentId = eventId.toString()
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

    fun fetchEventDetails(id: Int) {
        viewModelScope.launch {
            log.d { "Fetching event details for id: $id" }
            _error.value = null
            _isLoading.value = true

            val result = repository.getEventDetails(id)
            result.onSuccess { event ->
                log.i { "Successfully loaded event details: ${event.name}" }
                _eventDetails.value = event
                updateVideoPlayerState(event)
                analyticsManager.track(AnalyticsEvent.EventViewed(event.id))
            }.onFailure { exception ->
                log.e(exception) { "Failed to fetch event details for id: $id" }
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    private fun updateVideoPlayerState(event: EventEndpointDetailed) {
        val videos = event.vidUrls
        _videoPlayerState.value = _videoPlayerState.value.copy(
            availableVideos = videos,
            selectedVideoIndex = if (videos.isNotEmpty())
                _videoPlayerState.value.selectedVideoIndex.coerceAtMost(videos.size - 1)
            else 0
        )
    }

    fun selectVideo(index: Int) {
        val currentState = _videoPlayerState.value
        if (index >= 0 && index < currentState.availableVideos.size) {
            _videoPlayerState.value = currentState.copy(
                selectedVideoIndex = index,
                isPlayerVisible = false
            )
        }
    }

    fun setPlayerVisible(visible: Boolean) {
        _videoPlayerState.value = _videoPlayerState.value.copy(
            isPlayerVisible = visible
        )
    }
}
