package me.calebjones.spacelaunchnow.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.remoteconfig.FirebaseRemoteConfig
import dev.gitlive.firebase.remoteconfig.remoteConfig
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.data.model.PinnedContent
import me.calebjones.spacelaunchnow.data.model.RoadmapData
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 * Implementation of RemoteConfigRepository using GitLive Firebase SDK
 * 
 * Provides Firebase Remote Config access with:
 * - 1-hour default fetch interval (Firebase best practice)
 * - Force refresh option for pull-to-refresh
 * - JSON parsing with error handling
 * - Default values for offline/initial state
 * - Graceful fallback when Firebase is unavailable (Desktop)
 */
class RemoteConfigRepositoryImpl : RemoteConfigRepository {
    
    private val log by lazy { logger() }
    
    // Lazy initialization to catch Firebase setup errors gracefully
    private val remoteConfig: FirebaseRemoteConfig? by lazy {
        try {
            Firebase.remoteConfig
        } catch (e: Exception) {
            log.w(e) { "Firebase Remote Config unavailable - using fallback data" }
            null
        }
    }
    
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    companion object {
        private const val ROADMAP_DATA_KEY = "roadmap_data"
        private const val PINNED_CONTENT_KEY = "pinned_content"
        private val DEFAULT_FETCH_INTERVAL: Duration = 1.hours
        private val FORCE_REFRESH_INTERVAL: Duration = 0.seconds
        
        // Default JSON - empty state for initial load
        private val DEFAULT_ROADMAP_JSON = """
        {
            "items": [],
            "lastUpdated": null,
            "message": "Loading roadmap data..."
        }
        """.trimIndent()
        
        // Default JSON - no pinned content
        private val DEFAULT_PINNED_CONTENT_JSON = ""
    }
    
    override suspend fun fetchAndActivate(forceRefresh: Boolean): Result<Unit> {
        val config = remoteConfig
        if (config == null) {
            log.d { "Remote config unavailable - skipping fetch" }
            return Result.success(Unit) // No-op when Firebase unavailable
        }
        
        return try {
            val interval = if (forceRefresh) FORCE_REFRESH_INTERVAL else DEFAULT_FETCH_INTERVAL
            config.settings {
                minimumFetchInterval = interval
            }
            config.fetchAndActivate()
            log.d { "Remote config fetch and activate succeeded" }
            Result.success(Unit)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch remote config" }
            Result.failure(e)
        }
    }
    
    override suspend fun getRoadmapData(): Result<RoadmapData> {
        val config = remoteConfig
        if (config == null) {
            log.d { "Remote config unavailable - returning default data" }
            return Result.success(RoadmapData()) // Return default when unavailable
        }
        
        return try {
            val jsonString = config.getValue(ROADMAP_DATA_KEY).asString()
            
            if (jsonString.isBlank()) {
                return Result.success(RoadmapData())
            }
            
            val roadmapData = json.decodeFromString<RoadmapData>(jsonString)
            Result.success(roadmapData)
        } catch (e: Exception) {
            log.e(e) { "Failed to parse roadmap data" }
            Result.failure(e)
        }
    }
    
    override suspend fun getPinnedContent(): Result<PinnedContent?> {
        val config = remoteConfig
        if (config == null) {
            log.d { "Remote config unavailable - no pinned content" }
            return Result.success(null)
        }
        
        return try {
            val jsonString = config.getValue(PINNED_CONTENT_KEY).asString()
            
            if (jsonString.isBlank()) {
                log.d { "No pinned content configured" }
                return Result.success(null)
            }
            
            val pinnedContent = json.decodeFromString<PinnedContent>(jsonString)
            log.d { "Pinned content loaded: type=${pinnedContent.type}, id=${pinnedContent.id}, enabled=${pinnedContent.enabled}" }
            Result.success(pinnedContent)
        } catch (e: Exception) {
            log.e(e) { "Failed to parse pinned content" }
            Result.failure(e)
        }
    }
    
    override suspend fun setDefaults() {
        val config = remoteConfig ?: return // No-op when Firebase unavailable
        
        try {
            config.setDefaults(
                ROADMAP_DATA_KEY to DEFAULT_ROADMAP_JSON,
                PINNED_CONTENT_KEY to DEFAULT_PINNED_CONTENT_JSON
            )
        } catch (e: Exception) {
            // Log warning but don't fail - defaults are optional
            log.w(e) { "Failed to set remote config defaults" }
        }
    }
}
