package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.models.AgencyEndpointDetailed
import me.calebjones.spacelaunchnow.api.models.PaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.api.models.PaginatedLaunchNormalList


interface LaunchRepository {
    suspend fun getUpcomingLaunchesList(limit: Int): Result<PaginatedLaunchBasicList>
    suspend fun getUpcomingLaunchesNormal(limit: Int): Result<PaginatedLaunchNormalList>
    suspend fun getLaunchDetails(id: String): Result<LaunchDetailed>
    suspend fun getAgencyDetails(id: Int): Result<AgencyEndpointDetailed>
    suspend fun getNextLaunch(limit: Int): Result<PaginatedLaunchNormalList>
}
 