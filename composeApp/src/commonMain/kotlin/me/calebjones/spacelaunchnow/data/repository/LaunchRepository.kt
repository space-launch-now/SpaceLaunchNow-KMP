package me.calebjones.spacelaunchnow.data.repository

import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult


interface LaunchRepository {

    /** [forceRefresh] skips the fresh cache on the Terminus backend; the LL backend has no cache here and ignores it. */
    suspend fun getLaunchesByDayAndMonth(
        day: Int,
        month: Int,
        limit: Int = 100,
        forceRefresh: Boolean = false
    ): Result<PaginatedResult<Launch>>

    suspend fun getStaleDetailedLaunch(id: String): Launch?

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
        netLt: Instant? = null,
        forceRefresh: Boolean = false
    ): Result<PaginatedResult<Launch>>

    suspend fun getPreviousLaunchesDomain(
        limit: Int,
        offset: Int = 0,
        forceRefresh: Boolean = false
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

    /**
     * The upcoming launches the Android home-screen widgets draw, with the user's filters.
     * The Terminus backend reads the slim `/launches/mini/` list; the default (the LL backend)
     * reads the same list as [getUpcomingLaunchesNormalDomain].
     */
    suspend fun getUpcomingLaunchesForWidget(
        limit: Int,
        forceRefresh: Boolean = false,
        agencyIds: List<Int>? = null,
        locationIds: List<Int>? = null
    ): Result<DataResult<PaginatedResult<Launch>>> =
        getUpcomingLaunchesNormalDomain(limit, forceRefresh, agencyIds, locationIds)

    /**
     * The newest cached upcoming list a widget can draw, with no network call and no expiry
     * check ([DataResult.timestamp] says when it was stored). Null when nothing is cached, and
     * for a backend that keeps no such cache (the default).
     */
    suspend fun getCachedUpcomingLaunchesForWidget(
        limit: Int,
        agencyIds: List<Int>? = null,
        locationIds: List<Int>? = null
    ): DataResult<PaginatedResult<Launch>>? = null

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
        launcherConfigFamilyIds: List<Int>? = null,
        forceRefresh: Boolean = false
    ): Result<PaginatedResult<Launch>>
}