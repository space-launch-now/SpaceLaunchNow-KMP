package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherConfigDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherConfigNormalList

/**
 * Extension functions for LauncherConfigurationsApi to provide cleaner, named-parameter interfaces
 * instead of the verbose generated API methods
 */

/**
 * Get launcher configurations (rocket types) with program filter and pagination.
 *
 * LauncherConfig represents a rocket type/variant (e.g., "Super Heavy", "Falcon 9 Block 5").
 * This is the "category" level - individual launchers (boosters) belong to a configuration.
 *
 * @param programIds Filter by program IDs (e.g., [1] for Starship program)
 * @param limit Number of results per page (max 100)
 * @param offset Pagination offset
 * @param search Search term for name, manufacturer, etc.
 * @param active Filter by active status
 * @param reusable Filter by reusability
 * @param ordering Field to order by (e.g., "name", "-total_launch_count")
 */
suspend fun LauncherConfigurationsApi.getConfigurationsByProgram(
    programIds: List<Int>? = null,
    limit: Int? = null,
    offset: Int? = null,
    search: String? = null,
    active: Boolean? = null,
    reusable: Boolean? = null,
    ordering: String? = null,
    isPlaceholder: Boolean? = null
): HttpResponse<PaginatedLauncherConfigDetailedList> = launcherConfigurationsDetailedList(
    program = programIds,
    limit = limit,
    offset = offset,
    search = search,
    active = active,
    reusable = reusable,
    ordering = ordering,
    // All unused parameters set to null
    attemptedLandings = null,
    attemptedLandingsGt = null,
    attemptedLandingsGte = null,
    attemptedLandingsLt = null,
    attemptedLandingsLte = null,
    consecutiveSuccessfulLandings = null,
    consecutiveSuccessfulLandingsGt = null,
    consecutiveSuccessfulLandingsGte = null,
    consecutiveSuccessfulLandingsLt = null,
    consecutiveSuccessfulLandingsLte = null,
    consecutiveSuccessfulLaunches = null,
    consecutiveSuccessfulLaunchesGt = null,
    consecutiveSuccessfulLaunchesGte = null,
    consecutiveSuccessfulLaunchesLt = null,
    consecutiveSuccessfulLaunchesLte = null,
    failedLandings = null,
    failedLandingsGt = null,
    failedLandingsGte = null,
    failedLandingsLt = null,
    failedLandingsLte = null,
    failedLaunches = null,
    failedLaunchesGt = null,
    failedLaunchesGte = null,
    failedLaunchesLt = null,
    failedLaunchesLte = null,
    families = null,
    familiesContains = null,
    fullName = null,
    fullNameContains = null,
    isPlaceholder = isPlaceholder,
    maidenFlight = null,
    maidenFlightDay = null,
    maidenFlightGt = null,
    maidenFlightGte = null,
    maidenFlightLt = null,
    maidenFlightLte = null,
    maidenFlightMonth = null,
    maidenFlightYear = null,
    manufacturerName = null,
    manufacturerNameContains = null,
    name = null,
    nameContains = null,
    pendingLaunches = null,
    pendingLaunchesGt = null,
    pendingLaunchesGte = null,
    pendingLaunchesLt = null,
    pendingLaunchesLte = null,
    programContains = null,
    successfulLandings = null,
    successfulLandingsGt = null,
    successfulLandingsGte = null,
    successfulLandingsLt = null,
    successfulLandingsLte = null,
    successfulLaunches = null,
    successfulLaunchesGt = null,
    successfulLaunchesGte = null,
    successfulLaunchesLt = null,
    successfulLaunchesLte = null,
    totalLaunchCount = null,
    totalLaunchCountGt = null,
    totalLaunchCountGte = null,
    totalLaunchCountLt = null,
    totalLaunchCountLte = null
)

/**
 * Get launcher configuration list with commonly used parameters
 */
suspend fun LauncherConfigurationsApi.getRocketList(
    limit: Int? = null,
    offset: Int? = null,
    search: String? = null,
    ordering: String? = null,
    active: Boolean? = null,
    reusable: Boolean? = null,
    program: List<Int>? = null,
    families: List<Int>? = null,
    manufacturerName: String? = null,
    manufacturerNameContains: String? = null
): HttpResponse<PaginatedLauncherConfigNormalList> = launcherConfigurationsList(
    active = active,
    attemptedLandings = null,
    attemptedLandingsGt = null,
    attemptedLandingsGte = null,
    attemptedLandingsLt = null,
    attemptedLandingsLte = null,
    consecutiveSuccessfulLandings = null,
    consecutiveSuccessfulLandingsGt = null,
    consecutiveSuccessfulLandingsGte = null,
    consecutiveSuccessfulLandingsLt = null,
    consecutiveSuccessfulLandingsLte = null,
    consecutiveSuccessfulLaunches = null,
    consecutiveSuccessfulLaunchesGt = null,
    consecutiveSuccessfulLaunchesGte = null,
    consecutiveSuccessfulLaunchesLt = null,
    consecutiveSuccessfulLaunchesLte = null,
    failedLandings = null,
    failedLandingsGt = null,
    failedLandingsGte = null,
    failedLandingsLt = null,
    failedLandingsLte = null,
    failedLaunches = null,
    failedLaunchesGt = null,
    failedLaunchesGte = null,
    failedLaunchesLt = null,
    failedLaunchesLte = null,
    families = families,
    familiesContains = null,
    fullName = null,
    fullNameContains = null,
    isPlaceholder = null,
    limit = limit,
    maidenFlight = null,
    maidenFlightDay = null,
    maidenFlightGt = null,
    maidenFlightGte = null,
    maidenFlightLt = null,
    maidenFlightLte = null,
    maidenFlightMonth = null,
    maidenFlightYear = null,
    manufacturerName = manufacturerName,
    manufacturerNameContains = manufacturerNameContains,
    name = null,
    nameContains = null,
    offset = offset,
    ordering = ordering,
    pendingLaunches = null,
    pendingLaunchesGt = null,
    pendingLaunchesGte = null,
    pendingLaunchesLt = null,
    pendingLaunchesLte = null,
    program = program,
    programContains = null,
    reusable = reusable,
    search = search,
    successfulLandings = null,
    successfulLandingsGt = null,
    successfulLandingsGte = null,
    successfulLandingsLt = null,
    successfulLandingsLte = null,
    successfulLaunches = null,
    successfulLaunchesGt = null,
    successfulLaunchesGte = null,
    successfulLaunchesLt = null,
    successfulLaunchesLte = null,
    totalLaunchCount = null,
    totalLaunchCountGt = null,
    totalLaunchCountGte = null,
    totalLaunchCountLt = null,
    totalLaunchCountLte = null
)

/**
 * Get detailed rocket configuration by ID
 */
suspend fun LauncherConfigurationsApi.getRocketDetails(
    id: Int
): HttpResponse<LauncherConfigDetailed> = launcherConfigurationsRetrieve(id = id)

/**
 * Get launcher configurations (rockets) with comprehensive filtering.
 * 
 * This extension function wraps the verbose generated API method (70+ parameters) 
 * with a clean interface using the RocketFilters domain model.
 * 
 * @param filters RocketFilters domain model containing all query parameters
 * @return HttpResponse containing PaginatedLauncherConfigNormalList
 * @throws IllegalArgumentException if filters validation fails
 */
suspend fun LauncherConfigurationsApi.getRocketListFiltered(
    filters: me.calebjones.spacelaunchnow.data.model.RocketFilters
): HttpResponse<PaginatedLauncherConfigNormalList> {
    // Validate filters before making API call
    filters.validate()
    
    // Note: The Launch Library API doesn't support filtering by manufacturer IDs directly
    // Manufacturer filtering would need to be done client-side after fetching results
    
    return launcherConfigurationsList(
        // Core filter parameters
        search = filters.searchQuery?.takeIf { it.isNotEmpty() },
        active = filters.activeOnly,
        ordering = filters.sortField.apiParam,
        
        // Pagination
        limit = filters.limit,
        offset = filters.offset,
        
        // All other 70+ parameters set to null
        reusable = null,
        attemptedLandings = null,
        attemptedLandingsGt = null,
        attemptedLandingsGte = null,
        attemptedLandingsLt = null,
        attemptedLandingsLte = null,
        consecutiveSuccessfulLandings = null,
        consecutiveSuccessfulLandingsGt = null,
        consecutiveSuccessfulLandingsGte = null,
        consecutiveSuccessfulLandingsLt = null,
        consecutiveSuccessfulLandingsLte = null,
        consecutiveSuccessfulLaunches = null,
        consecutiveSuccessfulLaunchesGt = null,
        consecutiveSuccessfulLaunchesGte = null,
        consecutiveSuccessfulLaunchesLt = null,
        consecutiveSuccessfulLaunchesLte = null,
        failedLandings = null,
        failedLandingsGt = null,
        failedLandingsGte = null,
        failedLandingsLt = null,
        failedLandingsLte = null,
        failedLaunches = null,
        failedLaunchesGt = null,
        failedLaunchesGte = null,
        failedLaunchesLt = null,
        failedLaunchesLte = null,
        families = null,
        familiesContains = null,
        fullName = null,
        fullNameContains = null,
        isPlaceholder = null,
        maidenFlight = null,
        maidenFlightDay = null,
        maidenFlightGt = null,
        maidenFlightGte = null,
        maidenFlightLt = null,
        maidenFlightLte = null,
        maidenFlightMonth = null,
        maidenFlightYear = null,
        manufacturerName = null,
        manufacturerNameContains = null,
        name = null,
        nameContains = null,
        pendingLaunches = null,
        pendingLaunchesGt = null,
        pendingLaunchesGte = null,
        pendingLaunchesLt = null,
        pendingLaunchesLte = null,
        program = null,
        programContains = null,
        successfulLandings = null,
        successfulLandingsGt = null,
        successfulLandingsGte = null,
        successfulLandingsLt = null,
        successfulLandingsLte = null,
        successfulLaunches = null,
        successfulLaunchesGt = null,
        successfulLaunchesGte = null,
        successfulLaunchesLt = null,
        successfulLaunchesLte = null,
        totalLaunchCount = null,
        totalLaunchCountGt = null,
        totalLaunchCountGte = null,
        totalLaunchCountLt = null,
        totalLaunchCountLte = null
    )
}
