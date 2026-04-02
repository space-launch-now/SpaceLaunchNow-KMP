package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchNormalList
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


/**
 * Extension functions for LaunchesApi to provide cleaner, named-parameter interfaces
 * instead of the verbose generated API methods
 */

/**
 * Get launch mini list with commonly used parameters
 *
 * Note: API only supports single rocketConfigurationId, not multiple.
 * For filtering by multiple rockets, make separate API calls.
 */
@OptIn(ExperimentalTime::class)
suspend fun LaunchesApi.getLaunchMiniList(
    limit: Int? = null,
    offset: Int? = null,
    upcoming: Boolean? = null,
    previous: Boolean? = null,
    ordering: String? = null,
    status: Int? = null,
    statusIds: List<Int>? = null,
    search: String? = null,
    lspId: List<Int>? = null, // Agency/LSP IDs (supports multiple)
    relatedLspId: List<Int>? = null,
    rocketConfigurationId: Int? = null, // Rocket config ID (single only, API limitation)
    isCrewed: Boolean? = null,
    includeSuborbital: Boolean? = null,
    netGt: Instant? = null,
    netLt: Instant? = null,
    netDay: List<Double>? = null,
    netMonth: List<Double>? = null,
    pad: Int? = null,
    locationIds: List<Int>? = null, // Location IDs (supports multiple)
    program: List<Int>? = null, // Program IDs (supports multiple)
    orbitIds: List<Int>? = null, // Orbit IDs (supports multiple)
    missionTypeIds: List<Int>? = null, // Mission Type IDs (supports multiple)
    launcherConfigFamilyIds: List<Int>? = null // Launcher Config Family IDs (supports multiple)
): HttpResponse<PaginatedLaunchBasicList> = launchesMiniList(
    agencyLaunchAttemptCount = null,
    agencyLaunchAttemptCountGt = null,
    agencyLaunchAttemptCountGte = null,
    agencyLaunchAttemptCountLt = null,
    agencyLaunchAttemptCountLte = null,
    agencyLaunchAttemptCountYear = null,
    agencyLaunchAttemptCountYearGt = null,
    agencyLaunchAttemptCountYearGte = null,
    agencyLaunchAttemptCountYearLt = null,
    agencyLaunchAttemptCountYearLte = null,
    id = null,
    includeSuborbital = includeSuborbital,
    isCrewed = isCrewed,
    lastUpdatedGte = null,
    lastUpdatedLte = null,
    launchDesignator = null,
    launcherConfigId = null,
    launcherConfigFamilyIds = launcherConfigFamilyIds,
    limit = limit,
    locationIds = locationIds,
    locationLaunchAttemptCount = null,
    locationLaunchAttemptCountGt = null,
    locationLaunchAttemptCountGte = null,
    locationLaunchAttemptCountLt = null,
    locationLaunchAttemptCountLte = null,
    locationLaunchAttemptCountYear = null,
    locationLaunchAttemptCountYearGt = null,
    locationLaunchAttemptCountYearGte = null,
    locationLaunchAttemptCountYearLt = null,
    locationLaunchAttemptCountYearLte = null,
    lspId = lspId,
    lspName = null,
    missionAgencyIds = null,
    missionOrbitCelestialBodyId = null,
    missionOrbitName = null,
    missionOrbitNameIcontains = null,
    missionTypeIds = missionTypeIds,
    name = null,
    netDay = netDay,
    netGt = netGt,
    netGte = null,
    netLt = netLt,
    netLte = null,
    netMonth = netMonth,
    netYear = null,
    offset = offset,
    orbitIds = orbitIds,
    orbitalLaunchAttemptCount = null,
    orbitalLaunchAttemptCountGt = null,
    orbitalLaunchAttemptCountGte = null,
    orbitalLaunchAttemptCountLt = null,
    orbitalLaunchAttemptCountLte = null,
    orbitalLaunchAttemptCountYear = null,
    orbitalLaunchAttemptCountYearGt = null,
    orbitalLaunchAttemptCountYearGte = null,
    orbitalLaunchAttemptCountYearLt = null,
    orbitalLaunchAttemptCountYearLte = null,
    ordering = ordering,
    pad = pad,
    padLocation = null,
    padLocationCelestialBodyId = null,
    padLaunchAttemptCount = null,
    padLaunchAttemptCountGt = null,
    padLaunchAttemptCountGte = null,
    padLaunchAttemptCountLt = null,
    padLaunchAttemptCountLte = null,
    padLaunchAttemptCountYear = null,
    padLaunchAttemptCountYearGt = null,
    padLaunchAttemptCountYearGte = null,
    padLaunchAttemptCountYearLt = null,
    padLaunchAttemptCountYearLte = null,
    previous = previous,
    program = program,
    relatedLspId = relatedLspId,
    relatedLspName = null,
    rocketConfigurationFullName = null,
    rocketConfigurationFullNameIcontains = null,
    rocketConfigurationId = rocketConfigurationId,
    rocketConfigurationManufacturerName = null,
    rocketConfigurationManufacturerNameIcontains = null,
    rocketConfigurationName = null,
    rocketSpacecraftflightSpacecraftId = null,
    rocketSpacecraftflightSpacecraftName = null,
    rocketSpacecraftflightSpacecraftNameIcontains = null,
    search = search,
    serialNumber = null,
    slug = null,
    spacecraftConfigIds = null,
    status = status,
    statusIds = statusIds,
    upcoming = upcoming,
    upcomingWithRecent = null,
    videoUrl = null,
    windowEndGt = null,
    windowEndGte = null,
    windowEndLt = null,
    windowEndLte = null,
    windowStartGt = null,
    windowStartGte = null,
    windowStartLt = null,
    windowStartLte = null
)

/**
 * Get launch normal list with commonly used parameters
 *
 * Note: API only supports single rocketConfigurationId, not multiple.
 * For filtering by multiple rockets, make separate API calls.
 */
@OptIn(ExperimentalTime::class)
suspend fun LaunchesApi.getLaunchList(
    id: String? = null, // UUID of specific launch to retrieve
    limit: Int? = null,
    offset: Int? = null,
    upcoming: Boolean? = null,
    previous: Boolean? = null,
    ordering: String? = null,
    status: Int? = null,
    statusIds: List<Int>? = null,
    search: String? = null,
    lspId: List<Int>? = null, // Agency/LSP IDs (supports multiple)
    relatedLspId: List<Int>? = null,
    rocketConfigurationId: Int? = null, // Rocket config ID (single only, API limitation)
    isCrewed: Boolean? = null,
    includeSuborbital: Boolean? = null,
    netGt: Instant? = null,
    netLt: Instant? = null,
    netDay: List<Double>? = null,
    netMonth: List<Double>? = null,
    pad: Int? = null,
    locationIds: List<Int>? = null, // Location IDs (supports multiple)
    program: List<Int>? = null, // Program IDs (supports multiple)
    upcomingWithRecent: Boolean? = null,
    orbitIds: List<Int>? = null, // Orbit IDs (supports multiple)
    missionTypeIds: List<Int>? = null, // Mission Type IDs (supports multiple)
    launcherConfigFamilyIds: List<Int>? = null // Launcher Config Family IDs (supports multiple)
): HttpResponse<PaginatedLaunchNormalList> = launchesList(
    agencyLaunchAttemptCount = null,
    agencyLaunchAttemptCountGt = null,
    agencyLaunchAttemptCountGte = null,
    agencyLaunchAttemptCountLt = null,
    agencyLaunchAttemptCountLte = null,
    agencyLaunchAttemptCountYear = null,
    agencyLaunchAttemptCountYearGt = null,
    agencyLaunchAttemptCountYearGte = null,
    agencyLaunchAttemptCountYearLt = null,
    agencyLaunchAttemptCountYearLte = null,
    id = id?.let { listOf(it) },
    includeSuborbital = includeSuborbital,
    isCrewed = isCrewed,
    lastUpdatedGte = null,
    lastUpdatedLte = null,
    launchDesignator = null,
    launcherConfigId = null,
    launcherConfigFamilyIds = launcherConfigFamilyIds,
    limit = limit,
    locationIds = locationIds,
    locationLaunchAttemptCount = null,
    locationLaunchAttemptCountGt = null,
    locationLaunchAttemptCountGte = null,
    locationLaunchAttemptCountLt = null,
    locationLaunchAttemptCountLte = null,
    locationLaunchAttemptCountYear = null,
    locationLaunchAttemptCountYearGt = null,
    locationLaunchAttemptCountYearGte = null,
    locationLaunchAttemptCountYearLt = null,
    locationLaunchAttemptCountYearLte = null,
    lspId = lspId,
    lspName = null,
    missionAgencyIds = null,
    missionOrbitCelestialBodyId = null,
    missionOrbitName = null,
    missionOrbitNameIcontains = null,
    missionTypeIds = missionTypeIds,
    name = null,
    netDay = netDay,
    netGt = netGt,
    netGte = null,
    netLt = netLt,
    netLte = null,
    netMonth = netMonth,
    netYear = null,
    offset = offset,
    orbitIds = orbitIds,
    orbitalLaunchAttemptCount = null,
    orbitalLaunchAttemptCountGt = null,
    orbitalLaunchAttemptCountGte = null,
    orbitalLaunchAttemptCountLt = null,
    orbitalLaunchAttemptCountLte = null,
    orbitalLaunchAttemptCountYear = null,
    orbitalLaunchAttemptCountYearGt = null,
    orbitalLaunchAttemptCountYearGte = null,
    orbitalLaunchAttemptCountYearLt = null,
    orbitalLaunchAttemptCountYearLte = null,
    ordering = ordering,
    pad = pad,
    padLocation = null,
    padLocationCelestialBodyId = null,
    padLaunchAttemptCount = null,
    padLaunchAttemptCountGt = null,
    padLaunchAttemptCountGte = null,
    padLaunchAttemptCountLt = null,
    padLaunchAttemptCountLte = null,
    padLaunchAttemptCountYear = null,
    padLaunchAttemptCountYearGt = null,
    padLaunchAttemptCountYearGte = null,
    padLaunchAttemptCountYearLt = null,
    padLaunchAttemptCountYearLte = null,
    previous = previous,
    program = program,
    relatedLspId = relatedLspId,
    relatedLspName = null,
    rocketConfigurationFullName = null,
    rocketConfigurationFullNameIcontains = null,
    rocketConfigurationId = rocketConfigurationId,
    rocketConfigurationManufacturerName = null,
    rocketConfigurationManufacturerNameIcontains = null,
    rocketConfigurationName = null,
    rocketSpacecraftflightSpacecraftId = null,
    rocketSpacecraftflightSpacecraftName = null,
    rocketSpacecraftflightSpacecraftNameIcontains = null,
    search = search,
    serialNumber = null,
    slug = null,
    spacecraftConfigIds = null,
    status = status,
    statusIds = statusIds,
    upcoming = upcoming,
    upcomingWithRecent = upcomingWithRecent,
    videoUrl = null,
    windowEndGt = null,
    windowEndGte = null,
    windowEndLt = null,
    windowEndLte = null,
    windowStartGt = null,
    windowStartGte = null,
    windowStartLt = null,
    windowStartLte = null
)

/**
 * Get Starship launches filtered by program ID = 1
 */
suspend fun LaunchesApi.getStarshipLaunches(
    limit: Int = 10,
    upcoming: Boolean? = true
): HttpResponse<PaginatedLaunchNormalList> = getLaunchList(
    limit = limit,
    upcoming = upcoming,
    program = listOf(1),
    ordering = "net"
)

/**
 * Get a single launch by UUID.
 * Returns the launch as LaunchNormal if found, or null if not found.
 *
 * @param launchId The UUID of the launch to retrieve
 * @return The launch if found, null otherwise
 */
suspend fun LaunchesApi.getLaunchById(
    launchId: String
): HttpResponse<PaginatedLaunchNormalList> = getLaunchList(
    id = launchId,
    limit = 1
)
