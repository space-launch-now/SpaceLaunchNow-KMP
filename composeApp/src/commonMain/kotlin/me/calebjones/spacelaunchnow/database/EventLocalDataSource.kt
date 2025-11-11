package me.calebjones.spacelaunchnow.database

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointNormal
import kotlin.time.Duration.Companion.hours
import kotlin.time.Clock.System

class EventLocalDataSource(
    database: SpaceLaunchDatabase
) {
    private val queries = database.eventQueries
    private val json = Json { ignoreUnknownKeys = true }
    
    private val cacheDuration = 6.hours
    
    suspend fun cacheEvent(event: EventEndpointNormal) {
        val now = System.now().toEpochMilliseconds()
        val expiresAt = now + cacheDuration.inWholeMilliseconds
        
        queries.insertOrReplaceEvent(
            id = event.id.toLong(),
            name = event.name,
            type_name = event.type?.name,
            description = event.description,
            location = event.location,
            news_url = event.infoUrls.firstOrNull()?.url,
            video_url = event.vidUrls.firstOrNull()?.url,
            feature_image = event.image?.imageUrl,
            date = event.date?.toEpochMilliseconds(),
            json_data = json.encodeToString(event),
            cached_at = now,
            expires_at = expiresAt
        )
    }
    
    suspend fun cacheEvents(events: List<EventEndpointNormal>) {
        events.forEach { cacheEvent(it) }
    }
    
    suspend fun getEvent(id: Int): EventEndpointNormal? {
        val now = System.now().toEpochMilliseconds()
        val cached = queries.getEventById(id.toLong(), now).executeAsOneOrNull()
        return cached?.let { json.decodeFromString<EventEndpointNormal>(it.json_data) }
    }
    
    suspend fun getUpcomingEvents(limit: Int): List<EventEndpointNormal> {
        val now = System.now().toEpochMilliseconds()
        return queries.getUpcomingEvents(now, now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    json.decodeFromString<EventEndpointNormal>(cached.json_data)
                } catch (e: Exception) {
                    null
                }
            }
    }
    
    suspend fun getAllEvents(limit: Int): List<EventEndpointNormal> {
        val now = System.now().toEpochMilliseconds()
        return queries.getAllEvents(now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    json.decodeFromString<EventEndpointNormal>(cached.json_data)
                } catch (e: Exception) {
                    null
                }
            }
    }
    
    suspend fun deleteExpiredEvents() {
        val now = System.now().toEpochMilliseconds()
        queries.deleteExpiredEvents(now)
    }
    
    suspend fun clearAllEvents() {
        queries.clearAllEvents()
    }
}
