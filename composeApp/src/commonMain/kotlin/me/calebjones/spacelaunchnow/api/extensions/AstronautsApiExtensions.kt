package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.launchlibrary.apis.AstronautsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAstronautEndpointNormalList

/**
 * Extension functions for AstronautsApi to provide cleaner, named-parameter interfaces
 * instead of the verbose generated API methods with 70+ parameters.
 *
 * These extensions wrap the generated API methods and map commonly-used parameters,
 * making the API easier to use and more maintainable.
 */

/**
 * Get a paginated list of astronauts with commonly used filters.
 *
 * @param limit Maximum number of results to return per page (default: 20)
 * @param offset Number of results to skip (for pagination)
 * @param search Search query to filter astronauts by name
 * @param statusIds List of astronaut status IDs to filter by (e.g., active, retired)
 * @param agencyIds List of agency IDs to filter astronauts by their associated agencies
 * @param ordering Field to sort by (e.g., "name", "-name" for descending)
 *
 * @return HttpResponse containing PaginatedAstronautEndpointNormalList with astronaut data
 *
 * Example usage:
 * ```kotlin
 * val response = astronautsApi.getAstronautList(
 *     limit = 20,
 *     statusIds = listOf(1), // Active astronauts
 *     ordering = "name"
 * )
 * val astronauts = response.body()
 * ```
 */
suspend fun AstronautsApi.getAstronautList(
    limit: Int? = null,
    offset: Int? = null,
    search: String? = null,
    statusIds: List<Double>? = null,
    agencyIds: List<Double>? = null,
    ordering: String? = null
): HttpResponse<PaginatedAstronautEndpointNormalList> = astronautsList(
    age = null,
    ageGt = null,
    ageGte = null,
    ageLt = null,
    ageLte = null,
    agencyIds = agencyIds,
    dateOfBirth = null,
    dateOfBirthDay = null,
    dateOfBirthGt = null,
    dateOfBirthGte = null,
    dateOfBirthLt = null,
    dateOfBirthLte = null,
    dateOfBirthMonth = null,
    dateOfBirthYear = null,
    dateOfDeath = null,
    dateOfDeathDay = null,
    dateOfDeathGt = null,
    dateOfDeathGte = null,
    dateOfDeathLt = null,
    dateOfDeathLte = null,
    dateOfDeathMonth = null,
    dateOfDeathYear = null,
    firstFlight = null,
    firstFlightGt = null,
    firstFlightGte = null,
    firstFlightLt = null,
    firstFlightLte = null,
    flightsCount = null,
    flightsCountGt = null,
    flightsCountGte = null,
    flightsCountLt = null,
    flightsCountLte = null,
    hasFlown = null,
    inSpace = null,
    isHuman = null,
    landingsCount = null,
    landingsCountGt = null,
    landingsCountGte = null,
    landingsCountLt = null,
    landingsCountLte = null,
    lastFlight = null,
    lastFlightGt = null,
    lastFlightGte = null,
    lastFlightLt = null,
    lastFlightLte = null,
    limit = limit,
    nationality = null,
    offset = offset,
    ordering = ordering,
    search = search,
    statusIds = statusIds,
    typeId = null
)

/**
 * Get detailed information about a specific astronaut by their ID.
 *
 * @param id The unique identifier of the astronaut
 *
 * @return HttpResponse containing AstronautEndpointDetailed with comprehensive astronaut data
 *         including biography, flight history, career statistics, and more
 *
 * Example usage:
 * ```kotlin
 * val response = astronautsApi.getAstronautDetail(id = 123)
 * val astronaut = response.body()
 * println("${astronaut.name} has ${astronaut.flightsCount} flights")
 * ```
 */
suspend fun AstronautsApi.getAstronautDetail(
    id: Int
): HttpResponse<AstronautEndpointDetailed> = astronautsRetrieve(id = id)
