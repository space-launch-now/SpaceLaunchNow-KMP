package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.EventsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedEventEndpointNormalList
import me.calebjones.spacelaunchnow.api.extensions.getEventList
import me.calebjones.spacelaunchnow.api.extensions.getUpcomingEvents
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointDetailed
import me.calebjones.spacelaunchnow.database.EventLocalDataSource

class EventsRepositoryImpl(
    private val eventsApi: EventsApi,
    private val localDataSource: EventLocalDataSource? = null
) : EventsRepository {
    
    override suspend fun getUpcomingEvents(limit: Int): Result<PaginatedEventEndpointNormalList> {
        return try {
            // Try cache first if available
            val cachedEvents = localDataSource?.getUpcomingEvents(limit)
            if (cachedEvents != null && cachedEvents.isNotEmpty()) {
                println("EventsRepository: Returning ${cachedEvents.size} cached upcoming events")
                return Result.success(PaginatedEventEndpointNormalList(
                    count = cachedEvents.size,
                    next = null,
                    previous = null,
                    results = cachedEvents
                ))
            }
            
            println("=== EventsRepository: Getting upcoming events from API ===")
            println("Limit: $limit")
            
            val response = eventsApi.getUpcomingEvents(
                limit = limit,
                ordering = "date"
            )
            
            println("Response status: ${response.status}")
            val events = response.body()
            println("Events count: ${events.results.size}")
            
            // Cache the results for future use
            localDataSource?.cacheEvents(events.results)
            println("EventsRepository: Cached ${events.results.size} upcoming events from API")
            
            Result.success(events)
        } catch (e: ResponseException) {
            println("API Error: Status ${e.response.status}, Message: ${e.message}")
            // On error, try to return stale cache if available
            val staleCached = localDataSource?.getUpcomingEvents(limit)
            if (staleCached != null && staleCached.isNotEmpty()) {
                println("EventsRepository: Returning ${staleCached.size} stale cached events due to API error")
                return Result.success(PaginatedEventEndpointNormalList(
                    count = staleCached.size,
                    next = null,
                    previous = null,
                    results = staleCached
                ))
            }
            Result.failure(e)
        } catch (e: IOException) {
            // On network error, try to return stale cache if available
            val staleCached = localDataSource?.getUpcomingEvents(limit)
            if (staleCached != null && staleCached.isNotEmpty()) {
                println("EventsRepository: Returning ${staleCached.size} stale cached events due to network error")
                return Result.success(PaginatedEventEndpointNormalList(
                    count = staleCached.size,
                    next = null,
                    previous = null,
                    results = staleCached
                ))
            }
            Result.failure(e)
        } catch (e: Exception) {
            println("Failed to get upcoming events: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun getEventsByType(typeIds: List<Int>, limit: Int): Result<PaginatedEventEndpointNormalList> {
        return try {
            println("=== EventsRepository: Getting events by type ===")
            println("Type IDs: $typeIds")
            println("Limit: $limit")
            
            val response = eventsApi.getEventList(
                limit = limit,
                typeIds = typeIds,
                ordering = "date"
            )
            
            println("Response status: ${response.status}")
            println("Events count: ${response.body()?.results?.size ?: 0}")
            
            Result.success(response.body())
        } catch (e: ResponseException) {
            println("API Error: Status ${e.response.status}, Message: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("Failed to get events by type: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun getEvents(
        limit: Int,
        upcoming: Boolean?,
        typeIds: List<Int>?
    ): Result<PaginatedEventEndpointNormalList> {
        return try {
            println("=== EventsRepository: Getting events ===")
            println("Limit: $limit, Upcoming: $upcoming, Type IDs: $typeIds")
            
            val response = eventsApi.getEventList(
                limit = limit,
                upcoming = upcoming,
                typeIds = typeIds,
                ordering = "date"
            )
            
            println("Response status: ${response.status}")
            println("Events count: ${response.body()?.results?.size ?: 0}")
            
            Result.success(response.body())
        } catch (e: ResponseException) {
            println("API Error: Status ${e.response.status}, Message: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("Failed to get events: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getEventDetails(eventId: Int): Result<EventEndpointDetailed> {
        return try {
            println("=== EventsRepository: Getting event details for $eventId ===")
            val response = eventsApi.eventsRetrieve(eventId)
            println("Response status: ${response.status}")
            Result.success(response.body())
        } catch (e: ResponseException) {
            println("API Error: Status ${e.response.status}, Message: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("Failed to get event details: ${e.message}")
            Result.failure(e)
        }
    }
}
