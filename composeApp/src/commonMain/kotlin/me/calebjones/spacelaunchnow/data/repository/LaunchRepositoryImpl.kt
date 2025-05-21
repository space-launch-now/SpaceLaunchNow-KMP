package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.api.client.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.client.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.client.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.client.models.AgencyEndpointDetailed
import me.calebjones.spacelaunchnow.api.client.models.PaginatedPolymorphicLaunchEndpointList

class LaunchRepositoryImpl(private val launchesApi: LaunchesApi, private val agenciesApi: AgenciesApi) : LaunchRepository {

    override suspend fun getUpcomingLaunches(limit: Int, mode: String): Result<PaginatedPolymorphicLaunchEndpointList> {
        return try {
            val response = launchesApi.launchesUpcomingList(limit = limit)
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
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
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAgencyDetails(id: Int): Result<AgencyEndpointDetailed> {
        return try {
            val response = agenciesApi.agenciesRetrieve(id)
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNextLaunch(
        limit: Int,
        mode: String
    ): Result<PaginatedPolymorphicLaunchEndpointList> {
        return try {
            val response = launchesApi.launchesUpcomingList(limit = limit)
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            println(e)
            Result.failure(e)
        } catch (e: Exception) {
            println(e)
            Result.failure(e)
        }
    }
}
