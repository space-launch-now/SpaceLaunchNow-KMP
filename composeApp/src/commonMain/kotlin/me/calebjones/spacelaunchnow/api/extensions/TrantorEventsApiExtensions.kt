package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.trantor.apis.EventsApi
import me.calebjones.spacelaunchnow.api.trantor.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.trantor.models.EventDetail
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseEventList
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Extension functions for the Trantor EventsApi to provide clean, named-parameter interfaces
 * mirroring the retired LL EventsApiExtensions' call-site ergonomics.
 */

@OptIn(ExperimentalTime::class)
suspend fun EventsApi.getEventList(
    limit: Int? = null,
    offset: Int? = null,
    upcoming: Boolean? = null,
    dateAfter: Instant? = null,
    dateBefore: Instant? = null,
    ordering: String? = null,
    typeIds: List<Int>? = null,
    programIds: List<Int>? = null,
    launchId: String? = null,
    search: String? = null
): HttpResponse<PaginatedResponseEventList> = listEventsApiV1EventsGet(
    upcoming = upcoming,
    dateAfter = dateAfter,
    dateBefore = dateBefore,
    typeIds = typeIds?.joinToString(","),
    programIds = programIds?.joinToString(","),
    launchId = launchId,
    search = search,
    ordering = ordering,
    limit = limit,
    offset = offset
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
 * Get events related to a specific launch by launch ID (UUID)
 */
suspend fun EventsApi.getEventsByLaunchId(
    launchId: String,
    limit: Int? = null,
    ordering: String? = "-date"
) = getEventList(
    launchId = launchId,
    limit = limit,
    ordering = ordering
)

/**
 * Get event detail
 */
suspend fun EventsApi.getEventDetail(eventId: Int): HttpResponse<EventDetail> =
    getEventApiV1EventsEventIdGet(eventId)
