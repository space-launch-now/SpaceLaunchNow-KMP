package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchNormalList


interface LaunchRepository {
    suspend fun getUpcomingLaunchesList(limit: Int): Result<PaginatedLaunchBasicList>
    suspend fun getUpcomingLaunchesNormal(limit: Int): Result<PaginatedLaunchNormalList>
    suspend fun getLaunchDetails(id: String): Result<LaunchDetailed>
    suspend fun getAgencyDetails(id: Int): Result<AgencyEndpointDetailed>
    suspend fun getNextLaunch(limit: Int): Result<PaginatedLaunchNormalList>
    suspend fun getNextDetailedLaunch(limit: Int): Result<PaginatedLaunchDetailedList>
}
 