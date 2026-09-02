package me.calebjones.spacelaunchnow.database

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import me.calebjones.spacelaunchnow.api.trantor.models.EventList
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Clock.System

class EventLocalDataSource(
    database: SpaceLaunchDatabase,
    private val appPreferences: AppPreferences
) {
    private val queries = database.eventQueries
    private val json = Json { ignoreUnknownKeys = true }
    
    private val cacheDuration = 1.hours
    private val debugCacheDuration = 2.minutes

    private val log = logger()
    
    private suspend fun getEffectiveCacheDuration(): kotlin.time.Duration {
        return if (appPreferences.isDebugShortCacheTtlEnabled()) {
            log.w { "⚠️ DEBUG MODE: Using short cache TTL (2 minutes) instead of ${cacheDuration.inWholeHours} hours" }
            debugCacheDuration
        } else {
            cacheDuration
        }
    }
    
    suspend fun cacheEvent(event: EventList) {
        val now = System.now().toEpochMilliseconds()
        val duration = getEffectiveCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds

        queries.insertOrReplaceEvent(
            id = event.id.toLong(),
            name = event.name,
            type_name = event.type,
            // Trantor's compact list row has no description/news_url/video_url — those live
            // only on the detail row (description) or nowhere any more (news_url/video_url
            // died with LL 2.2; the app consumes info_urls/vid_urls from detail instead).
            description = null,
            location = event.location,
            news_url = null,
            video_url = null,
            feature_image = event.imageUrl,
            date = event.date?.toEpochMilliseconds(),
            json_data = json.encodeToString(event),
            cached_at = now,
            expires_at = expiresAt
        )
    }

    suspend fun cacheEvents(events: List<EventList>) {
        events.forEach { cacheEvent(it) }
    }

    suspend fun getEvent(id: Int): Event? {
        return getEventApi(id)?.toDomain()
    }

    suspend fun getEventApi(id: Int): EventList? {
        val now = System.now().toEpochMilliseconds()
        val cached = queries.getEventById(id.toLong(), now).executeAsOneOrNull()
        return cached?.let { json.decodeFromString<EventList>(it.json_data) }
    }

    suspend fun getUpcomingEvents(limit: Int): List<Event> {
        return getUpcomingEventsApi(limit).map { it.toDomain() }
    }

    suspend fun getUpcomingEventsApi(limit: Int): List<EventList> {
        val now = System.now().toEpochMilliseconds()
        val results = queries.getUpcomingEvents(now, now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    val ageMinutes = (now - cached.cached_at) / 60000
                    log.v { "Cache entry age: $ageMinutes minutes (cached at ${cached.cached_at}, expires at ${cached.expires_at})" }
                    json.decodeFromString<EventList>(cached.json_data)
                } catch (e: Exception) {
                    log.e { "Failed to parse cached event: ${cached.json_data}" }
                    null
                }
            }
        return results
    }

    suspend fun getAllEvents(limit: Int): List<Event> {
        return getAllEventsApi(limit).map { it.toDomain() }
    }

    suspend fun getAllEventsApi(limit: Int): List<EventList> {
        val now = System.now().toEpochMilliseconds()
        return queries.getAllEvents(now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    json.decodeFromString<EventList>(cached.json_data)
                } catch (e: Exception) {
                    log.e(e) { "Failed to parse cached event: ${cached.json_data}" }
                    null
                }
            }
    }
    
    suspend fun deleteExpiredEvents() {
        val now = System.now().toEpochMilliseconds()
        queries.deleteExpiredEvents(now)
    }
    
    /**
     * Gets the timestamp of when events were last cached.
     * Returns the most recent cached_at timestamp.
     */
    suspend fun getCacheTimestamp(key: String): Long? {
        return when (key) {
            "events" -> queries.getUpcomingEvents(Long.MAX_VALUE, Long.MAX_VALUE, 1).executeAsOneOrNull()?.cached_at
            else -> null
        }
    }
    
    suspend fun clearAllEvents() {
        queries.clearAllEvents()
    }
}
