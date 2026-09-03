package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.trantor.getLauncher
import me.calebjones.spacelaunchnow.api.extensions.trantor.listLaunchers
import me.calebjones.spacelaunchnow.api.trantor.apis.LaunchersApi
import me.calebjones.spacelaunchnow.api.trantor.models.LauncherDetail as TrantorLauncherDetail
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseLauncherListItem
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.model.LauncherDetail
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult

/**
 * Implementation of LauncherRepository using the Trantor LaunchersApi (`GET /launchers`).
 */
class LauncherRepositoryImpl(
    private val launchersApi: LaunchersApi
) : LauncherRepository {

    private suspend fun getLaunchersRaw(
        limit: Int,
        offset: Int,
        search: String?,
        ordering: String?,
        launcherConfigId: Int?,
        isPlaceholder: Boolean?
    ): Result<PaginatedResponseLauncherListItem> {
        return try {
            println("=== LauncherRepository.getLaunchers ===")
            println("Parameters: limit=$limit, offset=$offset, search=$search, ordering=$ordering, configId=$launcherConfigId")

            val response = launchersApi.listLaunchers(
                limit = limit,
                offset = offset,
                search = search,
                ordering = ordering,
                isPlaceholder = isPlaceholder,
                configIds = launcherConfigId?.let { listOf(it) }
            )

            val launchers = response.body()
            println("OK API SUCCESS: Fetched ${launchers.results.size} launchers (offset: $offset)")

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

    private suspend fun getLaunchersByConfigRaw(
        configId: Int,
        limit: Int,
        offset: Int
    ): Result<PaginatedResponseLauncherListItem> {
        return getLaunchersRaw(
            limit = limit,
            offset = offset,
            search = null,
            // Trantor's launchers ordering whitelist is flights/-flights only (default
            // -flights); LL's "-id" has no equivalent, so leave it unset and take the default.
            ordering = null,
            isPlaceholder = false,
            launcherConfigId = configId
        )
    }

    private suspend fun getLauncherDetailsRaw(launcherId: Int): Result<TrantorLauncherDetail> {
        return try {
            println("=== LauncherRepository.getLauncherDetails ===")
            println("Parameters: launcherId=$launcherId")

            val response = launchersApi.getLauncher(launcherId = launcherId)
            val launcher = response.body()

            println("OK API SUCCESS: Fetched launcher details for ID $launcherId")

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

    override suspend fun getLaunchersDomain(
        limit: Int,
        offset: Int,
        search: String?,
        ordering: String?,
        launcherConfigId: Int?,
        isPlaceholder: Boolean?
    ): Result<PaginatedResult<LauncherDetail>> = getLaunchersRaw(
        limit = limit,
        offset = offset,
        search = search,
        ordering = ordering,
        launcherConfigId = launcherConfigId,
        isPlaceholder = isPlaceholder
    ).map { it.toDomain() }

    override suspend fun getLaunchersByConfigDomain(
        configId: Int,
        limit: Int,
        offset: Int
    ): Result<PaginatedResult<LauncherDetail>> =
        getLaunchersByConfigRaw(configId = configId, limit = limit, offset = offset).map { it.toDomain() }

    override suspend fun getLauncherDetailsDomain(launcherId: Int): Result<LauncherDetail> =
        getLauncherDetailsRaw(launcherId).map { it.toDomain() }
}
