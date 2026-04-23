package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedEventEndpointNormalList
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult

interface EventsRepository {

    suspend fun getEventsByProgram(
        programId: Int,
        limit: Int = 20,
        upcoming: Boolean? = true,
        forceRefresh: Boolean = false
    ): Result<DataResult<PaginatedEventEndpointNormalList>>

    suspend fun getEvents(
        limit: Int = 10,
        upcoming: Boolean? = null,
        typeIds: List<Int>? = null
    ): Result<PaginatedEventEndpointNormalList>

    // -- Domain-returning methods --------------------------------------------

    suspend fun getUpcomingEventsDomain(
        limit: Int = 10,
        forceRefresh: Boolean = false
    ): Result<DataResult<PaginatedResult<Event>>>

    suspend fun getEventDetailDomain(eventId: Int): Result<Event>

    suspend fun getEventsByTypeDomain(
        typeIds: List<Int>,
        limit: Int = 10
    ): Result<PaginatedResult<Event>>

    suspend fun getEventsByLaunchIdDomain(
        launchId: String,
        limit: Int = 10
    ): Result<PaginatedResult<Event>>

    suspend fun getEventsPaginatedDomain(
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null,
        typeIds: List<Int>? = null,
        upcoming: Boolean? = true,
        forceRefresh: Boolean = false
    ): Result<DataResult<PaginatedResult<Event>>>

    /**
     * Get all available event types for filtering
     * @return List of domain EventType objects
     */
    suspend fun getEventTypesDomain(): Result<List<me.calebjones.spacelaunchnow.domain.model.EventType>>
}