package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.client.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.client.models.AgencyEndpointDetailed
import me.calebjones.spacelaunchnow.api.client.models.PaginatedPolymorphicLaunchEndpointList
import kotlinx.datetime.Instant


interface LaunchRepository {
    suspend fun getUpcomingLaunches(limit: Int, mode: String): Result<PaginatedPolymorphicLaunchEndpointList>
    suspend fun getLaunchDetails(id: String): Result<LaunchDetailed>
    suspend fun getAgencyDetails(id: Int): Result<AgencyEndpointDetailed>
    suspend fun getNextLaunch(limit: Int, mode: String): Result<PaginatedPolymorphicLaunchEndpointList>
}
