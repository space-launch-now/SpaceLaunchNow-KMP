package me.calebjones.spacelaunchnow.data.repository

import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult


interface LaunchRepository {

    suspend fun getLaunchesByDayAndMonth(
        day: Int,
        month: Int,
        limit: Int = 100
    ): Result<PaginatedLaunchNormalList>

    suspend fun getStaleDetailedLaunch(id: String): LaunchDetailed?
    suspend fun getAgencyDetails(id: Int): Result<AgencyEndpointDetailed>

    suspend fun getNextDetailedLaunch(limit: Int): Result<PaginatedLaunchDetailedList>
    suspend fun getNextNormalLaunch(limit: Int): Result<PaginatedLaunchNormalList>

    suspend fun getStatsCount(
        key: String,
        netGt: Instant,
        netLt: Instant,
        forceRefresh: Boolean = false
    ): Result<DataResult<Int>>

    // -- Domain-returning methods --------------------------------------------

    suspend fun getUpcomingLaunchesDomain(
        limit: Int,
        offset: Int = 0,
        netGt: Instant? = null,
        netLt: Instant? = null
    ): Result<PaginatedResult<Launch>>

    suspend fun getPreviousLaunchesDomain(
        limit: Int,
        offset: Int = 0
    ): Result<PaginatedResult<Launch>>

    suspend fun getFeaturedLaunchDomain(
        forceRefresh: Boolean = false,
        agencyIds: List<Int>? = null,
        locationIds: List<Int>? = null
    ): Result<DataResult<PaginatedResult<Launch>>>

    suspend fun getInFlightLaunchesDomain(
        forceRefresh: Boolean = false,
        agencyIds: List<Int>? = null,
        locationIds: List<Int>? = null
    ): Result<DataResult<PaginatedResult<Launch>>>

    suspend fun getUpcomingLaunchesNormalDomain(
        limit: Int,
        forceRefresh: Boolean = false,
        agencyIds: List<Int>? = null,
        locationIds: List<Int>? = null
    ): Result<DataResult<PaginatedResult<Launch>>>

    suspend fun getPreviousLaunchesNormalDomain(
        limit: Int,
        forceRefresh: Boolean = false,
        agencyIds: List<Int>? = null,
        locationIds: List<Int>? = null
    ): Result<DataResult<PaginatedResult<Launch>>>

    suspend fun getLaunchDetailDomain(
        id: String,
        forceRefresh: Boolean = false
    ): Result<Launch>

    suspend fun getStarshipLaunchesDomain(
        limit: Int,
        forceRefresh: Boolean = false,
        programId: List<Int>? = null
    ): Result<PaginatedResult<Launch>>

    suspend fun getStarshipHistoryDomain(
        limit: Int,
        forceRefresh: Boolean = false
    ): Result<DataResult<PaginatedResult<Launch>>>

    suspend fun getLaunchByIdDomain(id: String): Result<Launch?>

    suspend fun getFilteredLaunchesDomain(
        limit: Int,
        offset: Int = 0,
        upcoming: Boolean? = null,
        previous: Boolean? = null,
        ordering: String? = null,
        search: String? = null,
        lspIds: List<Int>? = null,
        locationIds: List<Int>? = null,
        programIds: List<Int>? = null,
        rocketConfigurationId: Int? = null,
        isCrewed: Boolean? = null,
        includeSuborbital: Boolean? = null,
        statusIds: List<Int>? = null,
        orbitIds: List<Int>? = null,
        missionTypeIds: List<Int>? = null,
        launcherConfigFamilyIds: List<Int>? = null
    ): Result<PaginatedResult<Launch>>
}