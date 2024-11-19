package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.client.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.client.models.PaginatedPolymorphicLaunchEndpointList


interface LaunchRepository {
    suspend fun getUpcomingLaunches(limit: Int): Result<PaginatedPolymorphicLaunchEndpointList>
    suspend fun getLaunchDetails(id: String): Result<LaunchDetailed>
}
