package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.launchlibrary.apis.EventsApi
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Extension functions for EventsApi to provide clean, named-parameter interfaces
 * instead of dealing with 70+ generated parameters
 */

/**
 * Get a list of events with clean parameter interface
 */
@OptIn(ExperimentalTime::class)
suspend fun EventsApi.getEventList(
    limit: Int? = null,
    upcoming: Boolean? = null,
    ordering: String? = null,
    typeIds: List<Int>? = null,
    agencyIds: List<Int>? = null,
    program: List<Int>? = null,
    dateGt: Instant? = null,
    dateGte: Instant? = null,
    dateLt: Instant? = null,
    dateLte: Instant? = null,
    search: String? = null
) = this.eventsList(
    agencyIds = agencyIds,
    dateGt = dateGt,
    dateGte = dateGte,
    dateLt = dateLt,
    dateLte = dateLte,
    day = null,
    id = null,
    lastUpdatedGte = null,
    lastUpdatedLte = null,
    limit = limit,
    month = null,
    offset = null,
    ordering = ordering,
    previous = null,
    program = program,
    search = search,
    slug = null,
    type = null,
    typeIds = typeIds,
    upcoming = upcoming,
    upcomingWithRecent = null,
    videoUrl = null,
    year = null
)

/**
 * Get upcoming events (convenience method)
 */
suspend fun EventsApi.getUpcomingEvents(
    limit: Int = 10,
    ordering: String = "date"
) = getEventList(
    limit = limit,
    upcoming = true,
    ordering = ordering
)

/**
 * Get events by type
 */
suspend fun EventsApi.getEventsByType(
    typeIds: List<Int>,
    limit: Int? = null,
    upcoming: Boolean? = null,
    ordering: String? = null
) = getEventList(
    limit = limit,
    upcoming = upcoming,
    ordering = ordering,
    typeIds = typeIds
)

/**
 * Get events for Starship program (program ID = 1)
 */
suspend fun EventsApi.getStarshipEvents(
    limit: Int = 20,
    upcoming: Boolean? = true
) = getEventList(
    limit = limit,
    upcoming = upcoming,
    program = listOf(1),
    ordering = "date"
)

