package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.EventsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedEventEndpointNormalList
import me.calebjones.spacelaunchnow.api.extensions.getEventList
import me.calebjones.spacelaunchnow.api.extensions.getUpcomingEvents

class EventsRepositoryImpl(
    private val eventsApi: EventsApi
) : EventsRepository {
    
    override suspend fun getUpcomingEvents(limit: Int): Result<PaginatedEventEndpointNormalList> {
        return try {
            println("=== EventsRepository: Getting upcoming events ===")
            println("Limit: $limit")
            
            val response = eventsApi.getUpcomingEvents(
                limit = limit,
                ordering = "date"
            )
            
            println("Response status: ${response.status}")
            println("Events count: ${response.body().results.size}")
            
            Result.success(response.body())
        } catch (e: ResponseException) {
            println("API Error: Status ${e.response.status}, Message: ${e.message}")
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
}
