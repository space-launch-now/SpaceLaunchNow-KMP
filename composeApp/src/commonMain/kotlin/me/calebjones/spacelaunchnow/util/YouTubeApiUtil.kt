package me.calebjones.spacelaunchnow.util

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Utility for fetching YouTube video information via the YouTube Data API v3.
 * Uses the same API key as Google Maps since YouTube API is also a Google service.
 */
object YouTubeApiUtil {
    
    private val log = logger()
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    // NASA's official YouTube channel ID
    private const val NASA_CHANNEL_ID = "UCLA_DiR1FfKNvjuUpBHmylQ"
    
    // YouTube Data API base URL
    private const val YOUTUBE_API_BASE = "https://www.googleapis.com/youtube/v3"
    
    /**
     * Data classes for YouTube API responses
     */
    @Serializable
    data class YouTubeSearchResponse(
        val items: List<YouTubeSearchItem> = emptyList()
    )
    
    @Serializable
    data class YouTubeSearchItem(
        val id: YouTubeVideoId? = null,
        val snippet: YouTubeSnippet? = null
    )
    
    @Serializable
    data class YouTubeVideoId(
        val videoId: String? = null
    )
    
    @Serializable
    data class YouTubeSnippet(
        val title: String? = null,
        val description: String? = null,
        val thumbnails: YouTubeThumbnails? = null,
        val liveBroadcastContent: String? = null
    )
    
    @Serializable
    data class YouTubeThumbnails(
        val high: YouTubeThumbnail? = null,
        val medium: YouTubeThumbnail? = null,
        val default: YouTubeThumbnail? = null
    )
    
    @Serializable
    data class YouTubeThumbnail(
        val url: String? = null
    )
    
    /**
     * Result class for live stream lookup
     */
    data class LiveStreamInfo(
        val videoId: String,
        val title: String,
        val thumbnailUrl: String?
    )
    
    /**
     * Fetch the current NASA live stream video ID from YouTube API.
     * 
     * @param httpClient The Ktor HTTP client to use for the request
     * @param apiKey The Google API key (same as Maps API key)
     * @return LiveStreamInfo if a live stream is found, null otherwise
     */
    suspend fun getNasaLiveStreamVideoId(
        httpClient: HttpClient,
        apiKey: String
    ): LiveStreamInfo? {
        if (apiKey.isBlank()) {
            log.w { "YouTube API key is blank, cannot fetch live stream" }
            return null
        }
        
        return try {
            // Search for live broadcasts on NASA's channel
            val response = httpClient.get("$YOUTUBE_API_BASE/search") {
                parameter("key", apiKey)
                parameter("channelId", NASA_CHANNEL_ID)
                parameter("eventType", "live")
                parameter("type", "video")
                parameter("part", "id,snippet")
                parameter("maxResults", "5")
                parameter("q", "ISS")  // Search for ISS-related live streams
            }
            
            val responseText = response.bodyAsText()
            log.d { "YouTube API response: $responseText" }
            
            val searchResponse = json.decodeFromString<YouTubeSearchResponse>(responseText)
            
            // Find the first live video (preferably ISS related)
            val liveItem = searchResponse.items.firstOrNull { item ->
                item.snippet?.liveBroadcastContent == "live"
            }
            
            if (liveItem != null && liveItem.id?.videoId != null) {
                val videoId = liveItem.id.videoId
                log.i { "Found NASA live stream: $videoId - ${liveItem.snippet?.title}" }
                LiveStreamInfo(
                    videoId = videoId,
                    title = liveItem.snippet?.title ?: "NASA Live Stream",
                    thumbnailUrl = liveItem.snippet?.thumbnails?.high?.url
                        ?: liveItem.snippet?.thumbnails?.medium?.url
                )
            } else {
                log.w { "No live NASA stream found" }
                null
            }
        } catch (e: Exception) {
            log.e(e) { "Error fetching NASA live stream from YouTube API" }
            null
        }
    }
    
    /**
     * Build a YouTube watch URL from a video ID
     */
    fun buildYouTubeUrl(videoId: String): String {
        return "https://www.youtube.com/watch?v=$videoId"
    }
}
