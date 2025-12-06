package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedEventEndpointNormalList
import me.calebjones.spacelaunchnow.data.model.DataResult

interface EventsRepository {

    /**
     * Get a list of upcoming events
     */
    suspend fun getUpcomingEvents(
        limit: Int = 10,
        forceRefresh: Boolean = false
    ): Result<DataResult<PaginatedEventEndpointNormalList>>

    /**
     * Get events by type
     */
    suspend fun getEventsByType(
        typeIds: List<Int>,
        limit: Int = 10
    ): Result<PaginatedEventEndpointNormalList>

    /**
     * Get events by program
     */
    suspend fun getEventsByProgram(
        programId: Int,
        limit: Int = 20,
        upcoming: Boolean? = true,
        forceRefresh: Boolean = false
    ): Result<DataResult<PaginatedEventEndpointNormalList>>

    /**
     * Get all events with pagination
     */
    suspend fun getEvents(
        limit: Int = 10,
        upcoming: Boolean? = null,
        typeIds: List<Int>? = null
    ): Result<PaginatedEventEndpointNormalList>

    /**
     * Get detailed information for a single event by ID
     */
    suspend fun getEventDetails(eventId: Int): Result<EventEndpointDetailed>
}
