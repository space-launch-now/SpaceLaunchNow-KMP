package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getLaunchers
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchersApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherDetailedList

/**
 * Implementation of LauncherRepository using the generated LaunchersApi
 */
class LauncherRepositoryImpl(
    private val launchersApi: LaunchersApi
) : LauncherRepository {

    override suspend fun getLaunchers(
        limit: Int,
        offset: Int,
        search: String?,
        ordering: String?,
        launcherConfigId: Int?
    ): Result<PaginatedLauncherDetailedList> {
        return try {
            println("=== LauncherRepository.getLaunchers ===")
            println("Parameters: limit=$limit, offset=$offset, search=$search, ordering=$ordering, configId=$launcherConfigId")

            val response = launchersApi.getLaunchers(
                limit = limit,
                offset = offset,
                search = search,
                ordering = ordering,
                launcherConfigIds = launcherConfigId?.let { listOf(it) }
            )

            val launchers = response.body()
            println("✓ API SUCCESS: Fetched ${launchers.results.size} launchers (offset: $offset)")

            Result.success(launchers)
        } catch (e: ResponseException) {
            println("LauncherRepository: API error: ${e.message}")
            Result.failure(e)
        } catch (e: IOException) {
            println("LauncherRepository: Network error: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("LauncherRepository: Unexpected error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getLaunchersByConfig(
        configId: Int,
        limit: Int,
        offset: Int
    ): Result<PaginatedLauncherDetailedList> {
        return getLaunchers(
            limit = limit,
            offset = offset,
            search = null,
            ordering = "-id",
            launcherConfigId = configId
        )
    }

    override suspend fun getLauncherDetails(launcherId: Int): Result<LauncherDetailed> {
        return try {
            println("=== LauncherRepository.getLauncherDetails ===")
            println("Parameters: launcherId=$launcherId")

            val response = launchersApi.launchersRetrieve(launcherId)
            val launcher = response.body()

            println("✓ API SUCCESS: Fetched launcher details for ID $launcherId")

            Result.success(launcher)
        } catch (e: ResponseException) {
            println("LauncherRepository: API error for launcher $launcherId: ${e.message}")
            Result.failure(e)
        } catch (e: IOException) {
            println("LauncherRepository: Network error for launcher $launcherId: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("LauncherRepository: Unexpected error for launcher $launcherId: ${e.message}")
            Result.failure(e)
        }
    }
}
