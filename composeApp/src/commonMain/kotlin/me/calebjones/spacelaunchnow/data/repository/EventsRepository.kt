package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedEventEndpointNormalList

interface EventsRepository {
    
    /**
     * Get a list of upcoming events
     */
    suspend fun getUpcomingEvents(limit: Int = 10): Result<PaginatedEventEndpointNormalList>
    
    /**
     * Get events by type
     */
    suspend fun getEventsByType(typeIds: List<Int>, limit: Int = 10): Result<PaginatedEventEndpointNormalList>
    
    /**
     * Get all events with pagination
     */
    suspend fun getEvents(
        limit: Int = 10,
        upcoming: Boolean? = null,
        typeIds: List<Int>? = null
    ): Result<PaginatedEventEndpointNormalList>
}
