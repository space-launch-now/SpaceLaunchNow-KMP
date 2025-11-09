package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getRocketDetails
import me.calebjones.spacelaunchnow.api.extensions.getRocketList
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherConfigNormalList

class RocketRepositoryImpl(
    private val launcherConfigurationsApi: LauncherConfigurationsApi
) : RocketRepository {

    override suspend fun getRockets(limit: Int, offset: Int): Result<PaginatedLauncherConfigNormalList> {
        return try {
            val response = launcherConfigurationsApi.getRocketList(
                limit = limit,
                offset = offset,
                ordering = "-total_launch_count" // Order by total launch count descending
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRocketDetails(id: Int): Result<LauncherConfigDetailed> {
        return try {
            val response = launcherConfigurationsApi.getRocketDetails(id = id)
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchRockets(searchQuery: String, limit: Int): Result<PaginatedLauncherConfigNormalList> {
        return try {
            val response = launcherConfigurationsApi.getRocketList(
                limit = limit,
                search = searchQuery,
                ordering = "-total_launch_count"
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
