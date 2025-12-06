package me.calebjones.spacelaunchnow.data.repository

import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.data.model.DataResult


interface LaunchRepository {
    suspend fun getUpcomingLaunchesList(limit: Int): Result<PaginatedLaunchBasicList>
    suspend fun getUpcomingLaunchesList(
        limit: Int,
        netGt: Instant?,
        netLt: Instant?
    ): Result<PaginatedLaunchBasicList>

    suspend fun getFeaturedLaunch(
        forceRefresh: Boolean = false,
        agencyIds: List<Int>? = null,
        locationIds: List<Int>? = null
    ): Result<DataResult<PaginatedLaunchNormalList>>

    suspend fun getUpcomingLaunchesNormal(
        limit: Int,
        forceRefresh: Boolean = false,
        agencyIds: List<Int>? = null,
        locationIds: List<Int>? = null
    ): Result<DataResult<PaginatedLaunchNormalList>>

    suspend fun getPreviousLaunchesNormal(
        limit: Int,
        forceRefresh: Boolean = false,
        agencyIds: List<Int>? = null,
        locationIds: List<Int>? = null
    ): Result<DataResult<PaginatedLaunchNormalList>>

    suspend fun getLaunchesByDayAndMonth(
        day: Int,
        month: Int,
        limit: Int = 100
    ): Result<PaginatedLaunchNormalList>

    suspend fun getLaunchDetails(id: String, forceRefresh: Boolean = false): Result<LaunchDetailed>
    suspend fun getAgencyDetails(id: Int): Result<AgencyEndpointDetailed>
    suspend fun getNextStarshipLaunch(
        limit: Int,
        forceRefresh: Boolean = false,
        programId: List<Int>? = null
    ): Result<PaginatedLaunchNormalList>

    suspend fun getNextDetailedLaunch(limit: Int): Result<PaginatedLaunchDetailedList>
}
 