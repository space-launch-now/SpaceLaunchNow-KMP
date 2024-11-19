package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import io.ktor.utils.io.errors.IOException
import me.calebjones.spacelaunchnow.api.client.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.client.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.client.models.PaginatedPolymorphicLaunchEndpointList

class LaunchRepositoryImpl(private val launchesApi: LaunchesApi) : LaunchRepository {

    override suspend fun getUpcomingLaunches(limit: Int): Result<PaginatedPolymorphicLaunchEndpointList> {
        return try {
            val response = launchesApi.launchesUpcomingList(limit = limit)
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    override suspend fun getLaunchDetails(id: String): Result<LaunchDetailed> {
        return try {
            val response = launchesApi.launchesRetrieve(id)
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
}
