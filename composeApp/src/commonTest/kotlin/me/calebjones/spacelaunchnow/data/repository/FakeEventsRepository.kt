package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedEventEndpointNormalList
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.domain.model.EventType
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult

class FakeEventsRepository : EventsRepository {

    // -- Configurable results for domain methods --------------------------

    var upcomingEventsDomainResult: Result<DataResult<PaginatedResult<Event>>> =
        Result.success(DataResult(PaginatedResult(count = 0, next = null, previous = null), DataSource.NETWORK))

    var eventDetailDomainResult: Result<Event>? = null

    var eventsByTypeDomainResult: Result<PaginatedResult<Event>> =
        Result.success(PaginatedResult(count = 0, next = null, previous = null))

    var eventsByLaunchIdDomainResult: Result<PaginatedResult<Event>> =
        Result.success(PaginatedResult(count = 0, next = null, previous = null))

    var eventsPaginatedDomainResult: Result<DataResult<PaginatedResult<Event>>> =
        Result.success(DataResult(PaginatedResult(count = 0, next = null, previous = null), DataSource.NETWORK))

    var eventTypesDomainResult: Result<List<EventType>> = Result.success(emptyList())

    var shouldFail = false
    private val failureException = Exception("FakeEventsRepository configured to fail")

    // -- Call tracking ----------------------------------------------------

    var getUpcomingEventsDomainCalled = false
    var getEventDetailDomainCalled = false
    var getEventsByTypeDomainCalled = false
    var getEventsByLaunchIdDomainCalled = false
    var getEventsPaginatedDomainCalled = false
    var getEventTypesDomainCalled = false

    var lastEventDetailId: Int? = null
    var lastEventsByLaunchIdLaunchId: String? = null

    // -- Domain-returning methods -----------------------------------------

    override suspend fun getUpcomingEventsDomain(
        limit: Int, forceRefresh: Boolean
    ): Result<DataResult<PaginatedResult<Event>>> {
        getUpcomingEventsDomainCalled = true
        if (shouldFail) return Result.failure(failureException)
        return upcomingEventsDomainResult
    }

    override suspend fun getEventDetailDomain(eventId: Int): Result<Event> {
        getEventDetailDomainCalled = true
        lastEventDetailId = eventId
        if (shouldFail) return Result.failure(failureException)
        return eventDetailDomainResult ?: Result.failure(Exception("No eventDetailDomainResult configured"))
    }

    override suspend fun getEventsByTypeDomain(
        typeIds: List<Int>, limit: Int
    ): Result<PaginatedResult<Event>> {
        getEventsByTypeDomainCalled = true
        if (shouldFail) return Result.failure(failureException)
        return eventsByTypeDomainResult
    }

    override suspend fun getEventsByLaunchIdDomain(
        launchId: String, limit: Int
    ): Result<PaginatedResult<Event>> {
        getEventsByLaunchIdDomainCalled = true
        lastEventsByLaunchIdLaunchId = launchId
        if (shouldFail) return Result.failure(failureException)
        return eventsByLaunchIdDomainResult
    }

    override suspend fun getEventsPaginatedDomain(
        limit: Int, offset: Int, search: String?,
        typeIds: List<Int>?, upcoming: Boolean?, forceRefresh: Boolean
    ): Result<DataResult<PaginatedResult<Event>>> {
        getEventsPaginatedDomainCalled = true
        if (shouldFail) return Result.failure(failureException)
        return eventsPaginatedDomainResult
    }

    override suspend fun getEventTypesDomain(): Result<List<EventType>> {
        getEventTypesDomainCalled = true
        if (shouldFail) return Result.failure(failureException)
        return eventTypesDomainResult
    }

    // -- Non-deprecated legacy passthroughs (API types kept on interface) --

    override suspend fun getEventsByProgram(programId: Int, limit: Int, upcoming: Boolean?, forceRefresh: Boolean): Result<DataResult<PaginatedEventEndpointNormalList>> =
        Result.failure(NotImplementedError("Not wired in fake"))

    override suspend fun getEvents(limit: Int, upcoming: Boolean?, typeIds: List<Int>?): Result<PaginatedEventEndpointNormalList> =
        Result.failure(NotImplementedError("Not wired in fake"))
}