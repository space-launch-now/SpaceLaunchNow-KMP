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

    /**
     * Get events related to a specific launch by launch ID (UUID)
     */
    suspend fun getEventsByLaunchId(
        launchId: String,
        limit: Int = 10
    ): Result<PaginatedEventEndpointNormalList>

    /**
     * Get events with full pagination and filtering support for News & Events screen
     * @param limit Number of events per page
     * @param offset Pagination offset
     * @param search Optional search query
     * @param typeIds Optional filter by event type IDs
     * @param upcoming True for upcoming, False for past, null for all
     * @param forceRefresh Force network fetch bypassing cache
     */
    suspend fun getEventsPaginated(
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null,
        typeIds: List<Int>? = null,
        upcoming: Boolean? = true,
        forceRefresh: Boolean = false
    ): Result<DataResult<PaginatedEventEndpointNormalList>>
}
